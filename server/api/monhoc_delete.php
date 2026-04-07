<?php

require_once __DIR__ . "/../core/Api.php";
require_once __DIR__ . "/../model/giangvien/monhoc.model.php";

$user = Api::requireLogin();
$data = Api::jsonInput();

$id_monhoc = (int) ($data["id_monhoc"] ?? 0);

if ($id_monhoc <= 0) {
    Api::json(["error" => "Thi?u ID môn h?c"], 400);
}

$ok = delete_monhoc($id_monhoc, (int) ($user["id_nguoidung"] ?? 0), $user["vaitro"] ?? "");

if (!$ok) {
    Api::json(["error" => "Không th? xóa môn h?c ho?c b?n không có quy?n"], 403);
}

Api::json([
    "success" => true,
    "message" => "Xóa môn h?c thành công",
]);

