<?php

require_once __DIR__ . "/../core/Api.php";
require_once __DIR__ . "/../model/Database.php";
require_once __DIR__ . "/../model/giangvien/cauhoi.model.php";

$user = Api::requireLogin();
$data = Api::jsonInput();

$id_baithi = (int) ($data["id_baithi"] ?? 0);
$id_cauhoi = (int) ($data["id_cauhoi"] ?? 0);
$noidungcauhoi = trim($data["noidungcauhoi"] ?? "");
$dokho = $data["dokho"] ?? "Dễ";
$loai_cauhoi = (int) ($data["loai_cauhoi"] ?? 1);
$options = $data["options"] ?? [];
$correctIndex = isset($data["correct_index"]) ? (int) $data["correct_index"] : -1;

if ($id_baithi <= 0 || $noidungcauhoi === "") {
    Api::json(["error" => "Dữ liệu câu hỏi không hợp lệ"], 400);
}

if ($loai_cauhoi === 1) {
    if (count($options) < 2 || $correctIndex < 0) {
        Api::json(["error" => "Trắc nghiệm cần ít nhất 2 đáp án và 1 đáp án đúng"], 400);
    }
} else {
    // Fill-in-the-blank
    if (count($options) < 1) {
        Api::json(["error" => "Điền từ cần chính xác 1 đáp án đúng"], 400);
    }
    $correctIndex = 0; // Force first
}

$conn = Database::connect();
$role = $user["vaitro"] ?? "";
$ownerId = (int) ($user["id_nguoidung"] ?? 0);
$sql = "SELECT bt.id_baithi
    FROM baithi bt
    JOIN monhoc mh ON bt.id_monhoc = mh.id_monhoc
    WHERE bt.id_baithi = ? AND (mh.id_nguoidung = ? OR ? = 'admin')";
$stmt = $conn->prepare($sql);
$stmt->bind_param("iis", $id_baithi, $ownerId, $role);
$stmt->execute();
if ($stmt->get_result()->num_rows === 0) {
    // $conn->close();
    Api::json(["error" => "Bạn không có quyền sửa bài thi này"], 403);
}
// $conn->close();

$dapan_list = [];
$temp_check = [];
foreach ($options as $index => $noidung) {
    $noidung = trim((string) $noidung);
    if ($noidung === "") {
        Api::json(["error" => "Đáp án không được để trống"], 400);
    }

    $normalized = mb_strtolower($noidung, "UTF-8");
    if (in_array($normalized, $temp_check, true)) {
        Api::json(["error" => "Các đáp án không được trùng nhau"], 400);
    }
    $temp_check[] = $normalized;

    $dapan_list[] = [
        "noidung" => $noidung,
        "dapandung" => $index === $correctIndex ? 1 : 0,
    ];
}

$model = new CauHoiModel();
$result = $id_cauhoi > 0
    ? $model->update($id_cauhoi, $noidungcauhoi, $dokho, $loai_cauhoi, $dapan_list)
    : $model->create($id_baithi, $noidungcauhoi, $dokho, $loai_cauhoi, $dapan_list);

if (!($result["success"] ?? false)) {
    Api::json(["error" => $result["message"] ?? "Không thể lưu câu hỏi"], 400);
}

Api::json([
    "success" => true,
    "message" => $id_cauhoi > 0 ? "Cập nhật câu hỏi thành công" : "Thêm câu hỏi thành công",
]);
