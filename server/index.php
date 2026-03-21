<?php
session_start();

// 1. Nạp file Database ngay từ đầu để dùng cho toàn hệ thống
require_once "model/Database.php"; 

$user_role = $_SESSION['user']['vaitro'] ?? '';
$act = $_GET['act'] ?? 'dashboard';

// 2. Kiểm tra đăng nhập
if (!isset($_SESSION['user']) && $act != 'login') {
    header("Location: index.php?act=login");
    exit;
}

// 3. Thiết lập View và Title
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
    default:
        $view = "views/404.php";
        break;
}

// 4. HIỂN THỊ GIAO DIỆN
if ($act == 'login') {
    // Trang login thường không dùng chung header/footer admin
    include $view;
} else {
    // Chèn Header 
    include "views/layouts/header.php"; 
    
    // Nạp nội dung trang
    if (file_exists($view)) {
        include $view;
    } else {
        echo "File không tồn tại: $view";
    }

    // Chèn Footer 
    include "views/layouts/footer.php"; 
}