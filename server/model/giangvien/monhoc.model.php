<?php
require_once "model/Database.php";

/**
 * Lấy danh sách môn học theo người dùng/vai trò
 */
function getAll_monhoc_with_user($id_nguoidung, $vaitro)
{
    $conn = Database::connect();
    $list = [];

    // SQL mới: Đếm số bài thi từ bảng baithi
    $sql = "SELECT m.*, COUNT(b.id_baithi) as so_bai_thi 
            FROM monhoc m 
            LEFT JOIN baithi b ON m.id_monhoc = b.id_monhoc";

    // Lọc theo người dùng nếu không phải admin
    if ($vaitro !== 'admin') {
        $sql .= " WHERE m.id_nguoidung = ?";
    }

    $sql .= " GROUP BY m.id_monhoc ORDER BY m.id_monhoc DESC";

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

/**
 * Kiểm tra trùng tên môn học (Gắt: bỏ dấu, bỏ khoảng trắng, bỏ ký tự đặc biệt)
 */
function isDuplicateMonHoc($tenmonhoc)
{
    $conn = Database::connect();

    // Hàm dọn dẹp chuỗi: viết thường, xóa sạch ký tự không phải chữ/số
    $normalize = function ($str) {
        $str = mb_strtolower($str, 'UTF-8');
        return preg_replace('/[^a-z0-9]/', '', $str);
    };

    $search = $normalize($tenmonhoc);

    $sql = "SELECT tenmonhoc FROM monhoc";
    $result = $conn->query($sql);

    if ($result) {
        while ($row = $result->fetch_assoc()) {
            if ($search === $normalize($row['tenmonhoc'])) {
                $conn->close();
                return true;
            }
        }
    }

    $conn->close();
    return false;
}

/**
 * Thêm môn học mới
 */
function insert_monhoc($tenmonhoc, $id_nguoidung)
{
    $conn = Database::connect();
    $tenmonhoc = trim($tenmonhoc);

    $sql = "INSERT INTO monhoc (tenmonhoc, id_nguoidung, ngaythem) VALUES (?, ?, NOW())";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("si", $tenmonhoc, $id_nguoidung);

    $result = $stmt->execute();
    $stmt->close();
    $conn->close();
    return $result;
}
/**
 * Xóa môn học theo ID
 */
function delete_monhoc($id_monhoc, $id_nguoidung, $vaitro)
{
    $conn = Database::connect();

    // Nếu là admin thì xóa thoải mái, nếu là giảng viên thì chỉ được xóa môn của mình
    $sql = "DELETE FROM monhoc WHERE id_monhoc = ?";
    if ($vaitro !== 'admin') {
        $sql .= " AND id_nguoidung = ?";
    }

    $stmt = $conn->prepare($sql);
    if ($vaitro !== 'admin') {
        $stmt->bind_param("ii", $id_monhoc, $id_nguoidung);
    } else {
        $stmt->bind_param("i", $id_monhoc);
    }

    $result = $stmt->execute();
    $stmt->close();
    $conn->close();
    return $result;
}