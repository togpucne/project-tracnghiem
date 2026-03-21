<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title><?php echo $title ?? 'Hệ thống Quản trị'; ?></title>
    <style>
        body { font-family: Arial, sans-serif; display: flex; margin: 0; }
        .sidebar { width: 250px; background: #2c3e50; color: white; min-height: 100vh; padding: 20px; }
        .sidebar a { color: white; display: block; padding: 10px; text-decoration: none; border-bottom: 1px solid #34495e; }
        .sidebar a:hover { background: #34495e; }
        .content { flex: 1; padding: 20px; background: #f4f4f4; }
        .user-info { margin-bottom: 20px; font-size: 0.9em; color: #bdc3c7; }
    </style>
</head>
<body>
    <div class="sidebar">
        <h2>PT QUIZ ADMIN</h2>
        <div class="user-info">
            Chào, <?php echo $_SESSION['user']['ten']; ?> (<?php echo $_SESSION['user']['vaitro']; ?>)
        </div>
        <a href="index.php?act=dashboard">Bảng điều khiển</a>

        <?php if ($_SESSION['user']['vaitro'] == 'giangvien' || $_SESSION['user']['vaitro'] == 'admin'): ?>
            <a href="index.php?act=quanly-monhoc">Quản lý môn học</a>
            <a href="index.php?act=quanly-dethi">Quản lý đề thi</a>
        <?php endif; ?>

        <?php if ($_SESSION['user']['vaitro'] == 'admin'): ?>
            <a href="index.php?act=quanly-nguoidung">Quản lý người dùng</a>
            <a href="index.php?act=hethong-api">Cấu hình API</a>
        <?php endif; ?>

        <a href="index.php?act=dangxuat" style="color: #e74c3c;">Đăng xuất</a>
    </div>
    <div class="content">