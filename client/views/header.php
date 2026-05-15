<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><?= $title ?? "PT QUIZ" ?></title>

    <link rel="stylesheet" href="public/css/style.css">

    <?php if (!empty($page_css)) : ?>
    <link rel="stylesheet" href="public/css/<?= $page_css ?>">
    <?php endif; ?>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/swiper@11/swiper-bundle.min.css" />
    <link rel="icon" type="image/jpg" href="public/img/ptstore-no-background.png">

    <!-- Latest compiled and minified CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- FONT AWESOME CDN -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">

    <!-- Latest compiled JavaScript -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <!-- Google Identity Services -->
    <script src="https://accounts.google.com/gsi/client" async defer></script>
</head>

<body>
    <div class="container-fluid p-0">
        <div class="topnav-container shadow-sm" style="position: relative; z-index: 1050;">
            <nav class="navbar navbar-expand-lg navbar-light bg-white">

                <div class="container">

                    <!-- Logo -->
                    <a class="navbar-brand d-flex align-items-center fw-bold" href="index.php?act=trangchu">

                        <img src="public/img/ptstore.jpg" class="topnav-logo" alt="Logo">

                        <span class="brand-text">PT QUIZ</span>
                    </a>

                    <!-- Toggle mobile -->
                    <button class="navbar-toggler" type="button" data-bs-toggle="collapse"
                        data-bs-target="#navbar-collapse">
                        <span class="navbar-toggler-icon"></span>
                    </button>

                    <!-- Menu -->
                    <div class="collapse navbar-collapse justify-content-end" id="navbar-collapse">

                        <ul class="navbar-nav align-items-center">

                            <li class="nav-item">
                                <a class="nav-link" href="index.php?act=gioithieu">
                                    Giới thiệu
                                </a>
                            </li>


                            <li class="nav-item">
                                <a class="nav-link" href="javascript:void(0)"
                                    onclick="requireLogin('index.php?act=dethi')">Đề thi</a>
                            </li>


                            <?php if (isset($_SESSION['user'])): ?>
                            <li class="nav-item dropdown ms-3">
                                <a class="nav-link dropdown-toggle d-flex align-items-center" href="#" id="userDropdown"
                                    role="button" data-bs-toggle="dropdown" aria-expanded="false">
                                    <img src="/project-tracnghiem/server/public/imgs/avatars/<?= htmlspecialchars($_SESSION['user']['avatar'] ?? 'default.jpg') ?>" class="rounded-circle me-2" style="width: 32px; height: 32px; object-fit: cover; border: 2px solid #e2e8f0;">
                                    <span class="fw-bold"><?= htmlspecialchars($_SESSION['user']['name']) ?></span>
                                </a>
                                <ul class="dropdown-menu dropdown-menu-end shadow" aria-labelledby="userDropdown">
                                    <li>
                                        <a class="dropdown-item py-2" href="index.php?act=thongtin">
                                            <i class="fa-solid fa-id-card me-2 text-primary"></i>Thông tin cá nhân
                                        </a>
                                    </li>
                                    <li>
                                        <a class="dropdown-item py-2" href="index.php?act=lichsu">
                                            <i class="fa-solid fa-clock-rotate-left me-2 text-success"></i>Lịch sử làm
                                            bài
                                        </a>
                                    </li>
                                    <li>
                                        <hr class="dropdown-divider">
                                    </li>
                                    <li>
                                        <a class="dropdown-item py-2 text-danger" href="index.php?act=dangxuat"
                                            onclick="return logoutConfirm()">
                                            <i class="fa-solid fa-right-from-bracket me-2"></i>Đăng xuất
                                        </a>
                                    </li>
                                </ul>
                            </li>
                            <?php else: ?>

                            <li class="nav-item ms-2">
                                <a class="nav-link" href="index.php?act=dangky">
                                    Đăng ký
                                </a>
                            </li>

                            <li class="nav-item ms-2">
                                <a href="index.php?act=dangnhap" class="btn btn-primary px-4">
                                    Đăng nhập
                                </a>
                            </li>

                            <?php endif; ?>

                        </ul>

                    </div>

            </nav>
        </div>

        <!-- Login Prompt Modal -->
        <div class="modal fade" id="loginPromptModal" tabindex="-1" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered">
                <div class="modal-content rounded-4 border-0 shadow-lg">
                    <div class="modal-body text-center p-4 p-md-5">
                        <div class="mb-4">
                            <div class="bg-warning bg-opacity-10 text-warning rounded-circle d-flex align-items-center justify-content-center mx-auto"
                                style="width: 80px; height: 80px;">
                                <i class="fa-solid fa-lock" style="font-size: 2.5rem;"></i>
                            </div>
                        </div>
                        <h4 class="fw-bold mb-3 text-dark">Yêu cầu đăng nhập</h4>
                        <p class="text-muted mb-4">Bạn phải đăng nhập tài khoản mới được tham gia làm bài thi. Vui lòng
                            đăng nhập để tiếp tục.</p>
                        <div class="d-flex flex-column gap-2">
                            <a href="index.php?act=dangnhap"
                                class="btn btn-primary rounded-pill py-2 fw-bold shadow-sm custom-btn"><i
                                    class="fa-solid fa-right-to-bracket me-2"></i>Đăng nhập ngay</a>
                            <button type="button"
                                class="btn btn-light rounded-pill py-2 fw-bold text-muted transition-hover"
                                data-bs-dismiss="modal">Để sau</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <script>
        const isLoggedIn = <?= isset($_SESSION['user']) ? 'true' : 'false' ?>;

        document.addEventListener("DOMContentLoaded", () => {
            const urlParams = new URLSearchParams(window.location.search);
            const error = urlParams.get('error');
            if (error === 'ongoing_mismatch') {
                alert("TRUY CẬP BỊ TỪ CHỐI!\n\nHệ thống ghi nhận bạn đang có một bài thi khác đang làm dở.\n\nBạn phải hoàn thành hoặc Hủy bài thi đó trước khi bắt đầu bài mới.");
            } else if (error === 'session_active') {
                alert("TRUY CẬP BỊ TỪ CHỐI!\n\nBài thi này hiện đang được ĐƯỢC MỞ ở một thiết bị hoặc trình duyệt khác.\n\nVui lòng đóng cửa sổ đang làm trước khi vào lại.");
            }
        });

        function apiUrl(route, params = {}) {
            const cleanRoute = String(route || '').replace(/^\/+|\/+$/g, '');
            const url = new URL(`api/${cleanRoute}`, window.location.href);

            Object.entries(params).forEach(([key, value]) => {
                if (value !== undefined && value !== null && value !== "") {
                    url.searchParams.set(key, value);
                }
            });

            return url.toString();
        }

        function logoutConfirm() {
            return confirm("Bạn chắc chắn muốn đăng xuất? Bài thi đang làm (nếu có) sẽ được bảo lưu.");
        }

        function requireLogin(url) {
            if (!isLoggedIn) {
                const loginModal = new bootstrap.Modal(document.getElementById('loginPromptModal'));
                loginModal.show();
            } else {
                window.location.href = url;
            }
        }

        async function confirmLambai(id, isOngoing) {
            if (!isLoggedIn) {
                const loginModal = new bootstrap.Modal(document.getElementById('loginPromptModal'));
                loginModal.show();
                return;
            }

            // CRITICAL SECURITY CHECK: Check for any ongoing or active exams
            try {
                const res = await fetch(apiUrl("exam/list"));
                const json = await res.json();
                if (json.success && json.data) {
                    const activeExam = json.data.find(row => parseInt(row.is_ongoing) === 1);
                    
                    if (activeExam) {
                        // 1. BLOCK if trying to start a DIFFERENT exam while one is ongoing
                        if (parseInt(activeExam.id_baithi) !== parseInt(id)) {
                            alert(`TRUY CẬP BỊ TỪ CHỐI!\n\nHệ thống ghi nhận bạn đang làm bài thi '${activeExam.ten_baithi}' dở dang.\n\nBạn phải hoàn thành hoặc Hủy bài thi đó trước khi bắt đầu bài mới.`);
                            return;
                        }
                        
                        // 2. BLOCK if the SAME exam is CURRENTLY active (Heartbeat < 30s)
                        if (parseInt(activeExam.is_active) === 1) {
                            alert(`TRUY CẬP BỊ TỪ CHỐI!\n\nHệ thống ghi nhận bài thi này hiện đang ĐƯỢC MỞ ở một thiết bị khác (App hoặc Web).\n\nBạn KHÔNG THỂ tham gia cùng lúc. Vui lòng đóng cửa sổ đang làm trước khi vào lại.`);
                            return;
                        }
                    }
                }
            } catch (e) {
                console.error("Lỗi kiểm tra session:", e);
            }

            const msg = isOngoing ? "Bạn có muốn tiếp tục làm bài thi này không?" :
                "Bạn có chắc chắn muốn bắt đầu làm bài thi này không?";
            if (confirm(msg)) {
                window.location.href = `index.php?act=lambai&id=${id}`;
            }
        }
        </script>
        <div class="main">



