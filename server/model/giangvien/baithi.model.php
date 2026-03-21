<?php
// Model này dùng MySQLi theo Database::connect() của Phúc
function getAll_baithi() {
    $conn = Database::connect();
    $sql = "SELECT bt.*, mh.tenmonhoc 
            FROM baithi bt 
            JOIN monhoc mh ON bt.id_monhoc = mh.id_monhoc 
            ORDER BY bt.id_baithi DESC";
    $result = $conn->query($sql);
    $data = [];
    if($result) {
        while ($row = $result->fetch_assoc()) { $data[] = $row; }
    }
    $conn->close();
    return $data;
}

function getAll_monhoc() {
    $conn = Database::connect();
    $sql = "SELECT id_monhoc, tenmonhoc FROM monhoc ORDER BY tenmonhoc ASC";
    $result = $conn->query($sql);
    $data = [];
    if($result) {
        while ($row = $result->fetch_assoc()) { $data[] = $row; }
    }
    $conn->close();
    return $data;
}

function save_baithi($data) {
    $conn = Database::connect();
    if (empty($data['id_baithi'])) {
        $sql = "INSERT INTO baithi (id_monhoc, ten_baithi, mieuta, tongcauhoi, thoigianlam, thoigianbatdau, thoigianketthuc, trangthai, ngaytao) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())";
        $stmt = $conn->prepare($sql);
        $stmt->bind_param("issiisss", $data['id_monhoc'], $data['ten_baithi'], $data['mieuta'], $data['tongcauhoi'], $data['thoigianlam'], $data['thoigianbatdau'], $data['thoigianketthuc'], $data['trangthai']);
    } else {
        $sql = "UPDATE baithi SET id_monhoc=?, ten_baithi=?, mieuta=?, tongcauhoi=?, thoigianlam=?, thoigianbatdau=?, thoigianketthuc=?, trangthai=? WHERE id_baithi=?";
        $stmt = $conn->prepare($sql);
        $stmt->bind_param("issiisssi", $data['id_monhoc'], $data['ten_baithi'], $data['mieuta'], $data['tongcauhoi'], $data['thoigianlam'], $data['thoigianbatdau'], $data['thoigianketthuc'], $data['trangthai'], $data['id_baithi']);
    }
    $stmt->execute();
    $conn->close();
}

function delete_baithi($id) {
    $conn = Database::connect();
    $stmt = $conn->prepare("DELETE FROM baithi WHERE id_baithi = ?");
    $stmt->bind_param("i", $id);
    $stmt->execute();
    $conn->close();
}