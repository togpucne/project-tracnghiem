<?php
// views/giangvien/cauhoi/list.php
$title = "Quản lý câu hỏi: " . htmlspecialchars($ten_baithi);
$current_count = count($list_cauhoi);
$max_count = (int)$baithi['tongcauhoi'];
?>

<div class="container" style="max-width: 1200px; margin: 0 auto; padding: 20px;">
    <?php if (isset($_SESSION['error'])): ?>
        <div class="alert alert-danger"
            style="background: #f8d7da; color: #721c24; padding: 15px; border-radius: 8px; margin-bottom: 20px;">
            <?= $_SESSION['error'];
            unset($_SESSION['error']); ?>
        </div>
    <?php endif; ?>

    <?php if (isset($_SESSION['success'])): ?>
        <div class="alert alert-success"
            style="background: #d4edda; color: #155724; padding: 15px; border-radius: 8px; margin-bottom: 20px;">
            <?= $_SESSION['success'];
            unset($_SESSION['success']); ?>
        </div>
    <?php endif; ?>

    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
        <div>
            <h2 style="margin: 0;"><?= htmlspecialchars($ten_baithi) ?></h2>
            <p style="color: #666; margin-top: 5px;">
                Môn: <?= htmlspecialchars($baithi['tenmonhoc']) ?> |
                Số câu hiện có: <strong id="displayCount"><?= $current_count ?></strong>/<?= $max_count ?>
            </p>
        </div>
        <div>
            <a href="index.php?act=quanly-dethi"
                style="background: #6c757d; color: white; padding: 8px 15px; border-radius: 6px; text-decoration: none; margin-right: 10px;">Quay
                lại</a>
            <button onclick="openAddModal()"
                style="background: #27ae60; color: white; padding: 8px 20px; border-radius: 6px; border: none; cursor: pointer;">Thêm
                câu hỏi</button>
        </div>
    </div>

    <div style="background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.1);">
        <table id="questionTable" style="width: 100%; border-collapse: collapse;">
            <thead style="background: #f8f9fa;">
                <tr>
                    <th style="padding: 12px; text-align: center;">STT</th>
                    <th style="padding: 12px; text-align: left;">Nội dung câu hỏi</th>
                    <th style="padding: 12px; text-align: left;">Đáp án</th>
                    <th style="padding: 12px; text-align: center;">Độ khó</th>
                    <th style="padding: 12px; text-align: center;">Thao tác</th>
                </tr>
            </thead>
            <tbody>
                <?php if (empty($list_cauhoi)): ?>
                    <tr>
                        <td colspan="5" style="text-align: center; padding: 40px; color: #999;">Chưa có câu hỏi nào.</td>
                    </tr>
                    <?php else: $stt = 1;
                    foreach ($list_cauhoi as $ch): ?>
                        <tr style="border-bottom: 1px solid #eee;" class="question-row">
                            <td style="padding: 12px; text-align: center;"><?= $stt++ ?></td>
                            <td style="padding: 12px;"><strong
                                    class="q-content"><?= htmlspecialchars($ch['noidungcauhoi']) ?></strong></td>
                            <td style="padding: 12px;">
                                <?php foreach ($ch['dapan'] as $d): ?>
                                    <div style="margin: 5px 0; display: flex; align-items: center;">
                                        <input type="checkbox" <?= $d['dapandung'] == 1 ? 'checked' : '' ?> disabled
                                            style="margin-right: 8px;">
                                        <span
                                            style="<?= $d['dapandung'] == 1 ? 'color: #27ae60; font-weight: bold;' : 'color: #666;' ?>">
                                            <?= htmlspecialchars($d['noidungdapan']) ?>
                                        </span>
                                    </div>
                                <?php endforeach; ?>
                            </td>
                            <td style="padding: 12px; text-align: center;"><?= $ch['dokho'] ?></td>
                            <td style="padding: 12px; text-align: center;">
                                <button onclick='openEditModal(<?= json_encode($ch) ?>)'
                                    style="background: #f39c12; color: white; border: none; padding: 5px 10px; border-radius: 4px; cursor: pointer;">Sửa</button>
                                <a href="index.php?act=cauhoi-delete&id=<?= $ch['id_cauhoi'] ?>&id_baithi=<?= $id_baithi ?>"
                                    onclick="return confirm('Xóa câu hỏi này?')"
                                    style="background: #e74c3c; color: white; padding: 5px 10px; border-radius: 4px; text-decoration: none; margin-left: 5px;">Xóa</a>
                            </td>
                        </tr>
                <?php endforeach;
                endif; ?>
            </tbody>
        </table>
    </div>
</div>

<div id="questionModal"
    style="display: none; position: fixed; z-index: 9999; left: 0; top: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.6); align-items: center; justify-content: center;">
    <div
        style="background: white; padding: 30px; border-radius: 10px; width: 700px; max-height: 90%; overflow-y: auto;">
        <h3 id="modalTitle" style="margin-top: 0;">Thêm Câu Hỏi Mới</h3>
        <form id="questionForm" method="POST">
            <input type="hidden" name="id_baithi" value="<?= $id_baithi ?>">
            <input type="hidden" name="id_cauhoi" id="edit_id_cauhoi">

            <div style="margin-bottom: 15px;">
                <label style="display: block; margin-bottom: 5px; font-weight: bold;">Nội dung câu hỏi:</label>
                <textarea name="noidungcauhoi" id="noidungcauhoi" rows="3"
                    style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 6px; box-sizing: border-box;"></textarea>
            </div>

            <div style="margin-bottom: 15px;">
                <label style="display: block; margin-bottom: 5px; font-weight: bold;">Độ khó:</label>
                <select name="dokho" id="dokho"
                    style="width: 200px; padding: 8px; border: 1px solid #ddd; border-radius: 6px;">
                    <option value="Dễ">Dễ</option>
                    <option value="Trung bình">Trung bình</option>
                    <option value="Khó">Khó</option>
                </select>
            </div>

            <div style="margin-bottom: 15px;">
                <label style="font-weight: bold;">Đáp án (Tích chọn ô bên cạnh để làm đáp án đúng):</label>
                <div id="optionsContainer" style="margin-top: 10px;"></div>
                <button type="button" onclick="addOption()"
                    style="background: #3498db; color: white; border: none; padding: 8px 15px; border-radius: 4px; margin-top: 10px; cursor: pointer;">+
                    Thêm đáp án</button>
            </div>

            <div style="margin-top: 20px; text-align: right; border-top: 1px solid #eee; padding-top: 15px;">
                <button type="button" onclick="closeModal()"
                    style="padding: 8px 20px; border: 1px solid #ccc; border-radius: 4px; cursor: pointer; background: white;">Hủy</button>
                <button type="submit"
                    style="background: #27ae60; color: white; border: none; padding: 8px 25px; border-radius: 4px; margin-left: 10px; cursor: pointer;">Lưu
                    câu hỏi</button>
            </div>
        </form>
    </div>
</div>

<script>
    const MAX_QUESTIONS = <?= $max_count ?>;
    const CURRENT_QUESTIONS = <?= $current_count ?>;

    function addOption(content = '', isCorrect = false) {
        const container = document.getElementById('optionsContainer');
        const div = document.createElement('div');
        div.className = 'option-item';
        div.style.cssText = "margin-bottom: 10px; display: flex; gap: 10px; align-items: center;";
        div.innerHTML = `
        <input type="text" name="option[]" value="${escapeHtml(content)}" placeholder="Nhập nội dung đáp án..." required 
               style="flex: 1; padding: 8px; border: 1px solid #ddd; border-radius: 4px;">
        <label style="display: flex; align-items: center; gap: 5px; cursor: pointer; white-space: nowrap;">
            <input type="radio" name="is_correct_radio" ${isCorrect ? 'checked' : ''}> Đúng
        </label>
        <button type="button" onclick="this.parentElement.remove()" style="background: #e74c3c; color: white; border: none; padding: 8px 12px; border-radius: 4px; cursor: pointer;">Xóa</button>
    `;
        container.appendChild(div);
    }

    document.getElementById('questionForm').onsubmit = function(e) {
        const id_cauhoi = document.getElementById('edit_id_cauhoi').value;
        const noidung = document.getElementById('noidungcauhoi').value.trim();
        const optionsNodes = document.querySelectorAll('input[name="option[]"]');
        const optionsValues = Array.from(optionsNodes).map(input => input.value.trim());
        const radioSelected = document.querySelector('input[name="is_correct_radio"]:checked');

        // 1. Chặn vượt mức (Chỉ khi THÊM)
        if (!id_cauhoi && CURRENT_QUESTIONS >= MAX_QUESTIONS) {
            alert(`⚠️ Không thể thêm! Bài thi này đã đạt giới hạn tối đa ${MAX_QUESTIONS} câu hỏi.`);
            e.preventDefault();
            return;
        }

        // 2. Kiểm tra bỏ trống câu hỏi
        if (noidung === "") {
            alert("Nội dung câu hỏi không được để trống!");
            e.preventDefault();
            return;
        }

        // 3. Kiểm tra trùng câu hỏi trong danh sách hiện tại (Chỉ khi THÊM)
        if (!id_cauhoi) {
            const existingQuestions = Array.from(document.querySelectorAll('.q-content')).map(el => el.innerText.trim()
                .toLowerCase());
            if (existingQuestions.includes(noidung.toLowerCase())) {
                alert("⚠️ Câu hỏi này đã có trong bộ đề này rồi!");
                e.preventDefault();
                return;
            }
        }

        // 4. Kiểm tra đáp án
        if (optionsValues.length < 2) {
            alert("Cần ít nhất 2 đáp án!");
            e.preventDefault();
            return;
        }
        if (optionsValues.some(opt => opt === "")) {
            alert("Nội dung các đáp án không được để rỗng!");
            e.preventDefault();
            return;
        }

        // 5. Kiểm tra trùng đáp án trong cùng 1 câu
        const uniqueOptions = new Set(optionsValues.map(v => v.toLowerCase()));
        if (uniqueOptions.size !== optionsValues.length) {
            alert("⚠️ Các đáp án của một câu hỏi không được trùng nhau!");
            e.preventDefault();
            return;
        }

        // 6. Kiểm tra đã chọn đáp án đúng chưa
        if (!radioSelected) {
            alert("Vui lòng tích chọn một đáp án đúng!");
            e.preventDefault();
            return;
        }

        // Chuẩn bị index cho Controller
        const radios = document.querySelectorAll('input[name="is_correct_radio"]');
        let selectedIndex = -1;
        radios.forEach((radio, index) => {
            if (radio.checked) selectedIndex = index;
        });

        // Gán lại value và name để PHP nhận
        const finalInput = document.createElement('input');
        finalInput.type = 'hidden';
        finalInput.name = 'is_correct';
        finalInput.value = selectedIndex;
        this.appendChild(finalInput);
    };

    function openAddModal() {
        if (CURRENT_QUESTIONS >= MAX_QUESTIONS) {
            alert(`⚠️ Bài thi đã đủ ${CURRENT_QUESTIONS}/${MAX_QUESTIONS} câu. Không thể thêm tiếp!`);
            return;
        }
        document.getElementById('modalTitle').innerText = 'Thêm Câu Hỏi Mới';
        document.getElementById('questionForm').action = 'index.php?act=cauhoi-add';
        document.getElementById('noidungcauhoi').value = '';
        document.getElementById('edit_id_cauhoi').value = '';
        document.getElementById('optionsContainer').innerHTML = '';
        addOption();
        addOption(); // Mặc định 2 ô
        document.getElementById('questionModal').style.display = 'flex';
    }

    function openEditModal(data) {
        document.getElementById('modalTitle').innerText = 'Sửa Câu Hỏi';
        document.getElementById('questionForm').action = 'index.php?act=cauhoi-edit';
        document.getElementById('noidungcauhoi').value = data.noidungcauhoi;
        document.getElementById('dokho').value = data.dokho;
        document.getElementById('edit_id_cauhoi').value = data.id_cauhoi;
        document.getElementById('optionsContainer').innerHTML = '';
        data.dapan.forEach(d => addOption(d.noidungdapan, d.dapandung == 1));
        document.getElementById('questionModal').style.display = 'flex';
    }

    function closeModal() {
        document.getElementById('questionModal').style.display = 'none';
    }

    function escapeHtml(str) {
        if (!str) return '';
        return str.replace(/[&<>"']/g, m => ({
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#39;'
        } [m]));
    }

    // Đóng modal khi click ra ngoài
    window.onclick = function(event) {
        const modal = document.getElementById('questionModal');
        if (event.target == modal) closeModal();
    }
</script>