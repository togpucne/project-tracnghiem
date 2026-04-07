<?php
if (!isset($_SESSION['user'])) {
    header("Location: index.php");
    exit;
}
?>

<div class="container my-5" style="min-height: 50vh;" id="profile-container">
    <div class="text-center py-5" id="profile-loading">
        <div class="spinner-border text-primary" role="status">
            <span class="visually-hidden">Đang tải...</span>
        </div>
    </div>

    <div class="row justify-content-center" id="profile-content" style="display: none;">
        <div class="col-md-8 col-lg-6">
            <div class="card premium-card border-0 p-4 p-md-5 bg-white position-relative">
                
                <a href="index.php?act=lichsu" class="position-absolute top-0 start-0 m-4 text-muted text-decoration-none">
                    <i class="fa-solid fa-arrow-left fa-lg transition-hover" title="Đóng"></i>
                </a>

                <div class="text-center mb-5 mt-3">
                    <div class="bg-primary bg-opacity-10 text-primary rounded-circle d-flex align-items-center justify-content-center mx-auto mb-3" style="width: 100px; height: 100px;">
                        <i class="fa-solid fa-user fs-1"></i>
                    </div>
                    <h3 class="fw-bold text-dark mb-1" id="displayName"></h3>
                    <p class="text-muted mb-0">Hồ sơ cá nhân</p>
                </div>

                <form id="profileForm">
                    <div class="mb-4">
                        <label class="form-label text-dark fw-bold small mb-2">HỌ VÀ TÊN</label>
                        <div class="input-group custom-input-group">
                            <span class="input-group-text border-0 bg-light text-muted px-3"><i class="fa-solid fa-id-card"></i></span>
                            <input type="text" name="ten" id="inputTen" class="form-control border-0 bg-light py-3" required>
                        </div>
                    </div>
                    
                    <div class="mb-4">
                        <label class="form-label text-dark fw-bold small mb-2">ĐỊA CHỈ EMAIL</label>
                        <div class="input-group custom-input-group">
                            <span class="input-group-text border-0 bg-light text-muted px-3"><i class="fa-solid fa-envelope"></i></span>
                            <input type="email" name="email" id="inputEmail" class="form-control border-0 bg-light py-3" required>
                        </div>
                    </div>

                    <div class="mb-4">
                        <label class="form-label text-dark fw-bold small mb-2">ĐỔI MẬT KHẨU (TÙY CHỌN)</label>
                        <div class="input-group custom-input-group">
                            <span class="input-group-text border-0 bg-light text-muted px-3"><i class="fa-solid fa-lock"></i></span>
                            <input type="password" name="matkhau" id="matkhauInput" class="form-control border-0 bg-light py-3" placeholder="Bỏ trống nếu giữ nguyên">
                            <button class="btn btn-light border-0 bg-light text-muted px-3" type="button" id="togglePasswordBtn">
                                <i class="fa-solid fa-eye" id="togglePasswordIcon"></i>
                            </button>
                        </div>
                    </div>

                    <div class="mb-5">
                        <label class="form-label text-dark fw-bold small mb-2">NGÀY THAM GIA</label>
                        <div class="input-group custom-input-group">
                            <span class="input-group-text border-0 bg-light text-muted px-3"><i class="fa-regular fa-calendar-check"></i></span>
                            <input type="text" id="inputNgaytao" class="form-control border-0 bg-light py-3 text-muted" disabled>
                        </div>
                    </div>

                    <button type="submit" class="btn btn-primary w-100 py-3 rounded-3 fw-bold shadow-sm custom-btn">
                        Lưu thay đổi
                    </button>
                    
                    <div class="text-center mt-4">
                        <a href="index.php?act=dangxuat" class="text-danger small fw-medium text-decoration-none" onclick="return logoutConfirm()"><i class="fa-solid fa-right-from-bracket me-1"></i>Đăng xuất</a>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<script>
document.addEventListener("DOMContentLoaded", async () => {
    const loading = document.getElementById('profile-loading');
    const content = document.getElementById('profile-content');
    const displayName = document.getElementById('displayName');
    const inputTen = document.getElementById('inputTen');
    const inputEmail = document.getElementById('inputEmail');
    const inputNgaytao = document.getElementById('inputNgaytao');

    try {
        const res = await fetch(apiUrl("profile/detail"));
        const json = await res.json();

        if (json.success) {
            const user = json.data;
            displayName.innerText = user.ten;
            inputTen.value = user.ten;
            inputEmail.value = user.email;
            
            // Format date d/m/Y
            const date = new Date(user.ngaytao);
            inputNgaytao.value = `${date.getDate().toString().padStart(2, '0')}/${(date.getMonth() + 1).toString().padStart(2, '0')}/${date.getFullYear()}`;
            
            loading.style.display = 'none';
            content.style.display = 'flex';
        } else {
            alert(json.error || 'Lỗi tải thông tin');
        }
    } catch (e) {
        console.error(e);
        alert('Lỗi kết nối máy chủ');
    }
});

document.getElementById('profileForm').addEventListener('submit', async function(e) {
    e.preventDefault();
    
    const formData = new FormData(this);
    const data = Object.fromEntries(formData.entries());

    try {
        const res = await fetch(apiUrl("profile/update"), {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });

        const result = await res.json();

        if (result.success) {
            alert(result.message);
            window.location.reload(); 
        } else {
            alert(result.error);
        }
    } catch (error) {
        console.error(error);
        alert('Lỗi kết nối đến server!');
    }
});

document.getElementById('togglePasswordBtn').addEventListener('click', function() {
    const pwdInput = document.getElementById('matkhauInput');
    const pwdIcon = document.getElementById('togglePasswordIcon');
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
</script>

