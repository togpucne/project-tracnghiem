<?php
ob_start();
session_start();

require_once "model/Database.php";
require_once "model/giangvien/monhoc.model.php";

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

        // --- QUẢN LÝ MÔN HỌC (Dùng Modal) ---
    case 'quanly-monhoc':
        require_once "controller/giangvien/monhoc.controller.php";
        $result = monhoc_index();
        $title = $result['title'];
        $view = $result['view'];
        $list_monhoc = $result['data'];
        break;

    case 'monhoc-save':
        // Xử lý cả THÊM và SỬA từ Modal gửi về
        require_once "controller/giangvien/monhoc.controller.php";
        monhoc_save();
        exit;

    case 'monhoc-delete':
        require_once "controller/giangvien/monhoc.controller.php";
        monhoc_delete();
        exit;

    default:
        $title = "404 - Không tìm thấy";
        $view = "views/404.php";
        break;
}

// HIỂN THỊ GIAO DIỆN
if ($act == 'login' || $view == "views/404.php") {
    if (file_exists($view)) {
        include $view;
    } else {
        echo "Trang không tồn tại!";
    }
} else {
    include "views/layouts/header.php";
    if (isset($view) && file_exists($view)) {
        include $view;
    } else {
        echo "View không tồn tại: $view";
    }
    include "views/layouts/footer.php";
}
ob_end_flush();
