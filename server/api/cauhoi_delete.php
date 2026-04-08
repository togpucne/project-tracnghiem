<?php

require_once __DIR__ . "/../core/Api.php";
require_once __DIR__ . "/../model/Database.php";
require_once __DIR__ . "/../model/giangvien/cauhoi.model.php";

$user = Api::requireLogin();
$data = Api::jsonInput();

$id_cauhoi = (int) ($data["id_cauhoi"] ?? 0);

if ($id_cauhoi <= 0) {
    Api::json(["error" => "Thiếu ID câu hỏi"], 400);
}

$model = new CauHoiModel();
$cauhoi = $model->getById($id_cauhoi);
if (!$cauhoi) {
    Api::json(["error" => "Không tìm thấy câu hỏi"], 404);
}

$conn = Database::connect();
$role = $user["vaitro"] ?? "";
$ownerId = (int) ($user["id_nguoidung"] ?? 0);
$sql = "SELECT bt.id_baithi
    FROM baithi bt
    JOIN monhoc mh ON bt.id_monhoc = mh.id_monhoc
    WHERE bt.id_baithi = ? AND (mh.id_nguoidung = ? OR ? = 'admin')";
$stmt = $conn->prepare($sql);
$stmt->bind_param("iis", $cauhoi["id_baithi"], $ownerId, $role);
$stmt->execute();
if ($stmt->get_result()->num_rows === 0) {
    $conn->close();
    Api::json(["error" => "Bạn không có quyền xóa câu hỏi này"], 403);
}
$conn->close();

$result = $model->delete($id_cauhoi);

if (!($result["success"] ?? false)) {
    Api::json(["error" => $result["message"] ?? "Không thể xóa câu hỏi"], 400);
}

Api::json([
    "success" => true,
    "message" => "Xóa câu hỏi thành công",
]);
