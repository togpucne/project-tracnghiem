package com.ptquiz.ui.lecturer;

import com.ptquiz.core.APIHelper;
import com.ptquiz.core.UserSession;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ResultViewPanel extends JPanel {
    private final Color COLOR_PRIMARY = new Color(79, 70, 229);
    private final Color COLOR_BG = new Color(249, 250, 251);
    private final Color COLOR_BORDER = new Color(229, 231, 235);
    private final Color COLOR_TEXT = new Color(17, 24, 39);
    private final Color COLOR_TEXT_LIGHT = new Color(107, 114, 128);

    private JPanel mainContent;
    private JTable table;
    private DefaultTableModel tableModel;

    public ResultViewPanel() {
        setLayout(new BorderLayout());
        setBackground(COLOR_BG);
        
        mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(COLOR_BG);
        mainContent.setBorder(new EmptyBorder(30, 40, 30, 40));
        
        showSummaryView();
        add(mainContent, BorderLayout.CENTER);
    }

    private void showSummaryView() {
        mainContent.removeAll();
        
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_BG);
        JLabel title = new JLabel("Thống kê kết quả thi");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(COLOR_TEXT);
        header.add(title, BorderLayout.WEST);
        
        JButton btnRefresh = createStyledButton("Làm mới", Color.WHITE, COLOR_PRIMARY);
        btnRefresh.addActionListener(e -> loadSummaryData());
        header.add(btnRefresh, BorderLayout.EAST);
        
        mainContent.add(header, BorderLayout.NORTH);

        // Table Summary
        String[] columns = {"STT", "Tên bài thi", "Môn học", "Lượt làm", "Điểm TB", "Thao tác", "ID"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        table = createStyledTable(tableModel);
        // Hide ID column
        table.getColumnModel().getColumn(6).setMinWidth(0);
        table.getColumnModel().getColumn(6).setMaxWidth(0);
        table.getColumnModel().getColumn(6).setPreferredWidth(0);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        mainContent.add(scrollPane, BorderLayout.CENTER);

        // Click event for "Xem danh sách"
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row >= 0 && col == 5) {
                    String idStr = tableModel.getValueAt(row, 6).toString();
                    String name = tableModel.getValueAt(row, 1).toString();
                    showSubmissionsView(idStr, name);
                }
            }
        });

        loadSummaryData();
        mainContent.revalidate();
        mainContent.repaint();
    }

    private void showSubmissionsView(String id_baithi, String ten_baithi) {
        mainContent.removeAll();
        
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_BG);
        
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        titlePanel.setBackground(COLOR_BG);
        
        JButton btnBack = createStyledButton("← Quay lại", Color.WHITE, COLOR_TEXT_LIGHT);
        btnBack.addActionListener(e -> showSummaryView());
        titlePanel.add(btnBack);
        
        JLabel title = new JLabel("Thí sinh: " + ten_baithi);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titlePanel.add(title);
        
        header.add(titlePanel, BorderLayout.WEST);
        mainContent.add(header, BorderLayout.NORTH);

        // Submissions Table
        String[] columns = {"STT", "Sinh viên", "Email", "Điểm", "Số câu đúng", "Ngày nộp", "ID"};
        DefaultTableModel subModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        JTable subTable = createStyledTable(subModel);
        // Hide ID column
        subTable.getColumnModel().getColumn(6).setMinWidth(0);
        subTable.getColumnModel().getColumn(6).setMaxWidth(0);
        subTable.getColumnModel().getColumn(6).setPreferredWidth(0);

        JScrollPane scrollPane = new JScrollPane(subTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        mainContent.add(scrollPane, BorderLayout.CENTER);

        // Click event to see individual submission detail
        subTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = subTable.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    String id_lanthi = subModel.getValueAt(row, 6).toString();
                    String ten_thisinh = (String) subModel.getValueAt(row, 1);
                    showDetailView(id_lanthi, ten_thisinh, id_baithi, ten_baithi);
                }
            }
        });

        // Load data manually (without GSON)
        new Thread(() -> {
            String json = APIHelper.sendGet("lecturer/ketqua/submissions?id_baithi=" + id_baithi);
            if (json == null || json.isEmpty()) return;

            SwingUtilities.invokeLater(() -> {
                subModel.setRowCount(0);
                int dataStart = json.indexOf("\"data\":[");
                if (dataStart != -1) {
                    String dataPart = json.substring(dataStart + 7);
                    int braceCount = 0;
                    int stt = 1;
                    StringBuilder currentItem = new StringBuilder();
                    for (int i = 0; i < dataPart.length(); i++) {
                        char c = dataPart.charAt(i);
                        if (c == '{') braceCount++;
                        if (braceCount > 0) currentItem.append(c);
                        if (c == '}') {
                            braceCount--;
                            if (braceCount == 0) {
                                String raw = currentItem.toString();
                                String diem = APIHelper.extractJsonValue(raw, "diem");
                                try {
                                    double d = Double.parseDouble(diem);
                                    diem = String.format("%.2f", d);
                                } catch (Exception ex) {}

                                subModel.addRow(new Object[]{
                                    stt++,
                                    APIHelper.unescapeUnicode(APIHelper.extractJsonValue(raw, "ten_thisinh")),
                                    APIHelper.extractJsonValue(raw, "email_thisinh"),
                                    diem,
                                    APIHelper.extractJsonValue(raw, "socaudung"),
                                    APIHelper.extractJsonValue(raw, "thoigiannop"),
                                    APIHelper.extractJsonValue(raw, "id_lanthi")
                                });
                                currentItem.setLength(0);
                            }
                        }
                        if (c == ']' && braceCount == 0) break;
                    }
                }
            });
        }).start();

        mainContent.revalidate();
        mainContent.repaint();
    }

    private void showDetailView(String id_lanthi, String ten_thisinh, String id_baithi, String ten_baithi) {
        mainContent.removeAll();
        
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_BG);
        
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        titlePanel.setBackground(COLOR_BG);
        
        JButton btnBack = createStyledButton("← Quay lại", Color.WHITE, COLOR_TEXT_LIGHT);
        btnBack.addActionListener(e -> showSubmissionsView(id_baithi, ten_baithi));
        titlePanel.add(btnBack);
        
        JLabel title = new JLabel("Bài làm: " + ten_thisinh);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titlePanel.add(title);
        
        header.add(titlePanel, BorderLayout.WEST);
        mainContent.add(header, BorderLayout.NORTH);

        // Content Area (Scrollable questions)
        JPanel questionsContainer = new JPanel();
        questionsContainer.setLayout(new BoxLayout(questionsContainer, BoxLayout.Y_AXIS));
        questionsContainer.setBackground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(questionsContainer);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainContent.add(scrollPane, BorderLayout.CENTER);

        // Load detail manually
        new Thread(() -> {
            String json = APIHelper.sendGet("lecturer/ketqua/detail?id_lanthi=" + id_lanthi);
            if (json == null || json.isEmpty()) return;

            SwingUtilities.invokeLater(() -> {
                questionsContainer.removeAll();
                int idx = 1;
                // Parse questions manually from JSON
                int qStart = json.indexOf("\"questions\":[");
                if (qStart != -1) {
                    String qPart = json.substring(qStart + 12);
                    int braceCount = 0;
                    StringBuilder currentQ = new StringBuilder();
                    for (int i = 0; i < qPart.length(); i++) {
                        char c = qPart.charAt(i);
                        if (c == '{') braceCount++;
                        if (braceCount > 0) currentQ.append(c);
                        if (c == '}') {
                            braceCount--;
                            if (braceCount == 0) {
                                questionsContainer.add(createQuestionDetailCard(idx++, currentQ.toString()));
                                questionsContainer.add(Box.createVerticalStrut(20));
                                currentQ.setLength(0);
                            }
                        }
                        if (c == ']' && braceCount == 0) break;
                    }
                }
                questionsContainer.revalidate();
                questionsContainer.repaint();
            });
        }).start();

        mainContent.revalidate();
        mainContent.repaint();
    }

    private JPanel createQuestionDetailCard(int index, String rawQ) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER),
            new EmptyBorder(20, 20, 20, 20)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        String content = APIHelper.unescapeUnicode(APIHelper.extractJsonValue(rawQ, "noidungcauhoi"));
        JLabel lblQ = new JLabel("Câu " + index + ": " + content);
        lblQ.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblQ.setForeground(COLOR_TEXT);
        card.add(lblQ);
        card.add(Box.createVerticalStrut(15));

        // Parse options manually
        String chosen = APIHelper.extractJsonValue(rawQ, "cautraloichon");
        int optStart = rawQ.indexOf("\"options\":[");
        if (optStart != -1) {
            String optPart = rawQ.substring(optStart + 10);
            int bCount = 0;
            StringBuilder currentOpt = new StringBuilder();
            for (int i = 0; i < optPart.length(); i++) {
                char c = optPart.charAt(i);
                if (c == '{') bCount++;
                if (bCount > 0) currentOpt.append(c);
                if (c == '}') {
                    bCount--;
                    if (bCount == 0) {
                        String rawOpt = currentOpt.toString();
                        String optId = APIHelper.extractJsonValue(rawOpt, "id_dapan");
                        boolean isCorrect = "1".equals(APIHelper.extractJsonValue(rawOpt, "dapandung"));
                        boolean isSelected = optId.equals(chosen);
                        String optText = APIHelper.unescapeUnicode(APIHelper.extractJsonValue(rawOpt, "noidungdapan"));

                        JPanel optRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
                        optRow.setBackground(Color.WHITE);
                        
                        JLabel icon = new JLabel(isCorrect ? "✓" : (isSelected ? "✗" : "○"));
                        icon.setFont(new Font("Segoe UI", Font.BOLD, 16));
                        if (isCorrect) icon.setForeground(new Color(34, 197, 94));
                        else if (isSelected) icon.setForeground(new Color(239, 68, 68));
                        else icon.setForeground(COLOR_TEXT_LIGHT);
                        
                        JLabel text = new JLabel(optText);
                        text.setFont(new Font("Segoe UI", isSelected || isCorrect ? Font.BOLD : Font.PLAIN, 14));
                        if (isSelected && !isCorrect) text.setForeground(new Color(239, 68, 68));
                        else if (isCorrect) text.setForeground(new Color(34, 197, 94));
                        
                        optRow.add(icon);
                        optRow.add(text);
                        card.add(optRow);
                        currentOpt.setLength(0);
                    }
                }
                if (c == ']' && bCount == 0) break;
            }
        }
        
        return card;
    }

    private void loadSummaryData() {
        new Thread(() -> {
            String json = APIHelper.sendGet("lecturer/ketqua/summary");
            if (json == null || json.isEmpty()) return;

            SwingUtilities.invokeLater(() -> {
                tableModel.setRowCount(0);
                int dataStart = json.indexOf("\"data\":[");
                if (dataStart != -1) {
                    String dataPart = json.substring(dataStart + 7);
                    int braceCount = 0;
                    int stt = 1;
                    StringBuilder currentItem = new StringBuilder();
                    for (int i = 0; i < dataPart.length(); i++) {
                        char c = dataPart.charAt(i);
                        if (c == '{') braceCount++;
                        if (braceCount > 0) currentItem.append(c);
                        if (c == '}') {
                            braceCount--;
                            if (braceCount == 0) {
                                String raw = currentItem.toString();
                                String diemTB = APIHelper.extractJsonValue(raw, "diem_trung_binh");
                                if (!diemTB.equals("null") && !diemTB.isEmpty()) {
                                    try {
                                        double d = Double.parseDouble(diemTB);
                                        diemTB = String.format("%.2f", d);
                                    } catch (Exception ex) { }
                                } else {
                                    diemTB = "0.00";
                                }

                                tableModel.addRow(new Object[]{
                                    stt++,
                                    APIHelper.unescapeUnicode(APIHelper.extractJsonValue(raw, "ten_baithi")),
                                    APIHelper.unescapeUnicode(APIHelper.extractJsonValue(raw, "tenmonhoc")),
                                    APIHelper.extractJsonValue(raw, "so_luot_lam"),
                                    diemTB,
                                    "Xem danh sách →",
                                    APIHelper.extractJsonValue(raw, "id_baithi")
                                });
                                currentItem.setLength(0);
                            }
                        }
                        if (c == ']' && braceCount == 0) break;
                    }
                }
            });
        }).start();
    }

    private JTable createStyledTable(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setRowHeight(45);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        t.setSelectionBackground(new Color(238, 242, 255));
        t.setSelectionForeground(COLOR_PRIMARY);
        t.setGridColor(COLOR_BORDER);
        t.setShowVerticalLines(false);
        
        JTableHeader header = t.getTableHeader();
        header.setBackground(Color.WHITE);
        header.setForeground(COLOR_TEXT_LIGHT);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setPreferredSize(new Dimension(0, 45));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, COLOR_BORDER));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        
        // Center some columns
        for(int i=3; i<t.getColumnCount(); i++) {
            if(i != (t.getColumnCount()-1)) t.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Action column renderer
        t.getColumnModel().getColumn(t.getColumnCount()-1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setForeground(COLOR_PRIMARY);
                label.setFont(new Font("Segoe UI", Font.BOLD, 13));
                label.setHorizontalAlignment(JLabel.CENTER);
                return label;
            }
        });

        return t;
    }

    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER),
            new EmptyBorder(8, 15, 8, 15)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
