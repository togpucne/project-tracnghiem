<?php
session_start();

require_once __DIR__ . "/../core/Database.php";
require_once __DIR__ . "/../core/Response.php";

if (!isset($_SESSION['user'])) {
    Response::json(["error" => "Unauthorized"], 401);
}

$data = json_decode(file_get_contents("php://input"), true);

$id_lanthi = (int)$data['id_lanthi'];
$id_baithi = (int)$data['id_baithi'];
$answers   = $data['answers'];

$conn = Database::connect();

/* xóa câu trả lời cũ */

$stmt = $conn->prepare("
DELETE FROM traloithisinh
WHERE id_lanthi=?
");

$stmt->bind_param("i", $id_lanthi);
$stmt->execute();


/* lưu câu trả lời */

$stmt = $conn->prepare("
INSERT INTO traloithisinh
(id_cauhoi,id_lanthi,cautraloichon)
VALUES(?,?,?)
");

foreach ($answers as $id_cauhoi => $id_dapan) {

    $stmt->bind_param("iii", $id_cauhoi, $id_lanthi, $id_dapan);

    $stmt->execute();
}


/* tính điểm */

$sql = "
SELECT COUNT(*) AS socaudung
FROM traloithisinh ts
JOIN dapan d
ON ts.cautraloichon=d.id_dapan
WHERE ts.id_lanthi=?
AND d.dapandung=1
";

$stmt = $conn->prepare($sql);

$stmt->bind_param("i", $id_lanthi);

$stmt->execute();

$socaudung = $stmt->get_result()->fetch_assoc()['socaudung'];


/* tổng câu */

$stmt = $conn->prepare("
SELECT tongcauhoi
FROM baithi
WHERE id_baithi=?
");

$stmt->bind_param("i", $id_baithi);

$stmt->execute();

$tong = $stmt->get_result()->fetch_assoc()['tongcauhoi'];


$diem = round(($socaudung / $tong) * 10, 2);


/* update lần thi */

$stmt = $conn->prepare("
UPDATE lanthi
SET diem=?,
socaudung=?,
tongdiem_toida=10,
thoigiannop=NOW(),
trangthai='done'
WHERE id_lanthi=?
");

$stmt->bind_param("dii", $diem, $socaudung, $id_lanthi);

$stmt->execute();


Response::json([
    "success" => true,
    "diem" => $diem,
    "socaudung" => $socaudung,
    "id_lanthi" => $id_lanthi
]);
