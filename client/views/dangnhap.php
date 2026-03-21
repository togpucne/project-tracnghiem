<div class="login-container">
    <div role="main">
        <div class="loginform">

            <div class="text-center mb-4">
                <div class="auth-icon-wrapper mb-3">
                    <i class="fa-solid fa-right-to-bracket text-primary fs-2"></i>
                </div>
                <h2 class="login-heading fw-bold mb-1">Đăng nhập</h2>
                <p class="text-muted small">Chào mừng bạn quay lại PT QUIZ</p>
            </div>

            <!-- BỎ action + method -->
            <form id="loginForm" class="login-form">

                <!-- Email -->
                <div class="mb-4">
                    <div class="input-group custom-input-group">
                        <span class="input-group-text border-0 bg-light text-muted px-3">
                            <i class="fa-solid fa-envelope"></i>
                        </span>
                        <input type="email" name="email" class="form-control border-0 bg-light py-3" placeholder="Địa chỉ Email" required>
                    </div>
                </div>

                <!-- Password -->
                <div class="mb-3">
                    <div class="input-group custom-input-group">
                        <span class="input-group-text border-0 bg-light text-muted px-3">
                            <i class="fa-solid fa-lock"></i>
                        </span>
                        <input type="password" name="password" id="loginPasswordInput" class="form-control border-0 bg-light py-3" placeholder="Mật khẩu" required>
                        <button class="btn btn-light border-0 bg-light text-muted px-3" type="button" id="toggleLoginPwdBtn">
                            <i class="fa-solid fa-eye" id="toggleLoginPwdIcon"></i>
                        </button>
                    </div>
                </div>

                <button class="btn btn-primary w-100 py-3 rounded-3 fw-bold shadow-sm custom-btn" type="submit">
                    Đăng nhập ngay
                </button>

                <!-- Link register -->
                <div class="text-center mt-4">
                    <span class="text-muted">Chưa có tài khoản?</span>
                    <a href="index.php?act=dangky" class="text-primary fw-bold text-decoration-none ms-1">Đăng ký</a>
                </div>

            </form>

        </div>
    </div>
</div>

<script>
document.addEventListener("DOMContentLoaded", function() {

    const form = document.getElementById("loginForm");
    if (!form) return;

    const toggleLoginPwdBtn = document.getElementById('toggleLoginPwdBtn');
    if (toggleLoginPwdBtn) {
        toggleLoginPwdBtn.addEventListener('click', function() {
            const pwdInput = document.getElementById('loginPasswordInput');
            const pwdIcon = document.getElementById('toggleLoginPwdIcon');
            if (pwdInput.type === 'password') {
                pwdInput.type = 'text';
                pwdIcon.classList.remove('fa-eye');
                pwdIcon.classList.add('fa-eye-slash');
            } else {
                pwdInput.type = 'password';
                pwdIcon.classList.remove('fa-eye-slash');
                pwdIcon.classList.add('fa-eye');
            }
        });
    }

    form.addEventListener("submit", async function(e) {
        e.preventDefault();

        const data = {
            email: form.email.value,
            password: form.password.value
        };

        try {
            const response = await fetch("api/login.php", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(data)
            });

            const result = await response.json();

            if (!response.ok) {
                alert(result.error);
                return;
            }

            // Lưu token
            localStorage.setItem("token", result.token);

            alert(result.message);

            // Chuyển về trang chủ
            window.location.href = "index.php";

        } catch (error) {
            console.error(error);
            alert("Lỗi kết nối server");
        }
    });

});
</script>