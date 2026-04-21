<?php

require_once __DIR__ . "/../Database.php";

function getAccessibleBankSubjects($id_nguoidung, $vaitro)
{
    $conn = Database::connect();
    $list = [];

    if ($vaitro === 'admin') {
        $sql = "SELECT m.id_monhoc, m.tenmonhoc, m.id_nguoidung, n.ten
                FROM monhoc m
                LEFT JOIN nguoidung n ON m.id_nguoidung = n.id_nguoidung
                ORDER BY m.tenmonhoc ASC";
        $stmt = $conn->prepare($sql);
    } else {
        $sql = "SELECT m.id_monhoc, m.tenmonhoc, m.id_nguoidung, n.ten
                FROM monhoc m
                LEFT JOIN nguoidung n ON m.id_nguoidung = n.id_nguoidung
                WHERE m.id_nguoidung = ?
                ORDER BY m.tenmonhoc ASC";
        $stmt = $conn->prepare($sql);
        $stmt->bind_param("i", $id_nguoidung);
    }

    $stmt->execute();
    $result = $stmt->get_result();
    while ($row = $result->fetch_assoc()) {
        $list[] = $row;
    }

    $stmt->close();
    $conn->close();
    return $list;
}

function getQuestionBanks($id_nguoidung, $vaitro)
{
    $conn = Database::connect();
    $list = [];

    $sql = "SELECT nh.id_nganhang,
                   nh.ten_nganhang,
                   nh.mieuta,
                   nh.id_nguoidung,
                   nh.trangthai,
                   nh.ngaytao,
                   u.ten AS ten_nguoidung,
                   COUNT(DISTINCT nm.id_monhoc) AS so_monhoc,
                   COUNT(DISTINCT ch.id_cauhoi_nganhang) AS so_cauhoi,
                   GROUP_CONCAT(DISTINCT m.tenmonhoc ORDER BY m.tenmonhoc ASC SEPARATOR '||') AS ds_monhoc
            FROM nganhang_cauhoi nh
            LEFT JOIN nguoidung u ON nh.id_nguoidung = u.id_nguoidung
            LEFT JOIN nganhang_monhoc nm ON nh.id_nganhang = nm.id_nganhang
            LEFT JOIN monhoc m ON nm.id_monhoc = m.id_monhoc
            LEFT JOIN cauhoi_nganhang ch ON nh.id_nganhang = ch.id_nganhang AND ch.trangthai = 'active'";

    if ($vaitro !== 'admin') {
        $sql .= " WHERE nh.id_nguoidung = ?";
    }

    $sql .= " GROUP BY nh.id_nganhang ORDER BY nh.id_nganhang DESC";

    $stmt = $conn->prepare($sql);
    if ($vaitro !== 'admin') {
        $stmt->bind_param("i", $id_nguoidung);
    }
    $stmt->execute();
    $result = $stmt->get_result();
    while ($row = $result->fetch_assoc()) {
        $list[] = $row;
    }

    $stmt->close();
    $conn->close();
    return $list;
}

function getQuestionBankById($id_nganhang, $id_nguoidung, $vaitro)
{
    $conn = Database::connect();
    $sql = "SELECT nh.*, u.ten AS ten_nguoidung
            FROM nganhang_cauhoi nh
            LEFT JOIN nguoidung u ON nh.id_nguoidung = u.id_nguoidung
            WHERE nh.id_nganhang = ?";

    if ($vaitro !== 'admin') {
        $sql .= " AND nh.id_nguoidung = ?";
    }

    $sql .= " LIMIT 1";

    $stmt = $conn->prepare($sql);
    if ($vaitro !== 'admin') {
        $stmt->bind_param("ii", $id_nganhang, $id_nguoidung);
    } else {
        $stmt->bind_param("i", $id_nganhang);
    }
    $stmt->execute();
    $bank = $stmt->get_result()->fetch_assoc();
    $stmt->close();

    if (!$bank) {
        $conn->close();
        return null;
    }

    $stmtSubjects = $conn->prepare("SELECT nm.id_monhoc, m.tenmonhoc
                                    FROM nganhang_monhoc nm
                                    JOIN monhoc m ON nm.id_monhoc = m.id_monhoc
                                    WHERE nm.id_nganhang = ?
                                    ORDER BY m.tenmonhoc ASC");
    $stmtSubjects->bind_param("i", $id_nganhang);
    $stmtSubjects->execute();
    $resSubjects = $stmtSubjects->get_result();
    $bank['subjects'] = [];
    while ($row = $resSubjects->fetch_assoc()) {
        $bank['subjects'][] = $row;
    }
    $stmtSubjects->close();

    $conn->close();
    return $bank;
}

function validateBankSubjects($subjectIds, $id_nguoidung, $vaitro)
{
    $subjectIds = array_values(array_unique(array_map('intval', $subjectIds)));
    $subjectIds = array_values(array_filter($subjectIds, fn($id) => $id > 0));

    if (empty($subjectIds)) {
        return [];
    }

    $conn = Database::connect();
    $placeholders = implode(',', array_fill(0, count($subjectIds), '?'));

    if ($vaitro === 'admin') {
        $sql = "SELECT id_monhoc FROM monhoc WHERE id_monhoc IN ($placeholders)";
        $stmt = $conn->prepare($sql);
        $types = str_repeat('i', count($subjectIds));
        $stmt->bind_param($types, ...$subjectIds);
    } else {
        $sql = "SELECT id_monhoc FROM monhoc WHERE id_monhoc IN ($placeholders) AND id_nguoidung = ?";
        $stmt = $conn->prepare($sql);
        $types = str_repeat('i', count($subjectIds)) . 'i';
        $params = array_merge($subjectIds, [$id_nguoidung]);
        $stmt->bind_param($types, ...$params);
    }

    $stmt->execute();
    $result = $stmt->get_result();
    $validIds = [];
    while ($row = $result->fetch_assoc()) {
        $validIds[] = (int) $row['id_monhoc'];
    }

    $stmt->close();
    $conn->close();
    sort($subjectIds);
    sort($validIds);
    return $subjectIds === $validIds ? $validIds : false;
}

function createQuestionBank($ten_nganhang, $mieuta, $subjectIds, $id_nguoidung)
{
    $conn = Database::connect();
    $conn->begin_transaction();

    try {
        $stmt = $conn->prepare("INSERT INTO nganhang_cauhoi (ten_nganhang, mieuta, id_nguoidung, trangthai, ngaytao)
                                VALUES (?, ?, ?, 'active', NOW())");
        $stmt->bind_param("ssi", $ten_nganhang, $mieuta, $id_nguoidung);
        $stmt->execute();
        $id_nganhang = (int) $conn->insert_id;
        $stmt->close();

        foreach ($subjectIds as $id_monhoc) {
            $stmtMap = $conn->prepare("INSERT INTO nganhang_monhoc (id_nganhang, id_monhoc) VALUES (?, ?)");
            $stmtMap->bind_param("ii", $id_nganhang, $id_monhoc);
            $stmtMap->execute();
            $stmtMap->close();
        }

        $conn->commit();
        $conn->close();
        return $id_nganhang;
    } catch (Exception $e) {
        $conn->rollback();
        $conn->close();
        return 0;
    }
}

function updateQuestionBank($id_nganhang, $ten_nganhang, $mieuta, $trangthai, $subjectIds)
{
    $conn = Database::connect();
    $conn->begin_transaction();

    try {
        $stmt = $conn->prepare("UPDATE nganhang_cauhoi
                                SET ten_nganhang = ?, mieuta = ?, trangthai = ?
                                WHERE id_nganhang = ?");
        $stmt->bind_param("sssi", $ten_nganhang, $mieuta, $trangthai, $id_nganhang);
        $stmt->execute();
        $stmt->close();

        $stmtDelete = $conn->prepare("DELETE FROM nganhang_monhoc WHERE id_nganhang = ?");
        $stmtDelete->bind_param("i", $id_nganhang);
        $stmtDelete->execute();
        $stmtDelete->close();

        foreach ($subjectIds as $id_monhoc) {
            $stmtMap = $conn->prepare("INSERT INTO nganhang_monhoc (id_nganhang, id_monhoc) VALUES (?, ?)");
            $stmtMap->bind_param("ii", $id_nganhang, $id_monhoc);
            $stmtMap->execute();
            $stmtMap->close();
        }

        $conn->commit();
        $conn->close();
        return true;
    } catch (Exception $e) {
        $conn->rollback();
        $conn->close();
        return false;
    }
}

function softDeleteQuestionBank($id_nganhang)
{
    $conn = Database::connect();
    $stmt = $conn->prepare("UPDATE nganhang_cauhoi SET trangthai = 'inactive' WHERE id_nganhang = ?");
    $stmt->bind_param("i", $id_nganhang);
    $ok = $stmt->execute();
    $stmt->close();
    $conn->close();
    return $ok;
}

function getQuestionBankQuestions($id_nganhang, $id_monhoc, $id_nguoidung, $vaitro)
{
    $conn = Database::connect();
    $questions = [];

    $sql = "SELECT ch.*
            FROM cauhoi_nganhang ch
            JOIN nganhang_cauhoi nh ON ch.id_nganhang = nh.id_nganhang
            WHERE ch.id_nganhang = ? AND ch.id_monhoc = ?";
    if ($vaitro !== 'admin') {
        $sql .= " AND nh.id_nguoidung = ?";
    }
    $sql .= " ORDER BY ch.id_cauhoi_nganhang DESC";

    $stmt = $conn->prepare($sql);
    if ($vaitro !== 'admin') {
        $stmt->bind_param("iii", $id_nganhang, $id_monhoc, $id_nguoidung);
    } else {
        $stmt->bind_param("ii", $id_nganhang, $id_monhoc);
    }
    $stmt->execute();
    $result = $stmt->get_result();
    while ($row = $result->fetch_assoc()) {
        $row['dapan'] = getBankAnswersByQuestionIdWithConnection($conn, (int) $row['id_cauhoi_nganhang']);
        $questions[] = $row;
    }

    $stmt->close();
    $conn->close();
    return $questions;
}

function getBankAnswersByQuestionIdWithConnection($conn, $id_cauhoi_nganhang)
{
    $stmt = $conn->prepare("SELECT * FROM dapan_nganhang WHERE id_cauhoi_nganhang = ? ORDER BY id_dapan_nganhang ASC");
    $stmt->bind_param("i", $id_cauhoi_nganhang);
    $stmt->execute();
    $result = $stmt->get_result();
    $answers = [];
    while ($row = $result->fetch_assoc()) {
        $answers[] = $row;
    }
    $stmt->close();
    return $answers;
}

function getQuestionBankQuestionById($id_cauhoi_nganhang, $id_nguoidung, $vaitro)
{
    $conn = Database::connect();
    $sql = "SELECT ch.*
            FROM cauhoi_nganhang ch
            JOIN nganhang_cauhoi nh ON ch.id_nganhang = nh.id_nganhang
            WHERE ch.id_cauhoi_nganhang = ?";
    if ($vaitro !== 'admin') {
        $sql .= " AND nh.id_nguoidung = ?";
    }
    $sql .= " LIMIT 1";

    $stmt = $conn->prepare($sql);
    if ($vaitro !== 'admin') {
        $stmt->bind_param("ii", $id_cauhoi_nganhang, $id_nguoidung);
    } else {
        $stmt->bind_param("i", $id_cauhoi_nganhang);
    }
    $stmt->execute();
    $question = $stmt->get_result()->fetch_assoc();
    $stmt->close();

    if ($question) {
        $question['dapan'] = getBankAnswersByQuestionIdWithConnection($conn, $id_cauhoi_nganhang);
    }

    $conn->close();
    return $question ?: null;
}

function createQuestionBankQuestion($id_nganhang, $id_monhoc, $noidungcauhoi, $dokho, $answers)
{
    $conn = Database::connect();
    $conn->begin_transaction();

    try {
        $stmt = $conn->prepare("INSERT INTO cauhoi_nganhang (id_nganhang, id_monhoc, noidungcauhoi, dokho, trangthai, ngaytao)
                                VALUES (?, ?, ?, ?, 'active', NOW())");
        $stmt->bind_param("iiss", $id_nganhang, $id_monhoc, $noidungcauhoi, $dokho);
        $stmt->execute();
        $id_cauhoi_nganhang = (int) $conn->insert_id;
        $stmt->close();

        foreach ($answers as $answer) {
            $stmtAnswer = $conn->prepare("INSERT INTO dapan_nganhang (id_cauhoi_nganhang, noidungdapan, dapandung)
                                          VALUES (?, ?, ?)");
            $stmtAnswer->bind_param("isi", $id_cauhoi_nganhang, $answer['noidung'], $answer['dapandung']);
            $stmtAnswer->execute();
            $stmtAnswer->close();
        }

        $conn->commit();
        $conn->close();
        return $id_cauhoi_nganhang;
    } catch (Exception $e) {
        $conn->rollback();
        $conn->close();
        return 0;
    }
}

function updateQuestionBankQuestion($id_cauhoi_nganhang, $id_monhoc, $noidungcauhoi, $dokho, $trangthai, $answers)
{
    $conn = Database::connect();
    $conn->begin_transaction();

    try {
        $stmt = $conn->prepare("UPDATE cauhoi_nganhang
                                SET id_monhoc = ?, noidungcauhoi = ?, dokho = ?, trangthai = ?
                                WHERE id_cauhoi_nganhang = ?");
        $stmt->bind_param("isssi", $id_monhoc, $noidungcauhoi, $dokho, $trangthai, $id_cauhoi_nganhang);
        $stmt->execute();
        $stmt->close();

        $stmtDelete = $conn->prepare("DELETE FROM dapan_nganhang WHERE id_cauhoi_nganhang = ?");
        $stmtDelete->bind_param("i", $id_cauhoi_nganhang);
        $stmtDelete->execute();
        $stmtDelete->close();

        foreach ($answers as $answer) {
            $stmtAnswer = $conn->prepare("INSERT INTO dapan_nganhang (id_cauhoi_nganhang, noidungdapan, dapandung)
                                          VALUES (?, ?, ?)");
            $stmtAnswer->bind_param("isi", $id_cauhoi_nganhang, $answer['noidung'], $answer['dapandung']);
            $stmtAnswer->execute();
            $stmtAnswer->close();
        }

        $conn->commit();
        $conn->close();
        return true;
    } catch (Exception $e) {
        $conn->rollback();
        $conn->close();
        return false;
    }
}

function softDeleteQuestionBankQuestion($id_cauhoi_nganhang)
{
    $conn = Database::connect();
    $stmt = $conn->prepare("UPDATE cauhoi_nganhang SET trangthai = 'inactive' WHERE id_cauhoi_nganhang = ?");
    $stmt->bind_param("i", $id_cauhoi_nganhang);
    $ok = $stmt->execute();
    $stmt->close();
    $conn->close();
    return $ok;
}
