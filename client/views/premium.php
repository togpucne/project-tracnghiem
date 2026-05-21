<?php
// client/views/premium.php
if (!isset($_SESSION['user'])) {
    header("Location: index.php?page=dangnhap");
    exit;
}

$is_premium = $_SESSION['user']['premium_status'] == 1;
$daysLeft = 0;
if ($is_premium && !empty($_SESSION['user']['premium_expire'])) {
    $now = new DateTime();
    $expireDate = new DateTime($_SESSION['user']['premium_expire']);
    if ($expireDate > $now) {
        $diff = $now->diff($expireDate);
        $daysLeft = $diff->days;
    }
}
?>

<div class="container mt-5">
    <div class="premium-packages text-center p-4 bg-white shadow rounded">
        <?php if ($is_premium && $daysLeft > 2): ?>
            <div class="py-5">
                <i class="fas fa-crown text-warning mb-4" style="font-size: 5rem;"></i>
                <h2 class="mb-3 text-warning font-weight-bold">Bạn đã là thành viên Premium!</h2>
                <p class="text-muted mb-2 text-lg">Gói Premium của bạn còn <strong class="text-dark"><?= $daysLeft ?> ngày</strong> sử dụng.</p>
                <p class="text-muted mb-4">Bạn chỉ có thể gia hạn khi gói cước còn dưới 2 ngày.</p>
                <a href="index.php" class="btn btn-primary px-4 py-2 rounded-pill"><i class="fas fa-arrow-left me-2"></i> Quay lại trang chủ</a>
            </div>
        <?php else: ?>
            <h2 class="mb-4 text-primary font-weight-bold">
                <?= ($is_premium && $daysLeft <= 2) ? 'Gia hạn tài khoản Premium' : 'Nâng cấp tài khoản Premium' ?>
            </h2>
            <?php if ($is_premium && $daysLeft <= 2): ?>
                <div class="alert alert-warning d-inline-block px-4 py-2 rounded-pill mb-4">
                    <i class="fas fa-exclamation-triangle me-2"></i> Gói của bạn sắp hết hạn. Gia hạn ngay để không bị gián đoạn!
                </div>
            <?php else: ?>
                <p class="text-muted mb-5">Trải nghiệm không giới hạn với đầy đủ tính năng ưu việt!</p>
            <?php endif; ?>
            
            <div class="row justify-content-center">
            <?php
            $conn = Database::connect();
            $result = $conn->query("SELECT * FROM goi_premium ORDER BY gia ASC");
            while ($pkg = $result->fetch_assoc()):
                $icon = ($pkg['gia'] >= 400000) ? 'fa-gem text-info' : 'fa-crown text-warning';
                $isBestValue = ($pkg['thoihan_ngay'] >= 365);
            ?>
                <!-- Gói <?= $pkg['ten_goi'] ?> -->
                <div class="col-md-5 mb-4">
                    <div class="package-card border <?= $isBestValue ? 'border-warning' : '' ?> rounded p-4 h-100 hover-shadow transition position-relative">
                        <?php if ($isBestValue): ?>
                            <span class="badge badge-warning position-absolute p-2 px-3" style="top: -15px; left: 50%; transform: translateX(-50%); font-size: 0.9rem;">Tiết kiệm nhất</span>
                        <?php endif; ?>
                        
                        <div class="package-header mb-4 text-center">
                            <i class="fas <?= $icon ?> fa-3x mb-3"></i>
                            <h3 class="h4"><?= htmlspecialchars($pkg['ten_goi']) ?></h3>
                            <h4 class="text-success display-6"><?= number_format($pkg['gia'], 0, ',', '.') ?>đ</h4>
                        </div>
                        <ul class="list-unstyled text-left mb-4 px-3" style="min-height: 120px;">
                            <?php 
                            if (!empty($pkg['mieuta'])) {
                                // Tách chuỗi tại vị trí trước mỗi chữ viết hoa (A-Z và các chữ có dấu in hoa)
                                // Sử dụng Regex \p{Lu} để hỗ trợ chữ Tiếng Việt in hoa
                                $lines = preg_split('/(?=\p{Lu})/u', $pkg['mieuta'], -1, PREG_SPLIT_NO_EMPTY);
                                foreach ($lines as $line): 
                                    if (trim($line) === '') continue;
                            ?>
                                <li class="mb-2"><i class="fas fa-check-circle text-success mr-2"></i> <?= htmlspecialchars(trim($line)) ?></li>
                            <?php 
                                endforeach; 
                            } else {
                                // Mặc định nếu không có miêu tả
                                echo '<li class="mb-2"><i class="fas fa-check-circle text-success mr-2"></i> Làm trắc nghiệm không giới hạn</li>';
                                echo '<li class="mb-2"><i class="fas fa-check-circle text-success mr-2"></i> Xem lời giải chi tiết ngay lập tức</li>';
                            }
                            ?>
                            <li class="mb-2"><i class="fas fa-check-circle text-success mr-2"></i> Thời hạn sử dụng: <strong><?= $pkg['thoihan_ngay'] ?> ngày</strong></li>
                        </ul>
                        <button onclick="createPayment(<?= (int)$pkg['gia'] ?>, '<?= addslashes($pkg['ten_goi']) ?>')" 
                            class="btn <?= $isBestValue ? 'btn-warning text-white' : 'btn-primary' ?> btn-lg btn-block rounded-pill">
                            Chọn gói <?= htmlspecialchars($pkg['ten_goi']) ?>
                        </button>
                    </div>
                </div>
            <?php endwhile; ?>
            </div>

        <div id="payment-result" class="mt-4"></div>
        <?php endif; ?>
    </div>
</div>

<style>
.package-card {
    transition: all 0.3s ease;
    border: 2px solid #f8f9fa;
}
.package-card:hover {
    transform: translateY(-10px);
    box-shadow: 0 1rem 3rem rgba(0,0,0,.175);
    border-color: #007bff;
}
.package-card.border-warning:hover {
    border-color: #ffc107;
}
.display-6 {
    font-size: 2rem;
    font-weight: 700;
}
</style>

<script>
async function createPayment(amount, packageType) {
    const resultDiv = document.getElementById('payment-result');
    resultDiv.innerHTML = `
        <div class="d-flex justify-content-center align-items-center p-4">
            <div class="spinner-border text-primary mr-3" role="status"></div>
            <span>Đang tạo đơn thanh toán...</span>
        </div>
    `;

    try {
        const formData = new FormData();
        formData.append('amount', amount);
        formData.append('package_type', packageType);

        const response = await fetch(apiUrl('premium/create-payment'), {
            method: 'POST',
            body: formData
        });

        const data = await response.json();

        if (data.success) {
            resultDiv.innerHTML = `
                <div class="alert alert-success text-left mt-4 animate__animated animate__fadeIn" style="border-left: 5px solid #28a745;">
                    <div class="row">
                        <div class="col-md-7 border-right">
                            <h3 class="h5 mb-3 text-success font-weight-bold">✅ Đơn thanh toán đã tạo thành công</h3>
                            <div class="mb-2"><strong>Mã đơn:</strong> <span class="badge badge-light text-dark border">${data.order_code}</span></div>
                            <div class="mb-2"><strong>Số tiền:</strong> <span class="text-danger font-weight-bold h5">${data.amount.toLocaleString('vi-VN')}đ</span></div>
                            <div class="p-3 bg-light rounded border mb-3">
                                <p class="mb-2"><strong>Nội dung chuyển khoản:</strong></p>
                                <div class="d-flex align-items-center">
                                    <span id="copy-content" class="h4 text-danger font-weight-bold mb-0">${data.qr_content}</span>
                                    <button onclick="copyToClipboard('${data.qr_content}')" class="btn btn-sm btn-outline-secondary ml-3">Co-ppy</button>
                                </div>
                            </div>
                            <p class="text-danger"><i class="fas fa-clock mr-2"></i>Thời gian thanh toán còn lại: <span id="countdown">15:00</span></p>
                            <p class="small text-muted mb-0">Hệ thống sẽ tự động cập nhật premium sau khi nhận được tiền (thường 5-10 giây).</p>
                        </div>
                        <div class="col-md-5 text-center d-flex flex-column align-items-center justify-content-center">
                            <p class="font-weight-bold mb-2">Quét mã VietQR để thanh toán</p>
                            <div class="bg-white p-2 border rounded">
                                <img src="https://img.vietqr.io/image/mbbank-0343635667-compact2.png?amount=${data.amount}&addInfo=${encodeURIComponent(data.qr_content)}&accountName=NGUYEN%20TRONG%20PHUC" alt="Mã QR Thanh Toán" class="img-fluid" style="max-height: 250px;">
                            </div>
                        </div>
                    </div>
                    <div class="text-center mt-3 pt-3 border-top">
                        <p class="mb-0 text-muted">Vui lòng chuyển khoản chính xác số tiền và nội dung trên.</p>
                    </div>
                </div>
            `;
            
            // Countdown timer
            let timeLeft = 15 * 60;
            const timer = setInterval(() => {
                const minutes = Math.floor(timeLeft / 60);
                const seconds = timeLeft % 60;
                document.getElementById('countdown').innerText = `${minutes}:${seconds < 10 ? '0' : ''}${seconds}`;
                if (timeLeft <= 0) {
                    clearInterval(timer);
                    resultDiv.innerHTML = `<div class="alert alert-danger">❌ Đã hết thời gian thanh toán. Vui lòng tạo đơn mới.</div>`;
                }
                timeLeft--;
            }, 1000);

            // Bắt đầu kiểm tra trạng thái thanh toán (polling)
            startPolling(data.order_code, timer);

        } else {
            const errorMsg = data.message || data.error || 'Lỗi không xác định';
            resultDiv.innerHTML = `<div class="alert alert-danger mt-3">❌ ${errorMsg}</div>`;
        }
    } catch (error) {
        console.error(error);
        resultDiv.innerHTML = `<div class="alert alert-danger mt-3">❌ Lỗi kết nối máy chủ. Vui lòng thử lại.</div>`;
    }
}

function copyToClipboard(text) {
    navigator.clipboard.writeText(text).then(() => {
        alert('Đã sao chép nội dung chuyển khoản!');
    });
}

async function startPolling(order_code, timer) {
    const pollInterval = setInterval(async () => {
        try {
            const response = await fetch(apiUrl('premium/check-status', { order_code: order_code }));
            const data = await response.json();
            if (data.status === 'completed' || data.is_premium === true) {
                clearInterval(pollInterval);
                clearInterval(timer);
                Swal.fire({
                    icon: 'success',
                    title: 'Nâng cấp thành công!',
                    text: 'Chúc mừng bạn đã trở thành thành viên Premium.',
                    confirmButtonText: 'Bắt đầu ngay'
                }).then(() => {
                    window.location.href = 'index.php'; // Quay lại trang chủ
                });
            }
        } catch (error) {
            console.error('Polling error:', error);
        }
    }, 5000); // Check every 5 seconds
}
</script>
