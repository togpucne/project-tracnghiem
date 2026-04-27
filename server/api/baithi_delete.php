<?php

require_once __DIR__ . "/../core/Api.php";
require_once __DIR__ . "/../model/Database.php";
require_once __DIR__ . "/../model/giangvien/baithi.model.php";

$user = Api::requireLogin();
$data = Api::jsonInput();

$id_baithi = (int) ($data["id_baithi"] ?? 0);
$id_nguoidung = (int) ($user["id_nguoidung"] ?? 0);

if ($id_baithi <= 0) {
    Api::json(["error" => "Thi?u ID bài thi"], 400);
}

$conn = Database::connect();
$sql = "SELECT bt.id_baithi
    FROM baithi bt
    JOIN monhoc mh ON bt.id_monhoc = mh.id_monhoc
    WHERE bt.id_baithi = ? AND mh.id_nguoidung = ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("ii", $id_baithi, $id_nguoidung);
$stmt->execute();

if ($stmt->get_result()->num_rows === 0) {
    // $conn->close();
    Api::json(["error" => "B?n không có quy?n xóa bài thi này"], 403);
}

// $conn->close();
$ok = delete_baithi($id_baithi);

if (!$ok) {
    Api::json(["error" => "Không th? xóa bài thi"], 500);
}

Api::json([
    "success" => true,
    "message" => "Xóa bài thi thành công",
]);
