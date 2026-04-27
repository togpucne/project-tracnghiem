package com.ptquiz.ui.lecturer;

import com.ptquiz.core.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class LecturerDashboard extends JPanel {
    private JPanel statsGrid;

    public LecturerDashboard() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(30, 40, 30, 40));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        
        JLabel welcomeLabel = new JLabel("Chào mừng, " + UserSession.ten + " (ID: " + UserSession.userId + ")!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        welcomeLabel.setForeground(new Color(31, 41, 55));

        JLabel subLabel = new JLabel("Theo dõi hiệu suất giảng dạy và kết quả thi của sinh viên.");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subLabel.setForeground(new Color(100, 116, 139));

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

        loadStats();
    }

    public void loadStats() {
        new Thread(() -> {
            String jsonResponse = APIHelper.sendGet("lecturer/stats");
            if (jsonResponse == null || jsonResponse.isEmpty() || jsonResponse.contains("\"error\"")) {
                return;
            }

            try {
                // The API returns stats inside a "stats" object
                int statsIndex = jsonResponse.indexOf("\"stats\":");
                if (statsIndex == -1) return;
                String statsPart = jsonResponse.substring(statsIndex);

                String subjects = APIHelper.extractJsonValue(statsPart, "subjects");
                String exams = APIHelper.extractJsonValue(statsPart, "exams");
                String questions = APIHelper.extractJsonValue(statsPart, "questions");
                String attempts = APIHelper.extractJsonValue(statsPart, "attempts");

                SwingUtilities.invokeLater(() -> {
                    statsGrid.removeAll();
                    statsGrid.add(createStatCard("Môn học", subjects, new Color(59, 130, 246), new Color(239, 246, 255)));
                    statsGrid.add(createStatCard("Bài thi", exams, new Color(8, 145, 178), new Color(236, 254, 255)));
                    statsGrid.add(createStatCard("Câu hỏi", questions, new Color(245, 158, 11), new Color(255, 247, 237)));
                    statsGrid.add(createStatCard("Lượt thi", attempts, new Color(16, 185, 129), new Color(240, 253, 244)));
                    statsGrid.revalidate();
                    statsGrid.repaint();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private JPanel createStatCard(String title, String value, Color iconColor, Color bgColor) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout(20, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(241, 245, 249), 1, true),
            new EmptyBorder(25, 25, 25, 25)
        ));

        JPanel iconPanel = new JPanel();
        iconPanel.setPreferredSize(new Dimension(10, 52));
        iconPanel.setBackground(iconColor);
        
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(new Color(100, 116, 139));

        JLabel valLabel = new JLabel(value != null ? value : "0");
        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valLabel.setForeground(new Color(30, 41, 59));

        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(valLabel);

        card.add(iconPanel, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);

        return card;
    }
}
