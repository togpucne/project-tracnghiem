package com.ptquiz.ui.student;

import com.ptquiz.core.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExamLibraryPanel extends JPanel {
    private JPanel gridPanel;
    private JTextField searchField;
    private JPanel subjectFilterPanel;
    private List<ExamItem> allExams = new ArrayList<>();
    private Set<String> selectedSubjects = new HashSet<>();

    public ExamLibraryPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Sidebar Filter
        JPanel filterSidebar = new JPanel(new BorderLayout());
        filterSidebar.setPreferredSize(new Dimension(250, 0));
        filterSidebar.setBackground(new Color(249, 250, 251));
        filterSidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(229, 231, 235)));

        JLabel filterTitle = new JLabel("Lọc theo môn học");
        filterTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        filterTitle.setBorder(new EmptyBorder(20, 20, 10, 20));
        filterSidebar.add(filterTitle, BorderLayout.NORTH);

        subjectFilterPanel = new JPanel();
        subjectFilterPanel.setLayout(new BoxLayout(subjectFilterPanel, BoxLayout.Y_AXIS));
        subjectFilterPanel.setBackground(new Color(249, 250, 251));
        subjectFilterPanel.setBorder(new EmptyBorder(0, 15, 20, 15));
        
        JScrollPane filterScroll = new JScrollPane(subjectFilterPanel);
        filterScroll.setBorder(null);
        filterScroll.setBackground(new Color(249, 250, 251));
        filterSidebar.add(filterScroll, BorderLayout.CENTER);

        add(filterSidebar, BorderLayout.WEST);

        // Main Content
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(Color.WHITE);

        // Search Bar
        JPanel searchBarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        searchBarPanel.setBackground(Color.WHITE);
        
        searchField = new JTextField(30);
        searchField.setPreferredSize(new Dimension(400, 40));
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(209, 213, 219), 1, true),
            new EmptyBorder(5, 10, 5, 10)
        ));
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) {
                applyFilters();
            }
        });

        JLabel searchIcon = new JLabel("Tìm kiếm:");
        searchIcon.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        searchBarPanel.add(searchIcon);
        searchBarPanel.add(searchField);
        mainContent.add(searchBarPanel, BorderLayout.NORTH);

        // Grid
        gridPanel = new JPanel(new GridLayout(0, 3, 20, 20));
        gridPanel.setBackground(Color.WHITE);
        gridPanel.setBorder(new EmptyBorder(0, 20, 20, 20));

        // Wrapper to prevent vertical stretching in GridLayout
        JPanel gridWrapper = new JPanel(new BorderLayout());
        gridWrapper.setBackground(Color.WHITE);
        gridWrapper.add(gridPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(gridWrapper);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainContent.add(scrollPane, BorderLayout.CENTER);

        add(mainContent, BorderLayout.CENTER);

        loadData();
    }

    private void loadData() {
        new Thread(() -> {
            // Load Subjects
            String subJson = APIHelper.sendGet("exam/subjects");
            // Load Exams
            String examJson = APIHelper.sendGet("exam/list");

            SwingUtilities.invokeLater(() -> {
                parseSubjects(subJson);
                parseExams(examJson);
                applyFilters();
            });
        }).start();
    }

    private void parseSubjects(String json) {
        if (json == null || json.isEmpty()) return;
        subjectFilterPanel.removeAll();
        int dataStart = json.indexOf("\"data\":[");
        if (dataStart != -1) {
            String dataPart = json.substring(dataStart);
            String[] items = dataPart.split("\\{");
            for (int i = 1; i < items.length; i++) {
                String name = APIHelper.unescapeUnicode(extractJsonValue("{" + items[i], "tenmonhoc"));
                if (!name.isEmpty()) {
                    JCheckBox cb = new JCheckBox(name);
                    cb.setBackground(new Color(249, 250, 251));
                    cb.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                    cb.addActionListener(e -> {
                        if (cb.isSelected()) selectedSubjects.add(name);
                        else selectedSubjects.remove(name);
                        applyFilters();
                    });
                    subjectFilterPanel.add(cb);
                    subjectFilterPanel.add(Box.createVerticalStrut(5));
                }
            }
        }
        subjectFilterPanel.revalidate();
    }

    private void parseExams(String json) {
        if (json == null || json.isEmpty()) return;
        allExams.clear();
        int dataStart = json.indexOf("\"data\":[");
        if (dataStart != -1) {
            String dataPart = json.substring(dataStart);
            String[] items = dataPart.split("\\{");
            for (int i = 1; i < items.length; i++) {
                String raw = "{" + items[i];
                ExamItem item = new ExamItem();
                item.id = extractJsonValue(raw, "id_baithi");
                item.title = APIHelper.unescapeUnicode(extractJsonValue(raw, "ten_baithi"));
                item.subject = APIHelper.unescapeUnicode(extractJsonValue(raw, "tenmonhoc"));
                item.time = extractJsonValue(raw, "thoigianlam") + " phút";
                item.questions = extractJsonValue(raw, "tongcauhoi") + " câu hỏi";
                item.isOngoing = "1".equals(extractJsonValue(raw, "is_ongoing"));
                allExams.add(item);
            }
        }
    }

    private void applyFilters() {
        gridPanel.removeAll();
        String query = searchField.getText().toLowerCase().trim();

        for (ExamItem item : allExams) {
            boolean matchSearch = item.title.toLowerCase().contains(query) || item.subject.toLowerCase().contains(query);
            boolean matchSubject = selectedSubjects.isEmpty() || selectedSubjects.contains(item.subject);

            if (matchSearch && matchSubject) {
                gridPanel.add(createExamCard(item));
            }
        }
        
        // Fill empty slots for grid alignment
        int count = gridPanel.getComponentCount();
        if (count == 0) {
            gridPanel.add(new JLabel("Không tìm thấy bài thi nào."));
        }
        
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private JPanel createExamCard(ExamItem item) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(229, 231, 235), 1, true),
            new EmptyBorder(25, 20, 25, 20)
        ));
        card.setMaximumSize(new Dimension(300, 220));

        JLabel title = new JLabel("<html><div style='width: 150px;'><b>" + item.title + "</b></div></html>");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(new Color(31, 41, 55));
        card.add(title);
        card.add(Box.createVerticalStrut(10));

        JLabel info = new JLabel("<html>" + item.time + " | " + item.questions + "</html>");
        info.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        info.setForeground(new Color(107, 114, 128));
        card.add(info);
        card.add(Box.createVerticalStrut(10));

        JLabel sub = new JLabel(item.subject);
        sub.setFont(new Font("Segoe UI", Font.BOLD, 12));
        sub.setForeground(new Color(37, 99, 235));
        sub.setBackground(new Color(239, 246, 255));
        sub.setOpaque(true);
        sub.setBorder(new EmptyBorder(4, 8, 4, 8));
        card.add(sub);
        card.add(Box.createVerticalGlue());
        card.add(Box.createVerticalStrut(20));

        JButton btn = new JButton(item.isOngoing ? "Làm tiếp" : "Làm bài");
        if (item.isOngoing) {
            btn.setBackground(new Color(255, 235, 59)); // Vibrant Yellow
            btn.setForeground(new Color(31, 41, 55)); // Dark text for contrast
            btn.setBorder(new LineBorder(new Color(234, 179, 8), 1, true)); // Subtle darker yellow border
        } else {
            btn.setBackground(Color.WHITE);
            btn.setForeground(Color.BLACK);
            btn.setBorder(new LineBorder(Color.BLACK, 1, true));
        }
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setPreferredSize(new Dimension(150, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addActionListener(e -> {
            JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            new ExamScreen(topFrame, item.id, item.title).setVisible(true);
        });
        card.add(btn);

        return card;
    }

    private String extractJsonValue(String json, String key) {
        return APIHelper.extractJsonValue(json, key);
    }

    static class ExamItem {
        String id, title, subject, time, questions;
        boolean isOngoing;
    }
}
