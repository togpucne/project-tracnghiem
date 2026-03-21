<div class="card"
    style="background: white; padding: 25px; border-radius: 12px; box-shadow: 0 4px 15px rgba(0,0,0,0.05); border: 2px solid #dee2e6;">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 25px;">
        <h2 style="margin: 0; color: #1a1a1a; font-weight: 700;">Quản lý Môn học</h2>
        <a href="index.php?act=monhoc-add"
            style="background: #3498db; color: white; padding: 12px 25px; border-radius: 8px; text-decoration: none; font-weight: 600; border: 1px solid #2980b9;">
            Thêm môn học mới
        </a>
    </div>

    <table style="width: 100%; border-collapse: collapse; border: 1px solid #ced4da;">
        <thead>
            <tr style="background: #f8f9fa; border-bottom: 2px solid #adb5bd; text-align: left;">
                <th
                    style="padding: 15px; width: 60px; text-align: center; border-right: 1px solid #ced4da; color: #333;">
                    STT</th>
                <th style="padding: 15px; border-right: 1px solid #ced4da; color: #333;">Tên môn học</th>
                <th style="padding: 15px; border-right: 1px solid #ced4da; color: #333;">Người tạo</th>
                <th style="padding: 15px; border-right: 1px solid #ced4da; color: #333;">Ngày thêm</th>
                <th style="padding: 15px; width: 220px; text-align: center; color: #333;">Thao tác</th>
            </tr>
        </thead>
        <tbody>
            <?php if (!empty($list_monhoc)): ?>
            <?php $stt = 1;
                foreach ($list_monhoc as $mon): ?>
            <tr style="border-bottom: 1px solid #ced4da;" onmouseover="this.style.backgroundColor='#f8f9fa'"
                onmouseout="this.style.backgroundColor='transparent'">
                <td style="padding: 15px; text-align: center; border-right: 1px solid #ced4da; font-weight: 600;">
                    <?= $stt++ ?></td>
                <td style="padding: 15px; border-right: 1px solid #ced4da; font-weight: 500;">
                    <?= htmlspecialchars($mon['tenmonhoc']) ?></td>
                <td style="padding: 15px; border-right: 1px solid #ced4da; color: #555;">
                    <?= htmlspecialchars($mon['ten_nguoi_tao'] ?? 'Hệ thống') ?></td>
                <td style="padding: 15px; border-right: 1px solid #ced4da; color: #555; font-size: 0.9rem;">
                    <?= ($mon['ngaythem']) ? date("d/m/Y H:i", strtotime($mon['ngaythem'])) : '---' ?>
                </td>
                <td style="padding: 15px; text-align: center;">
                    <div style="display: flex; justify-content: center; gap: 8px;">
                        <a href="index.php?act=monhoc-edit&id=<?= $mon['id_monhoc'] ?>"
                            style="color: #856404; background: #fff3cd; border: 1px solid #ffeeba; padding: 6px 14px; border-radius: 4px; text-decoration: none; font-size: 0.85rem; font-weight: 600;">Sửa</a>
                        <a href="index.php?act=monhoc-copy&id=<?= $mon['id_monhoc'] ?>"
                            style="color: #004085; background: #cce5ff; border: 1px solid #b8daff; padding: 6px 14px; border-radius: 4px; text-decoration: none; font-size: 0.85rem; font-weight: 600;">Chép</a>
                        <a href="javascript:void(0)"
                            onclick="showDeleteModal(<?= $mon['id_monhoc'] ?>, '<?= htmlspecialchars($mon['tenmonhoc']) ?>')"
                            style="color: #721c24; background: #f8d7da; border: 1px solid #f5c6cb; padding: 6px 14px; border-radius: 4px; text-decoration: none; font-size: 0.85rem; font-weight: 600;">
                            Xóa
                        </a>
                    </div>
                </td>
            </tr>
            <?php endforeach; ?>
            <?php else: ?>
            <tr>
                <td colspan="5" style="padding: 40px; text-align: center; color: #999; font-style: italic;">Chưa có môn
                    học nào được tạo bởi bạn.</td>
            </tr>
            <?php endif; ?>
        </tbody>
    </table>
</div>


<div id="deleteModal"
    style="display: none; position: fixed; z-index: 9999; left: 0; top: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); align-items: center; justify-content: center;">
    <div
        style="background: white; padding: 30px; border-radius: 12px; width: 400px; box-shadow: 0 5px 20px rgba(0,0,0,0.2); text-align: center;">
        <div style="color: #e74c3c; font-size: 50px; margin-bottom: 15px;">
            <i class="fas fa-exclamation-circle"></i>
        </div>
        <h3 style="margin-bottom: 10px; color: #333;">Xác nhận xóa?</h3>
        <p style="color: #666; margin-bottom: 25px;">Bạn có chắc chắn muốn xóa môn học <br><strong id="del_name"
                style="color: #000;"></strong>?</p>

        <div style="display: flex; gap: 10px; justify-content: center;">
            <a id="confirmDeleteBtn" href="#"
                style="background: #e74c3c; color: white; padding: 10px 25px; border-radius: 6px; text-decoration: none; font-weight: 600;">Xóa
                ngay</a>
            <button onclick="closeDeleteModal()"
                style="background: #eee; border: none; color: #333; padding: 10px 25px; border-radius: 6px; font-weight: 600; cursor: pointer;">Hủy</button>
        </div>
    </div>
</div>

<script>
function showDeleteModal(id, name) {
    document.getElementById('del_name').innerText = name;
    document.getElementById('confirmDeleteBtn').href = 'index.php?act=monhoc-delete&id=' + id;
    document.getElementById('deleteModal').style.display = 'flex';
}

function closeDeleteModal() {
    document.getElementById('deleteModal').style.display = 'none';
}

// Đóng modal khi click ra ngoài
window.onclick = function(event) {
    let modal = document.getElementById('deleteModal');
    if (event.target == modal) {
        closeDeleteModal();
    }
}
</script>