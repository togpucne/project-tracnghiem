<?php

require_once __DIR__ . "/../core/Api.php";
require_once __DIR__ . "/../model/giangvien/monhoc.model.php";

$user = Api::requireLogin();
$data = Api::jsonInput();

$id_monhoc = (int) ($data["id_monhoc"] ?? 0);
$tenmonhoc = trim($data["tenmonhoc"] ?? "");
$mieuta = trim($data["mieuta"] ?? "");
$mieuta = $mieuta === "" ? null : $mieuta;

if ($tenmonhoc === "") {
    Api::json(["error" => "Tên môn học không được để trống"], 400);
}

if (isDuplicateMonHoc($tenmonhoc, $id_monhoc)) {
    Api::json(["error" => "Tên môn học này đã tồn tại trong hệ thống"], 409);
}

if ($id_monhoc > 0) {
    $existing = getOne_monhoc($id_monhoc);
    if (!$existing) {
        Api::json(["error" => "Không tìm thấy môn học"], 404);
    }

    if (($user["vaitro"] ?? "") !== "admin" && (int) $existing["id_nguoidung"] !== (int) ($user["id_nguoidung"] ?? 0)) {
        Api::json(["error" => "Bạn không có quyền sửa môn học này"], 403);
    }

    $ok = update_monhoc_by_owner(
        $id_monhoc,
        $tenmonhoc,
        $mieuta,
        (int) ($user["id_nguoidung"] ?? 0),
        $user["vaitro"] ?? ""
    );
} else {
    $ok = insert_monhoc($tenmonhoc, (int) ($user["id_nguoidung"] ?? 0), $mieuta);
}

if (!$ok) {
    Api::json(["error" => "Không thể lưu môn học"], 500);
}

Api::json([
    "success" => true,
    "message" => $id_monhoc > 0 ? "Cập nhật môn học thành công" : "Thêm môn học thành công",
]);
