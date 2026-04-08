<?php

require_once __DIR__ . "/../core/Api.php";
require_once __DIR__ . "/../model/giangvien/monhoc.model.php";

$user = Api::requireLogin();
$data = Api::jsonInput();

$id_monhoc = (int) ($data["id_monhoc"] ?? 0);

if ($id_monhoc <= 0) {
    Api::json(["error" => "Thiếu ID môn học"], 400);
}

$ok = delete_monhoc($id_monhoc, (int) ($user["id_nguoidung"] ?? 0), $user["vaitro"] ?? "");

if (!$ok) {
    Api::json(["error" => "Không thể xóa môn học hoặc bạn không có quyền"], 403);
}

Api::json([
    "success" => true,
    "message" => "Xóa môn học thành công",
]);
