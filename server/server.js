const express = require("express");
const mongoose = require("mongoose");
const bodyParser = require("body-parser");
const cors = require("cors");
require("dotenv").config(); // .env 파일의 환경 변수를 로드

// MongoDB 연결 설정
mongoose.connect(process.env.MONGODB_URI, {});

const db = mongoose.connection;
db.on("error", console.error.bind(console, "MongoDB connection error:"));
db.once("open", () => {
  console.log("Connected to MongoDB");
});

// 사용자 스키마 및 모델 설정
const userSchema = new mongoose.Schema({
  name: String,
  email: String,
  profileUrl: String,
});

const User = mongoose.model("User", userSchema);

// Express 애플리케이션 설정
const app = express();
app.use(bodyParser.json());
app.use(cors());

// 사용자 저장 엔드포인트
app.post("/api/saveUser", async (req, res) => {
  const { name, email, profileUrl } = req.body;
  console.log(`Received user: ${name}, ${email}, ${profileUrl}`); // 추가된 로그

  const newUser = new User({
    name,
    email,
    profileUrl,
  });

  try {
    const savedUser = await newUser.save();
    console.log("User saved:", savedUser); // 추가된 로그
    res.status(201).send("User saved successfully");
  } catch (err) {
    console.error("Error saving user:", err); // 추가된 로그
    res.status(500).send("Failed to save user");
  }
});

// 서버 시작
const PORT = process.env.PORT || 5000; // .env 파일에서 PORT를 가져오도록 수정
app.listen(PORT, () => {
  console.log(`Server is running on port ${PORT}`);
});
