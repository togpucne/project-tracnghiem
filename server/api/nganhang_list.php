<?php

require_once __DIR__ . "/../core/Api.php";
require_once __DIR__ . "/../model/giangvien/nganhang.model.php";

$user = Api::requireRole(["admin", "giangvien"]);

Api::json([
    "success" => true,
    "banks" => getQuestionBanks((int) ($user["id_nguoidung"] ?? 0), $user["vaitro"] ?? ""),
    "subjects" => getAccessibleBankSubjects((int) ($user["id_nguoidung"] ?? 0), $user["vaitro"] ?? ""),
]);
