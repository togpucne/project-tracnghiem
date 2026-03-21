<?php
require_once __DIR__ . "/../core/Database.php";
$conn = Database::connect();
// 1. Add column thoigianconlai
$conn->query("ALTER TABLE lanthi ADD COLUMN thoigianconlai INT NULL AFTER diem");
// 2. Add column cautraloi_tam
$conn->query("ALTER TABLE lanthi ADD COLUMN cautraloi_tam TEXT NULL AFTER thoigianconlai");
echo "Done";
?>
