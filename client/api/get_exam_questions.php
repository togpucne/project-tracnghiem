<?php

require_once __DIR__ . "/../core/Api.php";
require_once __DIR__ . "/../core/Database.php";
require_once __DIR__ . "/../core/Response.php";


$conn = Database::connect();
$user_id = $_SESSION["user"]["id"];
$id_baithi = isset($_GET["id"]) ? (int) $_GET["id"] : 0;

if ($id_baithi === 0) {
    Response::json(["error" => "Thieu ID bai thi"], 400);
}

$stmt = $conn->prepare("SELECT ten_baithi, thoigianlam, IFNULL(xao_tron, 0) AS xao_tron FROM baithi WHERE id_baithi = ?");
$stmt->bind_param("i", $id_baithi);
$stmt->execute();
$baithi = $stmt->get_result()->fetch_assoc();

if (!$baithi) {
    Response::json(["error" => "Bai thi khong ton tai"], 404);
}

$ten_baithi = $baithi["ten_baithi"];
$thoigianlam = (int) $baithi["thoigianlam"];
$xao_tron = (int) ($baithi["xao_tron"] ?? 0);

$stmt = $conn->prepare("
    SELECT id_lanthi, thoigianconlai, cautraloi_tam, TIMESTAMPDIFF(SECOND, thoigianbatdau, NOW()) as elapsed_seconds
    FROM lanthi
    WHERE id_nguoidung = ? AND id_baithi = ? AND trangthai = 'ongoing'
    LIMIT 1
");
$stmt->bind_param("ii", $user_id, $id_baithi);
$stmt->execute();
$res = $stmt->get_result();

$elapsed_seconds = 0;
$thoigianconlai = null;
$cautraloi_tam = null;

if ($res->num_rows > 0) {
    $row = $res->fetch_assoc();
    $id_lanthi = $row["id_lanthi"];
    $elapsed_seconds = (int) $row["elapsed_seconds"];
    // Always calculate remaining time based on start time to ensure it "keeps running"
    $thoigianconlai = ($thoigianlam * 60) - $elapsed_seconds;
    if ($thoigianconlai < 0) $thoigianconlai = 0;
    $cautraloi_tam = $row["cautraloi_tam"];
} else {
    $stmt = $conn->prepare("
        INSERT INTO lanthi (id_nguoidung, id_baithi, diem, thoigianbatdau, trangthai)
        VALUES (?, ?, 0, NOW(), 'ongoing')
    ");
    $stmt->bind_param("ii", $user_id, $id_baithi);
    $stmt->execute();
    $id_lanthi = $conn->insert_id;
    $thoigianconlai = $thoigianlam * 60;
}

$cautraloi_tam_str = "";
if (!empty($cautraloi_tam)) {
    $arr = json_decode($cautraloi_tam, true);
    if (is_array($arr)) {
        $pairs = [];
        foreach ($arr as $k => $v) {
            $pairs[] = $k . ":" . $v;
        }
        $cautraloi_tam_str = implode("|", $pairs);
    }
}

$sql = "
    SELECT
        c.id_cauhoi, c.noidungcauhoi,
        d.id_dapan, d.noidungdapan
    FROM cauhoi c
    JOIN dapan d ON c.id_cauhoi = d.id_cauhoi
    WHERE c.id_baithi = ?
    ORDER BY c.id_cauhoi, d.id_dapan
";
$stmt = $conn->prepare($sql);
$stmt->bind_param("i", $id_baithi);
$stmt->execute();
$res = $stmt->get_result();

$cauhoi_map = [];
while ($row = $res->fetch_assoc()) {
    $id = $row["id_cauhoi"];
    if (!isset($cauhoi_map[$id])) {
        $cauhoi_map[$id] = [
            "id_cauhoi" => $id,
            "noidung" => htmlspecialchars($row["noidungcauhoi"]),
            "dapan" => [],
        ];
    }

    $cauhoi_map[$id]["dapan"][] = [
        "id_dapan" => $row["id_dapan"],
        "noidungdapan" => htmlspecialchars($row["noidungdapan"]),
    ];
}

$cauhoi = array_values($cauhoi_map);
if ($xao_tron) {
    shuffle($cauhoi);
    foreach ($cauhoi as &$item) {
        shuffle($item["dapan"]);
    }
    unset($item);
}

Response::json([
    "success" => true,
    "id_lanthi" => $id_lanthi,
    "id_baithi" => $id_baithi,
    "ten_baithi" => $ten_baithi,
    "thoigianlam" => $thoigianlam,
    "xao_tron" => $xao_tron,
    "elapsed_seconds" => $elapsed_seconds,
    "thoigianconlai" => $thoigianconlai,
    "cautraloi_tam" => $cautraloi_tam_str,
    "cauhoi" => $cauhoi,
]);

