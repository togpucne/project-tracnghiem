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
                if (statsIndex == -1)
                    return;
                String statsPart = jsonResponse.substring(statsIndex);

                String subjects = APIHelper.extractJsonValue(statsPart, "subjects");
                String exams = APIHelper.extractJsonValue(statsPart, "exams");
                String questions = APIHelper.extractJsonValue(statsPart, "questions");
                String attempts = APIHelper.extractJsonValue(statsPart, "attempts");

                SwingUtilities.invokeLater(() -> {
                    statsGrid.removeAll();
                    statsGrid.add(createStatCard("Môn học", subjects, "📚", new Color(59, 130, 246),
                            new Color(239, 246, 255)));
                    statsGrid.add(
                            createStatCard("Bài thi", exams, "📝", new Color(8, 145, 178), new Color(236, 254, 255)));
                    statsGrid.add(createStatCard("Câu hỏi", questions, "❓", new Color(245, 158, 11),
                            new Color(255, 247, 237)));
                    statsGrid.add(createStatCard("Lượt thi", attempts, "🏁", new Color(16, 185, 129),
                            new Color(240, 253, 244)));
                    statsGrid.revalidate();
                    statsGrid.repaint();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private JPanel createStatCard(String title, String value, String iconText, Color iconColor, Color bgColor) {
        JPanel card = new JPanel(new BorderLayout(20, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(231, 235, 241), 1, true),
                new EmptyBorder(20, 20, 20, 20)));

        // Left Icon Box
        JPanel iconBox = new JPanel(new GridBagLayout());
        iconBox.setPreferredSize(new Dimension(54, 54));
        iconBox.setBackground(bgColor);
        iconBox.setBorder(new LineBorder(bgColor, 1, true));

        JLabel lblIcon = new JLabel(iconText);
        lblIcon.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 22));
        lblIcon.setForeground(iconColor);
        iconBox.add(lblIcon);

        // Right Text Panel
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(Color.WHITE);

        JLabel valLabel = new JLabel(value != null ? value : "0");
        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valLabel.setForeground(new Color(30, 41, 59));

        JLabel titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLabel.setForeground(new Color(100, 116, 139));

        textPanel.add(valLabel);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(titleLabel);

        card.add(iconBox, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);

        return card;
    }
}
