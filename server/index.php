<?php
ob_start();
session_start();

require_once "model/Database.php";
require_once "model/giangvien/monhoc.model.php";
require_once "model/giangvien/baithi.model.php";
require_once "model/giangvien/cauhoi.model.php"; // Thêm model câu hỏi

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

        // --- QUẢN LÝ MÔN HỌC ---
    case 'quanly-monhoc':
        require_once "controller/giangvien/monhoc.controller.php";
        $result = monhoc_index();
        $title = $result['title'];
        $view = $result['view'];
        $list_monhoc = $result['data'];
        break;

    case 'monhoc-save':
        require_once "controller/giangvien/monhoc.controller.php";
        monhoc_save();
        exit;

    case 'monhoc-delete':
        require_once "controller/giangvien/monhoc.controller.php";
        monhoc_delete();
        exit;

        // --- QUẢN LÝ BÀI THI ---
    case 'quanly-baithi':
    case 'quanly-dethi':
        require_once "controller/giangvien/baithi.controller.php";

        // Gọi hàm index từ controller để lấy dữ liệu đã được lọc theo ID người dùng
        $result = baithi_index();

        $title = $result['title'];
        $view = $result['view'];
        $list_baithi = $result['data'];
        $list_monhoc = $result['list_monhoc'];
        break;

    case 'baithi-save':
        require_once "controller/giangvien/baithi.controller.php";
        baithi_save();
        exit;

    case 'baithi-delete':
        require_once "controller/giangvien/baithi.controller.php";
        baithi_delete();
        exit;

        // ========== QUẢN LÝ CÂU HỎI (THÊM MỚI) ==========

        // Danh sách câu hỏi của bài thi
    case 'cauhoi-list':
        require_once "controller/giangvien/cauhoi.controller.php";
        // Controller sẽ tự xử lý và gán $view, $title, $list_cauhoi, $baithi
        break;

    // Thêm câu hỏi
    case 'cauhoi-add':
        require_once "controller/giangvien/cauhoi.controller.php";
        // Controller xử lý thêm và redirect
        break;

    // Sửa câu hỏi
    case 'cauhoi-edit':
        require_once "controller/giangvien/cauhoi.controller.php";
        break;

    // Xóa câu hỏi
    case 'cauhoi-delete':
        require_once "controller/giangvien/cauhoi.controller.php";
        break;

    // ========== KẾT THÚC QUẢN LÝ CÂU HỎI ==========

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
