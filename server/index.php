<?php
ob_start();
session_start();

require_once "model/Database.php";

$user_role = $_SESSION['user']['vaitro'] ?? '';
$act = $_GET['act'] ?? 'dashboard';

if (!isset($_SESSION['user']) && $act != 'login') {
    header("Location: index.php?act=login");
    exit;
}

switch ($act) {
    case 'login':
        $title = "Đăng nhập";
        $view = "views/auth/login.php";
        break;

    case 'dashboard':
        $title = "Bảng điều khiển";
        $view = ($user_role == 'admin') ? "views/admin/dashboard.php" : "views/giangvien/dashboard.php";
        break;

    case 'dangxuat':
        session_destroy();
        header("Location: index.php?act=login");
        exit;

        // --- DI CHUYỂN CÁC CASE QUẢN LÝ LÊN TRÊN ---
    case 'quanly-monhoc':
        require_once "controller/giangvien/monhoc.controller.php";
        $result = monhoc_index();
        $title = $result['title'];
        $view = $result['view'];
        $list_monhoc = $result['data'];
        break;

    case 'monhoc-add':
        $title = "Thêm Môn học";
        $view = "views/giangvien/monhoc/add.php";
        break;

    case 'monhoc-save':
        require_once "controller/giangvien/monhoc.controller.php";
        monhoc_save(); // Hàm này sẽ header() chuyển hướng nên không cần $view
        exit;
    case 'monhoc-delete':
        require_once "controller/giangvien/monhoc.controller.php";
        monhoc_delete();
        break;
    case 'monhoc-edit':
        require_once "controller/giangvien/monhoc.controller.php";
        $title = "Sửa Môn học";
        $view = "views/giangvien/monhoc/add.php";
        break;

    default:
        $view = "views/404.php";
        break;
}

// 4. HIỂN THỊ GIAO DIỆN
if ($act == 'login' || $view == "views/404.php") {
    include $view;
} else {
    include "views/layouts/header.php";
    if (isset($view) && file_exists($view)) {
        include $view;
    } else {
        header("Location: index.php?act=404");
        exit;
    }
    include "views/layouts/footer.php";
}
