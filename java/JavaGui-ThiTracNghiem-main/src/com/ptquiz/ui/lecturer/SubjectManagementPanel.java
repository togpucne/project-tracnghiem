package com.ptquiz.ui.lecturer;

import com.ptquiz.core.APIHelper;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SubjectManagementPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private List<Subject> subjects = new ArrayList<>();
    
    private final Color COLOR_WARNING = new Color(254, 215, 170);
    private final Color COLOR_DANGER = new Color(254, 202, 202);
    private final Color COLOR_SUCCESS = new Color(187, 247, 208);
    private final Color COLOR_BG_LIGHT = new Color(248, 250, 252);
    private final Color COLOR_BORDER = new Color(226, 232, 240);

    public SubjectManagementPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(40, 40, 40, 40));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(0, 0, 30, 0));

        JLabel title = new JLabel("Quản lý môn học");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(15, 23, 42));
        header.add(title, BorderLayout.WEST);

        JButton btnAdd = createMiniButton("+ Thêm môn học mới", COLOR_SUCCESS);
        btnAdd.setPreferredSize(new Dimension(200, 40));
        
        JButton btnRefresh = new JButton("Tải lại");
        styleSecondaryButton(btnRefresh);
        
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setBackground(Color.WHITE);
        actionPanel.add(btnRefresh);
        actionPanel.add(btnAdd);
        
        header.add(actionPanel, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Actions
        btnRefresh.addActionListener(e -> loadData());
        btnAdd.addActionListener(e -> showAddEditDialog(null));

        // Table
        String[] columns = {"STT", "Tên môn học", "Số bài thi", "Thao tác"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3;
            }
        };
        
        table = new JTable(model);
        table.setRowHeight(60);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
        table.getTableHeader().setBackground(new Color(248, 250, 252));
        table.getTableHeader().setPreferredSize(new Dimension(0, 45));
        table.setShowVerticalLines(false);
        table.setGridColor(COLOR_BORDER);
        
        // Custom renderer for STT and Actions
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setMaxWidth(60);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(2).setMaxWidth(120);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        
        table.getColumnModel().getColumn(3).setMinWidth(160);
        table.getColumnModel().getColumn(3).setMaxWidth(160);
        table.getColumnModel().getColumn(3).setCellRenderer(new ActionPanelRenderer());
        table.getColumnModel().getColumn(3).setCellEditor(new ActionPanelEditor());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(COLOR_BORDER, 1));
        scroll.getViewport().setBackground(Color.WHITE);
        add(scroll, BorderLayout.CENTER);

        loadData();
    }

    public void loadData() {
        new Thread(() -> {
            try {
                String json = APIHelper.sendGet("lecturer/monhoc/list");
                if (json == null || json.isEmpty()) return;
                
                SwingUtilities.invokeLater(() -> {
                    try {
                        model.setRowCount(0);
                        subjects.clear();
                        
                        int dataStart = json.indexOf("\"data\":[");
                        if (dataStart != -1) {
                            String dataPart = json.substring(dataStart);
                            String[] items = dataPart.split("\\{");
                            
                            for (int i = 1; i < items.length; i++) {
                                String raw = "{" + items[i];
                                Subject s = new Subject();
                                s.id = APIHelper.extractJsonValue(raw, "id_monhoc");
                                s.name = APIHelper.unescapeUnicode(APIHelper.extractJsonValue(raw, "tenmonhoc"));
                                s.desc = APIHelper.unescapeUnicode(APIHelper.extractJsonValue(raw, "mieuta"));
                                s.examCount = APIHelper.extractJsonValue(raw, "so_bai_thi");
                                
                                subjects.add(s);
                                model.addRow(new Object[]{
                                    i, 
                                    "<html><div style='margin-left:10px;'><b>" + s.name + "</b><br/><font color='gray'>" + s.desc + "</font></div></html>", 
                                    s.examCount + " bài", 
                                    s
                                });
                            }
                        }
                    } catch (Exception ex) { ex.printStackTrace(); }
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void showAddEditDialog(Subject s) {
        boolean isEdit = s != null;
        JDialog dialog = new JDialog((Frame)null, isEdit ? "Sửa môn học" : "Thêm môn học mới", true);
        dialog.setSize(500, 350);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(25, 25, 25, 25));
        p.setBackground(Color.WHITE);

        JTextField nameField = new JTextField(isEdit ? s.name : "");
        p.add(new JLabel("Tên môn học:"));
        p.add(Box.createVerticalStrut(5));
        p.add(nameField);
        p.add(Box.createVerticalStrut(15));

        JTextArea descArea = new JTextArea(isEdit ? s.desc : "", 4, 20);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        p.add(new JLabel("Mô tả:"));
        p.add(Box.createVerticalStrut(5));
        p.add(new JScrollPane(descArea));

        dialog.add(p, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        bottom.setBackground(Color.WHITE);
        JButton btnCancel = createMiniButton("Hủy", Color.WHITE);
        btnCancel.addActionListener(e -> dialog.dispose());
        JButton btnSave = createMiniButton("Lưu môn học", new Color(187, 247, 208));
        btnSave.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) return;
            saveSubject(isEdit ? s.id : "0", name, descArea.getText().trim());
            dialog.dispose();
        });
        bottom.add(btnCancel);
        bottom.add(btnSave);
        dialog.add(bottom, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void saveSubject(String id, String name, String desc) {
        String payload = String.format("{\"id_monhoc\":%s, \"tenmonhoc\":\"%s\", \"mieuta\":\"%s\"}", 
            id, APIHelper.escapeJSON(name), APIHelper.escapeJSON(desc));
        new Thread(() -> {
            APIHelper.APIResponse res = APIHelper.sendPost("lecturer/monhoc/save", payload);
            SwingUtilities.invokeLater(() -> { 
                if (res.success) {
                    JOptionPane.showMessageDialog(this, "Lưu môn học thành công!");
                    loadData(); 
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi khi lưu môn học: " + res.message);
                }
            });
        }).start();
    }

    private void deleteSubject(Subject s) {
        if (JOptionPane.showConfirmDialog(this, "Xóa môn học '" + s.name + "'?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        new Thread(() -> {
            APIHelper.APIResponse res = APIHelper.sendPost("lecturer/monhoc/delete", "{\"id_monhoc\":" + s.id + "}");
            SwingUtilities.invokeLater(() -> { 
                if (res.success) {
                    JOptionPane.showMessageDialog(this, "Đã xóa môn học thành công.");
                    loadData(); 
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi khi xóa: " + res.message);
                }
            });
        }).start();
    }

    private void stylePrimaryButton(JButton button) {
        button.setBackground(new Color(37, 99, 235));
        button.setForeground(Color.BLACK);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void styleSecondaryButton(JButton button) {
        button.setBackground(Color.WHITE);
        button.setForeground(new Color(15, 23, 42));
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(new LineBorder(COLOR_BORDER, 1, true));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private JButton createMiniButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bg.darker(), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    class ActionPanelRenderer extends DefaultTableCellRenderer {
        private JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        private JButton bEdit = createMiniButton("Sửa", COLOR_WARNING);
        private JButton bDel = createMiniButton("Xóa", COLOR_DANGER);
        public ActionPanelRenderer() { p.setBackground(Color.WHITE); p.add(bEdit); p.add(bDel); }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            p.setBackground(s ? t.getSelectionBackground() : Color.WHITE); return p;
        }
    }

    class ActionPanelEditor extends DefaultCellEditor {
        private JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        private JButton bEdit = createMiniButton("Sửa", COLOR_WARNING);
        private JButton bDel = createMiniButton("Xóa", COLOR_DANGER);
        private Subject curS;
        public ActionPanelEditor() {
            super(new JCheckBox()); p.setBackground(Color.WHITE);
            bEdit.addActionListener(e -> { fireEditingStopped(); showAddEditDialog(curS); });
            bDel.addActionListener(e -> { fireEditingStopped(); deleteSubject(curS); });
            p.add(bEdit); p.add(bDel);
        }
        @Override public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) {
            curS = (Subject) v; p.setBackground(t.getSelectionBackground()); return p;
        }
        @Override public Object getCellEditorValue() { return curS; }
    }

    static class Subject { String id, name, desc, examCount; }
}
