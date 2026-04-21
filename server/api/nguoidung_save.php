<?php

require_once __DIR__ . "/../core/Api.php";
require_once __DIR__ . "/../model/admin/nguoidung.model.php";

Api::requireRole(["admin"]);

$data = Api::jsonInput();

$id_nguoidung = (int) ($data["id_nguoidung"] ?? 0);
$email = trim((string) ($data["email"] ?? ""));
$ten = trim((string) ($data["ten"] ?? ""));
$vaitro = normalizeManagedRole($data["vaitro"] ?? "");
$trangthai = normalizeUserStatus($data["trangthai"] ?? "active") ?? "active";
$matkhau = (string) ($data["matkhau"] ?? "");

if ($email === "" || !filter_var($email, FILTER_VALIDATE_EMAIL)) {
    Api::json(["error" => "Email không hợp lệ"], 400);
}

if ($ten === "") {
    Api::json(["error" => "Tên người dùng không được để trống"], 400);
}

if ($vaitro === null) {
    Api::json(["error" => "Vai trò chỉ được là thí sinh hoặc giảng viên"], 400);
}

if ($id_nguoidung === 0 && strlen($matkhau) < 6) {
    Api::json(["error" => "Mật khẩu phải có ít nhất 6 ký tự"], 400);
}

if ($id_nguoidung > 0 && $matkhau !== "" && strlen($matkhau) < 6) {
    Api::json(["error" => "Mật khẩu mới phải có ít nhất 6 ký tự"], 400);
}

if (isUserEmailExists($email, $id_nguoidung)) {
    Api::json(["error" => "Email này đã tồn tại"], 409);
}

if ($id_nguoidung > 0) {
    $existing = getManagedUserById($id_nguoidung);
    if (!$existing) {
        Api::json(["error" => "Không tìm thấy người dùng"], 404);
    }

    $ok = updateManagedUser($id_nguoidung, $email, $ten, $vaitro, $trangthai, $matkhau);
    if (!$ok) {
        Api::json(["error" => "Không thể cập nhật người dùng"], 500);
    }

    Api::json([
        "success" => true,
        "message" => "Cập nhật người dùng thành công",
        "data" => getManagedUserById($id_nguoidung),
    ]);
}

$newId = createManagedUser($email, $matkhau, $ten, $vaitro, $trangthai);
if ($newId <= 0) {
    Api::json(["error" => "Không thể tạo người dùng"], 500);
}

Api::json([
    "success" => true,
    "message" => "Thêm người dùng thành công",
    "data" => getManagedUserById($newId),
], 201);
