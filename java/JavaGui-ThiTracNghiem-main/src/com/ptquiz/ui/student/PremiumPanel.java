package com.ptquiz.ui.student;

import com.ptquiz.core.*;
import com.ptquiz.ui.main.Home;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class PremiumPanel extends JPanel {
    private JPanel paymentDetail;
    private String currentOrderCode;
    private Timer pollingTimer;
    private Home homeFrame;

    public PremiumPanel(Home homeFrame) {
        this.homeFrame = homeFrame;
        setLayout(new CardLayout());
        setBackground(Color.WHITE);

        initPackageSelection();
        initPaymentDetail();
    }

    private void initPackageSelection() {
        JPanel selectionPanel = new JPanel(new BorderLayout());
        selectionPanel.setBackground(new Color(249, 250, 251));
        selectionPanel.setBorder(new EmptyBorder(40, 60, 40, 60));

        // Header
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 5));
        header.setBackground(selectionPanel.getBackground());
        JLabel title = new JLabel("Nâng cấp tài khoản Premium", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(new Color(17, 24, 39));
        
        JLabel subtitle = new JLabel("Mở khóa toàn bộ tính năng và xem lời giải chi tiết ngay lập tức!", SwingConstants.CENTER);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitle.setForeground(new Color(107, 114, 128));
        
        header.add(title);
        header.add(subtitle);
        selectionPanel.add(header, BorderLayout.NORTH);

        // Cards Container
        JPanel cardsContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 30));
        cardsContainer.setBackground(selectionPanel.getBackground());

        cardsContainer.add(createModernCard("Premium Tháng", "45.000", "30", "Gói phổ thông", new Color(59, 130, 246)));
        cardsContainer.add(createModernCard("Premium Năm", "400.000", "365", "Gói tiết kiệm (Gợi ý)", new Color(245, 158, 11)));

        selectionPanel.add(cardsContainer, BorderLayout.CENTER);
        add(selectionPanel, "SELECTION");
    }

    private JPanel createModernCard(String name, String price, String days, String tag, Color accentColor) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 25, 25));
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(320, 480));
        card.setBackground(Color.WHITE);
        card.setBorder(new EmptyBorder(30, 30, 30, 30));

        JLabel tagLbl = new JLabel(tag.toUpperCase());
        tagLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        tagLbl.setForeground(accentColor);
        tagLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(tagLbl);
        card.add(Box.createVerticalStrut(10));

        JLabel nameLbl = new JLabel(name);
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        nameLbl.setForeground(Color.BLACK); 
        nameLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(nameLbl);
        card.add(Box.createVerticalStrut(20));

        JLabel priceLbl = new JLabel("đ" + price);
        priceLbl.setFont(new Font("Segoe UI", Font.BOLD, 36));
        priceLbl.setForeground(Color.BLACK);
        priceLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(priceLbl);
        
        JLabel unitLbl = new JLabel("/ " + days + " ngày");
        unitLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        unitLbl.setForeground(Color.GRAY);
        unitLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(unitLbl);
        
        card.add(Box.createVerticalStrut(30));
        card.add(new JSeparator());
        card.add(Box.createVerticalStrut(20));

        // DÙNG DẤU + ĐỂ KHÔNG BỊ LỖI ICON []
        String[] perks = {"+ Làm bài không giới hạn", "+ Xem lời giải chi tiết", "+ Tài liệu học tập cá nhân", "+ Không quảng cáo"};
        for (String perk : perks) {
            JLabel p = new JLabel(perk);
            p.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            p.setForeground(new Color(55, 65, 81));
            p.setAlignmentX(Component.CENTER_ALIGNMENT);
            card.add(p);
            card.add(Box.createVerticalStrut(8));
        }

        card.add(Box.createVerticalGlue());

        JButton buyBtn = new JButton("Chọn gói này");
        buyBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        buyBtn.setForeground(Color.BLACK); // CHUYỂN SANG CHỮ ĐEN CHO RÕ
        buyBtn.setBackground(new Color(229, 231, 235)); // Nền xám nhạt cho nút
        buyBtn.setFocusPainted(false);
        buyBtn.setBorder(new LineBorder(accentColor, 2, true));
        buyBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        buyBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        buyBtn.setMaximumSize(new Dimension(250, 45));
        buyBtn.addActionListener(e -> startPayment(name.contains("Tháng") ? "monthly" : "yearly", price.replace(".", "")));
        
        card.add(buyBtn);

        return card;
    }

    private void initPaymentDetail() {
        paymentDetail = new JPanel(new BorderLayout());
        paymentDetail.setBackground(Color.WHITE);
        add(paymentDetail, "PAYMENT");
    }

    private void startPayment(String packageName, String amount) {
        String json = String.format("{\"package_type\":\"%s\", \"amount\":%s}", packageName, amount);
        APIHelper.APIResponse res = APIHelper.sendPost("premium/create-payment", json);
        
        if (res.success) {
            currentOrderCode = APIHelper.extractJsonValue(res.rawData, "order_code");
            renderPaymentUI(packageName, amount, currentOrderCode);
            CardLayout cl = (CardLayout) getLayout();
            cl.show(this, "PAYMENT");
            startPolling();
        } else {
            JOptionPane.showMessageDialog(this, "Lỗi tạo đơn hàng: " + res.message);
        }
    }

    private void renderPaymentUI(String packageName, String amount, String orderCode) {
        paymentDetail.removeAll();
        
        JPanel container = new JPanel(new GridLayout(1, 2, 40, 0));
        container.setBackground(Color.WHITE);
        container.setBorder(new EmptyBorder(50, 80, 50, 80));

        // Left: Info
        JPanel infoSide = new JPanel();
        infoSide.setLayout(new BoxLayout(infoSide, BoxLayout.Y_AXIS));
        infoSide.setBackground(Color.WHITE);

        JLabel t = new JLabel("Chi tiết thanh toán");
        t.setFont(new Font("Segoe UI", Font.BOLD, 28));
        infoSide.add(t);
        infoSide.add(Box.createVerticalStrut(30));

        addInfoRow(infoSide, "Số tiền:", amount + " VNĐ");
        addInfoRow(infoSide, "Gói:", packageName.equals("monthly") ? "Premium Tháng" : "Premium Năm");
        addInfoRow(infoSide, "Mã đơn hàng:", orderCode);
        addInfoRow(infoSide, "Trạng thái:", "Đang chờ quét mã...");

        infoSide.add(Box.createVerticalStrut(40));
        JLabel note = new JLabel("<html><i>Hệ thống tự động kích hoạt khi nhận được tiền.<br>Vui lòng không đóng cửa sổ này.</i></html>");
        note.setForeground(new Color(107, 114, 128));
        infoSide.add(note);

        JButton backBtn = new JButton("Quay lại");
        backBtn.addActionListener(e -> {
            stopPolling();
            CardLayout cl = (CardLayout) getLayout();
            cl.show(this, "SELECTION");
        });
        infoSide.add(Box.createVerticalStrut(30));
        infoSide.add(backBtn);

        // Right: VietQR
        JPanel qrSide = new JPanel(new BorderLayout());
        qrSide.setBackground(Color.WHITE);
        qrSide.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(229, 231, 235), 1, true),
            new EmptyBorder(20, 20, 20, 20)
        ));

        // VietQR
        String bankId = "MB";
        String accountNo = "0356501726";
        String accountName = "PT QUIZ PREMIUM";
        String memo = orderCode;
        
        try {
            // SỬ DỤNG URLEncoder ĐỂ FIX LỖI ẢNH QR
            String encodedName = URLEncoder.encode(accountName, StandardCharsets.UTF_8.toString());
            String encodedMemo = URLEncoder.encode(memo, StandardCharsets.UTF_8.toString());
            
            String qrUrl = String.format("https://img.vietqr.io/image/%s-%s-compact.png?amount=%s&addInfo=%s&accountName=%s",
                            bankId, accountNo, amount, encodedMemo, encodedName);

            ImageIcon icon = new ImageIcon(new URL(qrUrl));
            JLabel qrLabel = new JLabel(icon);
            qrSide.add(qrLabel, BorderLayout.CENTER);
        } catch (Exception e) {
            qrSide.add(new JLabel("Đang tải mã QR...", SwingConstants.CENTER));
        }
        
        JLabel qrHint = new JLabel("Quét mã QR để thanh toán", SwingConstants.CENTER);
        qrHint.setFont(new Font("Segoe UI", Font.BOLD, 14));
        qrHint.setBorder(new EmptyBorder(15, 0, 0, 0));
        qrSide.add(qrHint, BorderLayout.SOUTH);

        container.add(infoSide);
        container.add(qrSide);
        paymentDetail.add(container, BorderLayout.CENTER);
        paymentDetail.revalidate();
        paymentDetail.repaint();
    }

    private void addInfoRow(JPanel p, String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.WHITE);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.BOLD, 16));
        row.add(l, BorderLayout.WEST);
        row.add(v, BorderLayout.EAST);
        p.add(row);
        p.add(Box.createVerticalStrut(10));
    }

    private void startPolling() {
        if (pollingTimer != null) pollingTimer.stop();
        pollingTimer = new Timer(3000, e -> {
            String res = APIHelper.sendGet("premium/check-status?order_code=" + currentOrderCode);
            if (res.contains("\"is_premium\":true") || res.contains("\"status\":\"completed\"")) {
                stopPolling();
                JOptionPane.showMessageDialog(this, "Nâng cấp Premium thành công!");
                refreshGlobalStatus();
            }
        });
        pollingTimer.start();
    }

    private void stopPolling() {
        if (pollingTimer != null) pollingTimer.stop();
    }

    private void refreshGlobalStatus() {
        new Thread(() -> {
            String res = APIHelper.sendGet("profile/detail");
            if (res.contains("\"success\":true")) {
                String data = APIHelper.extractJsonValue(res, "data");
                UserSession.premiumStatus = Integer.parseInt(APIHelper.extractJsonValue(data, "premium_status"));
                UserSession.premiumExpire = APIHelper.extractJsonValue(data, "premium_expire");
                UserSession.attemptsToday = Integer.parseInt(APIHelper.extractJsonValue(data, "attempts_today"));
                SwingUtilities.invokeLater(() -> homeFrame.refreshSidebar());
            }
        }).start();
    }
}
