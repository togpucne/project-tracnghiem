<?php
require_once "model/giangvien/monhoc.model.php";

function monhoc_index()
{
    // Bây giờ lấy 'id_nguoidung' là chắc chắn có dữ liệu
    $id_nguoidung = $_SESSION['user']['id_nguoidung'] ?? 0;
    $vaitro = $_SESSION['user']['vaitro'] ?? '';

    $list_monhoc = getAll_monhoc_with_user($id_nguoidung, $vaitro);

    return [
        'title' => 'Quản lý Môn học',
        'view' => 'views/giangvien/monhoc/list.php',
        'data' => $list_monhoc
    ];
}


function monhoc_save()
{
    if ($_SERVER['REQUEST_METHOD'] === 'POST') {
        $tenmonhoc = $_POST['tenmonhoc'] ?? '';
        $id_nguoidung = $_SESSION['user']['id_nguoidung'] ?? 0; // Lấy từ Session bạn đã sửa

        // 1. Kiểm tra để trống
        if (empty(trim($tenmonhoc))) {
            $_SESSION['error'] = "Tên môn học không được để trống!";
            header("Location: index.php?act=monhoc-add");
            exit;
        }

        // 2. Kiểm tra trùng tên (Bỏ khoảng trắng, viết thường)
        if (isDuplicateMonHoc($tenmonhoc)) {
            $_SESSION['error'] = "Môn học này đã tồn tại trên hệ thống!";
            header("Location: index.php?act=monhoc-add");
            exit;
        }

        // 3. Thực hiện thêm
        if (insert_monhoc($tenmonhoc, $id_nguoidung)) {
            $_SESSION['success'] = "Thêm môn học thành công!";
            header("Location: index.php?act=quanly-monhoc");
        } else {
            $_SESSION['error'] = "Có lỗi xảy ra, vui lòng thử lại.";
            header("Location: index.php?act=monhoc-add");
        }
        exit;
    }
}


function monhoc_delete()
{
    $id_monhoc = $_GET['id'] ?? 0;
    $id_nguoidung = $_SESSION['user']['id_nguoidung'] ?? 0;
    $vaitro = $_SESSION['user']['vaitro'] ?? '';

    if ($id_monhoc > 0) {
        if (delete_monhoc($id_monhoc, $id_nguoidung, $vaitro)) {
            $_SESSION['success'] = "Đã xóa môn học thành công!";
        } else {
            $_SESSION['error'] = "Không thể xóa môn học này hoặc bạn không có quyền!";
        }
    }
    header("Location: index.php?act=quanly-monhoc");
    exit;
}
