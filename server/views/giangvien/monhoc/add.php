<div
    style="max-width: 600px; margin: 40px auto; background: white; padding: 40px; border-radius: 15px; box-shadow: 0 10px 30px rgba(0,0,0,0.1);">
    <h2 style="text-align: center; color: #1a1a1a; margin-bottom: 30px;">
        <?php echo isset($_GET['id']) ? 'Chỉnh sửa Môn học' : 'Thêm Môn học mới'; ?>
    </h2>

    <form action="index.php?act=monhoc-save" method="POST">
        <?php if(isset($_GET['id'])): ?>
        <input type="hidden" name="id_monhoc" value="<?php echo $_GET['id']; ?>">
        <?php endif; ?>

        <div style="margin-bottom: 25px;">
            <label style="display: block; font-weight: 600; margin-bottom: 10px; color: #34495e;">Tên môn học</label>
            <input type="text" name="tenmonhoc"
                style="width: 100%; padding: 15px; border: 2px solid #eee; border-radius: 10px; font-size: 1rem; outline: none; transition: 0.3s;"
                placeholder="Ví dụ: Lập trình Java, Phân tích hệ thống..." required
                onfocus="this.style.borderColor='#3498db'" onblur="this.style.borderColor='#eee'">
        </div>

        <div style="display: flex; gap: 15px; margin-top: 30px;">
            <button type="submit"
                style="flex: 2; background: #3498db; color: white; border: none; padding: 15px; border-radius: 10px; font-weight: bold; cursor: pointer; transition: 0.3s;">
                <i class="fas fa-save"></i> Lưu môn học
            </button>
            <a href="index.php?act=quanly-monhoc"
                style="flex: 1; text-align: center; background: #f1f2f6; color: #57606f; padding: 15px; border-radius: 10px; text-decoration: none; font-weight: bold; transition: 0.3s;">
                Hủy
            </a>
        </div>
    </form>
</div>