<div id="baithiAlert"></div>

<div class="card" style="background: white; padding: 25px; border-radius: 12px; border: 1px solid #dee2e6; box-shadow: 0 2px 4px rgba(0,0,0,0.05);">
    <div style="display: flex; justify-content: space-between; margin-bottom: 20px;">
        <h2 style="margin: 0; color: #333;">Qu?n lý Bài thi</h2>
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
                <th style="padding: 12px; text-align: left;">Môn h?c</th>
                <th style="padding: 12px; text-align: center;">S? câu</th>
                <th style="padding: 12px; text-align: center;">Th?i gian làm</th>
                <th style="padding: 12px; text-align: center;">Câu h?i</th>
                <th style="padding: 12px; text-align: center;">Thao tác</th>
            </tr>
        </thead>
        <tbody id="baithiTableBody">
            <tr><td colspan="7" style="text-align:center;padding:20px;">Ðang t?i d? li?u...</td></tr>
        </tbody>
    </table>
</div>

<div id="examModal" style="display: none; position: fixed; z-index: 9999; left: 0; top: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.6); align-items: center; justify-content: center;">
    <div style="background: white; padding: 30px; border-radius: 10px; width: 650px;">
        <h3 id="modalTitle" style="margin-top: 0; border-bottom: 2px solid #eee; padding-bottom: 15px;">Thêm Bài Thi</h3>
        <form id="examForm">
            <input type="hidden" name="id_baithi" id="m_id">
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-top: 15px;">
                <div style="grid-column: span 2;">
                    <label style="font-weight: 600;">Tên bài thi</label>
                    <input type="text" name="ten_baithi" id="m_ten" required placeholder="Nh?p tên bài thi..." style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 6px;">
                </div>
                <div>
                    <label style="font-weight: 600;">Môn h?c</label>
                    <select name="id_monhoc" id="m_mon" style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 6px;"></select>
                </div>
                <div>
                    <label style="font-weight: 600;">Tr?ng thái</label>
                    <select name="trangthai" id="m_status" style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 6px;">
                        <option value="Ðang m?">Ðang m?</option>
                        <option value="Ðóng">Ðóng</option>
                    </select>
                </div>
                <div>
                    <label style="font-weight: 600;">S? câu (= 5)</label>
                    <input type="number" name="tongcauhoi" id="m_cau" required min="5" style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 6px;">
                </div>
                <div>
                    <label style="font-weight: 600;">Th?i gian (phút)</label>
                    <input type="number" name="thoigianlam" id="m_time" required min="2" style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 6px;">
                </div>
                <div>
                    <label style="font-weight: 600;">B?t d?u</label>
                    <input type="datetime-local" name="thoigianbatdau" id="m_start" required onchange="updateEndMin()" style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 6px;">
                </div>
                <div>
                    <label style="font-weight: 600;">K?t thúc</label>
                    <input type="datetime-local" name="thoigianketthuc" id="m_end" style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 6px;">
                </div>
                <div style="grid-column: span 2;">
                    <label style="font-weight: 600;">Miêu t?</label>
                    <textarea name="mieuta" id="m_mieuta" rows="3" style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 6px;"></textarea>
                </div>
            </div>
            <div style="margin-top: 30px; text-align: right;">
                <button type="button" onclick="closeExamModal()" style="padding: 10px 25px; border-radius: 6px; border: 1px solid #ccc; cursor: pointer;">H?y</button>
                <button type="submit" style="background: #27ae60; color: white; border: none; padding: 10px 30px; border-radius: 6px; cursor: pointer; margin-left: 10px;">Luu</button>
            </div>
        </form>
    </div>
</div>

<script>
let examItems = [];
let subjectItems = [];

function escapeHtml(str) {
    return String(str ?? '').replace(/[&<>"']/g, m => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[m]));
}

function showExamAlert(message, type = 'success') {
    const box = document.getElementById('baithiAlert');
    const bg = type === 'success' ? '#d4edda' : '#f8d7da';
    const color = type === 'success' ? '#155724' : '#721c24';
    const border = type === 'success' ? '#c3e6cb' : '#f5c6cb';
    box.innerHTML = `<div style="background:${bg};color:${color};padding:15px;border-radius:8px;margin-bottom:20px;border:1px solid ${border};">${escapeHtml(message)}</div>`;
}

function getLocalDateTimeString(date) {
    const tzOffset = date.getTimezoneOffset() * 60000;
    return (new Date(date - tzOffset)).toISOString().slice(0, 16);
}

function updateEndMin() {
    const startVal = document.getElementById('m_start').value;
    if (startVal) document.getElementById('m_end').min = startVal;
}

function renderSubjectOptions(selectedId = '') {
    const select = document.getElementById('m_mon');
    select.innerHTML = subjectItems.map(mh => `<option value="${mh.id_monhoc}" ${String(mh.id_monhoc) === String(selectedId) ? 'selected' : ''}>${escapeHtml(mh.tenmonhoc)}</option>`).join('');
}

async function loadExamData() {
    const tbody = document.getElementById('baithiTableBody');
    tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;padding:20px;">Ðang t?i d? li?u...</td></tr>';

    try {
        const res = await fetch(serverApiUrl('baithi/list'));
        const json = await res.json();
        if (!res.ok || !json.success) throw new Error(json.error || 'Không th? t?i bài thi');

        examItems = json.data || [];
        subjectItems = json.subjects || [];
        renderSubjectOptions();

        if (!examItems.length) {
            tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;padding:20px;">Chua có d? li?u.</td></tr>';
            return;
        }

        tbody.innerHTML = examItems.map((bt, index) => `
            <tr style="border-bottom:1px solid #eee;">
                <td style="padding:12px;text-align:center;color:#666;">${index + 1}</td>
                <td style="padding:12px;"><strong>${escapeHtml(bt.ten_baithi)}</strong></td>
                <td style="padding:12px;">${escapeHtml(bt.tenmonhoc)}</td>
                <td style="padding:12px;text-align:center;"><span style="background:#e1f5fe;color:#0288d1;padding:4px 10px;border-radius:12px;font-size:12px;font-weight:600;">${bt.tongcauhoi} câu</span></td>
                <td style="padding:12px;text-align:center;"><i class="far fa-clock"></i> ${bt.thoigianlam} phút</td>
                <td style="padding:12px;text-align:center;"><a href="index.php?act=cauhoi-list&id_baithi=${bt.id_baithi}" style="background:#3498db;color:white;padding:6px 12px;border-radius:4px;text-decoration:none;display:inline-block;"> <i class="fas fa-list"></i> Qu?n lý câu h?i</a></td>
                <td style="padding:12px;text-align:center;">
                    <button onclick='openExamModal(${JSON.stringify(bt)})' style="background:#f39c12;color:white;border:none;padding:6px 12px;border-radius:4px;cursor:pointer;"> <i class="fas fa-edit"></i> S?a</button>
                    <button onclick="deleteExam(${Number(bt.id_baithi)})" style="background:#e74c3c;color:white;border:none;padding:6px 12px;border-radius:4px;cursor:pointer;"> <i class="fas fa-trash"></i> Xóa</button>
                </td>
            </tr>
        `).join('');
    } catch (error) {
        tbody.innerHTML = `<tr><td colspan="7" style="text-align:center;padding:20px;color:#c0392b;">${escapeHtml(error.message)}</td></tr>`;
    }
}

function validateExamForm() {
    const tenVal = document.getElementById('m_ten').value.trim();
    if (tenVal.length < 5) {
        showExamAlert('Tên bài thi ph?i ít nh?t 5 ký t?', 'error');
        return false;
    }

    const startStr = document.getElementById('m_start').value;
    const endStr = document.getElementById('m_end').value;
    if (endStr && new Date(endStr) <= new Date(startStr)) {
        showExamAlert('Ngày k?t thúc ph?i sau ngày b?t d?u', 'error');
        return false;
    }

    return true;
}

function openExamModal(data = null) {
    const modal = document.getElementById('examModal');
    const startInput = document.getElementById('m_start');
    const endInput = document.getElementById('m_end');
    const now = new Date();
    const currentStr = getLocalDateTimeString(now);

    if (data) {
        document.getElementById('modalTitle').innerText = 'C?p nh?t Bài Thi';
        document.getElementById('m_id').value = data.id_baithi;
        document.getElementById('m_ten').value = data.ten_baithi;
        renderSubjectOptions(data.id_monhoc);
        document.getElementById('m_cau').value = data.tongcauhoi;
        document.getElementById('m_time').value = data.thoigianlam;
        document.getElementById('m_status').value = data.trangthai || 'Ðang m?';
        document.getElementById('m_mieuta').value = data.mieuta || '';
        startInput.value = (data.thoigianbatdau || '').replace(' ', 'T').substring(0, 16);
        endInput.value = data.thoigianketthuc ? data.thoigianketthuc.replace(' ', 'T').substring(0, 16) : '';
        startInput.removeAttribute('min');
        endInput.min = startInput.value;
    } else {
        document.getElementById('modalTitle').innerText = 'Thêm Bài Thi M?i';
        document.getElementById('examForm').reset();
        document.getElementById('m_id').value = '';
        document.getElementById('m_cau').value = '10';
        document.getElementById('m_time').value = '15';
        document.getElementById('m_status').value = 'Ðang m?';
        renderSubjectOptions();
        startInput.value = currentStr;
        startInput.min = currentStr;
        endInput.value = '';
        endInput.min = currentStr;
    }

    modal.style.display = 'flex';
}

function closeExamModal() {
    document.getElementById('examModal').style.display = 'none';
}

async function deleteExam(id) {
    if (!confirm('Xóa bài thi này?')) return;

    try {
        const res = await fetch(serverApiUrl('baithi/delete'), {
            method: 'DELETE',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id_baithi: id })
        });
        const json = await res.json();
        if (!res.ok || !json.success) throw new Error(json.error || 'Xóa th?t b?i');
        showExamAlert(json.message, 'success');
        loadExamData();
    } catch (error) {
        showExamAlert(error.message, 'error');
    }
}

document.getElementById('examForm').addEventListener('submit', async function(e) {
    e.preventDefault();
    if (!validateExamForm()) return;

    const payload = Object.fromEntries(new FormData(this).entries());

    try {
        const res = await fetch(serverApiUrl('baithi/save'), {
            method: payload.id_baithi && Number(payload.id_baithi) > 0 ? 'PATCH' : 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const json = await res.json();
        if (!res.ok || !json.success) throw new Error(json.error || 'Luu th?t b?i');
        closeExamModal();
        showExamAlert(json.message, 'success');
        loadExamData();
    } catch (error) {
        showExamAlert(error.message, 'error');
    }
});

window.addEventListener('click', (event) => {
    if (event.target === document.getElementById('examModal')) {
        closeExamModal();
    }
});

loadExamData();
</script>




