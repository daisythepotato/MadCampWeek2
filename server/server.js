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
  item1: Number,
  item2: Number,
  item3: Number,
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
  currentCardName: String,
  currentCardImage: String,
  player1Bet: { type: Number, default: null },
  player2Bet: { type: Number, default: null },
  cardDeck: { type: Array, default: [] },
});

const Game = mongoose.model("Game", gameSchema);

const cards = [
  { name: "castle", power: 3000, image: "castle.png" },
  { name: "wall", power: 2000, image: "wall.png" },
  { name: "castle", power: 3000, image: "castle.png" },
  { name: "wall", power: 2000, image: "wall.png" },
  { name: "soldier", power: 300, image: "soldier.png" },
  { name: "spear", power: 500, image: "spear.png" },
  { name: "archer", power: 700, image: "archer.png" },
  { name: "cavalry", power: 1000, image: "cavalry.png" },
  { name: "scholar", power: 300, image: "scholar.png" },
  { name: "merchant", power: 500, image: "merchant.png" },
  { name: "craft", power: 700, image: "craft.png" },
  { name: "farmer", power: 1000, image: "farmer.png" },
];

const initializeCardDeck = () => {
  const deck = [];

  // castle과 wall 카드 각각 두 장씩 추가
  deck.push(...cards.filter((card) => card.name === "castle").slice(0, 2));
  deck.push(...cards.filter((card) => card.name === "wall").slice(0, 2));

  // 나머지 카드 중에서 하나씩 추가
  const remainingCards = cards.filter(
    (card) => card.name !== "castle" && card.name !== "wall"
  );
  deck.push(...remainingCards);

  // 총 15장으로 덱을 완성하기 위해 남은 카드를 무작위로 추가
  while (deck.length < 15) {
    const randomCard =
      remainingCards[Math.floor(Math.random() * remainingCards.length)];
    deck.push(randomCard);
  }

  // 덱을 섞음
  for (let i = deck.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [deck[i], deck[j]] = [deck[j], deck[i]];
  }

  return deck.slice(0, 15); // 15개의 카드를 반환
};

// Express 애플리케이션 설정
const app = express();
const server = http.createServer(app);
const io = socketIo(server);

app.use(bodyParser.json());
app.use(cors());

// 소켓 설정
io.on("connection", (socket) => {
  //console.log("A user connected");

  socket.on("joinRoom", (roomCode) => {
    socket.join(roomCode);
    console.log(`User joined room: ${roomCode}`);
  });

  socket.on("leaveRoom", (roomCode) => {
    socket.leave(roomCode);
    console.log(`User left room: ${roomCode}`);
  });

  socket.on("disconnect", () => {
    //console.log("A user disconnected");
  });
});

app.post("/api/startGame", async (req, res) => {
  const { player1, player2 } = req.body;

  const existingGame = await Game.findOne({ player1, player2 });
  if (existingGame) {
    return res
      .status(400)
      .json({ success: false, message: "Game already in progress" });
  }

  const deck = initializeCardDeck();

  const newGame = new Game({
    player1,
    player2,
    rounds: 15,
    currentRound: 1,
    player1Gold: 10000,
    player2Gold: 10000,
    player1Power: 0,
    player2Power: 0,
    currentCardPower: deck[0].power,
    currentCardName: deck[0].name,
    currentCardImage: deck[0].image,
    cardDeck: deck,
  });

  try {
    const savedGame = await newGame.save();
    res.status(201).json(savedGame);
  } catch (err) {
    console.error("Error starting game:", err);
    res.status(500).send("Failed to start game");
  }
});

// 라운드 종료 후 게임 종료 체크 및 승패 결과 전송
const checkGameOver = async (game) => {
  if (game.currentRound > game.rounds) {
    let result;
    if (game.player1Power > game.player2Power) {
      result = {
        winner: game.player1,
        loser: game.player2,
        message: `${game.player1} wins!`,
      };
    } else if (game.player2Power > game.player1Power) {
      result = {
        winner: game.player2,
        loser: game.player1,
        message: `${game.player2} wins!`,
      };
    } else {
      result = {
        message: "It's a draw!",
      };
    }

    io.to(`${game.player1}-${game.player2}`).emit("gameOver", result);
    await Game.deleteOne({ code: game.code }); // 게임 종료 후 데이터 삭제
  }
};

// 배팅 엔드포인트
app.post("/api/placeBet", async (req, res) => {
  const { player1, player2, playerEmail, betAmount } = req.body;

  try {
    const game = await Game.findOne({ player1, player2 });
    if (!game) {
      return res
        .status(404)
        .json({ success: false, message: "Game not found" });
    }

    if (game.currentRound > game.rounds) {
      return res
        .status(400)
        .json({ success: false, message: "Game has already ended" });
    }
    //배팅 값 확인 및 배팅
    if (playerEmail === game.player1 && game.player1Bet == null) {
      if (betAmount > game.player1Gold) {
        return res
          .status(400)
          .json({
            success: false,
            message: "Bet amount exceeds available gold",
          });
      }
      game.player1Bet = betAmount;
    } else if (playerEmail === game.player2 && game.player2Bet == null) {
      if (betAmount > game.player2Gold) {
        return res
          .status(400)
          .json({
            success: false,
            message: "Bet amount exceeds available gold",
          });
      }
      game.player2Bet = betAmount;
    } else {
      return res
        .status(400)
        .json({
          success: false,
          message: "Invalid player or bet already placed",
        });
    }
    await game.save();

    // 양쪽 플레이어가 모두 배팅을 완료했는지 확인
    if (game.player1Bet !== null && game.player2Bet !== null) {
      const player1Bet = game.player1Bet;
      const player2Bet = game.player2Bet;

      // 배팅 결과 계산
      if (player1Bet > player2Bet) {
        game.player1Gold -= player1Bet;
        game.player2Gold -= player2Bet;
        game.player1Power += game.currentCardPower;
      } else if (player2Bet > player1Bet) {
        game.player1Gold -= player1Bet;
        game.player2Gold -= player2Bet;
        game.player2Power += game.currentCardPower;
      } else {
        game.player1Gold -= player1Bet;
        game.player2Gold -= player2Bet;
        //홀수 라운드에 플레이어1의 점수가 오름
        if (game.currentRound % 2 === 1) {
          game.player1Power += game.currentCardPower;
        } else {
          game.player2Power += game.currentCardPower;
        }
      }
      // 다음 라운드로 진행
      game.currentRound += 1;

      if (game.currentRound <= 15) {
        const newCard = game.cardDeck[game.currentRound - 1]; // 새로운 카드 제공
        game.currentCardPower = newCard.power;
        game.currentCardName = newCard.name;
        game.currentCardImage = newCard.image;

        io.to(`${game.player1}-${game.player2}`).emit("roundResult", {
          player1Gold: game.player1Gold,
          player2Gold: game.player2Gold,
          player1Power: game.player1Power,
          player2Power: game.player2Power,
          currentCardName: game.currentCardName,
          currentCardPower: game.currentCardPower,
          currentCardImage: game.currentCardImage,
          currentRound: game.currentRound,
          player1Bet: game.player1Bet,
          player2Bet: game.player2Bet,
        });
      }

      // 배팅 금액 초기화
      game.player1Bet = null;
      game.player2Bet = null;
      res.status(200).json({ success: true });
      await game.save();
      // 게임 종료 체크
      await checkGameOver(game);
    } else {
      await game.save();
      res
        .status(200)
        .json({ success: true, message: "Bet placed, waiting for opponent" });
    }
  } catch (err) {
    console.error("Error placing bet:", err);
    res.status(500).send("Failed to place bet");
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

      res.status(200).json({ success: true, player1Email, player2Email });
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
    // 모든 Guest 계정을 가져옵니다.
    const guests = await User.find({ email: "" }).sort({ nickname: 1 });

    // Guest 번호를 추적하기 위한 배열 생성
    const guestNumbers = guests.map((guest) =>
      parseInt(guest.nickname.replace("Guest", ""))
    );

    // 첫 번째 비어 있는 Guest 번호를 찾습니다.
    let newGuestNumber = 1;
    while (guestNumbers.includes(newGuestNumber)) {
      newGuestNumber++;
    }

    // 새로운 Guest 계정을 생성합니다.
    const newGuest = new User({
      email: "AuctionKingdomGuest${newGuestNumber}",
      nickname: `Guest${newGuestNumber}`,
      kingdomName: `GuestKingdom${newGuestNumber}`,
      score: 0,
      wins: 0,
      losses: 0,
      draws: 0,
      coins: 0,
    });

    // 새로운 Guest 계정을 저장합니다.
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

// Delete guest user data
app.post("/api/deleteUser", (req, res) => {
  const { email } = req.body;
  db.collection("users").deleteOne({ email: email }, (err, result) => {
    if (err) {
      res.status(500).send("Error deleting user data");
    } else if (result.deletedCount === 0) {
      res.status(404).send("User not found");
    } else {
      res.send("User deleted successfully");
    }
  });
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
    item1: 0,
    item2: 0,
    item3: 0,
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

app.get("/api/getUser", (req, res) => {
  const email = req.query.email;
  db.collection("users").findOne({ email: email }, (err, user) => {
    if (err) {
      res.status(500).json({ error: "Error retrieving user data" });
    } else if (!user) {
      res.status(404).json({ error: "User not found" });
    } else {
      res.json(user);
    }
  });
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

// Update user data
app.post("/api/updateUser", (req, res) => {
  const { email, nickname, kingdomName, profileImage } = req.body;
  db.collection("users").updateOne(
    { email: email },
    {
      $set: {
        nickname: nickname,
        kingdomName: kingdomName,
        profileImage: profileImage,
      },
    },
    (err, result) => {
      if (err) {
        res.status(500).send("Error updating user data");
      } else if (result.matchedCount === 0) {
        res.status(404).send("User not found");
      } else {
        res.send("User updated successfully");
      }
    }
  );
});

// Check for duplicate nickname or kingdom name
app.post("/api/checkAvailability", (req, res) => {
  const { field, value } = req.body;
  const isAvailable = !users.some((user) => user[field] === value);
  res.json({ available: isAvailable });
});

// Update user items and coins
app.post("/api/updateUserItems", (req, res) => {
  const { email, item, cost } = req.body;
  const updateQuery = { email: email };
  const updateData = {
    $inc: { coins: -cost, [item]: 1 }, // 아이템 개수 증가 및 코인 차감
  };

  db.collection("users").updateOne(updateQuery, updateData, (err, result) => {
    if (err) {
      res.status(500).json({ error: "Error updating user items" });
    } else if (result.matchedCount === 0) {
      res.status(404).json({ error: "User not found" });
    } else {
      // 성공 시 새로운 코인 값을 반환
      db.collection("users").findOne(updateQuery, (err, user) => {
        if (err || !user) {
          res.status(500).json({ error: "Error fetching updated user data" });
        } else {
          res.json({ coins: user.coins, [item]: user[item] });
        }
      });
    }
  });
});

const PORT = process.env.PORT || 80;
server.listen(PORT, () => {
  console.log(`Server is running on port ${PORT}`);
});
