<?php
session_start();
header("Content-Type: application/json");

require_once __DIR__ . "/../core/Database.php";
require_once __DIR__ . "/../core/Response.php";

$data = json_decode(file_get_contents("php://input"), true);

if (empty($data['email']) || empty($data['password'])) {
    Response::json(["error" => "Thiếu thông tin"], 400);
}

$conn = Database::connect();

$stmt = $conn->prepare("
    SELECT id_nguoidung, ten, matkhau, ngaytao
    FROM nguoidung
    WHERE email = ?
");

$stmt->bind_param("s", $data['email']);
$stmt->execute();
$stmt->bind_result($id, $ten, $hashedPassword, $ngaytao);

if (!$stmt->fetch()) {
    Response::json(["error" => "Sai tài khoản hoặc mật khẩu"], 401);
}

if (!password_verify($data['password'], $hashedPassword)) {
    Response::json(["error" => "Sai tài khoản hoặc mật khẩu"], 401);
}

/* LƯU SESSION */
$_SESSION['user'] = [
    "id" => $id,
    "name" => $ten
];

Response::json([
    "message" => "Đăng nhập thành công",
    "ten" => $ten,
    "email" => $data['email'],
    "ngaytao" => $ngaytao
]);