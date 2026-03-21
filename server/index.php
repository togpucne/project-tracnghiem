<?php
ob_start(); // 1. Fix lỗi "Cannot modify header information"
session_start();

// Nạp file Database
require_once "model/Database.php";

$user_role = $_SESSION['user']['vaitro'] ?? '';
$act = $_GET['act'] ?? 'dashboard';

// 2. Kiểm tra đăng nhập
if (!isset($_SESSION['user']) && $act != 'login') {
    header("Location: index.php?act=login");
    exit;
}

// 3. Xử lý logic điều hướng (Controller)
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
        // Kết thúc case dangxuat ở đây

        // --- QUẢN LÝ MÔN HỌC (GIẢNG VIÊN) ---
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

    case 'monhoc-edit':
        require_once "controller/giangvien/monhoc.controller.php";
        // Giả sử hàm này lấy dữ liệu môn học cụ thể theo ID
        // $monhoc = monhoc_edit($_GET['id']);

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
    // Nạp Header (Sidebar nằm trong này)
    include "views/layouts/header.php";

    if (isset($view) && file_exists($view)) {
        include $view;
    } else {
        // Chuyển hướng nếu không tìm thấy file view
        header("Location: index.php?act=404");
        exit;
    }

    include "views/layouts/footer.php";
}
