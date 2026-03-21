<?php
session_start();
header("Content-Type: application/json");

require_once __DIR__ . "/../core/Database.php";
require_once __DIR__ . "/../core/Response.php";

if (!isset($_SESSION['user'])) {
    Response::json(["success" => false, "error" => "Vui lòng đăng nhập"], 401);
}

$conn = Database::connect();
$data = json_decode(file_get_contents("php://input"), true);

$user_id = $_SESSION['user']['id'];
$ten = trim($data['ten'] ?? '');
$email = trim($data['email'] ?? '');
$matkhau = $data['matkhau'] ?? '';

if (empty($ten) || empty($email)) {
    Response::json(["success" => false, "error" => "Họ tên và Email không được để trống"], 400);
}

// Kiểm tra email trùng
$stmt = $conn->prepare("SELECT id_nguoidung FROM nguoidung WHERE email = ? AND id_nguoidung != ?");
$stmt->bind_param("si", $email, $user_id);
$stmt->execute();
if ($stmt->get_result()->num_rows > 0) {
    Response::json(["success" => false, "error" => "Email này đã được sử dụng bởi người khác"], 400);
}

if (!empty($matkhau)) {
    if (strlen($matkhau) < 6) {
        Response::json(["success" => false, "error" => "Mật khẩu mới phải có tối thiểu 6 ký tự"], 400);
    }
    $hashedPassword = password_hash($matkhau, PASSWORD_DEFAULT);
    $stmt = $conn->prepare("UPDATE nguoidung SET ten = ?, email = ?, matkhau = ? WHERE id_nguoidung = ?");
    $stmt->bind_param("sssi", $ten, $email, $hashedPassword, $user_id);
}
else {
    $stmt = $conn->prepare("UPDATE nguoidung SET ten = ?, email = ? WHERE id_nguoidung = ?");
    $stmt->bind_param("ssi", $ten, $email, $user_id);
}

if ($stmt->execute()) {
    // Lưu lại tên mới vào session để thanh điều hướng tự cập nhật tên mới
    $_SESSION['user']['name'] = $ten;
    Response::json(["success" => true, "message" => "Cập nhật thông tin thành công!"]);
}
else {
    Response::json(["success" => false, "error" => "Lỗi hệ thống khi cập nhật cơ sở dữ liệu"], 500);
}
