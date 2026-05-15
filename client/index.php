<?php
ini_set('display_errors', 1);
error_reporting(E_ALL);
session_start();
require_once "core/Database.php";

// Check if logged in user is still active
if (isset($_SESSION['user'])) {
    $conn = Database::connect();
    $stmt = $conn->prepare("SELECT trangthai FROM nguoidung WHERE id_nguoidung = ?");
    $stmt->bind_param("i", $_SESSION['user']['id']);
    $stmt->execute();
    $result = $stmt->get_result();
    $userStatus = $result->fetch_assoc();
    // $conn->close();

    if (!$userStatus || $userStatus['trangthai'] !== 'active') {
        session_destroy();
        header("Location: index.php?act=dangnhap&error=account_locked");
        exit;
    }
}

$act = $_GET['act'] ?? 'trangchu';

switch ($act) {

    case 'dangky':
        $title = "Đăng ký - PT QUIZ";
        $page_css = "dangnhap-dangky.css";
        $view = "views/dangky.php";
        break;

    case 'dangnhap':
        $title = "Đăng nhập - PT QUIZ";
        $page_css = "dangnhap-dangky.css";
        $view = "views/dangnhap.php";
        break;

    case 'quenmatkhau':
        $title = "Quên mật khẩu - PT QUIZ";
        $page_css = "dangnhap-dangky.css";
        $view = "views/quenmatkhau.php";
        break;

    case 'gioithieu':
        $title = "Giới thiệu - PT QUIZ";
        $page_css = "gioithieu-dangky.css";
        $view = "views/gioithieu.php";
        break;

    case 'dethi':
        if (!isset($_SESSION['user'])) {
            header("Location: index.php?act=dangnhap");
            exit;
        }
        $title = "Đề thi - PT QUIZ";
        $page_css = "trangchu.css";
        $view = "views/dethi.php";
        break;

    case 'lambai':
        if (!isset($_SESSION['user'])) {
            header("Location: index.php?act=dangnhap");
            exit;
        }
        $id_baithi = (int)($_GET['id'] ?? 0);
        $user_id = $_SESSION['user']['id'];
        
        // SERVER-SIDE SECURITY: Prevent multiple exams or dual sessions
        $conn = Database::connect();
        $sql = "SELECT id_baithi, last_active FROM lanthi WHERE id_nguoidung = ? AND trangthai = 'ongoing' LIMIT 1";
        $stmt = $conn->prepare($sql);
        $stmt->bind_param("i", $user_id);
        $stmt->execute();
        $ongoing = $stmt->get_result()->fetch_assoc();
        
        if ($ongoing) {
            $ongoing_id = (int)$ongoing['id_baithi'];
            $last_active = $ongoing['last_active'];
            
            // 1. Block if trying to start a DIFFERENT exam
            if ($ongoing_id !== $id_baithi) {
                header("Location: index.php?act=dethi&error=ongoing_mismatch");
                exit;
            }
            
            // 2. Block if SAME exam is ACTIVE elsewhere (Heartbeat < 30s)
            if ($last_active) {
                $last_time = strtotime($last_active);
                if ((time() - $last_time) < 30) {
                    header("Location: index.php?act=dethi&error=session_active");
                    exit;
                }
            }
        }

        $title = "Làm bài - PT QUIZ";
        $page_css = "lambai.css";
        $view = "views/lambai.php";
        break;

    case 'ketqua':
        if (!isset($_SESSION['user'])) {
            header("Location: index.php?act=dangnhap");
            exit;
        }
        $title = "Kết quả - PT QUIZ";
        $page_css = "dethi.css";
        $view = "views/ketqua.php";
        break;

    case 'dangxuat':
        session_destroy();
        header("Location: index.php");
        exit;

    case 'thongtin':
        if (!isset($_SESSION['user'])) {
            header("Location: index.php?act=dangnhap");
            exit;
        }
        $title = "Thông tin cá nhân - PT QUIZ";
        $view = "views/thongtin.php";
        break;

    case 'lichsu':
        if (!isset($_SESSION['user'])) {
            header("Location: index.php?act=dangnhap");
            exit;
        }
        $title = "Lịch sử làm bài - PT QUIZ";
        $view = "views/lichsu.php";
        break;

    default:
        $title = "Trang chủ - PT QUIZ";
        $page_css = "trangchu.css";
        $view = "views/trangchu.php";
}

include "views/header.php";
include $view;
include "views/footer.php";
