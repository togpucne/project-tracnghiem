<?php
require_once __DIR__ . "/../Database.php";

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


function isDuplicateMonHoc($tenmonhoc, $exclude_id = 0)
{
    $conn = Database::connect();

    // Hàm dọn dẹp chuỗi: viết thường, xóa sạch ký tự không phải chữ/số để so sánh gắt
    $normalize = function ($str) {
        $str = mb_strtolower($str, 'UTF-8');
        // Loại bỏ dấu tiếng Việt (nếu cần Phúc có thể thêm hàm bỏ dấu ở đây)
        return preg_replace('/[^a-z0-9]/', '', $str);
    };

    $search = $normalize($tenmonhoc);

    // Lấy tất cả trừ chính môn đang sửa
    $sql = "SELECT id_monhoc, tenmonhoc FROM monhoc WHERE id_monhoc != ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $exclude_id);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result) {
        while ($row = $result->fetch_assoc()) {
            if ($search === $normalize($row['tenmonhoc'])) {
                $stmt->close();
                $conn->close();
                return true;
            }
        }
    }

    $stmt->close();
    $conn->close();
    return false;
}
function insert_monhoc($tenmonhoc, $id_nguoidung, $mieuta = null)
{
    $conn = Database::connect();
    $tenmonhoc = trim($tenmonhoc);
    $mieuta = !empty($mieuta) ? trim($mieuta) : null; // Nếu rỗng thì gán null

    $sql = "INSERT INTO monhoc (tenmonhoc, id_nguoidung, mieuta, ngaythem) VALUES (?, ?, ?, NOW())";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("sis", $tenmonhoc, $id_nguoidung, $mieuta);

    $result = $stmt->execute();
    $stmt->close();
    $conn->close();
    return $result;
}

/**
 * Cập nhật môn học (Cập nhật cả tên và miêu tả)
 */
function update_monhoc($id, $tenmonhoc, $mieuta = null)
{
    $conn = Database::connect();
    $tenmonhoc = trim($tenmonhoc);
    $mieuta = !empty($mieuta) ? trim($mieuta) : null;

    $sql = "UPDATE monhoc SET tenmonhoc = ?, mieuta = ? WHERE id_monhoc = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("ssi", $tenmonhoc, $mieuta, $id);
    
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

/**
 * Lấy thông tin chi tiết 1 môn học
 */
function getOne_monhoc($id)
{
    $conn = Database::connect();
    $sql = "SELECT * FROM monhoc WHERE id_monhoc = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $id);
    $stmt->execute();
    $result = $stmt->get_result()->fetch_assoc();
    $stmt->close();
    $conn->close();
    return $result;
}
