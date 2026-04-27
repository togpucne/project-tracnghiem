<?php

require_once __DIR__ . "/../core/Api.php";
require_once __DIR__ . "/../core/Database.php";
require_once __DIR__ . "/../core/Response.php";

$data = Api::jsonInput();

if (empty($data["email"]) || empty($data["password"])) {
    Response::json(["error" => "Thieu thong tin"], 400);
}

$conn = Database::connect();

$stmt = $conn->prepare("
    SELECT id_nguoidung, ten, matkhau, ngaytao, vaitro, trangthai, avatar
    FROM nguoidung
    WHERE email = ?
");

$stmt->bind_param("s", $data["email"]);
$stmt->execute();
$stmt->bind_result($id, $ten, $hashedPassword, $ngaytao, $vaitro, $trangthai, $avatar);

if (!$stmt->fetch()) {
    Response::json(["error" => "Sai tai khoan hoac mat khau"], 401);
}

if (!password_verify($data["password"], $hashedPassword)) {
    Response::json(["error" => "Sai tai khoan hoac mat khau"], 401);
}

if ($trangthai !== "active") {
    Response::json(["error" => "Tai khoan da bi khoa"], 403);
}

require_once __DIR__ . "/../core/TokenManager.php";

$_SESSION["user"] = [
    "id" => $id,
    "name" => $ten,
    "role" => $vaitro,
    "avatar" => $avatar ?? 'default.jpg'
];

// Generate API Token for Desktop App
$token = TokenManager::generateToken([
    "id" => $id,
    "email" => $data["email"],
    "role" => $vaitro
]);

Response::json([
    "success" => true,
    "message" => "Dang nhap thanh cong",
    "token" => $token,
    "id" => $id,
    "id_nguoidung" => $id,
    "ten" => $ten,
    "email" => $data["email"],
    "ngaytao" => $ngaytao,
    "role" => $vaitro,
    "avatar" => $avatar ?? 'default.jpg'
]);
