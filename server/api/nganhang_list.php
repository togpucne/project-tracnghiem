<?php

require_once __DIR__ . "/../core/Api.php";
require_once __DIR__ . "/../model/giangvien/nganhang.model.php";

$user = Api::requireRole(["admin", "giangvien"]);

$id_monhoc = isset($_GET["id_monhoc"]) ? (int) $_GET["id_monhoc"] : 0;

Api::json([
    "success" => true,
    "banks" => getQuestionBanks((int) ($user["id_nguoidung"] ?? 0), $user["vaitro"] ?? "", $id_monhoc),
    "subjects" => getAccessibleBankSubjects((int) ($user["id_nguoidung"] ?? 0), $user["vaitro"] ?? ""),
]);
