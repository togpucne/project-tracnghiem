<?php

require_once __DIR__ . "/../core/Api.php";
require_once __DIR__ . "/../core/Database.php";
require_once __DIR__ . "/../core/Response.php";


$data = Api::jsonInput();

if (!isset($data["id_lanthi"])) {
    Response::json(["error" => "Invalid payload"], 400);
}

$id_lanthi = (int) $data["id_lanthi"];
$thoigianconlai = isset($data["thoigianconlai"]) ? (int) $data["thoigianconlai"] : null;
$answers = isset($data["answers"]) ? json_encode($data["answers"]) : null;

$conn = Database::connect();
$user_id = $_SESSION["user"]["id"];

$stmt = $conn->prepare("SELECT id_lanthi FROM lanthi WHERE id_lanthi = ? AND id_nguoidung = ?");
$stmt->bind_param("ii", $id_lanthi, $user_id);
$stmt->execute();

if ($stmt->get_result()->num_rows === 0) {
    Response::json(["error" => "Forbidden"], 403);
}

$stmt = $conn->prepare("UPDATE lanthi SET thoigianconlai = ?, cautraloi_tam = ? WHERE id_lanthi = ?");
$stmt->bind_param("isi", $thoigianconlai, $answers, $id_lanthi);

if ($stmt->execute()) {
    Response::json(["success" => true]);
}

Response::json(["error" => "Failed to sync"], 500);

