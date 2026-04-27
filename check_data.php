<?php
require_once __DIR__ . "/server/model/Database.php";
$conn = Database::connect();
$res = $conn->query("SELECT id_baithi, id_nguoidung, diem, trangthai FROM lanthi");
$data = [];
while($row = $res->fetch_assoc()) $data[] = $row;
echo json_encode($data);
