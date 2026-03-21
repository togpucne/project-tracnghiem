<div class="card"
    style="background: white; padding: 25px; border-radius: 12px; box-shadow: 0 4px 15px rgba(0,0,0,0.05); border: 2px solid #dee2e6;">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 25px;">
        <h2 style="margin: 0; color: #1a1a1a; font-weight: 700;">Quản lý Môn học</h2>
        <button onclick="openFormModal()"
            style="background: #3498db; color: white; padding: 12px 25px; border-radius: 8px; font-weight: 600; border: 1px solid #2980b9; cursor: pointer;">
            <i class="fas fa-plus"></i> Thêm môn học mới
        </button>
    </div>

    <?php if (isset($_SESSION['success'])): ?>
    <div
        style="background: #d4edda; color: #155724; padding: 15px; border-radius: 8px; margin-bottom: 20px; border: 1px solid #c3e6cb;">
        <i class="fas fa-check-circle"></i> <?= $_SESSION['success']; unset($_SESSION['success']); ?>
    </div>
    <?php endif; ?>
    <?php if (isset($_SESSION['error'])): ?>
    <div
        style="background: #f8d7da; color: #721c24; padding: 15px; border-radius: 8px; margin-bottom: 20px; border: 1px solid #f5c6cb;">
        <i class="fas fa-exclamation-triangle"></i> <?= $_SESSION['error']; unset($_SESSION['error']); ?>
    </div>
    <?php endif; ?>

    <table style="width: 100%; border-collapse: collapse; border: 1px solid #ced4da;">
        <thead>
            <tr style="background: #f8f9fa; border-bottom: 2px solid #adb5bd; text-align: left;">
                <th style="padding: 15px; width: 60px; text-align: center; border-right: 1px solid #ced4da;">STT</th>
                <th style="padding: 15px; border-right: 1px solid #ced4da;">Tên môn học</th>
                <th style="padding: 15px; border-right: 1px solid #ced4da; text-align: center;">Số bài thi</th>
                <th style="padding: 15px; border-right: 1px solid #ced4da;">Ngày thêm</th>
                <th style="padding: 15px; width: 180px; text-align: center;">Thao tác</th>
            </tr>
        </thead>
        <tbody>
            <?php if (!empty($list_monhoc)): ?>
            <?php $stt = 1; foreach ($list_monhoc as $mon): ?>
            <tr style="border-bottom: 1px solid #ced4da;" onmouseover="this.style.backgroundColor='#f8f9fa'"
                onmouseout="this.style.backgroundColor='transparent'">
                <td style="padding: 15px; text-align: center; border-right: 1px solid #ced4da; font-weight: 600;">
                    <?= $stt++ ?></td>
                <td style="padding: 15px; border-right: 1px solid #ced4da; font-weight: 500;">
                    <?= htmlspecialchars($mon['tenmonhoc']) ?></td>
                <td style="padding: 15px; border-right: 1px solid #ced4da; text-align: center;">
                    <span
                        style="background: #e1f5fe; color: #0288d1; padding: 4px 12px; border-radius: 20px; font-weight: bold; font-size: 0.85rem; border: 1px solid #b3e5fc;">
                        <?= $mon['so_bai_thi'] ?> bài thi
                    </span>
                </td>
                <td style="padding: 15px; border-right: 1px solid #ced4da; color: #555; font-size: 0.9rem;">
                    <?= ($mon['ngaythem']) ? date("d/m/Y", strtotime($mon['ngaythem'])) : '---' ?>
                </td>
                <td style="padding: 15px; text-align: center;">
                    <div style="display: flex; justify-content: center; gap: 8px;">
                        <button
                            onclick="openFormModal(<?= $mon['id_monhoc'] ?>, '<?= htmlspecialchars($mon['tenmonhoc']) ?>')"
                            style="color: #856404; background: #fff3cd; border: 1px solid #ffeeba; padding: 6px 14px; border-radius: 4px; font-size: 0.85rem; font-weight: 600; cursor: pointer;">
                            Sửa
                        </button>
                        <button
                            onclick="showDeleteModal(<?= $mon['id_monhoc'] ?>, '<?= htmlspecialchars($mon['tenmonhoc']) ?>', <?= $mon['so_bai_thi'] ?>)"
                            style="color: #721c24; background: #f8d7da; border: 1px solid #f5c6cb; padding: 6px 14px; border-radius: 4px; font-size: 0.85rem; font-weight: 600; cursor: pointer;">
                            Xóa
                        </button>
                    </div>
                </td>
            </tr>
            <?php endforeach; ?>
            <?php else: ?>
            <tr>
                <td colspan="5" style="padding: 40px; text-align: center; color: #999; font-style: italic;">Chưa có môn
                    học nào.</td>
            </tr>
            <?php endif; ?>
        </tbody>
    </table>
</div>

<div id="formModal"
    style="display: none; position: fixed; z-index: 10000; left: 0; top: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.6); align-items: center; justify-content: center; backdrop-filter: blur(4px);">
    <div
        style="background: white; padding: 35px; border-radius: 15px; width: 450px; box-shadow: 0 10px 30px rgba(0,0,0,0.3); position: relative;">
        <h2 id="formTitle"
            style="margin-top: 0; color: #333; font-weight: 700; border-bottom: 2px solid #eee; padding-bottom: 15px;">
            Thêm môn học mới</h2>

        <form action="index.php?act=monhoc-save" method="POST" id="monhocForm">
            <input type="hidden" name="id_monhoc" id="form_id_monhoc">

            <div style="margin: 25px 0;">
                <label style="display: block; margin-bottom: 10px; font-weight: 600; color: #555;">Tên môn học</label>
                <input type="text" name="tenmonhoc" id="form_tenmonhoc" required
                    placeholder="VD: Lập trình Web, Cơ sở dữ liệu..."
                    style="width: 100%; padding: 12px; border: 2px solid #ddd; border-radius: 8px; font-size: 1rem; box-sizing: border-box;">
            </div>

            <div style="display: flex; gap: 10px; justify-content: flex-end; margin-top: 30px;">
                <button type="button" onclick="closeFormModal()"
                    style="background: #f1f1f1; color: #333; padding: 10px 20px; border-radius: 8px; border: none; font-weight: 600; cursor: pointer;">Hủy</button>
                <button type="submit" id="formSubmitBtn"
                    style="background: #3498db; color: white; padding: 10px 25px; border-radius: 8px; border: none; font-weight: 600; cursor: pointer;">Lưu
                    lại</button>
            </div>
        </form>
    </div>
</div>

<div id="deleteModal"
    style="display: none; position: fixed; z-index: 10000; left: 0; top: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.6); align-items: center; justify-content: center; backdrop-filter: blur(2px);">
    <div style="background: white; padding: 30px; border-radius: 12px; width: 400px; text-align: center;">
        <div id="modalIcon" style="font-size: 50px; margin-bottom: 15px;"></div>
        <h3 id="deleteTitle"></h3>
        <p id="deleteBody" style="color: #666; margin-bottom: 25px;"></p>
        <div style="display: flex; gap: 10px; justify-content: center;">
            <a id="confirmDeleteBtn" href="#"
                style="background: #e74c3c; color: white; padding: 10px 25px; border-radius: 6px; text-decoration: none; font-weight: 600; display: none;">Xác
                nhận xóa</a>
            <button onclick="closeDeleteModal()"
                style="background: #eee; border: none; padding: 10px 25px; border-radius: 6px; font-weight: 600; cursor: pointer;">Đóng</button>
        </div>
    </div>
</div>

<script>
// --- XỬ LÝ MODAL THÊM / SỬA ---
function openFormModal(id = null, name = '') {
    const modal = document.getElementById('formModal');
    const title = document.getElementById('formTitle');
    const inputId = document.getElementById('form_id_monhoc');
    const inputName = document.getElementById('form_tenmonhoc');
    const submitBtn = document.getElementById('formSubmitBtn');

    if (id) {
        // Chế độ Sửa
        title.innerText = "Chỉnh sửa môn học";
        inputId.value = id;
        inputName.value = name;
        submitBtn.innerText = "Cập nhật ngay";
        submitBtn.style.background = "#f39c12"; // Đổi màu nút sang cam cho dễ phân biệt
    } else {
        // Chế độ Thêm
        title.innerText = "Thêm môn học mới";
        inputId.value = "";
        inputName.value = "";
        submitBtn.innerText = "Lưu môn học";
        submitBtn.style.background = "#3498db";
    }

    modal.style.display = 'flex';
    inputName.focus();
}

function closeFormModal() {
    document.getElementById('formModal').style.display = 'none';
}

// --- XỬ LÝ MODAL XÓA ---
function showDeleteModal(id, name, count) {
    const modal = document.getElementById('deleteModal');
    const icon = document.getElementById('modalIcon');
    const btn = document.getElementById('confirmDeleteBtn');

    if (count > 0) {
        icon.innerHTML = '<i class="fas fa-exclamation-triangle" style="color: #f39c12;"></i>';
        document.getElementById('deleteTitle').innerText = "Không thể xóa!";
        document.getElementById('deleteBody').innerHTML =
            `Môn <strong>${name}</strong> có <strong>${count} bài thi</strong>. Hãy xóa bài thi trước.`;
        btn.style.display = 'none';
    } else {
        icon.innerHTML = '<i class="fas fa-trash-alt" style="color: #e74c3c;"></i>';
        document.getElementById('deleteTitle').innerText = "Xác nhận xóa?";
        document.getElementById('deleteBody').innerHTML =
            `Xóa môn <strong>${name}</strong>? Thao tác này không thể hoàn tác.`;
        btn.style.display = 'inline-block';
        btn.href = 'index.php?act=monhoc-delete&id=' + id;
    }
    modal.style.display = 'flex';
}

function closeDeleteModal() {
    document.getElementById('deleteModal').style.display = 'none';
}

// Đóng modal khi click ra ngoài
window.onclick = function(event) {
    if (event.target == document.getElementById('formModal')) closeFormModal();
    if (event.target == document.getElementById('deleteModal')) closeDeleteModal();
}
</script>