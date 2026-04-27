<?php
require_once __DIR__ . "/../core/Api.php";
require_once __DIR__ . "/../model/admin/logs.model.php";

Api::requireRole(["admin"]);

$filters = [
    "status_code" => $_GET["status_code"] ?? "",
    "keyword" => $_GET["keyword"] ?? "",
];

Api::json([
    "success" => true,
    "data" => get_api_logs($filters)
]);
