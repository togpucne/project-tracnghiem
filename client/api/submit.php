<?php

require_once __DIR__ . "/../core/Api.php";
require_once __DIR__ . "/../core/Database.php";
require_once __DIR__ . "/../core/Response.php";


$data = Api::jsonInput();

$id_lanthi = (int) ($data["id_lanthi"] ?? 0);
$id_baithi = (int) ($data["id_baithi"] ?? 0);
$answers = $data["answers"] ?? [];

if ($id_lanthi === 0 || $id_baithi === 0 || !is_array($answers)) {
    Response::json(["error" => "Du lieu khong hop le"], 400);
}

$conn = Database::connect();

$stmt = $conn->prepare("
    DELETE FROM traloithisinh
    WHERE id_lanthi = ?
");
$stmt->bind_param("i", $id_lanthi);
$stmt->execute();

$stmt = $conn->prepare("
    INSERT INTO traloithisinh
    (id_cauhoi, id_lanthi, cautraloichon)
    VALUES (?, ?, ?)
");

foreach ($answers as $id_cauhoi => $id_dapan) {
    $id_cauhoi = (int) $id_cauhoi;
    $id_dapan = (int) $id_dapan;
    $stmt->bind_param("iii", $id_cauhoi, $id_lanthi, $id_dapan);
    $stmt->execute();
}

$sql = "
    SELECT COUNT(*) AS socaudung
    FROM traloithisinh ts
    JOIN dapan d ON ts.cautraloichon = d.id_dapan
    WHERE ts.id_lanthi = ?
    AND d.dapandung = 1
";

$stmt = $conn->prepare($sql);
$stmt->bind_param("i", $id_lanthi);
$stmt->execute();
$socaudung = $stmt->get_result()->fetch_assoc()["socaudung"];

$stmt = $conn->prepare("
    SELECT tongcauhoi
    FROM baithi
    WHERE id_baithi = ?
");
$stmt->bind_param("i", $id_baithi);
$stmt->execute();
$tong = $stmt->get_result()->fetch_assoc()["tongcauhoi"];

$diem = $tong > 0 ? round(($socaudung / $tong) * 10, 2) : 0;

$stmt = $conn->prepare("
    UPDATE lanthi
    SET diem = ?,
        socaudung = ?,
        tongdiem_toida = 10,
        thoigiannop = NOW(),
        trangthai = 'done'
    WHERE id_lanthi = ?
");
$stmt->bind_param("dii", $diem, $socaudung, $id_lanthi);
$stmt->execute();

Response::json([
    "success" => true,
    "diem" => $diem,
    "socaudung" => $socaudung,
    "id_lanthi" => $id_lanthi,
]);

