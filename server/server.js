const express = require("express");
const mongoose = require("mongoose");
const bodyParser = require("body-parser");
const cors = require("cors");
const http = require("http");
const socketIo = require("socket.io");
require("dotenv").config(); // .env 파일의 환경 변수를 로드

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
  profileImage: String,
});

const User = mongoose.model("User", userSchema);

// 방 스키마 및 모델 설정
const roomSchema = new mongoose.Schema({
  code: String,
  players: [{ email: String, ready: Boolean }],
  gameState: String,
});

const Room = mongoose.model("Room", roomSchema);

// 게임 스키마 및 모델 설정
const gameSchema = new mongoose.Schema({
  player1: String,
  player2: String,
  rounds: Number,
  currentRound: Number,
  player1Gold: Number,
  player2Gold: Number,
  player1Power: Number,
  player2Power: Number,
  currentCardPower: Number,
});

const Game = mongoose.model("Game", gameSchema);

// Express 애플리케이션 설정
const app = express();
const server = http.createServer(app);
const io = socketIo(server);

app.use(bodyParser.json());
app.use(cors());

// 소켓 설정
io.on("connection", (socket) => {
  console.log("A user connected");

  socket.on("joinRoom", (roomCode) => {
    socket.join(roomCode);
  });

  socket.on("disconnect", () => {
    console.log("A user disconnected");
  });
});

//게임 관련 엔드포인트
app.post("/api/startGame", async (req, res) => {
  const { player1, player2 } = req.body;
  const newGame = new Game({
    player1,
    player2,
    rounds: 15,
    currentRound: 1,
    player1Gold: 10000,
    player2Gold: 10000,
    player1Power: 0,
    player2Power: 0,
    currentCardPower: Math.floor(Math.random() * 10) + 1, // 임의의 카드 국력 값
  });

  try {
    const savedGame = await newGame.save();
    res.status(201).json(savedGame);
  } catch (err) {
    console.error("Error starting game:", err);
    res.status(500).send("Failed to start game");
  }
});

app.post("/api/updateGame", async (req, res) => {
  const { gameId, gameState } = req.body;

  try {
    const game = await Game.findById(gameId);
    if (game) {
      game.gameState = gameState;
      await game.save();
      io.to(game.player1Email).emit("gameUpdated", game);
      io.to(game.player2Email).emit("gameUpdated", game);
      res.status(200).json({ success: true });
    } else {
      res.status(404).json({ success: false, message: "Game not found" });
    }
  } catch (err) {
    console.error("Error updating game:", err);
    res.status(500).send("Failed to update game");
  }
});

app.get("/api/getGameStatus", async (req, res) => {
  const { player1, player2 } = req.query;

  try {
    const game = await Game.findOne({ player1, player2 });
    if (game) {
      res.status(200).json(game);
    } else {
      res.status(404).json({ message: "Game not found" });
    }
  } catch (err) {
    console.error("Error fetching game status:", err);
    res.status(500).send("Failed to fetch game status");
  }
});

// 방이랑 유저 관련 엔드포인트들 (건드리지 마시오)

// 방 생성 엔드포인트
app.post("/api/createRoom", async (req, res) => {
  const { code, player } = req.body;
  const newRoom = new Room({
    code,
    players: [{ email: player, ready: false }],
    gameState: "waiting",
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
        if (!room.players.some((p) => p.email === player)) {
          room.players.push({ email: player, ready: false });
        }
        if (room.players.length === 2) {
          room.gameState = "ready";
        }
        await room.save();
        io.emit("roomUpdated", { code: room.code });
        res.status(200).json({ success: true, gameState: room.gameState });
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

app.get("/api/getRoomDetails", async (req, res) => {
  const { code } = req.query;

  try {
    const room = await Room.findOne({ code });
    if (room) {
      const playersDetails = await Promise.all(
        room.players.map(async (player) => {
          const user = await User.findOne({ email: player.email });
          if (user) {
            return {
              email: player.email,
              nickname: user.nickname,
              kingdomName: user.kingdomName,
              profileImage: user.profileImage,
              ready: player.ready,
            };
          }
          return null; // Handle null case
        })
      );

      res.status(200).json({
        code: room.code,
        players: playersDetails.filter((p) => p !== null),
      }); // Filter out null values
    } else {
      res.status(404).json({ message: "Room not found" });
    }
  } catch (err) {
    console.error("Error fetching room details:", err);
    res.status(500).send("Failed to fetch room details");
  }
});

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
      const player1Email = room.players[0].email;
      const player2Email = room.players[1].email;

      io.to(code).emit("startMatch", { player1Email, player2Email });

      await Room.deleteOne({ code });

      res.status(200).json({ success: true });
    } else {
      res.status(404).json({ success: false, message: "Room not found" });
    }
  } catch (err) {
    console.error("Error starting match:", err);
    res.status(500).send("Failed to start match");
  }
});

// 준비 상태 토글 엔드포인트
app.post("/api/toggleReady", async (req, res) => {
  const { code, email } = req.body;

  try {
    const room = await Room.findOne({ code });
    if (room) {
      const player = room.players.find((p) => p.email === email);
      if (player) {
        player.ready = !player.ready;
        await room.save();
        io.emit("roomUpdated", { code: room.code });
        res.status(200).json({ success: true, room });
      } else {
        res.status(404).json({ success: false, message: "Player not found" });
      }
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

// 랭킹 데이터를 가져오는 엔드포인트
app.get("/api/ranking", async (req, res) => {
  try {
    const players = await User.find().sort({ score: -1 }); // 점수 기준 내림차순 정렬
    res.json(players);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
});

const PORT = process.env.PORT || 80;
server.listen(PORT, () => {
  console.log(`Server is running on port ${PORT}`);
});
