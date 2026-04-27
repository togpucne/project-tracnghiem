<?php

require_once __DIR__ . "/../core/Api.php";
require_once __DIR__ . "/../model/giangvien/monhoc.model.php";

$user = Api::requireLogin();
$data = Api::jsonInput();

$id_monhoc = (int) ($data["id_monhoc"] ?? 0);

if ($id_monhoc <= 0) {
    Api::json(["error" => "Thi?u ID môn h?c"], 400);
}

$existing = getOne_monhoc($id_monhoc);
if (!$existing) {
    Api::json(["error" => "Không tìm th?y môn h?c"], 404);
}

if (($user["vaitro"] ?? "") !== "admin" && (int) $existing["id_nguoidung"] !== (int) ($user["id_nguoidung"] ?? 0)) {
    Api::json(["error" => "B?n không có quy?n xóa môn h?c này"], 403);
}

$examCount = count_baithi_by_monhoc($id_monhoc);
if ($examCount > 0) {
    Api::json(["error" => "Môn h?c dang có bài thi liên quan, vui lòng xóa bài thi tru?c"], 409);
}

$ok = delete_monhoc($id_monhoc, (int) ($user["id_nguoidung"] ?? 0), $user["vaitro"] ?? "");

if (!$ok) {
    Api::json(["error" => "Không th? xóa môn h?c"], 500);
}

Api::json([
    "success" => true,
    "message" => "Xóa môn h?c thành công",
]);
