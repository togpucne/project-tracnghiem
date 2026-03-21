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
