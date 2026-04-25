<?php

require_once __DIR__ . "/../core/Api.php";
require_once __DIR__ . "/../core/Database.php";
require_once __DIR__ . "/../core/Response.php";


$data = Api::jsonInput();

if (empty($data["fullname"]) || empty($data["email"]) || empty($data["password"])) {
    Response::json(["error" => "Thieu thong tin"], 400);
}

if (!filter_var($data["email"], FILTER_VALIDATE_EMAIL)) {
    Response::json(["error" => "Email khong hop le"], 400);
}

if (strlen($data["password"]) < 6) {
    Response::json(["error" => "Mat khau toi thieu 6 ky tu"], 400);
}

$conn = Database::connect();

$stmt = $conn->prepare("SELECT id_nguoidung FROM nguoidung WHERE email = ?");
$stmt->bind_param("s", $data["email"]);
$stmt->execute();
$stmt->store_result();

if ($stmt->num_rows > 0) {
    Response::json(["error" => "Email da ton tai"], 400);
}

$passwordHash = password_hash($data["password"], PASSWORD_BCRYPT);

$stmt = $conn->prepare("
    INSERT INTO nguoidung(email, matkhau, ten, vaitro, trangthai, ngaytao, avatar)
    VALUES (?, ?, ?, 'thisinh', 'active', NOW(), 'default.jpg')
");

$stmt->bind_param("sss", $data["email"], $passwordHash, $data["fullname"]);

if (!$stmt->execute()) {
    Response::json(["error" => "Loi he thong"], 500);
}

Response::json([
    "success" => true,
    "message" => "Dang ky thanh cong",
]);

