<?php
session_start();
header("Content-Type: application/json");
require_once __DIR__ . "/../core/Database.php";
require_once __DIR__ . "/../core/Response.php";

if (!isset($_SESSION['user'])) {
    Response::json(["error" => "Unauthorized"], 401);
}

$data = json_decode(file_get_contents("php://input"), true);
if (!$data) {
    // Attempt to handle cases where input is raw string
    $raw = file_get_contents("php://input");
    $data = json_decode($raw, true);
}

if (!$data || !isset($data['id_lanthi'])) {
    Response::json(["error" => "Invalid payload"], 400);
}

$id_lanthi = (int)$data['id_lanthi'];
$thoigianconlai = isset($data['thoigianconlai']) ? (int)$data['thoigianconlai'] : null;
$answers = isset($data['answers']) ? json_encode($data['answers']) : null;

$conn = Database::connect();
$user_id = $_SESSION['user']['id'];

// verify ownership
$stmt = $conn->prepare("SELECT id_lanthi FROM lanthi WHERE id_lanthi=? AND id_nguoidung=?");
$stmt->bind_param("ii", $id_lanthi, $user_id);
$stmt->execute();
if ($stmt->get_result()->num_rows === 0) {
    Response::json(["error" => "Forbidden"], 403);
}

$stmt = $conn->prepare("UPDATE lanthi SET thoigianconlai=?, cautraloi_tam=? WHERE id_lanthi=?");
$stmt->bind_param("isi", $thoigianconlai, $answers, $id_lanthi);

if ($stmt->execute()) {
    Response::json(["success" => true]);
} else {
    Response::json(["error" => "Failed to sync"], 500);
}
?>
