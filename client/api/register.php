<?php
header("Content-Type: application/json");

require_once __DIR__ . "/../core/Database.php";
require_once __DIR__ . "/../core/Response.php";

$data = json_decode(file_get_contents("php://input"), true);

if (
    empty($data['fullname']) ||
    empty($data['email']) ||
    empty($data['password'])
) {
    Response::json(["error" => "Thiếu thông tin"], 400);
}

if (!filter_var($data['email'], FILTER_VALIDATE_EMAIL)) {
    Response::json(["error" => "Email không hợp lệ"], 400);
}

if (strlen($data['password']) < 6) {
    Response::json(["error" => "Mật khẩu tối thiểu 6 ký tự"], 400);
}

$conn = Database::connect();

/* Kiểm tra email tồn tại */
$stmt = $conn->prepare("SELECT id_nguoidung FROM nguoidung WHERE email = ?");
$stmt->bind_param("s", $data['email']);
$stmt->execute();
$stmt->store_result();

if ($stmt->num_rows > 0) {
    Response::json(["error" => "Email đã tồn tại"], 400);
}

/* Insert */
$passwordHash = password_hash($data['password'], PASSWORD_BCRYPT);

$stmt = $conn->prepare("
    INSERT INTO nguoidung(email, matkhau, ten, vaitro, trangthai, ngaytao)
    VALUES (?, ?, ?, 'thisinh', 'active', NOW())
");

$stmt->bind_param(
    "sss",
    $data['email'],
    $passwordHash,
    $data['fullname']
);

if (!$stmt->execute()) {
    Response::json(["error" => "Lỗi hệ thống"], 500);
}

Response::json(["message" => "Đăng ký thành công"]);