<?php
header("Content-Type: application/json");
require_once __DIR__ . "/../core/Database.php";
require_once __DIR__ . "/../core/Response.php";

session_start();

if (!isset($_SESSION['user'])) {
    Response::json(["success" => false, "error" => "Vui lòng đăng nhập"], 401);
}

$user_id = $_SESSION['user']['id'];
$conn = Database::connect();

$stmt = $conn->prepare("SELECT email, ten, ngaytao FROM nguoidung WHERE id_nguoidung = ?");
$stmt->bind_param("i", $user_id);
$stmt->execute();
$res = $stmt->get_result();
$user = $res->fetch_assoc();

if (!$user) {
    Response::json(["success" => false, "error" => "Không tìm thấy người dùng"], 404);
}

Response::json(["success" => true, "data" => $user]);
