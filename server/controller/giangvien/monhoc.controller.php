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
        $id_monhoc = $_POST['id_monhoc'] ?? 0;
        $tenInput = trim($_POST['tenmonhoc'] ?? '');
        $id_nguoidung = $_SESSION['user']['id_nguoidung'] ?? 0;
        $mieuta = $_POST['mieuta'] ?? null; // Lấy dữ liệu từ textarea

        if (empty($tenInput)) {
            $_SESSION['error'] = "Tên môn học không được để trống!";
            header("Location: " . $_SERVER['HTTP_REFERER']);
            exit;
        }

        // Truyền $id_monhoc vào để nếu đang sửa môn A, đặt tên vẫn là A thì không báo trùng
        if (isDuplicateMonHoc($tenInput, $id_monhoc)) {
            $_SESSION['error'] = "Tên môn học này đã tồn tại trong hệ thống!";
            header("Location: " . $_SERVER['HTTP_REFERER']);
            exit;
        }

        if ($id_monhoc > 0) {
            update_monhoc($id_monhoc, $tenInput,  $mieuta);
            $_SESSION['success'] = "Cập nhật môn học thành công!";
        } else {
            insert_monhoc($tenInput, $id_nguoidung,  $mieuta);
            $_SESSION['success'] = "Thêm môn học mới thành công!";
        }

        header("Location: index.php?act=quanly-monhoc");
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