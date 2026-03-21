<div class="card" style="background: white; padding: 25px; border-radius: 12px; border: 1px solid #dee2e6;">
    <div style="display: flex; justify-content: space-between; margin-bottom: 20px;">
        <h2 style="margin: 0;">Quản lý Bài thi</h2>
        <button onclick="openExamModal()"
            style="background: #27ae60; color: white; padding: 10px 20px; border-radius: 6px; border: none; cursor: pointer; font-weight: 600;">+
            Thêm bài thi</button>
    </div>

    <table style="width: 100%; border-collapse: collapse;">
        <tr style="background: #f4f4f4; border-bottom: 2px solid #ddd;">
            <th style="padding: 12px; text-align: left;">Tên bài thi</th>
            <th style="padding: 12px; text-align: left;">Môn học</th>
            <th style="padding: 12px; text-align: center;">Thời gian</th>
            <th style="padding: 12px; text-align: center;">Thao tác</th>
        </tr>
        <?php foreach ($list_baithi as $bt): ?>
        <tr style="border-bottom: 1px solid #eee;">
            <td style="padding: 12px;"><strong><?= htmlspecialchars($bt['ten_baithi']) ?></strong></td>
            <td style="padding: 12px;"><?= htmlspecialchars($bt['tenmonhoc']) ?></td>
            <td style="padding: 12px; text-align: center;"><?= $bt['thoigianlam'] ?> phút</td>
            <td style="padding: 12px; text-align: center;">
                <button onclick='openExamModal(<?= json_encode($bt) ?>)'
                    style="background: #f39c12; color: white; border: none; padding: 5px 10px; border-radius: 4px; cursor: pointer;">Sửa</button>
                <a href="index.php?act=baithi-delete&id=<?= $bt['id_baithi'] ?>"
                    onclick="return confirm('Xóa bài này?')"
                    style="background: #e74c3c; color: white; padding: 5px 10px; border-radius: 4px; text-decoration: none; font-size: 13px;">Xóa</a>
            </td>
        </tr>
        <?php endforeach; ?>
    </table>
</div>

<div id="examModal"
    style="display: none; position: fixed; z-index: 999; left: 0; top: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); align-items: center; justify-content: center;">
    <div style="background: white; padding: 30px; border-radius: 10px; width: 600px;">
        <h3 id="modalTitle">Thêm Bài Thi</h3>
        <form action="index.php?act=baithi-save" method="POST">
            <input type="hidden" name="id_baithi" id="m_id">
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 15px;">
                <div>
                    <label>Tên bài thi</label>
                    <input type="text" name="ten_baithi" id="m_ten" required
                        style="width: 100%; padding: 8px; margin-top: 5px;">
                </div>
                <div>
                    <label>Môn học</label>
                    <select name="id_monhoc" id="m_mon" style="width: 100%; padding: 8px; margin-top: 5px;">
                        <?php foreach($list_monhoc as $mh): ?>
                        <option value="<?= $mh['id_monhoc'] ?>"><?= $mh['tenmonhoc'] ?></option>
                        <?php endforeach; ?>
                    </select>
                </div>
                <div>
                    <label>Tổng câu hỏi</label>
                    <input type="number" name="tongcauhoi" id="m_cau" required
                        style="width: 100%; padding: 8px; margin-top: 5px;">
                </div>
                <div>
                    <label>Thời gian (phút)</label>
                    <input type="number" name="thoigianlam" id="m_time" required
                        style="width: 100%; padding: 8px; margin-top: 5px;">
                </div>
                <div>
                    <label>Bắt đầu</label>
                    <input type="datetime-local" name="thoigianbatdau" id="m_start" required
                        style="width: 100%; padding: 8px; margin-top: 5px;">
                </div>
                <div>
                    <label>Kết thúc</label>
                    <input type="datetime-local" name="thoigianketthuc" id="m_end" required
                        style="width: 100%; padding: 8px; margin-top: 5px;">
                </div>
            </div>
            <div style="margin-top: 20px; text-align: right;">
                <button type="button" onclick="closeExamModal()" style="padding: 8px 15px;">Hủy</button>
                <button type="submit"
                    style="background: #27ae60; color: white; border: none; padding: 8px 20px; border-radius: 4px; cursor: pointer;">Lưu
                    lại</button>
            </div>
        </form>
    </div>
</div>

<script>
function openExamModal(data = null) {
    const modal = document.getElementById('examModal');
    if (data) {
        document.getElementById('modalTitle').innerText = "Cập nhật Bài Thi";
        document.getElementById('m_id').value = data.id_baithi;
        document.getElementById('m_ten').value = data.ten_baithi;
        document.getElementById('m_mon').value = data.id_monhoc;
        document.getElementById('m_cau').value = data.tongcauhoi;
        document.getElementById('m_time').value = data.thoigianlam;
        document.getElementById('m_start').value = data.thoigianbatdau.replace(" ", "T");
        document.getElementById('m_end').value = data.thoigianketthuc.replace(" ", "T");
    } else {
        document.getElementById('modalTitle').innerText = "Thêm Bài Thi Mới";
        document.getElementById('m_id').value = "";
        document.getElementById('m_ten').value = "";
    }
    modal.style.display = 'flex';
}

function closeExamModal() {
    document.getElementById('examModal').style.display = 'none';
}
</script>