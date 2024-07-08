const express = require("express");
const mongoose = require("mongoose");
const bodyParser = require("body-parser");
const cors = require("cors");
const http = require("http");
const socketIo = require("socket.io");
require("dotenv").config();

// MongoDB 연결 설정
mongoose.connect(process.env.MONGODB_URI, {
  useNewUrlParser: true,
  useUnifiedTopology: true,
});

const db = mongoose.connection;
db.on("error", console.error.bind(console, "MongoDB connection error:"));
db.once("open", () => {
  console.log("Connected to MongoDB");
});

// 사용자 스키마 및 모델 설정
const userSchema = new mongoose.Schema({
  email: String,
  nickname: String,
  kingdomName: String,
  score: Number,
  wins: Number,
  losses: Number,
  draws: Number,
  coins: Number,
  profileImage: String, // Change to String to store the resource name
});

const User = mongoose.model("User", userSchema);

// 방 스키마 및 모델 설정
const roomSchema = new mongoose.Schema({
  code: String,
  players: [
    { email: String, ready: Boolean, gold: Number, nationalPower: Number },
  ],
  gameState: String,
  currentRound: Number,
  currentCard: { name: String, power: Number, image: String }, // 카드 정보 추가
});

const Room = mongoose.model("Room", roomSchema);

const app = express();
const server = http.createServer(app);
const io = socketIo(server);

app.use(bodyParser.json());
app.use(cors());

const cards = [
  { name: "castle", power: 3000, image: "card_castle" },
  { name: "wall", power: 2000, image: "card_wall" },
  { name: "soldier", power: 300, image: "card_soldier" },
  { name: "spear", power: 500, image: "card_spear" },
  { name: "archer", power: 700, image: "card_archer" },
  { name: "cavalry", power: 1000, image: "card_cavalry" },
  { name: "scholar", power: 300, image: "card_scholar" },
  { name: "merchant", power: 500, image: "card_merchant" },
  { name: "craft", power: 700, image: "card_craft" },
  { name: "farmer", power: 1000, image: "card_farmer" },
];

// 게임 상태 초기화 함수
const initializeGameState = (room) => {
  room.players.forEach((player) => {
    player.gold = 10000;
    player.nationalPower = 0;
  });
  room.currentRound = 1;
  room.currentCard = cards[Math.floor(Math.random() * cards.length)];
};

// 방 생성 엔드포인트
app.post("/api/createRoom", async (req, res) => {
  const { code, player } = req.body;
  const newRoom = new Room({
    code,
    players: [{ email: player, ready: false, gold: 10000, nationalPower: 0 }],
    gameState: "waiting",
    currentRound: 0,
  });

  try {
    const existingRoom = await Room.findOne({ code });
    if (existingRoom) {
      return res
        .status(400)
        .json({ success: false, message: "Room code already exists" });
    }

    const savedRoom = await newRoom.save();
    io.emit("roomListUpdated");
    res.status(201).json({ success: true, roomCode: savedRoom.code });
  } catch (err) {
    console.error("Error creating room:", err);
    res.status(500).send("Failed to create room");
  }
});

// 방 입장 엔드포인트
app.post("/api/joinRoom", async (req, res) => {
  const { code, player } = req.body;

  try {
    const room = await Room.findOne({ code });
    if (room) {
      if (room.players.length < 2) {
        if (!room.players.find((p) => p.email === player)) {
          room.players.push({
            email: player,
            ready: false,
            gold: 10000,
            nationalPower: 0,
          });
          await room.save();
          io.emit("roomUpdated", { code: room.code });
          res.status(200).json({ success: true, gameState: room.gameState });
        } else {
          res
            .status(400)
            .json({ success: false, message: "Player already in room" });
        }
      } else {
        res.status(400).json({ success: false, message: "Room is full" });
      }
    } else {
      res.status(404).json({ success: false, message: "Room not found" });
    }
  } catch (err) {
    console.error("Error joining room:", err);
    res.status(500).send("Failed to join room");
  }
});

// 방 나가기 엔드포인트
app.post("/api/leaveRoom", async (req, res) => {
  const { code, email } = req.body;

  try {
    const room = await Room.findOne({ code });
    if (room) {
      room.players = room.players.filter((player) => player.email !== email);
      if (room.players.length === 0) {
        await Room.deleteOne({ code });
        io.emit("roomListUpdated");
        return res
          .status(200)
          .json({ success: true, message: "Room deleted successfully" });
      }
      await room.save();
      io.emit("roomUpdated", { code: room.code });
      res
        .status(200)
        .json({ success: true, message: "Left room successfully" });
    } else {
      res.status(404).json({ success: false, message: "Room not found" });
    }
  } catch (err) {
    console.error("Error leaving room:", err);
    res.status(500).send("Failed to leave room");
  }
});

// 게임 시작 엔드포인트
app.post("/api/checkAndStartMatch", async (req, res) => {
  const { code, email } = req.body;

  try {
    const room = await Room.findOne({ code });
    if (room) {
      if (room.players[0].email !== email) {
        return res.status(403).json({
          success: false,
          message: "Only the host can start the match",
        });
      }

      const allReady = room.players.every((player) => player.ready);
      if (!allReady) {
        return res.status(400).json({
          success: false,
          message: "All players must be ready to start the match",
        });
      }

      // 모든 플레이어가 준비된 상태라면 게임을 시작하도록 소켓 이벤트 전송
      initializeGameState(room);
      await room.save();
      io.to(code).emit("startMatch", {
        player1Email: room.players[0].email,
        player2Email: room.players[1].email,
      });

      res.status(200).json({ success: true });
    } else {
      res.status(404).json({ success: false, message: "Room not found" });
    }
  } catch (err) {
    console.error("Error starting match:", err);
    res.status(500).send("Failed to start match");
  }
});

// 방 목록 가져오기 엔드포인트
app.get("/api/getRooms", async (req, res) => {
  try {
    const rooms = await Room.find({});
    res.status(200).json(rooms);
  } catch (err) {
    console.error("Error fetching rooms:", err);
    res.status(500).send("Failed to fetch rooms");
  }
});

// 사용자 방 목록 가져오기 엔드포인트
app.get("/api/getUserRooms", async (req, res) => {
  const { email } = req.query;

  try {
    const rooms = await Room.find({ "players.email": email });
    res.status(200).json(rooms);
  } catch (err) {
    console.error("Error fetching user rooms:", err);
    res.status(500).send("Failed to fetch user rooms");
  }
});

// 방 세부 정보 가져오기 엔드포인트
app.get("/api/getRoomDetails", async (req, res) => {
  const { code } = req.query;

  try {
    const room = await Room.findOne({ code });
    if (room) {
      const playersWithDetails = await Promise.all(
        room.players.map(async (player) => {
          const user = await User.findOne({ email: player.email });
          return {
            email: player.email,
            nickname: user.nickname,
            kingdomName: user.kingdomName,
            profileImage: user.profileImage,
            ready: player.ready,
            gold: player.gold,
            nationalPower: player.nationalPower,
          };
        })
      );

      res.status(200).json({ ...room.toObject(), players: playersWithDetails });
    } else {
      res.status(404).json({ message: "Room not found" });
    }
  } catch (err) {
    console.error("Error fetching room details:", err);
    res.status(500).send("Failed to fetch room details");
  }
});

// 라운드 진행 및 배팅 처리 엔드포인트
app.post("/api/placeBet", async (req, res) => {
  const { code, email, betAmount } = req.body;

  try {
    const room = await Room.findOne({ code });
    if (room) {
      const player = room.players.find((p) => p.email === email);
      if (player.gold < betAmount) {
        return res
          .status(400)
          .json({ success: false, message: "Not enough gold" });
      }

      player.gold -= betAmount;
      player.betAmount = betAmount;

      // 두 플레이어 모두 배팅을 완료했는지 확인
      if (room.players.every((p) => p.hasOwnProperty("betAmount"))) {
        const player1 = room.players[0];
        const player2 = room.players[1];

        if (player1.betAmount > player2.betAmount) {
          player1.nationalPower += room.currentCard.power;
        } else if (player2.betAmount > player1.betAmount) {
          player2.nationalPower += room.currentCard.power;
        } else {
          // 동점인 경우 먼저 배팅한 플레이어가 카드를 구매
          player1.nationalPower += room.currentCard.power;
        }

        // 배팅 금액 초기화 및 다음 라운드 설정
        delete player1.betAmount;
        delete player2.betAmount;
        room.currentRound += 1;
        room.currentCard = cards[Math.floor(Math.random() * cards.length)];

        if (room.currentRound > 15) {
          room.gameState = "ended";
          const winner =
            player1.nationalPower > player2.nationalPower
              ? player1.email
              : player2.nationalPower > player1.nationalPower
              ? player2.email
              : "draw";

          io.to(code).emit("gameEnded", { winner });
        } else {
          io.to(code).emit("roundEnded", { room });
        }
      }

      await room.save();
      res.status(200).json({ success: true });
    } else {
      res.status(404).json({ success: false, message: "Room not found" });
    }
  } catch (err) {
    console.error("Error placing bet:", err);
    res.status(500).send("Failed to place bet");
  }
});

app.get("/api/getGameState", async (req, res) => {
  const { code } = req.query;

  try {
    const room = await Room.findOne({ code });
    if (room) {
      res.status(200).json(room);
    } else {
      res.status(404).json({ message: "Room not found" });
    }
  } catch (err) {
    console.error("Error fetching game state:", err);
    res.status(500).send("Failed to fetch game state");
  }
});

app.post("/api/toggleReady", async (req, res) => {
  const { code, email } = req.body;

  try {
    const room = await Room.findOne({ code });
    if (room) {
      const player = room.players.find((p) => p.email === email);
      player.ready = !player.ready;
      await room.save();
      io.to(code).emit("roomUpdated", { code: room.code });
      res.status(200).json({ success: true });
    } else {
      res.status(404).json({ success: false, message: "Room not found" });
    }
  } catch (err) {
    console.error("Error toggling ready state:", err);
    res.status(500).send("Failed to toggle ready state");
  }
});

// 기타 기존 엔드포인트들...
app.post("/api/checkUser", async (req, res) => {
  const { email } = req.body;

  try {
    const user = await User.findOne({ email });
    if (user) {
      res.status(200).json({ exists: true, user });
    } else {
      res.status(200).json({ exists: false });
    }
  } catch (err) {
    console.error("Error checking user:", err);
    res.status(500).send("Server error");
  }
});

app.post("/api/addGuest", async (req, res) => {
  try {
    const guests = await User.find({ email: "" }).sort({ nickname: 1 });
    let newGuestNumber = 1;

    for (const guest of guests) {
      const guestNumber = parseInt(guest.nickname.replace("Guest", ""));
      if (guestNumber !== newGuestNumber) {
        break;
      }
      newGuestNumber++;
    }

    const newGuest = new User({
      email: "",
      nickname: `Guest${newGuestNumber}`,
      kingdomName: `GuestKingdom${newGuestNumber}`,
      score: 0,
      wins: 0,
      losses: 0,
      draws: 0,
      coins: 0,
      profileImage: "default_image",
    });

    const savedGuest = await newGuest.save();
    res.status(201).json({ guest: savedGuest });
  } catch (err) {
    console.error("Error adding guest:", err);
    res.status(500).send("Failed to add guest");
  }
});

app.post("/api/deleteGuest", async (req, res) => {
  const { nickname } = req.body;

  try {
    await User.deleteOne({ nickname, email: "" });
    res.status(200).send("Guest deleted successfully");
  } catch (err) {
    console.error("Error deleting guest:", err);
    res.status(500).send("Failed to delete guest");
  }
});

app.post("/api/saveUser", async (req, res) => {
  const { email, nickname, kingdomName, profileImage } = req.body;

  const newUser = new User({
    email,
    nickname,
    kingdomName,
    score: 0,
    wins: 0,
    losses: 0,
    draws: 0,
    coins: 0,
    profileImage,
  });

  try {
    const savedUser = await newUser.save();
    res.status(201).send("User saved successfully");
  } catch (err) {
    console.error("Error saving user:", err);
    res.status(500).send("Failed to save user");
  }
});

app.post("/api/checkAvailability", async (req, res) => {
  const { field, value } = req.body;

  try {
    const query = {};
    query[field] = value;

    const user = await User.findOne(query);
    if (user) {
      res.status(200).json({ available: false });
    } else {
      res.status(200).json({ available: true });
    }
  } catch (err) {
    console.error("Error checking availability:", err);
    res.status(500).send("Failed to check availability");
  }
});

app.get("/api/getUser", async (req, res) => {
  const { email } = req.query;

  try {
    const user = await User.findOne({ email });
    if (user) {
      res.status(200).json(user);
    } else {
      res.status(404).send("User not found");
    }
  } catch (err) {
    console.error("Error fetching user:", err);
    res.status(500).send("Failed to fetch user");
  }
});

const PORT = process.env.PORT || 80;
server.listen(PORT, () => {
  console.log(`Server is running on port ${PORT}`);
});
