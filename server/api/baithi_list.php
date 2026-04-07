<?php

require_once __DIR__ . "/../core/Api.php";
require_once __DIR__ . "/../model/giangvien/baithi.model.php";

$user = Api::requireLogin();

$id_nguoidung = (int) ($user["id_nguoidung"] ?? 0);

Api::json([
    "success" => true,
    "data" => getAll_baithi($id_nguoidung),
    "subjects" => getAll_monhoc($id_nguoidung),
]);

