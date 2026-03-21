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

// 4. HIỂN THỊ GIAO DIỆN TRONG server/index.php
if ($act == 'login' || $view == "views/404.php") {
    // Nếu là trang Login hoặc trang 404 thì hiện file view trực tiếp (Full Screen)
    // Không include header.php hay footer.php ở đây
    include $view;
} else {
    // Các trang bình thường thì mới có khung Header/Sidebar/Footer
    include "views/layouts/header.php";

    if (file_exists($view)) {
        include $view;
    } else {
        // Nếu file không tồn tại, tự động chuyển sang trang 404 full screen
        header("Location: index.php?act=404"); // Bạn có thể thêm case 404 vào switch
        exit;
    }

    include "views/layouts/footer.php";
}
