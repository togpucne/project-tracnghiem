<div id="bankAlert"></div>

<div style="display:flex;justify-content:space-between;align-items:center;gap:16px;flex-wrap:wrap;margin-bottom:24px;">
    <div>
        <h2 style="margin:0;color:#0f172a;">Ngân hàng câu hỏi</h2>
        <p style="margin:6px 0 0;color:#64748b;">Tạo ngân hàng, gắn môn học và quản lý câu hỏi theo độ khó bằng API nội bộ.</p>
    </div>
    <div style="display:flex;gap:10px;flex-wrap:wrap;">
        <button type="button" class="btn btn-outline-primary" onclick="openBankModal()">
            <i class="fas fa-folder-plus me-2"></i>Thêm ngân hàng
        </button>
        <button type="button" class="btn btn-primary" id="openQuestionBtn" onclick="openQuestionModal()" disabled>
            <i class="fas fa-circle-plus me-2"></i>Thêm câu hỏi
        </button>
    </div>
</div>

<div class="row g-4">
    <div class="col-lg-4">
        <div class="card border-0 shadow-sm" style="border-radius:18px;">
            <div class="card-body">
                <div style="display:flex;justify-content:space-between;align-items:center;gap:12px;margin-bottom:14px;">
                    <div>
                        <h5 style="margin:0;">Danh sách ngân hàng</h5>
                        <small class="text-muted" id="bankCountLabel">Đang tải...</small>
                    </div>
                </div>
                <div id="bankList" style="display:flex;flex-direction:column;gap:12px;">
                    <div class="text-muted">Đang tải ngân hàng câu hỏi...</div>
                </div>
            </div>
        </div>
    </div>

    <div class="col-lg-8">
        <div class="card border-0 shadow-sm" style="border-radius:18px;">
            <div class="card-body">
                <div style="display:flex;justify-content:space-between;align-items:center;gap:12px;flex-wrap:wrap;margin-bottom:18px;">
                    <div>
                        <h4 style="margin:0;color:#0f172a;" id="questionPanelTitle">Chọn một ngân hàng câu hỏi</h4>
                        <p style="margin:6px 0 0;color:#64748b;" id="questionPanelMeta">Bạn có thể quản lý câu hỏi theo từng môn học trong ngân hàng.</p>
                    </div>
                    <div style="min-width:220px;">
                        <select id="subjectFilter" class="form-select" disabled></select>
                    </div>
                </div>

                <div class="table-responsive">
                    <table class="table align-middle" style="margin-bottom:0;">
                        <thead>
                            <tr>
                                <th>STT</th>
                                <th>Nội dung câu hỏi</th>
                                <th>Đáp án</th>
                                <th>Độ khó</th>
                                <th>Trạng thái</th>
                                <th class="text-end">Thao tác</th>
                            </tr>
                        </thead>
                        <tbody id="bankQuestionTableBody">
                            <tr>
                                <td colspan="6" class="text-center text-muted py-4">Chưa có dữ liệu để hiển thị.</td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>

<div id="bankModal" style="display:none;position:fixed;z-index:10000;inset:0;background:rgba(15,23,42,0.55);align-items:center;justify-content:center;padding:24px;backdrop-filter:blur(2px);">
    <div style="width:100%;max-width:720px;background:#fff;border-radius:18px;box-shadow:0 24px 80px rgba(15,23,42,0.28);overflow:hidden;">
        <form id="bankForm">
            <div style="display:flex;justify-content:space-between;align-items:flex-start;gap:16px;padding:22px 24px 0;">
                <div>
                    <h4 id="bankModalTitle" style="margin:0;">Thêm ngân hàng câu hỏi</h4>
                    <small class="text-muted">Một ngân hàng có thể chứa nhiều môn học và nhiều câu hỏi theo từng môn.</small>
                </div>
                <button type="button" class="btn-close" onclick="closeBankModal()" aria-label="Close"></button>
            </div>
            <div style="padding:18px 24px 8px;">
                <input type="hidden" id="bankId">
                <div class="row g-3">
                    <div class="col-md-6">
                        <label class="form-label fw-semibold">Tên ngân hàng</label>
                        <input type="text" class="form-control" id="bankName" required>
                    </div>
                    <div class="col-md-6">
                        <label class="form-label fw-semibold">Trạng thái</label>
                        <select class="form-select" id="bankStatus">
                            <option value="active">Đang hoạt động</option>
                            <option value="inactive">Đã khóa</option>
                        </select>
                    </div>
                    <div class="col-12">
                        <label class="form-label fw-semibold">Mô tả</label>
                        <textarea class="form-control" id="bankDescription" rows="3"></textarea>
                    </div>
                    <div class="col-12">
                        <label class="form-label fw-semibold">Môn học thuộc ngân hàng</label>
                        <div id="bankSubjectList" style="display:grid;grid-template-columns:repeat(auto-fit,minmax(180px,1fr));gap:10px;"></div>
                    </div>
                </div>
            </div>
            <div style="display:flex;justify-content:flex-end;gap:12px;padding:8px 24px 24px;">
                <button type="button" class="btn btn-light" onclick="closeBankModal()">Đóng</button>
                <button type="submit" class="btn btn-primary">
                    <i class="fas fa-save me-2"></i>Lưu ngân hàng
                </button>
            </div>
        </form>
    </div>
</div>

<div id="questionModalBank" style="display:none;position:fixed;z-index:10000;inset:0;background:rgba(15,23,42,0.55);align-items:center;justify-content:center;padding:24px;backdrop-filter:blur(2px);">
    <div style="width:100%;max-width:820px;background:#fff;border-radius:18px;box-shadow:0 24px 80px rgba(15,23,42,0.28);overflow:hidden;max-height:90vh;overflow-y:auto;">
        <form id="bankQuestionForm">
            <div style="display:flex;justify-content:space-between;align-items:flex-start;gap:16px;padding:22px 24px 0;">
                <div>
                    <h4 id="bankQuestionModalTitle" style="margin:0;">Thêm câu hỏi ngân hàng</h4>
                    <small class="text-muted">Câu hỏi sẽ được gắn vào môn học đang chọn trong ngân hàng.</small>
                </div>
                <button type="button" class="btn-close" onclick="closeQuestionModal()" aria-label="Close"></button>
            </div>
            <div style="padding:18px 24px 8px;">
                <input type="hidden" id="bankQuestionId">
                <div class="row g-3">
                    <div class="col-md-6">
                        <label class="form-label fw-semibold">Môn học</label>
                        <select class="form-select" id="bankQuestionSubject" required></select>
                    </div>
                    <div class="col-md-3">
                        <label class="form-label fw-semibold">Độ khó</label>
                        <select class="form-select" id="bankQuestionDifficulty">
                            <option value="de">Dễ</option>
                            <option value="trungbinh">Trung bình</option>
                            <option value="kho">Khó</option>
                        </select>
                    </div>
                    <div class="col-md-3">
                        <label class="form-label fw-semibold">Trạng thái</label>
                        <select class="form-select" id="bankQuestionStatus">
                            <option value="active">Đang hoạt động</option>
                            <option value="inactive">Đã khóa</option>
                        </select>
                    </div>
                    <div class="col-12">
                        <label class="form-label fw-semibold">Nội dung câu hỏi</label>
                        <textarea class="form-control" id="bankQuestionContent" rows="4" required></textarea>
                    </div>
                    <div class="col-12">
                        <div style="display:flex;justify-content:space-between;align-items:center;gap:12px;flex-wrap:wrap;">
                            <label class="form-label fw-semibold mb-0">Đáp án</label>
                            <button type="button" class="btn btn-outline-primary btn-sm" onclick="addBankAnswerOption()">
                                <i class="fas fa-plus me-1"></i>Thêm đáp án
                            </button>
                        </div>
                        <div id="bankAnswerOptions" style="display:flex;flex-direction:column;gap:10px;margin-top:12px;"></div>
                    </div>
                </div>
            </div>
            <div style="display:flex;justify-content:flex-end;gap:12px;padding:8px 24px 24px;">
                <button type="button" class="btn btn-light" onclick="closeQuestionModal()">Đóng</button>
                <button type="submit" class="btn btn-primary">
                    <i class="fas fa-save me-2"></i>Lưu câu hỏi
                </button>
            </div>
        </form>
    </div>
</div>

<script>
let bankItems = [];
let subjectItems = [];
let selectedBank = null;
let bankQuestions = [];

function escapeHtml(value) {
    return String(value ?? '')
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#039;');
}

function showBankAlert(message, type = 'success') {
    document.getElementById('bankAlert').innerHTML = `
        <div class="alert alert-${type} alert-dismissible fade show border-0 shadow-sm" role="alert" style="border-radius:14px;">
            ${escapeHtml(message)}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    `;
}

function difficultyLabel(value) {
    if (value === 'kho') return 'Khó';
    if (value === 'trungbinh') return 'Trung bình';
    return 'Dễ';
}

function statusLabel(value) {
    return value === 'inactive' ? 'Đã khóa' : 'Đang hoạt động';
}

function renderSubjectCheckboxes(selectedIds = []) {
    const box = document.getElementById('bankSubjectList');
    if (!subjectItems.length) {
        box.innerHTML = '<div class="text-muted">Chưa có môn học khả dụng.</div>';
        return;
    }

    box.innerHTML = subjectItems.map((subject) => `
        <label style="display:flex;align-items:flex-start;gap:10px;padding:12px;border:1px solid #e2e8f0;border-radius:12px;background:#f8fafc;cursor:pointer;">
            <input type="checkbox" class="bank-subject-checkbox" value="${Number(subject.id_monhoc)}" ${selectedIds.includes(Number(subject.id_monhoc)) ? 'checked' : ''}>
            <span>
                <strong style="display:block;color:#0f172a;">${escapeHtml(subject.tenmonhoc)}</strong>
                <small class="text-muted">${escapeHtml(subject.ten || '')}</small>
            </span>
        </label>
    `).join('');
}

function renderBankList() {
    const listEl = document.getElementById('bankList');
    document.getElementById('bankCountLabel').textContent = `${bankItems.length} ngân hàng`;

    if (!bankItems.length) {
        listEl.innerHTML = '<div class="text-muted">Chưa có ngân hàng câu hỏi nào.</div>';
        return;
    }

    listEl.innerHTML = bankItems.map((bank) => {
        const isActive = Number(selectedBank?.id_nganhang || 0) === Number(bank.id_nganhang);
        return `
            <div class="bank-item" data-id="${Number(bank.id_nganhang)}" style="padding:16px;border:1px solid ${isActive ? '#bfdbfe' : '#e2e8f0'};border-radius:14px;background:${isActive ? '#eff6ff' : '#fff'};cursor:pointer;">
                <div style="display:flex;justify-content:space-between;gap:12px;align-items:flex-start;">
                    <div>
                        <h6 style="margin:0 0 6px;color:#0f172a;">${escapeHtml(bank.ten_nganhang)}</h6>
                        <div class="text-muted" style="font-size:13px;">${escapeHtml(bank.ds_monhoc || 'Chưa gắn môn học')}</div>
                    </div>
                    <span class="badge ${bank.trangthai === 'inactive' ? 'text-bg-secondary' : 'text-bg-success'}">${escapeHtml(statusLabel(bank.trangthai))}</span>
                </div>
                <div style="display:flex;gap:12px;flex-wrap:wrap;margin-top:12px;font-size:13px;color:#475569;">
                    <span><i class="fas fa-book me-1"></i>${Number(bank.so_monhoc || 0)} môn</span>
                    <span><i class="fas fa-circle-question me-1"></i>${Number(bank.so_cauhoi || 0)} câu hỏi</span>
                </div>
                <div style="display:flex;justify-content:flex-end;gap:8px;margin-top:12px;">
                    <button type="button" class="btn btn-sm btn-outline-primary btn-edit-bank" data-id="${Number(bank.id_nganhang)}">Sửa</button>
                    <button type="button" class="btn btn-sm btn-outline-danger btn-delete-bank" data-id="${Number(bank.id_nganhang)}">Khóa</button>
                </div>
            </div>
        `;
    }).join('');
}

function renderSubjectFilter() {
    const select = document.getElementById('subjectFilter');
    if (!selectedBank || !Array.isArray(selectedBank.subjects) || !selectedBank.subjects.length) {
        select.disabled = true;
        select.innerHTML = '<option value="">Chưa có môn học</option>';
        return;
    }

    select.disabled = false;
    select.innerHTML = selectedBank.subjects.map((subject) => `
        <option value="${Number(subject.id_monhoc)}">${escapeHtml(subject.tenmonhoc)}</option>
    `).join('');
}

function renderQuestionPanelMeta() {
    const title = document.getElementById('questionPanelTitle');
    const meta = document.getElementById('questionPanelMeta');
    const openBtn = document.getElementById('openQuestionBtn');

    if (!selectedBank) {
        title.textContent = 'Chọn một ngân hàng câu hỏi';
        meta.textContent = 'Bạn có thể quản lý câu hỏi theo từng môn học trong ngân hàng.';
        openBtn.disabled = true;
        return;
    }

    title.textContent = selectedBank.ten_nganhang || 'Ngân hàng câu hỏi';
    meta.textContent = `${selectedBank.subjects?.length || 0} môn học được gắn vào ngân hàng này.`;
    openBtn.disabled = !(selectedBank.subjects && selectedBank.subjects.length);
}

function renderQuestionTable() {
    const tbody = document.getElementById('bankQuestionTableBody');

    if (!selectedBank) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted py-4">Chọn một ngân hàng câu hỏi để xem dữ liệu.</td></tr>';
        return;
    }

    if (!bankQuestions.length) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted py-4">Chưa có câu hỏi nào cho môn học đang chọn.</td></tr>';
        return;
    }

    tbody.innerHTML = bankQuestions.map((question, index) => `
        <tr>
            <td>${index + 1}</td>
            <td style="max-width:260px;">
                <div style="font-weight:600;color:#0f172a;">${escapeHtml(question.noidungcauhoi)}</div>
            </td>
            <td>
                ${(question.dapan || []).map((answer) => `
                    <div style="margin-bottom:4px;color:${Number(answer.dapandung) === 1 ? '#15803d' : '#475569'};">
                        ${Number(answer.dapandung) === 1 ? '<strong>[Đúng]</strong> ' : ''}${escapeHtml(answer.noidungdapan)}
                    </div>
                `).join('')}
            </td>
            <td>${escapeHtml(difficultyLabel(question.dokho))}</td>
            <td><span class="badge ${question.trangthai === 'inactive' ? 'text-bg-secondary' : 'text-bg-success'}">${escapeHtml(statusLabel(question.trangthai))}</span></td>
            <td class="text-end">
                <button type="button" class="btn btn-sm btn-outline-primary btn-edit-bank-question" data-id="${Number(question.id_cauhoi_nganhang)}">Sửa</button>
                <button type="button" class="btn btn-sm btn-outline-danger btn-delete-bank-question" data-id="${Number(question.id_cauhoi_nganhang)}">Khóa</button>
            </td>
        </tr>
    `).join('');
}

async function loadBanks() {
    const res = await fetch(serverApiUrl('nganhang/list'));
    const json = await res.json();
    if (!res.ok || !json.success) {
        throw new Error(json.error || 'Không thể tải ngân hàng câu hỏi');
    }

    bankItems = Array.isArray(json.banks) ? json.banks : [];
    subjectItems = Array.isArray(json.subjects) ? json.subjects : [];

    if (selectedBank) {
        const freshBank = bankItems.find((item) => Number(item.id_nganhang) === Number(selectedBank.id_nganhang));
        if (!freshBank) {
            selectedBank = null;
            bankQuestions = [];
        }
    }

    renderBankList();
    renderQuestionPanelMeta();
    renderQuestionTable();
}

async function loadBankQuestions(bankId, subjectId = '') {
    const res = await fetch(serverApiUrl('nganhang/cauhoi/list', {
        id_nganhang: bankId,
        id_monhoc: subjectId
    }));
    const json = await res.json();
    if (!res.ok || !json.success) {
        throw new Error(json.error || 'Không thể tải câu hỏi ngân hàng');
    }

    selectedBank = json.bank || null;
    bankQuestions = Array.isArray(json.questions) ? json.questions : [];
    renderBankList();
    renderQuestionPanelMeta();
    renderSubjectFilter();
    if (json.selected_subject_id) {
        document.getElementById('subjectFilter').value = String(json.selected_subject_id);
    }
    renderQuestionTable();
}

function openBankModal(id = 0) {
    document.getElementById('bankForm').reset();
    document.getElementById('bankId').value = '';
    document.getElementById('bankModalTitle').textContent = 'Thêm ngân hàng câu hỏi';
    renderSubjectCheckboxes([]);

    if (id) {
        const bank = selectedBank && Number(selectedBank.id_nganhang) === Number(id)
            ? selectedBank
            : null;
        const baseBank = bank || bankItems.find((item) => Number(item.id_nganhang) === Number(id));
        if (!baseBank) return;

        const selectedIds = Array.isArray(baseBank.subjects)
            ? baseBank.subjects.map((item) => Number(item.id_monhoc))
            : [];

        document.getElementById('bankModalTitle').textContent = 'Cập nhật ngân hàng câu hỏi';
        document.getElementById('bankId').value = baseBank.id_nganhang;
        document.getElementById('bankName').value = baseBank.ten_nganhang || '';
        document.getElementById('bankDescription').value = baseBank.mieuta || '';
        document.getElementById('bankStatus').value = baseBank.trangthai || 'active';
        renderSubjectCheckboxes(selectedIds);
    }

    document.getElementById('bankModal').style.display = 'flex';
}

function closeBankModal() {
    document.getElementById('bankModal').style.display = 'none';
}

function getSelectedBankSubjectIds() {
    return Array.from(document.querySelectorAll('.bank-subject-checkbox:checked'))
        .map((input) => Number(input.value));
}

async function saveBank(payload) {
    const method = payload.id_nganhang ? 'PATCH' : 'POST';
    const res = await fetch(serverApiUrl('nganhang/save'), {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    });
    const json = await res.json();
    if (!res.ok || !json.success) {
        throw new Error(json.error || 'Không thể lưu ngân hàng câu hỏi');
    }
    return json;
}

async function deleteBank(id) {
    const res = await fetch(serverApiUrl('nganhang/delete'), {
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ id_nganhang: id })
    });
    const json = await res.json();
    if (!res.ok || !json.success) {
        throw new Error(json.error || 'Không thể khóa ngân hàng câu hỏi');
    }
    return json;
}

function fillQuestionSubjectOptions(selectedId = '') {
    const select = document.getElementById('bankQuestionSubject');
    if (!selectedBank || !selectedBank.subjects?.length) {
        select.innerHTML = '<option value="">Chưa có môn học</option>';
        return;
    }
    select.innerHTML = selectedBank.subjects.map((subject) => `
        <option value="${Number(subject.id_monhoc)}" ${String(subject.id_monhoc) === String(selectedId) ? 'selected' : ''}>${escapeHtml(subject.tenmonhoc)}</option>
    `).join('');
}

function addBankAnswerOption(content = '', checked = false) {
    const container = document.getElementById('bankAnswerOptions');
    const index = container.children.length;
    const row = document.createElement('div');
    row.style.cssText = 'display:flex;align-items:center;gap:10px;';
    row.innerHTML = `
        <input type="text" class="form-control bank-answer-input" value="${escapeHtml(content)}" placeholder="Nhập nội dung đáp án">
        <label style="display:flex;align-items:center;gap:6px;white-space:nowrap;">
            <input type="radio" name="bank_correct_answer" ${checked ? 'checked' : ''}> Đúng
        </label>
        <button type="button" class="btn btn-outline-danger btn-sm" onclick="this.parentElement.remove()">Xóa</button>
    `;
    container.appendChild(row);
}

function openQuestionModal(id = 0) {
    if (!selectedBank) {
        showBankAlert('Vui lòng chọn ngân hàng câu hỏi trước.', 'danger');
        return;
    }

    document.getElementById('bankQuestionForm').reset();
    document.getElementById('bankQuestionId').value = '';
    document.getElementById('bankQuestionModalTitle').textContent = 'Thêm câu hỏi ngân hàng';
    document.getElementById('bankAnswerOptions').innerHTML = '';
    fillQuestionSubjectOptions(document.getElementById('subjectFilter').value || '');
    addBankAnswerOption('', true);
    addBankAnswerOption('', false);

    if (id) {
        const question = bankQuestions.find((item) => Number(item.id_cauhoi_nganhang) === Number(id));
        if (!question) return;

        document.getElementById('bankQuestionModalTitle').textContent = 'Cập nhật câu hỏi ngân hàng';
        document.getElementById('bankQuestionId').value = question.id_cauhoi_nganhang;
        document.getElementById('bankQuestionContent').value = question.noidungcauhoi || '';
        document.getElementById('bankQuestionDifficulty').value = question.dokho || 'de';
        document.getElementById('bankQuestionStatus').value = question.trangthai || 'active';
        fillQuestionSubjectOptions(question.id_monhoc || '');
        document.getElementById('bankAnswerOptions').innerHTML = '';
        (question.dapan || []).forEach((answer) => {
            addBankAnswerOption(answer.noidungdapan || '', Number(answer.dapandung) === 1);
        });
    }

    document.getElementById('questionModalBank').style.display = 'flex';
}

function closeQuestionModal() {
    document.getElementById('questionModalBank').style.display = 'none';
}

async function saveBankQuestion(payload) {
    const method = payload.id_cauhoi_nganhang ? 'PATCH' : 'POST';
    const res = await fetch(serverApiUrl('nganhang/cauhoi/save'), {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    });
    const json = await res.json();
    if (!res.ok || !json.success) {
        throw new Error(json.error || 'Không thể lưu câu hỏi ngân hàng');
    }
    return json;
}

async function deleteBankQuestion(id) {
    const res = await fetch(serverApiUrl('nganhang/cauhoi/delete'), {
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ id_cauhoi_nganhang: id })
    });
    const json = await res.json();
    if (!res.ok || !json.success) {
        throw new Error(json.error || 'Không thể khóa câu hỏi ngân hàng');
    }
    return json;
}

document.getElementById('bankForm').addEventListener('submit', async function(event) {
    event.preventDefault();

    const payload = {
        id_nganhang: Number(document.getElementById('bankId').value || 0),
        ten_nganhang: document.getElementById('bankName').value.trim(),
        mieuta: document.getElementById('bankDescription').value.trim(),
        trangthai: document.getElementById('bankStatus').value,
        subject_ids: getSelectedBankSubjectIds()
    };

    try {
        const json = await saveBank(payload);
        closeBankModal();
        showBankAlert(json.message || 'Lưu ngân hàng câu hỏi thành công');
        await loadBanks();
        const savedBankId = json.data?.id_nganhang || payload.id_nganhang;
        if (savedBankId) {
            await loadBankQuestions(savedBankId);
        }
    } catch (error) {
        showBankAlert(error.message, 'danger');
    }
});

document.getElementById('bankList').addEventListener('click', async function(event) {
    const editBtn = event.target.closest('.btn-edit-bank');
    if (editBtn) {
        event.stopPropagation();
        if (!selectedBank || Number(selectedBank.id_nganhang) !== Number(editBtn.dataset.id)) {
            await loadBankQuestions(Number(editBtn.dataset.id));
        }
        openBankModal(Number(editBtn.dataset.id));
        return;
    }

    const deleteBtn = event.target.closest('.btn-delete-bank');
    if (deleteBtn) {
        event.stopPropagation();
        if (!confirm('Bạn có chắc muốn khóa ngân hàng câu hỏi này không?')) {
            return;
        }
        try {
            const json = await deleteBank(Number(deleteBtn.dataset.id));
            showBankAlert(json.message || 'Khóa ngân hàng thành công');
            if (selectedBank && Number(selectedBank.id_nganhang) === Number(deleteBtn.dataset.id)) {
                selectedBank = null;
                bankQuestions = [];
            }
            await loadBanks();
        } catch (error) {
            showBankAlert(error.message, 'danger');
        }
        return;
    }

    const item = event.target.closest('.bank-item');
    if (item) {
        try {
            await loadBankQuestions(Number(item.dataset.id));
        } catch (error) {
            showBankAlert(error.message, 'danger');
        }
    }
});

document.getElementById('subjectFilter').addEventListener('change', async function() {
    if (!selectedBank) return;
    try {
        await loadBankQuestions(Number(selectedBank.id_nganhang), this.value);
    } catch (error) {
        showBankAlert(error.message, 'danger');
    }
});

document.getElementById('bankQuestionForm').addEventListener('submit', async function(event) {
    event.preventDefault();

    const optionInputs = Array.from(document.querySelectorAll('.bank-answer-input'));
    const radioInputs = Array.from(document.querySelectorAll('input[name="bank_correct_answer"]'));
    const options = optionInputs.map((input) => input.value.trim());
    const correctIndex = radioInputs.findIndex((input) => input.checked);

    const payload = {
        id_cauhoi_nganhang: Number(document.getElementById('bankQuestionId').value || 0),
        id_nganhang: Number(selectedBank?.id_nganhang || 0),
        id_monhoc: Number(document.getElementById('bankQuestionSubject').value || 0),
        noidungcauhoi: document.getElementById('bankQuestionContent').value.trim(),
        dokho: document.getElementById('bankQuestionDifficulty').value,
        trangthai: document.getElementById('bankQuestionStatus').value,
        options,
        correct_index: correctIndex
    };

    try {
        const json = await saveBankQuestion(payload);
        closeQuestionModal();
        showBankAlert(json.message || 'Lưu câu hỏi ngân hàng thành công');
        await loadBankQuestions(Number(selectedBank.id_nganhang), document.getElementById('subjectFilter').value || '');
        await loadBanks();
    } catch (error) {
        showBankAlert(error.message, 'danger');
    }
});

document.getElementById('bankQuestionTableBody').addEventListener('click', async function(event) {
    const editBtn = event.target.closest('.btn-edit-bank-question');
    if (editBtn) {
        openQuestionModal(Number(editBtn.dataset.id));
        return;
    }

    const deleteBtn = event.target.closest('.btn-delete-bank-question');
    if (deleteBtn) {
        if (!confirm('Bạn có chắc muốn khóa câu hỏi ngân hàng này không?')) {
            return;
        }
        try {
            const json = await deleteBankQuestion(Number(deleteBtn.dataset.id));
            showBankAlert(json.message || 'Khóa câu hỏi thành công');
            await loadBankQuestions(Number(selectedBank.id_nganhang), document.getElementById('subjectFilter').value || '');
            await loadBanks();
        } catch (error) {
            showBankAlert(error.message, 'danger');
        }
    }
});

window.addEventListener('click', function(event) {
    if (event.target === document.getElementById('bankModal')) {
        closeBankModal();
    }
    if (event.target === document.getElementById('questionModalBank')) {
        closeQuestionModal();
    }
});

(async function initQuestionBankPage() {
    try {
        await loadBanks();
        if (bankItems.length) {
            await loadBankQuestions(Number(bankItems[0].id_nganhang));
        }
    } catch (error) {
        showBankAlert(error.message, 'danger');
    }
})();
</script>
