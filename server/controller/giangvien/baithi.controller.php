<?php
require_once "model/giangvien/baithi.model.php";

function baithi_index()
{
    return [
        'title' => 'Quản lý Bài thi',
        'view' => 'views/giangvien/baithi/list.php',
        'data' => getAll_baithi()
    ];
}

function baithi_save()
{
    if ($_SERVER['REQUEST_METHOD'] == 'POST') {
        $result = save_baithi($_POST);

        if ($result === false) {
            // Đã có lỗi, Model đã set $_SESSION['error'], quay về ngay
            header("Location: index.php?act=quanly-baithi");
            exit;
        } else {
            $_SESSION['success'] = "Lưu bài thi thành công!";
            header("Location: index.php?act=quanly-baithi");
            exit;
        }
    }
}

function baithi_delete()
{
    $id = $_GET['id'] ?? 0;
    if ($id > 0) {
        delete_baithi($id);
        $_SESSION['success'] = "Xóa bài thi thành công!";
    }
    header("Location: index.php?act=quanly-baithi");
    exit;
}
