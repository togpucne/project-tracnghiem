<?php

require_once __DIR__ . "/../core/Api.php";
require_once __DIR__ . "/../core/ApiSecurityValidator.php";
require_once __DIR__ . "/../model/Database.php";
require_once __DIR__ . "/../model/giangvien/baithi.model.php";

$user = Api::requireLogin();
$data = Api::jsonInput();

$id_nguoidung = (int) ($user["id_nguoidung"] ?? 0);

try {
    $idRaw = $data["id_baithi"] ?? 0;
    $id_baithi = ($idRaw === "" || $idRaw === null) ? 0 : ApiSecurityValidator::validateInt($idRaw, "ID bài thi", 0);
    $only_toggle = !empty($data["only_toggle"]) || !empty($data["only_xao_tron"]);
    
    $xao_tron = isset($data["xao_tron"]) ? ApiSecurityValidator::validateBool($data["xao_tron"], "Xáo trộn") : null;
    $hien_dapan = isset($data["hien_dapan"]) ? ApiSecurityValidator::validateBool($data["hien_dapan"], "Hiện đáp án") : null;

    if (!$only_toggle) {
        $id_monhoc = ApiSecurityValidator::validateInt($data["id_monhoc"] ?? 0, "ID môn học", 1);
        $ten_baithi = ApiSecurityValidator::validateString($data["ten_baithi"] ?? "", "Tên bài thi", 3, 200);
        $tongcauhoi = isset($data["tongcauhoi"]) ? ApiSecurityValidator::validateInt($data["tongcauhoi"], "Số câu hỏi", 5, 1000) : 0;
        $thoigianlam = isset($data["thoigianlam"]) ? ApiSecurityValidator::validateInt($data["thoigianlam"], "Thời gian làm bài", 1, 480) : 0;
        $trangthai = ApiSecurityValidator::validateEnum($data["trangthai"] ?? "Đang mở", "Trạng thái", ["Đang mở", "Đóng"]);
        $thoigianbatdau = isset($data["thoigianbatdau"]) ? ApiSecurityValidator::validateDateTime($data["thoigianbatdau"], "Thời gian bắt đầu") : "";
        $thoigianketthuc = isset($data["thoigianketthuc"]) ? ApiSecurityValidator::validateDateTime($data["thoigianketthuc"], "Thời gian kết thúc", true) : null;
        $mieuta = isset($data["mieuta"]) ? ApiSecurityValidator::validateString($data["mieuta"], "Miêu tả", 0, 1000) : null;
    }
} catch (Exception $e) {
    Api::json(["error" => $e->getMessage()], 400);
}

$conn = Database::connect();
$shouldCloseConnection = true;

if (!$only_toggle) {
    $sqlMon = "SELECT id_monhoc FROM monhoc WHERE id_monhoc = ? AND id_nguoidung = ?";
    $stmtMon = $conn->prepare($sqlMon);
    $stmtMon->bind_param("ii", $id_monhoc, $id_nguoidung);
    $stmtMon->execute();

    if ($stmtMon->get_result()->num_rows === 0) {
        $conn->close();
        Api::json(["error" => "Môn học không hợp lệ hoặc không thuộc quyền của bạn"], 403);
    }
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
    "only_toggle" => $only_toggle
];

if ($only_toggle) {
    if ($xao_tron !== null) $payload["xao_tron"] = $xao_tron;
    if ($hien_dapan !== null) $payload["hien_dapan"] = $hien_dapan;
} else {
    $payload["id_monhoc"] = $id_monhoc;
    $payload["ten_baithi"] = $ten_baithi;
    $payload["tongcauhoi"] = $tongcauhoi;
    $payload["thoigianlam"] = $thoigianlam;
    $payload["thoigianbatdau"] = $thoigianbatdau;
    $payload["thoigianketthuc"] = $thoigianketthuc;
    $payload["trangthai"] = $trangthai;
    $payload["xao_tron"] = $xao_tron;
    $payload["hien_dapan"] = $hien_dapan;
    $payload["mieuta"] = $mieuta;
}

$ok = save_baithi($payload);

if ($shouldCloseConnection) {
    $conn->close();
}

if (!$ok) {
    $message = $_SESSION["error"] ?? "Không thể lưu bài thi";
    unset($_SESSION["error"]);
    Api::json(["error" => $message], 400);
}

Api::json([
    "success" => true,
    "message" => $id_baithi > 0 ? "Cập nhật bài thi thành công" : "Thêm bài thi thành công",
]);
