<?php
ini_set('display_errors', 1);
error_reporting(E_ALL);
session_start();

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
