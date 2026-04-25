<?php

require_once __DIR__ . "/../core/Api.php";
require_once __DIR__ . "/../core/Database.php";
require_once __DIR__ . "/../core/Response.php";


$user_id = $_SESSION["user"]["id"];
$conn = Database::connect();

$stmt = $conn->prepare("SELECT email, ten, ngaytao, avatar FROM nguoidung WHERE id_nguoidung = ?");
$stmt->bind_param("i", $user_id);
$stmt->execute();
$res = $stmt->get_result();
$user = $res->fetch_assoc();

if (!$user) {
    Response::json(["success" => false, "error" => "Khong tim thay nguoi dung"], 404);
}

Response::json(["success" => true, "data" => $user]);

