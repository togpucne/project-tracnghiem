<?php
if (!isset($_SESSION['user'])) {
    header("Location:index.php?act=dangnhap");
    exit;
}
$id_lanthi = (int)($_GET['id'] ?? 0);
?>
<style>
    .result-header {
        background: #fff;
        border-radius: 12px;
        padding: 30px;
        box-shadow: 0 4px 20px rgba(0,0,0,0.05);
        margin-bottom: 30px;
    }
    .score-value {
        font-size: 48px;
        font-weight: 800;
        color: #3b5bdb;
        line-height: 1;
    }
    .stat-card {
        padding: 15px;
        border-radius: 10px;
        background: #f8fafc;
        border: 1px solid #f1f5f9;
        text-align: center;
    }
    .stat-num {
        font-size: 20px;
        font-weight: 700;
        display: block;
    }
    .stat-txt {
        font-size: 13px;
        color: #64748b;
    }
    .question-item {
        border-left: 4px solid #e5e7eb;
        background: #fff;
        padding: 20px;
        border-radius: 8px;
        margin-bottom: 15px;
        box-shadow: 0 2px 5px rgba(0,0,0,0.02);
    }
    .question-item.correct { border-left-color: #22c55e; }
    .question-item.wrong { border-left-color: #ef4444; }
    .question-item.empty { border-left-color: #f59e0b; }
    
    .ans-opt {
        padding: 10px 15px;
        border-radius: 6px;
        margin-bottom: 8px;
        border: 1px solid #f1f5f9;
    }
    .ans-opt.correct { background: #f0fdf4; border-color: #bcf0da; color: #166534; }
    .ans-opt.selected-wrong { background: #fef2f2; border-color: #fecaca; color: #991b1b; }
</style>

<div class="container my-5" id="result-container">
    <div class="text-center py-5">
        <div class="spinner-border text-primary"></div>
    </div>
</div>

<script>
document.addEventListener("DOMContentLoaded", async () => {
    const container = document.getElementById("result-container");
    const id_lanthi = <?= $id_lanthi ?>;

    try {
        const res = await fetch(`api/get_result.php?id=${id_lanthi}`);
        const json = await res.json();

        if (json.success) {
            const { lanthi, questions } = json;
            
            let correct = 0, wrong = 0, empty = 0;
            questions.forEach(q => {
                let selected = q.answers.find(a => a.selected);
                if (!selected) empty++;
                else if (selected.dapandung) correct++;
                else wrong++;
            });

            let html = `
                <div class="row justify-content-center">
                    <div class="col-lg-8">
                        <!-- Simplified Summary -->
                        <div class="result-header text-center">
                            <h4 class="fw-bold mb-4">${lanthi.ten_baithi}</h4>
                            <div class="mb-4">
                                <span class="text-muted d-block small mb-1">Điểm số</span>
                                <span class="score-value">${lanthi.diem}</span>
                            </div>
                            
                            <div class="row g-3">
                                <div class="col-4">
                                    <div class="stat-card">
                                        <span class="stat-num text-success">${correct}</span>
                                        <span class="stat-txt">Đúng</span>
                                    </div>
                                </div>
                                <div class="col-4">
                                    <div class="stat-card">
                                        <span class="stat-num text-danger">${wrong}</span>
                                        <span class="stat-txt">Sai</span>
                                    </div>
                                </div>
                                <div class="col-4">
                                    <div class="stat-card">
                                        <span class="stat-num text-warning">${empty}</span>
                                        <span class="stat-txt">Bỏ trống</span>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Mini Review -->
                        <div class="mt-5">
                            <h5 class="fw-bold mb-4">Xem lại đáp án</h5>
            `;

            questions.forEach((q, i) => {
                let selected = q.answers.find(a => a.selected);
                let typeCls = !selected ? 'empty' : (selected.dapandung ? 'correct' : 'wrong');
                
                html += `
                    <div class="question-item ${typeCls}">
                        <p class="fw-bold mb-3">Câu ${i+1}: ${q.noidungcauhoi}</p>
                        <div class="options">
                            ${q.answers.map(ans => {
                                let cls = "ans-opt";
                                if (ans.dapandung) cls += " correct";
                                if (ans.selected && !ans.dapandung) cls += " selected-wrong";
                                return `<div class="${cls}">${ans.noidungdapan} ${ans.selected ? '<strong>(Bạn chọn)</strong>' : ''}</div>`;
                            }).join('')}
                        </div>
                    </div>
                `;
            });

            html += `
                            <div class="d-flex gap-3 justify-content-center mt-5">
                                <a href="index.php?act=dethi" class="btn btn-primary px-4 rounded-pill">
                                    <i class="fas fa-list me-2"></i>Trang đề thi
                                </a>
                                <a href="index.php?act=lichsu" class="btn btn-outline-secondary px-4 rounded-pill">
                                    <i class="fas fa-history me-2"></i>Lịch sử làm bài
                                </a>
                            </div>
                        </div>
                    </div>
                </div>
            `;
            container.innerHTML = html;
        } else {
            container.innerHTML = `<div class='alert alert-danger'>${json.error || 'Lỗi không xác định'}</div>`;
        }
    } catch (e) {
        console.error(e);
        container.innerHTML = "<div class='alert alert-danger'>Lỗi kết nối máy chủ</div>";
    }
});
</script>
