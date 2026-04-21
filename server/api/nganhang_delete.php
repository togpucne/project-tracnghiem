<?php

require_once __DIR__ . "/../core/Api.php";
require_once __DIR__ . "/../model/giangvien/nganhang.model.php";

$user = Api::requireRole(["admin", "giangvien"]);
$data = Api::jsonInput();
$id_nganhang = (int) ($data["id_nganhang"] ?? 0);

if ($id_nganhang <= 0) {
    Api::json(["error" => "Thiếu ID ngân hàng câu hỏi"], 400);
}

$existing = getQuestionBankById($id_nganhang, (int) ($user["id_nguoidung"] ?? 0), $user["vaitro"] ?? "");
if (!$existing) {
    Api::json(["error" => "Không tìm thấy ngân hàng câu hỏi"], 404);
}

if (!softDeleteQuestionBank($id_nganhang)) {
    Api::json(["error" => "Không thể khóa ngân hàng câu hỏi"], 500);
}

Api::json([
    "success" => true,
    "message" => "Đã khóa ngân hàng câu hỏi thành công",
]);
