<?php
require_once __DIR__ . "/model/admin/logs.model.php";
$logs = get_api_logs([]);
echo json_encode(["data" => $logs]);
