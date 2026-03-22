<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><?php echo $title ?? 'PT QUIZ Admin'; ?></title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="icon" type="image/jpg" href="public/imgs/ptstore-no-background.png">

    <style>
    :root {
        --dark-bg: #1a1a1a;
        --dark-secondary: #2d2d2d;
        --accent-color: #3498db;
        --text-color: #e0e0e0;
        --sidebar-width: 260px;
    }

    body {
        margin: 0;
        font-family: 'Inter', 'Segoe UI', sans-serif;
        background: #f4f7f6;
        overflow: hidden;
        /* Để scroll bên trong content-body */
    }

    /* --- Top Nav --- */
    .top-nav {
        height: 60px;
        background: var(--dark-bg);
        color: white;
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 0 25px;
        position: fixed;
        top: 0;
        width: 100%;
        z-index: 1050;
        border-bottom: 1px solid #333;
    }

    .nav-logo {
        font-size: 18px;
        font-weight: 800;
        color: var(--accent-color);
        letter-spacing: 1px;
    }

    /* --- Sidebar --- */
    .main-wrapper {
        display: flex;
        margin-top: 60px;
        height: calc(100vh - 60px);
    }

    .sidebar {
        width: var(--sidebar-width);
        background: var(--dark-bg);
        color: var(--text-color);
        flex-shrink: 0;
        border-right: 1px solid #333;
    }

    .sidebar-menu {
        list-style: none;
        padding: 0;
        margin: 0;
    }

    .sidebar-menu li a {
        padding: 14px 25px;
        display: flex;
        align-items: center;
        gap: 12px;
        color: #a0aec0;
        text-decoration: none;
        transition: 0.3s;
        font-size: 15px;
    }

    .sidebar-menu li a:hover,
    .sidebar-menu li.active a {
        background: var(--dark-secondary);
        color: white;
        border-left: 4px solid var(--accent-color);
    }

    /* --- Content Area --- */
    .content-body {
        flex-grow: 1;
        padding: 30px;
        overflow-y: auto;
        background: #f8fafc;
    }

    .role-badge {
        background: rgba(52, 152, 219, 0.2);
        color: var(--accent-color);
        border: 1px solid var(--accent-color);
        font-size: 10px;
        padding: 2px 10px;
        border-radius: 20px;
        font-weight: 700;
    }
    </style>
</head>

<body>

    <nav class="top-nav">
        <div class="nav-logo">
            <i class="fas fa-bolt me-2"></i> PT QUIZ SERVER
        </div>
        <div class="nav-user d-flex align-items-center gap-3">
            <span class="role-badge"><?php echo strtoupper($_SESSION['user']['vaitro']); ?></span>
            <div class="d-none d-md-block text-end">
                <small class="d-block text-muted" style="font-size: 10px;">Xin chào,</small>
                <span style="font-size: 14px; font-weight: 600;"><?php echo $_SESSION['user']['ten']; ?></span>
            </div>
            <i class="fas fa-user-circle fs-4 text-secondary"></i>
        </div>
    </nav>

    <div class="main-wrapper">
        <aside class="sidebar">
            <ul class="sidebar-menu">
                <li class="<?php echo ($act == 'dashboard') ? 'active' : ''; ?>">
                    <a href="index.php?act=dashboard"><i class="fas fa-th-large"></i> Dashboard</a>
                </li>

                <?php if ($_SESSION['user']['vaitro'] == 'giangvien'): ?>
                <li class="<?php echo ($act == 'quanly-monhoc') ? 'active' : ''; ?>">
                    <a href="index.php?act=quanly-monhoc"><i class="fas fa-book"></i> Quản lý Môn học</a>
                </li>
                <li class="<?php echo ($act == 'quanly-dethi') ? 'active' : ''; ?>">
                    <a href="index.php?act=quanly-dethi"><i class="fas fa-file-signature"></i> Quản lý Đề thi</a>
                </li>
                <?php endif; ?>

                <?php if ($_SESSION['user']['vaitro'] == 'admin'): ?>
                <div class="px-4 mt-4 mb-2 text-uppercase text-muted" style="font-size: 10px; font-weight: 800;">Hệ
                    thống</div>
                <li class="<?php echo ($act == 'quanly-nguoidung') ? 'active' : ''; ?>">
                    <a href="index.php?act=quanly-nguoidung"><i class="fas fa-users-cog"></i> Người dùng</a>
                </li>
                <li class="<?php echo ($act == 'thongke') ? 'active' : ''; ?>">
                    <a href="#"><i class="fas fa-chart-line"></i> Thống kê</a>
                </li>
                <li class="<?php echo ($act == 'hethong-api') ? 'active' : ''; ?>">
                    <a href="index.php?act=hethong-api"><i class="fas fa-code"></i> Logs API</a>
                </li>
                <?php endif; ?>

                <li class="mt-5">
                    <a href="index.php?act=dangxuat" class="text-danger"><i class="fas fa-power-off"></i> Đăng xuất</a>
                </li>
            </ul>
        </aside>

        <main class="content-body">