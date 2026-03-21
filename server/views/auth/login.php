<?php
if (isset($_POST['btn_login'])) {
    $email = $_POST['email'];
    $passwordInput = $_POST['password'];

    $conn = Database::connect();

    // 1. Tìm user theo email và phải có trạng thái active
    $stmt = $conn->prepare("SELECT * FROM nguoidung WHERE email = ? AND trangthai = 'active'");
    $stmt->bind_param("s", $email);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($user = $result->fetch_assoc()) {
        // 2. Kiểm tra mật khẩu (BCrypt)
        if (password_verify($passwordInput, $user['matkhau'])) {

            // 3. CHỐT CHẶN VAI TRÒ: Chỉ cho phép giangvien hoặc admin
            if ($user['vaitro'] == 'giangvien' || $user['vaitro'] == 'admin') {
                $_SESSION['user'] = [
                    'id_nguoidung' => $user['id_nguoidung'],
                    'ten' => $user['ten'],
                    'vaitro' => $user['vaitro']
                ];

                header("Location: index.php?act=dashboard");
                exit;
            } else {
                // Nếu là thisinh thì báo lỗi quyền truy cập
                $error = "Tài khoản của bạn không có quyền truy cập vào khu vực quản trị!";
            }
        } else {
            $error = "Sai mật khẩu!";
        }
    } else {
        $error = "Email không tồn tại hoặc tài khoản bị khóa!";
    }
}
?>

<?php if (isset($error)): ?>
    <div style="color: red; text-align: center; margin-bottom: 10px;"><?php echo $error; ?></div>
<?php endif; ?>

<!DOCTYPE html>
<html>

<head>
    <title>Đăng nhập quản trị</title>
    <link rel="stylesheet" href="../assets/css/style.css">
</head>

<body>
    <div class="login-container" style="width: 300px; margin: 100px auto; padding: 20px; border: 1px solid #ccc;">
        <h2 style="text-align: center;">HỆ THỐNG QUẢN TRỊ</h2>
        <form method="POST">
            <input type="email" name="email" placeholder="Email" required style="width: 100%; margin-bottom: 10px;">
            <input type="password" name="password" placeholder="Mật khẩu" required
                style="width: 100%; margin-bottom: 10px;">
            <button type="submit" name="btn_login" style="width: 100%; cursor: pointer;">Đăng nhập</button>
        </form>
    </div>
</body>

</html>