<?php

return [
    "profile/detail" => [
        "handler" => __DIR__ . "/../api/profile_detail.php",
        "methods" => ["GET"],
        "auth" => true,
        "roles" => ["admin", "giangvien"],
    ],
    "profile/update" => [
        "handler" => __DIR__ . "/../api/profile_update.php",
        "methods" => ["PATCH"],
        "auth" => true,
        "roles" => ["admin", "giangvien"],
    ],
    "nguoidung/list" => [
        "handler" => __DIR__ . "/../api/nguoidung_list.php",
        "methods" => ["GET"],
        "auth" => true,
        "roles" => ["admin"],
    ],
    "nguoidung/save" => [
        "handler" => __DIR__ . "/../api/nguoidung_save.php",
        "methods" => ["POST", "PATCH"],
        "auth" => true,
        "roles" => ["admin"],
    ],
    "nguoidung/delete" => [
        "handler" => __DIR__ . "/../api/nguoidung_delete.php",
        "methods" => ["DELETE"],
        "auth" => true,
        "roles" => ["admin"],
    ],
    "monhoc/list" => [
        "handler" => __DIR__ . "/../api/monhoc_list.php",
        "methods" => ["GET"],
        "auth" => true,
        "roles" => ["admin", "giangvien"],
    ],
    "monhoc/save" => [
        "handler" => __DIR__ . "/../api/monhoc_save.php",
        "methods" => ["POST", "PATCH"],
        "auth" => true,
        "roles" => ["admin", "giangvien"],
    ],
    "monhoc/delete" => [
        "handler" => __DIR__ . "/../api/monhoc_delete.php",
        "methods" => ["DELETE"],
        "auth" => true,
        "roles" => ["admin", "giangvien"],
    ],
    "baithi/list" => [
        "handler" => __DIR__ . "/../api/baithi_list.php",
        "methods" => ["GET"],
        "auth" => true,
        "roles" => ["admin", "giangvien"],
    ],
    "baithi/save" => [
        "handler" => __DIR__ . "/../api/baithi_save.php",
        "methods" => ["POST", "PATCH"],
        "auth" => true,
        "roles" => ["admin", "giangvien"],
    ],
    "baithi/delete" => [
        "handler" => __DIR__ . "/../api/baithi_delete.php",
        "methods" => ["DELETE"],
        "auth" => true,
        "roles" => ["admin", "giangvien"],
    ],
    "cauhoi/list" => [
        "handler" => __DIR__ . "/../api/cauhoi_list.php",
        "methods" => ["GET"],
        "auth" => true,
        "roles" => ["admin", "giangvien"],
    ],
    "cauhoi/save" => [
        "handler" => __DIR__ . "/../api/cauhoi_save.php",
        "methods" => ["POST", "PATCH"],
        "auth" => true,
        "roles" => ["admin", "giangvien"],
    ],
    "cauhoi/import-word" => [
        "handler" => __DIR__ . "/../api/cauhoi_import_word.php",
        "methods" => ["POST"],
        "auth" => true,
        "roles" => ["admin", "giangvien"],
    ],
    "cauhoi/delete" => [
        "handler" => __DIR__ . "/../api/cauhoi_delete.php",
        "methods" => ["DELETE"],
        "auth" => true,
        "roles" => ["admin", "giangvien"],
    ],
];
