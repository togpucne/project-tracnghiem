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
                // Extract stats directly from the full response string for maximum reliability
                String vUsers = APIHelper.extractJsonValue(json, "total_users");
                String vLocked = APIHelper.extractJsonValue(json, "total_locked");
                String vAlerts = APIHelper.extractJsonValue(json, "total_alerts");
                String vRequests = APIHelper.extractJsonValue(json, "total_requests");

                SwingUtilities.invokeLater(() -> {
                    statsGrid.removeAll();
                    
                    // Ensure values default to "0" if missing
                    String finalUsers = (vUsers == null || vUsers.isEmpty()) ? "0" : vUsers;
                    String finalLocked = (vLocked == null || vLocked.isEmpty()) ? "0" : vLocked;
                    String finalAlerts = (vAlerts == null || vAlerts.isEmpty()) ? "0" : vAlerts;
                    String finalRequests = (vRequests == null || vRequests.isEmpty()) ? "0" : vRequests;

                    statsGrid.add(createStatCard("Người dùng", finalUsers, "👤", COLOR_PRIMARY, new Color(239, 246, 255)));
                    statsGrid.add(createStatCard("Đã khóa", finalLocked, "🔒", COLOR_DANGER, new Color(254, 242, 242)));
                    statsGrid.add(createStatCard("Cảnh báo 48h", finalAlerts, "⚡", COLOR_WARNING, new Color(255, 247, 237)));
                    statsGrid.add(createStatCard("Requests 24h", finalRequests, "🔄", COLOR_SUCCESS, new Color(240, 253, 244)));
                    
                    statsGrid.revalidate();
                    statsGrid.repaint();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private JPanel createStatCard(String title, String value, String iconText, Color iconColor, Color bgColor) {
        JPanel card = new JPanel(new BorderLayout(15, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(231, 235, 241), 1, true),
            new EmptyBorder(15, 15, 15, 15)
        ));

        // Left Icon Box
        JPanel iconBox = new JPanel(new GridBagLayout());
        iconBox.setPreferredSize(new Dimension(50, 50));
        iconBox.setBackground(bgColor);
        
        JLabel lblIcon = new JLabel(iconText);
        lblIcon.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 20));
        lblIcon.setForeground(iconColor);
        iconBox.add(lblIcon);

        // Right Text Panel
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(Color.WHITE);
        
        JLabel valLabel = new JLabel(value);
        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valLabel.setForeground(new Color(30, 41, 59));
        
        JLabel titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        titleLabel.setForeground(new Color(100, 116, 139));
        
        textPanel.add(valLabel);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(titleLabel);
        
        card.add(iconBox, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);

        return card;
    }
}
