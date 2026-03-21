<?php
require_once "model/Database.php";

function getAll_monhoc_with_user($id_nguoidung, $vaitro)
{
    $conn = Database::connect();
    $list = [];

    // Lấy môn học + Tên giảng viên tạo
    $sql = "SELECT m.*, n.ten as ten_nguoi_tao 
            FROM monhoc m 
            LEFT JOIN nguoidung n ON m.id_nguoidung = n.id_nguoidung";

    // Nếu không phải admin thì chỉ lọc đồ của mình
    if ($vaitro !== 'admin') {
        $sql .= " WHERE m.id_nguoidung = ?";
    }
    $sql .= " ORDER BY m.id_monhoc DESC";

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
