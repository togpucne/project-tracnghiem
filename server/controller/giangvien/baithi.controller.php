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
    if ($_SERVER['REQUEST_METHOD'] === 'POST') {
        save_baithi($_POST);
        header("Location: index.php?act=quanly-baithi");
    }
}

function baithi_delete()
{
    $id = $_GET['id'] ?? 0;
    if ($id > 0) delete_baithi($id);
    header("Location: index.php?act=quanly-baithi");
}
