<div class="card" style="background: white; padding: 25px; border-radius: 12px; box-shadow: 0 4px 15px rgba(0,0,0,0.05); border: 2px solid #dee2e6;">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 25px;">
        <h2 style="margin: 0; color: #1a1a1a; font-weight: 700;">Qu?n lý Môn h?c</h2>
        <button onclick="openFormModal()"
            style="background: #3498db; color: white; padding: 12px 25px; border-radius: 8px; font-weight: 600; border: 1px solid #2980b9; cursor: pointer;">
            <i class="fas fa-plus"></i> Thêm môn h?c m?i
        </button>
    </div>

    <div id="monhocAlert"></div>

    <table style="width: 100%; border-collapse: collapse; border: 1px solid #ced4da;">
        <thead>
            <tr style="background: #f8f9fa; border-bottom: 2px solid #adb5bd; text-align: left;">
                <th style="padding: 15px; width: 60px; text-align: center; border-right: 1px solid #ced4da;">STT</th>
                <th style="padding: 15px; border-right: 1px solid #ced4da; width: 200px;">Tên môn h?c</th>
                <th style="padding: 15px; border-right: 1px solid #ced4da;">Miêu t?</th>
                <th style="padding: 15px; border-right: 1px solid #ced4da; text-align: center; width: 120px;">S? bài thi</th>
                <th style="padding: 15px; border-right: 1px solid #ced4da; width: 130px;">Ngày thêm</th>
                <th style="padding: 15px; width: 180px; text-align: center;">Thao tác</th>
            </tr>
        </thead>
        <tbody id="monhocTableBody">
            <tr><td colspan="6" style="padding: 40px; text-align: center; color: #999;">Ðang t?i d? li?u...</td></tr>
        </tbody>
    </table>
</div>

<div id="formModal"
    style="display: none; position: fixed; z-index: 10000; left: 0; top: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.6); align-items: center; justify-content: center; backdrop-filter: blur(4px);">
    <div style="background: white; padding: 35px; border-radius: 15px; width: 450px; box-shadow: 0 10px 30px rgba(0,0,0,0.3); position: relative;">
        <h2 id="formTitle" style="margin-top: 0; color: #333; font-weight: 700; border-bottom: 2px solid #eee; padding-bottom: 15px;">Thêm môn h?c m?i</h2>

        <form id="monhocForm">
            <input type="hidden" name="id_monhoc" id="form_id_monhoc">

            <div style="margin: 25px 0;">
                <label style="display: block; margin-bottom: 10px; font-weight: 600; color: #555;">Tên môn h?c</label>
                <input type="text" name="tenmonhoc" id="form_tenmonhoc" required
                    placeholder="VD: L?p trình Web, Co s? d? li?u..."
                    style="width: 100%; padding: 12px; border: 2px solid #ddd; border-radius: 8px; font-size: 1rem; box-sizing: border-box;">
            </div>
            <div style="margin: 25px 0;">
                <label style="display: block; margin-bottom: 10px; font-weight: 600; color: #555;">Miêu t? môn h?c (không b?t bu?c)</label>
                <textarea name="mieuta" id="form_mieuta" placeholder="Nh?p ghi chú ho?c miêu t? môn h?c..."
                    style="width: 100%; padding: 12px; border: 2px solid #ddd; border-radius: 8px; font-size: 1rem; box-sizing: border-box; min-height: 100px; font-family: inherit;"></textarea>
            </div>

            <div style="display: flex; gap: 10px; justify-content: flex-end; margin-top: 30px;">
                <button type="button" onclick="closeFormModal()"
                    style="background: #f1f1f1; color: #333; padding: 10px 20px; border-radius: 8px; border: none; font-weight: 600; cursor: pointer;">H?y</button>
                <button type="submit" id="formSubmitBtn"
                    style="background: #3498db; color: white; padding: 10px 25px; border-radius: 8px; border: none; font-weight: 600; cursor: pointer;">Luu l?i</button>
            </div>
        </form>
    </div>
</div>

<script>
let monhocItems = [];

function formatDate(dateStr) {
    if (!dateStr) return '---';
    const d = new Date(dateStr);
    return `${String(d.getDate()).padStart(2, '0')}/${String(d.getMonth() + 1).padStart(2, '0')}/${d.getFullYear()}`;
}

function escapeHtml(str) {
    return String(str ?? '').replace(/[&<>"']/g, m => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[m]));
}

function showAlert(message, type = 'success') {
    const box = document.getElementById('monhocAlert');
    const bg = type === 'success' ? '#d4edda' : '#f8d7da';
    const color = type === 'success' ? '#155724' : '#721c24';
    const border = type === 'success' ? '#c3e6cb' : '#f5c6cb';
    box.innerHTML = `<div style="background:${bg};color:${color};padding:15px;border-radius:8px;margin-bottom:20px;border:1px solid ${border};">${escapeHtml(message)}</div>`;
}

async function loadMonHoc() {
    const tbody = document.getElementById('monhocTableBody');
    tbody.innerHTML = '<tr><td colspan="6" style="padding:40px;text-align:center;color:#999;">Ðang t?i d? li?u...</td></tr>';

    try {
        const res = await fetch(serverApiUrl('monhoc/list'));
        const json = await res.json();
        if (!res.ok || !json.success) throw new Error(json.error || 'Không th? t?i môn h?c');

        monhocItems = json.data || [];
        if (!monhocItems.length) {
            tbody.innerHTML = '<tr><td colspan="6" style="padding:40px;text-align:center;color:#999;">Chua có môn h?c nào.</td></tr>';
            return;
        }

        tbody.innerHTML = monhocItems.map((mon, index) => `
            <tr style="border-bottom: 1px solid #ced4da;" onmouseover="this.style.backgroundColor='#f8f9fa'" onmouseout="this.style.backgroundColor='transparent'">
                <td style="padding: 15px; text-align: center; border-right: 1px solid #ced4da; font-weight: 600;">${index + 1}</td>
                <td style="padding: 15px; border-right: 1px solid #ced4da; font-weight: 500;">${escapeHtml(mon.tenmonhoc)}</td>
                <td style="padding: 15px; border-right: 1px solid #ced4da; font-size: 0.9rem; color: #666;">${mon.mieuta ? `<div style="max-width: 250px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;" title="${escapeHtml(mon.mieuta)}">${escapeHtml(mon.mieuta)}</div>` : '<span style="color:#ccc;font-style:italic;font-size:0.85rem;">(Chua có miêu t?)</span>'}</td>
                <td style="padding: 15px; border-right: 1px solid #ced4da; text-align: center;"><span style="background: #e1f5fe; color: #0288d1; padding: 4px 12px; border-radius: 20px; font-weight: bold; font-size: 0.85rem; border: 1px solid #b3e5fc;">${mon.so_bai_thi} bài thi</span></td>
                <td style="padding: 15px; border-right: 1px solid #ced4da; color: #555; font-size: 0.9rem;">${formatDate(mon.ngaythem)}</td>
                <td style="padding: 15px; text-align: center;">
                    <div style="display:flex;justify-content:center;gap:8px;">
                        <button onclick="openFormModal(${Number(mon.id_monhoc)}, ${JSON.stringify(mon.tenmonhoc)}, ${JSON.stringify(mon.mieuta || '')})" style="color:#856404;background:#fff3cd;border:1px solid #ffeeba;padding:6px 14px;border-radius:4px;font-size:0.85rem;font-weight:600;cursor:pointer;">S?a</button>
                        <button onclick="deleteMonHoc(${Number(mon.id_monhoc)}, ${JSON.stringify(mon.tenmonhoc)}, ${Number(mon.so_bai_thi)})" style="color:#721c24;background:#f8d7da;border:1px solid #f5c6cb;padding:6px 14px;border-radius:4px;font-size:0.85rem;font-weight:600;cursor:pointer;">Xóa</button>
                    </div>
                </td>
            </tr>
        `).join('');
    } catch (error) {
        tbody.innerHTML = `<tr><td colspan="6" style="padding:40px;text-align:center;color:#c0392b;">${escapeHtml(error.message)}</td></tr>`;
    }
}

function openFormModal(id = null, name = '', description = '') {
    const modal = document.getElementById('formModal');
    const title = document.getElementById('formTitle');
    const inputId = document.getElementById('form_id_monhoc');
    const inputName = document.getElementById('form_tenmonhoc');
    const inputDesc = document.getElementById('form_mieuta');
    const submitBtn = document.getElementById('formSubmitBtn');

    if (id) {
        title.innerText = 'Ch?nh s?a môn h?c';
        inputId.value = id;
        inputName.value = name;
        inputDesc.value = description;
        submitBtn.innerText = 'C?p nh?t ngay';
        submitBtn.style.background = '#f39c12';
    } else {
        title.innerText = 'Thêm môn h?c m?i';
        inputId.value = '';
        inputName.value = '';
        inputDesc.value = '';
        submitBtn.innerText = 'Luu môn h?c';
        submitBtn.style.background = '#3498db';
    }

    modal.style.display = 'flex';
    inputName.focus();
}

function closeFormModal() {
    document.getElementById('formModal').style.display = 'none';
}

async function deleteMonHoc(id, name, count) {
    if (count > 0) {
        showAlert(`Môn ${name} dang có ${count} bài thi. Hãy xóa bài thi tru?c.`, 'error');
        return;
    }

    if (!confirm(`Xóa môn ${name}? Thao tác này không th? hoàn tác.`)) return;

    try {
        const res = await fetch(serverApiUrl('monhoc/delete'), {
            method: 'DELETE',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id_monhoc: id })
        });
        const json = await res.json();
        if (!res.ok || !json.success) throw new Error(json.error || 'Xóa th?t b?i');
        showAlert(json.message, 'success');
        loadMonHoc();
    } catch (error) {
        showAlert(error.message, 'error');
    }
}

document.getElementById('monhocForm').addEventListener('submit', async function(e) {
    e.preventDefault();

    const payload = {
        id_monhoc: document.getElementById('form_id_monhoc').value || 0,
        tenmonhoc: document.getElementById('form_tenmonhoc').value,
        mieuta: document.getElementById('form_mieuta').value
    };

    try {
        const res = await fetch(serverApiUrl('monhoc/save'), {
            method: payload.id_monhoc && Number(payload.id_monhoc) > 0 ? 'PATCH' : 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const json = await res.json();
        if (!res.ok || !json.success) throw new Error(json.error || 'Luu th?t b?i');
        closeFormModal();
        showAlert(json.message, 'success');
        loadMonHoc();
    } catch (error) {
        showAlert(error.message, 'error');
    }
});

window.addEventListener('click', (event) => {
    if (event.target === document.getElementById('formModal')) {
        closeFormModal();
    }
});

loadMonHoc();
</script>




