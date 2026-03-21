<?php
function getAll_baithi($id_nguoidung) // Thêm tham số nhận ID người dùng
{
    $conn = Database::connect();
    // Thêm JOIN với bảng monhoc và điều kiện WHERE để lọc theo id_nguoidung
    $sql = "SELECT bt.*, mh.tenmonhoc 
            FROM baithi bt 
            JOIN monhoc mh ON bt.id_monhoc = mh.id_monhoc 
            WHERE mh.id_nguoidung = ? 
            ORDER BY bt.id_baithi DESC";
            
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $id_nguoidung);
    $stmt->execute();
    $result = $stmt->get_result();
    
    $data = [];
    if ($result) {
        while ($row = $result->fetch_assoc()) {
            $data[] = $row;
        }
    }
    $conn->close();
    return $data;
}

function getAll_monhoc($id_nguoidung) // Thêm tham số nhận ID người dùng
{
    $conn = Database::connect();
    // Chỉ lấy những môn do chính người này tạo
    $sql = "SELECT id_monhoc, tenmonhoc FROM monhoc WHERE id_nguoidung = ? ORDER BY tenmonhoc ASC";
    
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $id_nguoidung);
    $stmt->execute();
    $result = $stmt->get_result();
    
    $data = [];
    if ($result) {
        while ($row = $result->fetch_assoc()) {
            $data[] = $row;
        }
    }
    $conn->close();
    return $data;
}
function save_baithi($data)
{
    $conn = Database::connect();

    // 1. Chuẩn hóa tên: "  DEEP lea rning  " -> "Deep lea rning"
    $ten = trim($data['ten_baithi']);
    $ten = mb_strtolower($ten, 'UTF-8');
    $ten = mb_ucfirst($ten);

    $id_baithi = !empty($data['id_baithi']) ? (int)$data['id_baithi'] : 0;
    $id_monhoc = (int)$data['id_monhoc'];
    $tongcauhoi = abs((int)$data['tongcauhoi']);
    $thoigianlam = abs((int)$data['thoigianlam']);
    $trangthai = !empty($data['trangthai']) ? $data['trangthai'] : "Đang mở";
    $mieuta = !empty($data['mieuta']) ? $data['mieuta'] : null;

    // Xử lý ngày kết thúc: Nếu trống thì gán NULL cho Database
    $tg_ketthuc = !empty($data['thoigianketthuc']) ? $data['thoigianketthuc'] : null;

    // 2. Kiểm tra trùng tên (Xóa khoảng trắng khi so sánh)
    $sql_check = "SELECT id_baithi FROM baithi WHERE REPLACE(ten_baithi, ' ', '') = REPLACE(?, ' ', '') AND id_baithi != ?";
    $stmt_check = $conn->prepare($sql_check);
    $stmt_check->bind_param("si", $ten, $id_baithi);
    $stmt_check->execute();
    if ($stmt_check->get_result()->num_rows > 0) {
        $_SESSION['error'] = "Bài thi '$ten' đã tồn tại (hoặc tương tự)!";
        $conn->close();
        return false;
    }

    // 3. Thực hiện lưu (Đã fix đủ cột mieuta)
    if ($id_baithi == 0) {
        $sql = "INSERT INTO baithi (id_monhoc, ten_baithi, mieuta, tongcauhoi, thoigianlam, thoigianbatdau, thoigianketthuc, trangthai, ngaytao) 
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())";
        $stmt = $conn->prepare($sql);
        $stmt->bind_param("issiisss", $id_monhoc, $ten, $mieuta, $tongcauhoi, $thoigianlam, $data['thoigianbatdau'], $tg_ketthuc, $trangthai);
    } else {
        $sql = "UPDATE baithi SET id_monhoc=?, ten_baithi=?, mieuta=?, tongcauhoi=?, thoigianlam=?, thoigianbatdau=?, thoigianketthuc=?, trangthai=? 
                WHERE id_baithi=?";
        $stmt = $conn->prepare($sql);
        $stmt->bind_param("issiisssi", $id_monhoc, $ten, $mieuta, $tongcauhoi, $thoigianlam, $data['thoigianbatdau'], $tg_ketthuc, $trangthai, $id_baithi);
    }

    $res = $stmt->execute();
    if (!$res) {
        $_SESSION['error'] = "Lỗi Database: " . $conn->error;
    }
    $conn->close();
    return $res;
}



function delete_baithi($id)
{
    $conn = Database::connect();

    // 1. Tắt kiểm tra khóa ngoại để tránh lỗi Constraint
    $conn->query("SET FOREIGN_KEY_CHECKS=0");

    // 2. Thực hiện xóa bài thi
    $stmt = $conn->prepare("DELETE FROM baithi WHERE id_baithi = ?");
    $stmt->bind_param("i", $id);
    $res = $stmt->execute();

    // 3. Bật lại kiểm tra khóa ngoại để bảo vệ cấu trúc DB
    $conn->query("SET FOREIGN_KEY_CHECKS=1");

    $conn->close();
    return $res;
}

if (!function_exists('mb_ucfirst')) {
    function mb_ucfirst($str, $encoding = "UTF-8")
    {
        $firstChar = mb_substr($str, 0, 1, $encoding);
        $then = mb_substr($str, 1, null, $encoding);
        return mb_strtoupper($firstChar, $encoding) . $then;
    }
}