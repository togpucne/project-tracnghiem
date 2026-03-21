<div
    style="max-width: 600px; margin: 40px auto; background: white; padding: 40px; border-radius: 15px; box-shadow: 0 10px 30px rgba(0,0,0,0.1); border: 1px solid #eee;">
    <h2 style="text-align: center; color: #1a1a1a; margin-bottom: 30px;">
        Thêm Môn học mới
    </h2>

    <?php if(isset($_SESSION['error'])): ?>
    <div
        style="background: #fee2e2; color: #dc2626; padding: 15px; border-radius: 10px; margin-bottom: 20px; font-weight: 600; text-align: center; border: 1px solid #fecaca;">
        <?= $_SESSION['error']; unset($_SESSION['error']); ?>
    </div>
    <?php endif; ?>

    <form action="index.php?act=monhoc-save" method="POST">
        <div style="margin-bottom: 25px;">
            <label style="display: block; font-weight: 600; margin-bottom: 10px; color: #34495e;">Tên môn học</label>
            <input type="text" name="tenmonhoc"
                style="width: 100%; padding: 15px; border: 2px solid #eee; border-radius: 10px; font-size: 1rem; outline: none; transition: 0.3s;"
                placeholder="Ví dụ: Lập trình PHP, Cơ sở dữ liệu..." required onfocus="this.style.borderColor='#3498db'"
                onblur="this.style.borderColor='#eee'">

        </div>

        <div style="display: flex; gap: 15px; margin-top: 30px;">
            <button type="submit"
                style="flex: 2; background: #3498db; color: white; border: none; padding: 15px; border-radius: 10px; font-weight: bold; cursor: pointer; transition: 0.3s;">
                Lưu môn học
            </button>
            <a href="index.php?act=quanly-monhoc"
                style="flex: 1; text-align: center; background: #f1f2f6; color: #57606f; padding: 15px; border-radius: 10px; text-decoration: none; font-weight: bold; transition: 0.3s;">
                Hủy
            </a>
        </div>
    </form>
</div>