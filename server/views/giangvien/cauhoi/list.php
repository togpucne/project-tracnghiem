<?php $id_baithi = (int) ($_GET['id_baithi'] ?? 0); ?>

<div id="questionAlert"></div>

<div class="container" style="max-width: 1200px; margin: 0 auto; padding: 20px;">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
        <div>
            <h2 style="margin: 0;" id="questionExamTitle">Đang tải...</h2>
            <p style="color: #666; margin-top: 5px;" id="questionMeta">Đang tải thông tin bài thi...</p>
        </div>
        <div>
            <a href="index.php?act=quanly-dethi" style="background: #6c757d; color: white; padding: 8px 15px; border-radius: 6px; text-decoration: none; margin-right: 10px;">Quay lại</a>
            <button id="addQuestionBtn" onclick="openAddModal()" style="background: #27ae60; color: white; padding: 8px 20px; border-radius: 6px; border: none; cursor: pointer;">Thêm câu hỏi</button>
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
            <tbody id="questionTableBody">
                <tr><td colspan="5" style="text-align:center;padding:40px;color:#999;">Đang tải dữ liệu...</td></tr>
            </tbody>
        </table>
    </div>
</div>

<div id="questionModal" style="display: none; position: fixed; z-index: 9999; left: 0; top: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.6); align-items: center; justify-content: center;">
    <div style="background: white; padding: 30px; border-radius: 10px; width: 700px; max-height: 90%; overflow-y: auto;">
        <h3 id="modalTitle" style="margin-top: 0;">Thêm Câu Hỏi Mới</h3>
        <form id="questionForm">
            <input type="hidden" name="id_baithi" value="<?= $id_baithi ?>">
            <input type="hidden" name="id_cauhoi" id="edit_id_cauhoi">

            <div style="margin-bottom: 15px;">
                <label style="display: block; margin-bottom: 5px; font-weight: bold;">Nội dung câu hỏi:</label>
                <textarea name="noidungcauhoi" id="noidungcauhoi" rows="3" style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 6px; box-sizing: border-box;"></textarea>
            </div>

            <div style="margin-bottom: 15px;">
                <label style="display: block; margin-bottom: 5px; font-weight: bold;">Độ khó:</label>
                <select name="dokho" id="dokho" style="width: 200px; padding: 8px; border: 1px solid #ddd; border-radius: 6px;">
                    <option value="Dễ">Dễ</option>
                    <option value="Trung bình">Trung bình</option>
                    <option value="Khó">Khó</option>
                </select>
            </div>

            <div style="margin-bottom: 15px;">
                <label style="font-weight: bold;">Đáp án:</label>
                <div id="optionsContainer" style="margin-top: 10px;"></div>
                <button type="button" onclick="addOption()" style="background: #3498db; color: white; border: none; padding: 8px 15px; border-radius: 4px; margin-top: 10px; cursor: pointer;">+ Thêm đáp án</button>
            </div>

            <div style="margin-top: 20px; text-align: right; border-top: 1px solid #eee; padding-top: 15px;">
                <button type="button" onclick="closeModal()" style="padding: 8px 20px; border: 1px solid #ccc; border-radius: 4px; cursor: pointer; background: white;">Hủy</button>
                <button type="submit" style="background: #27ae60; color: white; border: none; padding: 8px 25px; border-radius: 4px; margin-left: 10px; cursor: pointer;">Lưu câu hỏi</button>
            </div>
        </form>
    </div>
</div>

<script>
const examId = <?= $id_baithi ?>;
let questionItems = [];
let examInfo = null;
let maxQuestions = 0;
let examLocked = false;

function escapeHtml(str) {
    return String(str ?? '').replace(/[&<>"']/g, m => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[m]));
}

function showQuestionAlert(message, type = 'success') {
    const box = document.getElementById('questionAlert');
    const bg = type === 'success' ? '#d4edda' : '#f8d7da';
    const color = type === 'success' ? '#155724' : '#721c24';
    box.innerHTML = `<div style="background:${bg};color:${color};padding:15px;border-radius:8px;margin-bottom:20px;">${escapeHtml(message)}</div>`;
}

function addOption(content = '', isCorrect = false) {
    const container = document.getElementById('optionsContainer');
    const div = document.createElement('div');
    div.className = 'option-item';
    div.style.cssText = 'margin-bottom:10px;display:flex;gap:10px;align-items:center;';
    div.innerHTML = `
        <input type="text" name="option[]" value="${escapeHtml(content)}" placeholder="Nhập nội dung đáp án..." required style="flex:1;padding:8px;border:1px solid #ddd;border-radius:4px;">
        <label style="display:flex;align-items:center;gap:5px;cursor:pointer;white-space:nowrap;">
            <input type="radio" name="is_correct_radio" ${isCorrect ? 'checked' : ''}> Đúng
        </label>
        <button type="button" onclick="this.parentElement.remove()" style="background:#e74c3c;color:white;border:none;padding:8px 12px;border-radius:4px;cursor:pointer;">Xóa</button>
    `;
    container.appendChild(div);
}

function renderQuestionActions(question) {
    if (examLocked) {
        return '<span style="color:#6c757d;font-size:13px;font-weight:600;">Chỉ xem</span>';
    }

    return `
        <button class="btn-edit-question" data-id="${Number(question.id_cauhoi)}" style="background:#f39c12;color:white;border:none;padding:5px 10px;border-radius:4px;cursor:pointer;">Sửa</button>
        <button class="btn-delete-question" data-id="${Number(question.id_cauhoi)}" style="background:#e74c3c;color:white;border:none;padding:5px 10px;border-radius:4px;cursor:pointer;margin-left:5px;">Xóa</button>
    `;
}

async function loadQuestions() {
    const tbody = document.getElementById('questionTableBody');
    tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;padding:40px;color:#999;">Đang tải dữ liệu...</td></tr>';

    try {
        const res = await fetch(serverApiUrl('cauhoi/list', { id_baithi: examId }));
        const json = await res.json();
        if (!res.ok || !json.success) throw new Error(json.error || 'Không thể tải câu hỏi');

        examInfo = json.baithi;
        examLocked = !!json.is_locked;
        questionItems = json.questions || [];
        maxQuestions = Number(examInfo.tongcauhoi || 0);
        document.getElementById('questionExamTitle').innerText = examInfo.ten_baithi || 'Quản lý câu hỏi';
        document.getElementById('questionMeta').innerHTML = `Môn: ${escapeHtml(examInfo.tenmonhoc || '')} | Số câu hiện có: <strong id="displayCount">${questionItems.length}</strong>/${maxQuestions} | Xáo trộn: <strong>${Number(examInfo.xao_tron) === 1 ? 'Câu hỏi và đáp án' : 'Không'}</strong>${examLocked ? ' | <span style="color:#c0392b;font-weight:700;">Chế độ chỉ xem vì đã có thí sinh làm bài</span>' : ''}`;
        document.getElementById('addQuestionBtn').disabled = examLocked;
        document.getElementById('addQuestionBtn').style.opacity = examLocked ? '0.6' : '1';
        document.getElementById('addQuestionBtn').style.cursor = examLocked ? 'not-allowed' : 'pointer';

        if (!questionItems.length) {
            tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;padding:40px;color:#999;">Chưa có câu hỏi nào.</td></tr>';
            return;
        }

        tbody.innerHTML = questionItems.map((ch, index) => `
            <tr style="border-bottom:1px solid #eee;" class="question-row">
                <td style="padding:12px;text-align:center;">${index + 1}</td>
                <td style="padding:12px;"><strong class="q-content">${escapeHtml(ch.noidungcauhoi)}</strong></td>
                <td style="padding:12px;">${(ch.dapan || []).map(d => `<div style="margin:5px 0;display:flex;align-items:center;"><input type="checkbox" ${Number(d.dapandung) === 1 ? 'checked' : ''} disabled style="margin-right:8px;"><span style="${Number(d.dapandung) === 1 ? 'color:#27ae60;font-weight:bold;' : 'color:#666;'}">${escapeHtml(d.noidungdapan)}</span></div>`).join('')}</td>
                <td style="padding:12px;text-align:center;">${escapeHtml(ch.dokho)}</td>
                <td style="padding:12px;text-align:center;">
                    ${renderQuestionActions(ch)}
                </td>
            </tr>
        `).join('');
    } catch (error) {
        tbody.innerHTML = `<tr><td colspan="5" style="text-align:center;padding:40px;color:#c0392b;">${escapeHtml(error.message)}</td></tr>`;
    }
}

function openAddModal() {
    if (examLocked) {
        showQuestionAlert('Bài thi đã có thí sinh làm, không được phép thêm câu hỏi.', 'error');
        return;
    }
    if (questionItems.length >= maxQuestions) {
        showQuestionAlert(`Bài thi đã đủ ${questionItems.length}/${maxQuestions} câu.`, 'error');
        return;
    }
    document.getElementById('modalTitle').innerText = 'Thêm Câu Hỏi Mới';
    document.getElementById('questionForm').reset();
    document.getElementById('edit_id_cauhoi').value = '';
    document.getElementById('optionsContainer').innerHTML = '';
    addOption();
    addOption();
    document.getElementById('questionModal').style.display = 'flex';
}

function openEditModal(data) {
    if (examLocked) {
        showQuestionAlert('Bài thi đã có thí sinh làm, không được phép sửa câu hỏi.', 'error');
        return;
    }
    document.getElementById('modalTitle').innerText = 'Sửa Câu Hỏi';
    document.getElementById('noidungcauhoi').value = data.noidungcauhoi;
    document.getElementById('dokho').value = data.dokho;
    document.getElementById('edit_id_cauhoi').value = data.id_cauhoi;
    document.getElementById('optionsContainer').innerHTML = '';
    (data.dapan || []).forEach(d => addOption(d.noidungdapan, Number(d.dapandung) === 1));
    document.getElementById('questionModal').style.display = 'flex';
}

function closeModal() {
    document.getElementById('questionModal').style.display = 'none';
}

async function deleteQuestion(id) {
    if (examLocked) {
        showQuestionAlert('Bài thi đã có thí sinh làm, không được phép xóa câu hỏi.', 'error');
        return;
    }
    if (!confirm('Xóa câu hỏi này?')) return;

    try {
        const res = await fetch(serverApiUrl('cauhoi/delete'), {
            method: 'DELETE',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id_cauhoi: id })
        });
        const json = await res.json();
        if (!res.ok || !json.success) throw new Error(json.error || 'Xóa thất bại');
        showQuestionAlert(json.message, 'success');
        loadQuestions();
    } catch (error) {
        showQuestionAlert(error.message, 'error');
    }
}

document.getElementById('questionTableBody').addEventListener('click', function(event) {
    const editButton = event.target.closest('.btn-edit-question');
    if (editButton) {
        const question = questionItems.find(item => Number(item.id_cauhoi) === Number(editButton.dataset.id));
        if (question) {
            openEditModal(question);
        }
        return;
    }

    const deleteButton = event.target.closest('.btn-delete-question');
    if (deleteButton) {
        deleteQuestion(Number(deleteButton.dataset.id || 0));
    }
});

document.getElementById('questionForm').addEventListener('submit', async function(e) {
    e.preventDefault();

    const id_cauhoi = document.getElementById('edit_id_cauhoi').value;
    const noidung = document.getElementById('noidungcauhoi').value.trim();
    const optionsNodes = document.querySelectorAll('input[name="option[]"]');
    const optionsValues = Array.from(optionsNodes).map(input => input.value.trim());
    const radios = document.querySelectorAll('input[name="is_correct_radio"]');
    let selectedIndex = -1;
    radios.forEach((radio, index) => { if (radio.checked) selectedIndex = index; });

    if (!id_cauhoi && questionItems.length >= maxQuestions) {
        showQuestionAlert(`Bài thi đã đạt giới hạn tối đa ${maxQuestions} câu.`, 'error');
        return;
    }
    if (noidung === '') {
        showQuestionAlert('Nội dung câu hỏi không được để trống', 'error');
        return;
    }
    if (optionsValues.length < 2 || optionsValues.some(opt => opt === '')) {
        showQuestionAlert('Cần ít nhất 2 đáp án hợp lệ', 'error');
        return;
    }
    if (new Set(optionsValues.map(v => v.toLowerCase())).size !== optionsValues.length) {
        showQuestionAlert('Các đáp án của một câu hỏi không được trùng nhau', 'error');
        return;
    }
    if (selectedIndex < 0) {
        showQuestionAlert('Vui lòng chọn một đáp án đúng', 'error');
        return;
    }

    const payload = {
        id_baithi: examId,
        id_cauhoi: id_cauhoi || 0,
        noidungcauhoi: noidung,
        dokho: document.getElementById('dokho').value,
        options: optionsValues,
        correct_index: selectedIndex
    };

    try {
        const res = await fetch(serverApiUrl('cauhoi/save'), {
            method: payload.id_cauhoi && Number(payload.id_cauhoi) > 0 ? 'PATCH' : 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const json = await res.json();
        if (!res.ok || !json.success) throw new Error(json.error || 'Lưu thất bại');
        closeModal();
        showQuestionAlert(json.message, 'success');
        loadQuestions();
    } catch (error) {
        showQuestionAlert(error.message, 'error');
    }
});

window.addEventListener('click', (event) => {
    if (event.target === document.getElementById('questionModal')) {
        closeModal();
    }
});

loadQuestions();
</script>
