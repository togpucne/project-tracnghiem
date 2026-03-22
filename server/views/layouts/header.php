<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><?php echo $title ?? 'PT QUIZ Admin'; ?></title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="icon" type="image/jpg" href="public/imgs/ptstore-no-background.png">

    <style>
        :root {
            --dark-bg: #1a1a1a;
            /* Màu đen chủ đạo */
            --dark-secondary: #2d2d2d;
            --accent-color: #3498db;
            --text-color: #e0e0e0;
        }

        body {
            margin: 0;
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            display: flex;
            flex-direction: column;
            height: 100vh;
            background: #f4f7f6;
        }

        /* --- Top Nav --- */
        .top-nav {
            height: 60px;
            background: var(--dark-bg);
            color: white;
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 0 20px;
            position: fixed;
            top: 0;
            width: 100%;
            z-index: 1000;
            box-sizing: border-box;
            border-bottom: 1px solid #333;
        }

        .nav-logo {
            font-size: 20px;
            font-weight: bold;
            color: var(--accent-color);
            text-transform: uppercase;
        }

        .nav-user {
            display: flex;
            align-items: center;
            gap: 10px;
        }

        .nav-user i {
            font-size: 1.2rem;
        }

        /* --- Sidebar --- */
        .main-wrapper {
            display: flex;
            margin-top: 60px;
            height: calc(100vh - 60px);
        }

        .sidebar {
            width: 260px;
            background: var(--dark-bg);
            color: var(--text-color);
            padding-top: 20px;
            transition: all 0.3s;
        }

        .sidebar-menu {
            list-style: none;
            padding: 0;
            margin: 0;
        }

        .sidebar-menu li a {
            padding: 15px 25px;
            display: flex;
            align-items: center;
            gap: 12px;
            color: #bdc3c7;
            text-decoration: none;
            transition: 0.2s;
        }

        .sidebar-menu li a:hover {
            background: var(--dark-secondary);
            color: white;
            border-left: 4px solid var(--accent-color);
        }

        .sidebar-menu li.active a {
            background: var(--dark-secondary);
            color: white;
            border-left: 4px solid var(--accent-color);
        }

        /* --- Content Area --- */
        .content-body {
            flex: 1;
            padding: 25px;
            overflow-y: auto;
        }

        .role-badge {
            background: var(--accent-color);
            font-size: 11px;
            padding: 2px 8px;
            border-radius: 10px;
            text-transform: uppercase;
        }
    </style>
</head>

<body>

    <nav class="top-nav">
        <div class="nav-logo">
            <i class="fas fa-graduation-cap"></i> PT QUIZ SERVER
        </div>
        <div class="nav-user">
            <span class="role-badge"><?php echo $_SESSION['user']['vaitro']; ?></span>
            <i class="fas fa-user-circle"></i>
            <strong><?php echo $_SESSION['user']['ten']; ?></strong>
        </div>
    </nav>

    <div class="main-wrapper">
        <aside class="sidebar">
            <ul class="sidebar-menu">
                <li class="<?php echo ($act == 'dashboard') ? 'active' : ''; ?>">
                    <a href="index.php?act=dashboard"><i class="fas fa-home"></i> Dashboard</a>
                </li>

                <?php if ($_SESSION['user']['vaitro'] == 'giangvien'): ?>
                    <li class="<?php echo ($act == 'quanly-monhoc') ? 'active' : ''; ?>">
                        <a href="index.php?act=quanly-monhoc"><i class="fas fa-book"></i> Quản lý Môn học</a>
                    </li>
                    <li class="<?php echo ($act == 'quanly-dethi') ? 'active' : ''; ?>">
                        <a href="index.php?act=quanly-dethi"><i class="fas fa-file-alt"></i> Quản lý Đề thi</a>
                    </li>
                    <li class="<?php echo ($act == 'nganhang-cauhoi') ? 'active' : ''; ?>">
                        <a href="#"><i class="fas fa-database"></i> Ngân hàng câu hỏi</a>
                    </li>
                <?php endif; ?>

                <?php if ($_SESSION['user']['vaitro'] == 'admin'): ?>
                    <li class="<?php echo ($act == 'quanly-nguoidung') ? 'active' : ''; ?>">
                        <a href="index.php?act=quanly-nguoidung"><i class="fas fa-users-cog"></i> Quản lý Người dùng</a>
                    </li>
                    <li class="<?php echo ($act == 'thongke') ? 'active' : ''; ?>">
                        <a href="#"><i class="fas fa-chart-bar"></i> Thống kê hệ thống</a>
                    </li>
                    <li class="<?php echo ($act == 'hethong-api') ? 'active' : ''; ?>">
                        <a href="index.php?act=hethong-api"><i class="fas fa-server"></i> Logs API</a>
                    </li>
                <?php endif; ?>

                <hr style="border: 0.5px solid #333; margin: 10px 0;">
                <li>
                    <a href="index.php?act=dangxuat" style="color: #e74c3c;"><i class="fas fa-sign-out-alt"></i> Đăng
                        xuất</a>
                </li>
            </ul>
        </aside>

        <main class="content-body">