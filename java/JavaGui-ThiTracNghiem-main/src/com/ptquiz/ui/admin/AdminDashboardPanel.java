package com.ptquiz.ui.admin;

import com.ptquiz.core.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class AdminDashboardPanel extends JPanel {
    private JPanel statsGrid;
    private final Color COLOR_PRIMARY = new Color(59, 130, 246);
    private final Color COLOR_DANGER = new Color(239, 68, 68);
    private final Color COLOR_WARNING = new Color(245, 158, 11);
    private final Color COLOR_SUCCESS = new Color(16, 185, 129);
    private final Color COLOR_TEXT = new Color(31, 41, 55);
    private final Color COLOR_TEXT_LIGHT = new Color(100, 116, 139);

    public AdminDashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(30, 40, 30, 40));

        initComponents();
        loadStats();
    }

    private void initComponents() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        
        JLabel welcomeLabel = new JLabel("Hệ thống Quản trị PT Quiz");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        welcomeLabel.setForeground(COLOR_TEXT);

        JLabel subLabel = new JLabel("Giám sát hoạt động hệ thống, quản lý người dùng và bảo mật.");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subLabel.setForeground(COLOR_TEXT_LIGHT);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(Color.WHITE);
        textPanel.add(welcomeLabel);
        textPanel.add(Box.createVerticalStrut(8));
        textPanel.add(subLabel);

        header.add(textPanel, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // Stats Grid
        statsGrid = new JPanel(new GridLayout(1, 4, 25, 0));
        statsGrid.setBackground(Color.WHITE);
        
        JPanel statsContainer = new JPanel(new BorderLayout());
        statsContainer.setBackground(Color.WHITE);
        statsContainer.add(statsGrid, BorderLayout.NORTH);
        
        add(statsContainer, BorderLayout.CENTER);
    }

    public void loadStats() {
        new Thread(() -> {
            String json = APIHelper.sendGet("admin/dashboard");
            if (json == null || json.isEmpty()) return;

            try {
                String statsPart = APIHelper.extractJsonValue(json, "stats");
                String totalUsers = APIHelper.extractJsonValue(statsPart, "total_users");
                String totalLocked = APIHelper.extractJsonValue(statsPart, "total_locked");
                String totalAlerts = APIHelper.extractJsonValue(statsPart, "total_alerts");
                String totalRequests = APIHelper.extractJsonValue(statsPart, "total_requests");

                SwingUtilities.invokeLater(() -> {
                    statsGrid.removeAll();
                    statsGrid.add(createStatCard("Tổng người dùng", totalUsers, COLOR_PRIMARY, new Color(239, 246, 255)));
                    statsGrid.add(createStatCard("Tài khoản bị khóa", totalLocked, COLOR_DANGER, new Color(254, 242, 242)));
                    statsGrid.add(createStatCard("Cảnh báo bảo mật", totalAlerts, COLOR_WARNING, new Color(255, 247, 237)));
                    statsGrid.add(createStatCard("Yêu cầu hệ thống", totalRequests, COLOR_SUCCESS, new Color(240, 253, 244)));
                    statsGrid.revalidate();
                    statsGrid.repaint();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private JPanel createStatCard(String title, String value, Color iconColor, Color bgColor) {
        JPanel card = new JPanel(new BorderLayout(20, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(241, 245, 249), 1, true),
            new EmptyBorder(25, 25, 25, 25)
        ));

        JPanel leftSide = new JPanel(new BorderLayout());
        leftSide.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(COLOR_TEXT_LIGHT);
        
        JLabel valLabel = new JLabel(value);
        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valLabel.setForeground(COLOR_TEXT);
        
        leftSide.add(titleLabel, BorderLayout.NORTH);
        leftSide.add(valLabel, BorderLayout.CENTER);
        
        card.add(leftSide, BorderLayout.CENTER);

        // A small vertical accent bar
        JPanel accent = new JPanel();
        accent.setPreferredSize(new Dimension(5, 0));
        accent.setBackground(iconColor);
        card.add(accent, BorderLayout.WEST);

        return card;
    }
}
