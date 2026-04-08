<?php

require_once __DIR__ . "/../core/Api.php";
require_once __DIR__ . "/../model/Database.php";
require_once __DIR__ . "/../model/giangvien/baithi.model.php";

$user = Api::requireLogin();
$data = Api::jsonInput();

$id_nguoidung = (int) ($user["id_nguoidung"] ?? 0);
$id_baithi = (int) ($data["id_baithi"] ?? 0);
$id_monhoc = (int) ($data["id_monhoc"] ?? 0);

if ($id_monhoc <= 0 || trim($data["ten_baithi"] ?? "") === "") {
    Api::json(["error" => "Thiếu thông tin bài thi"], 400);
}

$conn = Database::connect();

$sqlMon = "SELECT id_monhoc FROM monhoc WHERE id_monhoc = ? AND id_nguoidung = ?";
$stmtMon = $conn->prepare($sqlMon);
$stmtMon->bind_param("ii", $id_monhoc, $id_nguoidung);
$stmtMon->execute();

if ($stmtMon->get_result()->num_rows === 0) {
    $conn->close();
    Api::json(["error" => "Môn học không hợp lệ hoặc không thuộc quyền của bạn"], 403);
}

if ($id_baithi > 0) {
    $sqlExam = "SELECT bt.id_baithi
        FROM baithi bt
        JOIN monhoc mh ON bt.id_monhoc = mh.id_monhoc
        WHERE bt.id_baithi = ? AND mh.id_nguoidung = ?";
    $stmtExam = $conn->prepare($sqlExam);
    $stmtExam->bind_param("ii", $id_baithi, $id_nguoidung);
    $stmtExam->execute();

    if ($stmtExam->get_result()->num_rows === 0) {
        $conn->close();
        Api::json(["error" => "Bạn không có quyền sửa bài thi này"], 403);
    }
}

$payload = [
    "id_baithi" => $id_baithi,
    "id_monhoc" => $id_monhoc,
    "ten_baithi" => trim($data["ten_baithi"] ?? ""),
    "tongcauhoi" => (int) ($data["tongcauhoi"] ?? 0),
    "thoigianlam" => (int) ($data["thoigianlam"] ?? 0),
    "thoigianbatdau" => $data["thoigianbatdau"] ?? "",
    "thoigianketthuc" => $data["thoigianketthuc"] ?? null,
    "trangthai" => $data["trangthai"] ?? "Đang mở",
    "mieuta" => $data["mieuta"] ?? null,
];

$ok = save_baithi($payload);
$conn->close();

if (!$ok) {
    $message = $_SESSION["error"] ?? "Không thể lưu bài thi";
    unset($_SESSION["error"]);
    Api::json(["error" => $message], 400);
}

Api::json([
    "success" => true,
    "message" => $id_baithi > 0 ? "Cập nhật bài thi thành công" : "Thêm bài thi thành công",
]);
