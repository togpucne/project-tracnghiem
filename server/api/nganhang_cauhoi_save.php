<?php

require_once __DIR__ . "/../core/Api.php";
require_once __DIR__ . "/../core/ApiSecurityValidator.php";
require_once __DIR__ . "/../model/giangvien/nganhang.model.php";

$user = Api::requireRole(["admin", "giangvien"]);
$data = Api::jsonInput();

try {
    $id_nganhang = ApiSecurityValidator::validateInt($data["id_nganhang"] ?? 0, "ID ngân hàng", 1);
    $id_monhoc = ApiSecurityValidator::validateInt($data["id_monhoc"] ?? 0, "ID môn học", 1);
    $id_cauhoi_nganhang = isset($data["id_cauhoi_nganhang"]) ? ApiSecurityValidator::validateInt($data["id_cauhoi_nganhang"], "ID câu hỏi", 0) : 0;
    $noidungcauhoi = ApiSecurityValidator::validateString($data["noidungcauhoi"] ?? "", "Nội dung câu hỏi", 5, 5000);
    $dokho = ApiSecurityValidator::validateEnum($data["dokho"] ?? "de", "Độ khó", ["de", "trungbinh", "kho"]);
    $trangthai = ApiSecurityValidator::validateEnum($data["trangthai"] ?? "active", "Trạng thái", ["active", "inactive"]);
    $loai_cauhoi = (int) ($data["loai_cauhoi"] ?? 1);
} catch (Exception $e) {
    Api::json(["error" => $e->getMessage()], 400);
}

$bank = getQuestionBankById($id_nganhang, (int) ($user["id_nguoidung"] ?? 0), $user["vaitro"] ?? "");
if (!$bank) {
    Api::json(["error" => "Không tìm thấy ngân hàng câu hỏi"], 404);
}

$bankSubjectIds = array_map(fn($item) => (int) $item["id_monhoc"], $bank["subjects"] ?? []);
if (!in_array($id_monhoc, $bankSubjectIds, true)) {
    Api::json(["error" => "Môn học chưa được gắn vào ngân hàng câu hỏi này"], 400);
}

$options = $data["options"] ?? [];
$correctIndex = isset($data["correct_index"]) ? (int) $data["correct_index"] : -1;

// Flexible validation based on type
if ($loai_cauhoi === 1) {
    if (!is_array($options) || count($options) < 2 || $correctIndex < 0) {
        Api::json(["error" => "Trắc nghiệm cần ít nhất 2 đáp án và 1 đáp án đúng"], 400);
    }
} else {
    if (!is_array($options) || count($options) < 1) {
        Api::json(["error" => "Điền từ cần chính xác 1 đáp án đúng"], 400);
    }
    $correctIndex = 0; // Force first one as correct for fill-in
}

$answers = [];
$normalized = [];
foreach ($options as $index => $option) {
    $value = trim((string) $option);
    if ($value === "") {
        Api::json(["error" => "Đáp án không được để trống"], 400);
    }
    $check = mb_strtolower($value, "UTF-8");
    if (in_array($check, $normalized, true)) {
        Api::json(["error" => "Các đáp án không được trùng nhau"], 400);
    }
    $normalized[] = $check;
    $answers[] = [
        "noidung" => $value,
        "dapandung" => $index === $correctIndex ? 1 : 0,
    ];
}

if ($id_cauhoi_nganhang > 0) {
    $existingQuestion = getQuestionBankQuestionById($id_cauhoi_nganhang, (int) ($user["id_nguoidung"] ?? 0), $user["vaitro"] ?? "");
    if (!$existingQuestion) {
        Api::json(["error" => "Không tìm thấy câu hỏi ngân hàng"], 404);
    }

    $ok = updateQuestionBankQuestion($id_cauhoi_nganhang, $id_monhoc, $noidungcauhoi, $dokho, $loai_cauhoi, $trangthai, $answers);
    if (!$ok) {
        Api::json(["error" => "Không thể cập nhật câu hỏi ngân hàng"], 500);
    }

    Api::json([
        "success" => true,
        "message" => "Cập nhật câu hỏi ngân hàng thành công",
    ]);
}

$newId = createQuestionBankQuestion($id_nganhang, $id_monhoc, $noidungcauhoi, $dokho, $loai_cauhoi, $trangthai, $answers);
if ($newId <= 0) {
    Api::json(["error" => "Không thể tạo câu hỏi ngân hàng"], 500);
}

Api::json([
    "success" => true,
    "message" => "Tạo câu hỏi ngân hàng thành công",
], 201);
