<?php if (isset($_SESSION['error'])): ?>
    <div class="alert-msg"
        style="background: #f8d7da; color: #721c24; padding: 15px; border-radius: 8px; margin-bottom: 20px; border: 1px solid #f5c6cb; position: relative;">
        <i class="fas fa-exclamation-circle"></i> <strong>Lỗi:</strong>
        <?= $_SESSION['error'];
        unset($_SESSION['error']); ?>
        <span onclick="this.parentElement.style.display='none'"
            style="position: absolute; right: 15px; cursor: pointer;">&times;</span>
    </div>
<?php endif; ?>

<?php if (isset($_SESSION['success'])): ?>
    <div class="alert-msg"
        style="background: #d4edda; color: #155724; padding: 15px; border-radius: 8px; margin-bottom: 20px; border: 1px solid #c3e6cb; position: relative;">
        <i class="fas fa-check-circle"></i> <?= $_SESSION['success'];
                                            unset($_SESSION['success']); ?>
        <span onclick="this.parentElement.style.display='none'"
            style="position: absolute; right: 15px; cursor: pointer;">&times;</span>
    </div>
<?php endif; ?>

<div class="card"
    style="background: white; padding: 25px; border-radius: 12px; border: 1px solid #dee2e6; box-shadow: 0 2px 4px rgba(0,0,0,0.05);">
    <div style="display: flex; justify-content: space-between; margin-bottom: 20px;">
        <h2 style="margin: 0; color: #333;">Quản lý Bài thi</h2>
        <button onclick="openExamModal()"
            style="background: #27ae60; color: white; padding: 10px 20px; border-radius: 6px; border: none; cursor: pointer; font-weight: 600;">
            <i class="fas fa-plus"></i> Thêm bài thi
        </button>
    </div>

    <table style="width: 100%; border-collapse: collapse;">
        <thead>
            <tr style="background: #f4f4f4; border-bottom: 2px solid #ddd;">
                <th style="padding: 12px; text-align: center; width: 50px;">STT</th>
                <th style="padding: 12px; text-align: left;">Tên bài thi</th>
                <th style="padding: 12px; text-align: left;">Môn học</th>
                <th style="padding: 12px; text-align: center;">Số câu</th>
                <th style="padding: 12px; text-align: center;">Thời gian làm</th>
                <th style="padding: 12px; text-align: center;">Câu hỏi</th> <!-- Cột mới -->
                <th style="padding: 12px; text-align: center;">Thao tác</th>
            </tr>
        </thead>
        <tbody>
            <?php $stt = 1;
            if (!empty($list_baithi)): foreach ($list_baithi as $bt): ?>
                    <tr style="border-bottom: 1px solid #eee;">
                        <td style="padding: 12px; text-align: center; color: #666;"><?= $stt++ ?></td>
                        <td style="padding: 12px;"><strong><?= htmlspecialchars($bt['ten_baithi']) ?></strong></td>
                        <td style="padding: 12px;"><?= htmlspecialchars($bt['tenmonhoc']) ?></td>
                        <td style="padding: 12px; text-align: center;">
                            <span
                                style="background: #e1f5fe; color: #0288d1; padding: 4px 10px; border-radius: 12px; font-size: 12px; font-weight: 600;">
                                <?= $bt['tongcauhoi'] ?> câu
                            </span>
                        </td>
                        <td style="padding: 12px; text-align: center;"><i class="far fa-clock"></i> <?= $bt['thoigianlam'] ?>
                            phút</td>

                        <!-- Cột Câu hỏi - Nút bấm để vào quản lý câu hỏi -->
                        <td style="padding: 12px; text-align: center;">
                            <a href="index.php?act=cauhoi-list&id_baithi=<?= $bt['id_baithi'] ?>&ten_baithi=<?= urlencode($bt['ten_baithi']) ?>"
                                style="background: #3498db; color: white; padding: 6px 12px; border-radius: 4px; text-decoration: none; display: inline-block;">
                                <i class="fas fa-list"></i> Quản lý câu hỏi
                            </a>
                        </td>

                        <td style="padding: 12px; text-align: center;">
                            <button onclick='openExamModal(<?= json_encode($bt) ?>)'
                                style="background: #f39c12; color: white; border: none; padding: 6px 12px; border-radius: 4px; cursor: pointer;">
                                <i class="fas fa-edit"></i> Sửa
                            </button>
                            <a href="index.php?act=baithi-delete&id=<?= $bt['id_baithi'] ?>"
                                onclick="return confirm('Xóa bài này?')"
                                style="background: #e74c3c; color: white; padding: 6px 12px; border-radius: 4px; text-decoration: none; font-size: 13px; display: inline-block;">
                                <i class="fas fa-trash"></i> Xóa
                            </a>
                        </td>
                    </tr>
                <?php endforeach;
            else: ?>
                <tr>
                    <td colspan="7" style="text-align: center; padding: 20px;">Chưa có dữ liệu.</td>
                </tr>
            <?php endif; ?>
        </tbody>
    </table>
</div>

<!-- Modal Thêm/Sửa Bài Thi (giữ nguyên như cũ) -->
<div id="examModal"
    style="display: none; position: fixed; z-index: 9999; left: 0; top: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.6); align-items: center; justify-content: center;">
    <div style="background: white; padding: 30px; border-radius: 10px; width: 650px;">
        <h3 id="modalTitle" style="margin-top: 0; border-bottom: 2px solid #eee; padding-bottom: 15px;">Thêm Bài Thi
        </h3>
        <form action="index.php?act=baithi-save" method="POST" onsubmit="return validateExamForm()">
            <input type="hidden" name="id_baithi" id="m_id">
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-top: 15px;">
                <div style="grid-column: span 2;">
                    <label style="font-weight: 600;">Tên bài thi</label>
                    <input type="text" name="ten_baithi" id="m_ten" required placeholder="Nhập tên bài thi..."
                        style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 6px;">
                </div>
                <div>
                    <label style="font-weight: 600;">Môn học</label>
                    <select name="id_monhoc" id="m_mon"
                        style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 6px;">
                        <?php foreach ($list_monhoc as $mh): ?>
                            <option value="<?= $mh['id_monhoc'] ?>"><?= $mh['tenmonhoc'] ?></option>
                        <?php endforeach; ?>
                    </select>
                </div>
                <div>
                    <label style="font-weight: 600;">Trạng thái (Mặc định: Đang mở)</label>
                    <select name="trangthai" id="m_status"
                        style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 6px;">
                        <option value="Đang mở">Đang mở</option>
                        <option value="Đóng">Đóng</option>
                    </select>
                </div>
                <div>
                    <label style="font-weight: 600;">Số câu (≥ 5)</label>
                    <input type="number" name="tongcauhoi" id="m_cau" required min="5"
                        style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 6px;">
                </div>
                <div>
                    <label style="font-weight: 600;">Thời gian (phút)</label>
                    <input type="number" name="thoigianlam" id="m_time" required min="2"
                        style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 6px;">
                </div>
                <div>
                    <label style="font-weight: 600;">Bắt đầu (Bắt buộc)</label>
                    <input type="datetime-local" name="thoigianbatdau" id="m_start" required onchange="updateEndMin()"
                        style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 6px;">
                </div>
                <div>
                    <label style="font-weight: 600;">Kết thúc (Có thể bỏ trống)</label>
                    <input type="datetime-local" name="thoigianketthuc" id="m_end"
                        style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 6px;">
                </div>
            </div>
            <div style="margin-top: 30px; text-align: right;">
                <button type="button" onclick="closeExamModal()"
                    style="padding: 10px 25px; border-radius: 6px; border: 1px solid #ccc; cursor: pointer;">Hủy</button>
                <button type="submit"
                    style="background: #27ae60; color: white; border: none; padding: 10px 30px; border-radius: 6px; cursor: pointer; margin-left: 10px;">Lưu</button>
            </div>
        </form>
    </div>
</div>

<script>
    // Hàm định dạng ngày giờ địa phương cho input datetime-local (YYYY-MM-DDTHH:mm)
    function getLocalDateTimeString(date) {
        const tzOffset = date.getTimezoneOffset() * 60000;
        const localISOTime = (new Date(date - tzOffset)).toISOString().slice(0, 16);
        return localISOTime;
    }

    // Cập nhật giới hạn Min cho ngày kết thúc
    function updateEndMin() {
        const startVal = document.getElementById('m_start').value;
        const endInput = document.getElementById('m_end');
        if (startVal) {
            endInput.min = startVal;
        }
    }

    function validateExamForm() {
        let tenInput = document.getElementById('m_ten');
        let tenVal = tenInput.value.trim();

        if (tenVal.length < 5) {
            alert("⚠️ Tên bài thi phải ít nhất 5 ký tự!");
            return false;
        }

        const startStr = document.getElementById('m_start').value;
        const endStr = document.getElementById('m_end').value;

        // Chỉ check ngày kết thúc NẾU người dùng có nhập
        if (endStr !== "") {
            if (new Date(endStr) <= new Date(startStr)) {
                alert("⚠️ Lỗi: Ngày kết thúc phải sau ngày bắt đầu!");
                return false;
            }
        }
        return true; // Cho phép gửi form
    }

    function openExamModal(data = null) {
        const modal = document.getElementById('examModal');
        const startInput = document.getElementById('m_start');
        const endInput = document.getElementById('m_end');
        const now = new Date();
        const currentStr = getLocalDateTimeString(now);

        if (data) {
            // Chế độ SỬA
            document.getElementById('modalTitle').innerText = "Cập nhật Bài Thi";
            document.getElementById('m_id').value = data.id_baithi;
            document.getElementById('m_ten').value = data.ten_baithi;
            document.getElementById('m_mon').value = data.id_monhoc;
            document.getElementById('m_cau').value = data.tongcauhoi;
            document.getElementById('m_time').value = data.thoigianlam;
            document.getElementById('m_status').value = data.trangthai || "Đang mở";

            startInput.value = data.thoigianbatdau.replace(" ", "T").substring(0, 16);
            endInput.value = data.thoigianketthuc ? data.thoigianketthuc.replace(" ", "T").substring(0, 16) : "";

            startInput.removeAttribute('min');
            endInput.min = startInput.value;
        } else {
            // Chế độ THÊM MỚI
            document.getElementById('modalTitle').innerText = "Thêm Bài Thi Mới";
            document.getElementById('m_id').value = "";
            document.getElementById('m_ten').value = "";
            document.getElementById('m_cau').value = "10";
            document.getElementById('m_time').value = "15";
            document.getElementById('m_status').value = "Đang mở";

            // Mặc định ngày hiện tại
            startInput.value = currentStr;
            startInput.min = currentStr;

            endInput.value = "";
            endInput.min = currentStr;
        }
        modal.style.display = 'flex';
    }

    function closeExamModal() {
        document.getElementById('examModal').style.display = 'none';
    }
</script>