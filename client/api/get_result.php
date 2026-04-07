<?php

require_once __DIR__ . "/../core/Api.php";
require_once __DIR__ . "/../core/Database.php";
require_once __DIR__ . "/../core/Response.php";


$conn = Database::connect();
$user_id = $_SESSION["user"]["id"];
$id_lanthi = isset($_GET["id"]) ? (int) $_GET["id"] : 0;

if ($id_lanthi === 0) {
    Response::json(["error" => "Thieu ID lan thi"], 400);
}

$stmt = $conn->prepare("
    SELECT l.*, b.ten_baithi
    FROM lanthi l
    JOIN baithi b ON l.id_baithi = b.id_baithi
    WHERE l.id_lanthi = ? AND l.id_nguoidung = ?
");
$stmt->bind_param("ii", $id_lanthi, $user_id);
$stmt->execute();
$lanthi = $stmt->get_result()->fetch_assoc();

if (!$lanthi) {
    Response::json(["error" => "Khong tim thay ket qua"], 404);
}

$id_baithi = (int) $lanthi["id_baithi"];

$sql = "
    SELECT
        c.id_cauhoi,
        c.noidungcauhoi,
        d.id_dapan,
        d.noidungdapan,
        d.dapandung,
        ts.cautraloichon
    FROM cauhoi c
    JOIN dapan d ON c.id_cauhoi = d.id_cauhoi
    LEFT JOIN traloithisinh ts
        ON ts.id_cauhoi = c.id_cauhoi
        AND ts.id_lanthi = ?
    WHERE c.id_baithi = ?
    ORDER BY c.id_cauhoi, d.id_dapan
";

$stmt = $conn->prepare($sql);
$stmt->bind_param("ii", $id_lanthi, $id_baithi);
$stmt->execute();
$res = $stmt->get_result();

$questions = [];
while ($row = $res->fetch_assoc()) {
    $qid = (int) $row["id_cauhoi"];

    if (!isset($questions[$qid])) {
        $questions[$qid] = [
            "id_cauhoi" => $qid,
            "noidungcauhoi" => htmlspecialchars($row["noidungcauhoi"]),
            "answers" => [],
        ];
    }

    $selected = ((string) $row["cautraloichon"] !== "" && (int) $row["cautraloichon"] === (int) $row["id_dapan"]);

    $questions[$qid]["answers"][] = [
        "id_dapan" => (int) $row["id_dapan"],
        "noidungdapan" => htmlspecialchars($row["noidungdapan"]),
        "dapandung" => (int) $row["dapandung"] === 1,
        "selected" => $selected,
    ];
}

Response::json([
    "success" => true,
    "lanthi" => $lanthi,
    "questions" => array_values($questions),
]);

