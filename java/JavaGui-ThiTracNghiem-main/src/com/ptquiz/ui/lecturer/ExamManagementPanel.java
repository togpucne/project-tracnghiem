package com.ptquiz.ui.lecturer;

import com.ptquiz.core.APIHelper;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ExamManagementPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private List<Exam> exams = new ArrayList<>();
    private List<Subject> cachedSubjects = new ArrayList<>();

    private final Color COLOR_WARNING = new Color(254, 215, 170);
    private final Color COLOR_DANGER = new Color(254, 202, 202);
    private final Color COLOR_SUCCESS = new Color(187, 247, 208);
    private final Color COLOR_PRIMARY = new Color(191, 219, 254);
    private final Color COLOR_BG_LIGHT = new Color(248, 250, 252);
    private final Color COLOR_BORDER = new Color(226, 232, 240);

    public ExamManagementPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(40, 40, 40, 40));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(0, 0, 30, 0));

        JLabel title = new JLabel("Quản lý đề thi");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        header.add(title, BorderLayout.WEST);

        JButton addBtn = createMiniButton("+ Tạo đề thi mới", COLOR_SUCCESS);
        addBtn.setPreferredSize(new Dimension(180, 40));
        addBtn.addActionListener(e -> showAddEditDialog(null));
        header.add(addBtn, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        String[] columns = {"STT", "Tên đề thi", "Môn học", "Thời gian", "Số câu", "Trạng thái", "Thao tác"};
        model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 6; }
        };
        table = new JTable(model);
        table.setRowHeight(60);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(COLOR_BG_LIGHT);
        table.getTableHeader().setPreferredSize(new Dimension(0, 45));
        table.setShowVerticalLines(false);
        table.setGridColor(COLOR_BORDER);
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setMaxWidth(100);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setMaxWidth(80);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(5).setMaxWidth(100);
        table.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        
        table.getColumnModel().getColumn(6).setMinWidth(240); // Increased for 3 buttons
        table.getColumnModel().getColumn(6).setMaxWidth(240);
        table.getColumnModel().getColumn(6).setCellRenderer(new ActionPanelRenderer());
        table.getColumnModel().getColumn(6).setCellEditor(new ActionPanelEditor());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        scroll.getViewport().setBackground(Color.WHITE);
        add(scroll, BorderLayout.CENTER);

        loadData();
    }

    public void loadData() {
        new Thread(() -> {
            String json = APIHelper.sendGet("lecturer/baithi/list");
            if (json == null || json.isEmpty()) return;

            SwingUtilities.invokeLater(() -> {
                model.setRowCount(0);
                exams.clear();
                cachedSubjects.clear();

                int dataStart = json.indexOf("\"data\":[");
                int dataEnd = json.indexOf("],\"subjects\":");
                if (dataEnd == -1) dataEnd = json.lastIndexOf("]");
                
                if (dataStart != -1 && dataEnd > dataStart) {
                    String dataPart = json.substring(dataStart, dataEnd + 1);
                    String[] items = dataPart.split("\\{");
                    for (int i = 1; i < items.length; i++) {
                        String raw = "{" + items[i];
                        Exam ex = new Exam();
                        ex.id = APIHelper.extractJsonValue(raw, "id_baithi");
                        ex.title = APIHelper.unescapeUnicode(APIHelper.extractJsonValue(raw, "ten_baithi"));
                        ex.subjectId = APIHelper.extractJsonValue(raw, "id_monhoc");
                        ex.subjectName = APIHelper.unescapeUnicode(APIHelper.extractJsonValue(raw, "tenmonhoc"));
                        ex.time = APIHelper.extractJsonValue(raw, "thoigianlam");
                        ex.count = APIHelper.extractJsonValue(raw, "tongcauhoi");
                        ex.status = "Đang mở".equals(APIHelper.unescapeUnicode(APIHelper.extractJsonValue(raw, "trangthai"))) ? "Đang mở" : "Ẩn";
                        ex.desc = APIHelper.unescapeUnicode(APIHelper.extractJsonValue(raw, "mieuta"));
                        
                        String st = APIHelper.extractJsonValue(raw, "thoigianbatdau");
                        if (st != null && st.length() >= 16 && !st.equals("null")) {
                            ex.startTime = st.substring(0, 16).replace(" ", "T");
                        } else { ex.startTime = ""; }
                        
                        String et = APIHelper.extractJsonValue(raw, "thoigianketthuc");
                        if (et != null && et.length() >= 16 && !et.equals("null")) {
                            ex.endTime = et.substring(0, 16).replace(" ", "T");
                        } else { ex.endTime = ""; }
                        
                        ex.shuffle = "1".equals(APIHelper.extractJsonValue(raw, "xao_tron"));
                        ex.isLocked = "1".equals(APIHelper.extractJsonValue(raw, "is_locked"));
                        
                        exams.add(ex);
                        model.addRow(new Object[]{i, "<html><b>"+ex.title+"</b></html>", ex.subjectName, ex.time + " phút", ex.count, ex.status, ex});
                    }
                }

                int subjStart = json.indexOf("\"subjects\":[");
                if (subjStart != -1) {
                    String subjPart = json.substring(subjStart, json.lastIndexOf("]") + 1);
                    String[] items = subjPart.split("\\{");
                    for (int i = 1; i < items.length; i++) {
                        String raw = "{" + items[i];
                        Subject s = new Subject();
                        s.id = APIHelper.extractJsonValue(raw, "id_monhoc");
                        s.name = APIHelper.unescapeUnicode(APIHelper.extractJsonValue(raw, "tenmonhoc"));
                        cachedSubjects.add(s);
                    }
                }
            });
        }).start();
    }

    private void showAddEditDialog(Exam ex) {
        boolean isEdit = ex != null;
        boolean locked = isEdit && ex.isLocked;
        JDialog dialog = new JDialog((Frame)null, isEdit ? "Sửa đề thi" : "Tạo đề thi mới", true);
        dialog.setSize(800, 550);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel p = new JPanel(new GridLayout(0, 4, 15, 15));
        p.setBorder(new EmptyBorder(25, 25, 10, 25));
        p.setBackground(Color.WHITE);

        p.add(new JLabel("Tên bài thi:"));
        JTextField titleField = new JTextField(isEdit ? ex.title : "");
        p.add(titleField);

        p.add(new JLabel("Môn học:"));
        JComboBox<Subject> subCombo = new JComboBox<>();
        for (Subject s : cachedSubjects) {
            subCombo.addItem(s);
            if (isEdit && s.id.equals(ex.subjectId)) subCombo.setSelectedItem(s);
        }
        subCombo.setEnabled(!locked);
        p.add(subCombo);

        p.add(new JLabel("Số câu:"));
        JTextField countField = new JTextField(isEdit ? ex.count : "10");
        countField.setEnabled(!locked);
        p.add(countField);

        p.add(new JLabel("Thời gian (phút):"));
        JTextField timeField = new JTextField(isEdit ? ex.time : "60");
        p.add(timeField);

        p.add(new JLabel("Bắt đầu:"));
        JPanel startP = new JPanel(new BorderLayout());
        JTextField startF = new JTextField(isEdit ? ex.startTime : "");
        JButton startB = new JButton("...");
        startB.addActionListener(e -> { String s = DateTimePicker.showPicker(dialog, startF.getText()); if(s!=null) startF.setText(s); });
        startP.add(startF); startP.add(startB, BorderLayout.EAST);
        p.add(startP);

        p.add(new JLabel("Kết thúc:"));
        JPanel endP = new JPanel(new BorderLayout());
        JTextField endF = new JTextField(isEdit ? ex.endTime : "");
        JButton endB = new JButton("...");
        endB.addActionListener(e -> { String s = DateTimePicker.showPicker(dialog, endF.getText()); if(s!=null) endF.setText(s); });
        endP.add(endF); endP.add(endB, BorderLayout.EAST);
        p.add(endP);

        p.add(new JLabel("Xáo trộn:"));
        JCheckBox shuffleC = new JCheckBox();
        if (isEdit) shuffleC.setSelected(ex.shuffle);
        shuffleC.setEnabled(!locked);
        p.add(shuffleC);

        p.add(new JLabel("Trạng thái:"));
        JComboBox<String> statC = new JComboBox<>(new String[]{"Đang mở", "Ẩn"});
        if (isEdit) statC.setSelectedItem(ex.status);
        p.add(statC);

        JPanel mainP = new JPanel(new BorderLayout());
        mainP.add(p, BorderLayout.NORTH);
        
        JPanel dP = new JPanel(new BorderLayout(5, 5));
        dP.setBorder(new EmptyBorder(0, 25, 10, 25));
        dP.setBackground(Color.WHITE);
        dP.add(new JLabel("Miêu tả:"), BorderLayout.NORTH);
        JTextArea dArea = new JTextArea(isEdit ? ex.desc : "", 4, 20);
        dArea.setLineWrap(true);
        dP.add(new JScrollPane(dArea), BorderLayout.CENTER);
        mainP.add(dP, BorderLayout.CENTER);

        dialog.add(mainP, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        bottom.setBackground(Color.WHITE);
        JButton btnCancel = createMiniButton("Hủy", Color.WHITE);
        btnCancel.addActionListener(e -> dialog.dispose());
        JButton btnSave = createMiniButton("Lưu bài thi", COLOR_SUCCESS);
        btnSave.addActionListener(e -> {
            Exam n = new Exam();
            n.id = isEdit ? ex.id : "0";
            n.title = titleField.getText().trim();
            Subject s = (Subject) subCombo.getSelectedItem();
            n.subjectId = s != null ? s.id : "0";
            n.status = (String) statC.getSelectedItem();
            n.count = countField.getText();
            n.time = timeField.getText();
            n.shuffle = shuffleC.isSelected();
            n.startTime = startF.getText();
            n.endTime = endF.getText();
            n.desc = dArea.getText();
            saveExam(n, dialog);
        });
        bottom.add(btnCancel); bottom.add(btnSave);
        dialog.add(bottom, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void saveExam(Exam ex, JDialog dialog) {
        String payload = String.format("{\"id_baithi\":%s, \"ten_baithi\":\"%s\", \"id_monhoc\":%s, \"trangthai\":\"%s\", \"tongcauhoi\":%s, \"thoigianlam\":%s, \"xao_tron\":%d, \"hien_dapan\":%d, \"thoigianbatdau\":\"%s\", \"thoigianketthuc\":\"%s\", \"mieuta\":\"%s\"}", 
            ex.id, APIHelper.escapeJSON(ex.title), ex.subjectId, APIHelper.escapeJSON(ex.status),
            ex.count, ex.time, ex.shuffle ? 1 : 0, ex.shuffle ? 1 : 0, ex.startTime, ex.endTime, APIHelper.escapeJSON(ex.desc));
        new Thread(() -> {
            APIHelper.APIResponse res = APIHelper.sendPost("lecturer/baithi/save", payload);
            SwingUtilities.invokeLater(() -> { 
                if(res.success) { 
                    JOptionPane.showMessageDialog(dialog, "Lưu đề thi thành công!");
                    dialog.dispose(); 
                    loadData(); 
                } else {
                    JOptionPane.showMessageDialog(dialog, "Lỗi: " + res.message);
                }
            });
        }).start();
    }

    private void deleteExam(Exam ex) {
        if (JOptionPane.showConfirmDialog(this, "Xóa bài thi '" + ex.title + "'?") != JOptionPane.YES_OPTION) return;
        new Thread(() -> {
            APIHelper.APIResponse res = APIHelper.sendPost("lecturer/baithi/delete", "{\"id_baithi\":" + ex.id + "}");
            SwingUtilities.invokeLater(() -> { 
                if (res.success) {
                    JOptionPane.showMessageDialog(this, "Đã xóa bài thi thành công.");
                    loadData(); 
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi: " + res.message);
                }
            });
        }).start();
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
        private JButton bQues = createMiniButton("Câu hỏi", COLOR_PRIMARY);
        private JButton bEdit = createMiniButton("Sửa", COLOR_WARNING);
        private JButton bDel = createMiniButton("Xóa", COLOR_DANGER);
        public ActionPanelRenderer() { 
            p.setBackground(Color.WHITE); 
            p.add(bQues); p.add(bEdit); p.add(bDel); 
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            p.setBackground(s ? t.getSelectionBackground() : Color.WHITE); return p;
        }
    }

    class ActionPanelEditor extends DefaultCellEditor {
        private JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        private JButton bQues = createMiniButton("Câu hỏi", COLOR_PRIMARY);
        private JButton bEdit = createMiniButton("Sửa", COLOR_WARNING);
        private JButton bDel = createMiniButton("Xóa", COLOR_DANGER);
        private Exam curE;
        public ActionPanelEditor() {
            super(new JCheckBox()); p.setBackground(Color.WHITE);
            bQues.addActionListener(e -> { 
                fireEditingStopped(); 
                new QuestionManagementFrame(Integer.parseInt(curE.id), curE.title).setVisible(true);
            });
            bEdit.addActionListener(e -> { fireEditingStopped(); showAddEditDialog(curE); });
            bDel.addActionListener(e -> { fireEditingStopped(); deleteExam(curE); });
            p.add(bQues); p.add(bEdit); p.add(bDel);
        }
        @Override public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) {
            curE = (Exam) v; p.setBackground(t.getSelectionBackground()); return p;
        }
        @Override public Object getCellEditorValue() { return curE; }
    }

    static class Exam { String id, title, subjectId, subjectName, time, count, status, desc, startTime, endTime; boolean shuffle, isLocked; }
    static class Subject { String id, name; @Override public String toString() { return name; } }

    static class DateTimePicker extends JDialog {
        private String selectedDateTime = null;
        private java.util.Calendar cal = java.util.Calendar.getInstance();
        private JPanel daysPanel;
        private JLabel monthYearLabel;
        private JSpinner hourSpinner;
        private JSpinner minuteSpinner;
        private JButton[] dayButtons = new JButton[42];
        private int selectedDay = -1;
        public static String showPicker(Window parent, String currentVal) {
            DateTimePicker picker = new DateTimePicker(parent, currentVal); picker.setVisible(true); return picker.selectedDateTime;
        }
        private DateTimePicker(Window parent, String currentVal) {
            super(parent, "Chọn ngày giờ", Dialog.ModalityType.APPLICATION_MODAL);
            setSize(320, 380); setLocationRelativeTo(parent); setLayout(new BorderLayout());
            if (currentVal != null && currentVal.length() >= 16) {
                try { cal.setTime(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").parse(currentVal)); } catch (Exception e) {}
            }
            JPanel top = new JPanel(new BorderLayout());
            JButton prev = new JButton("<"); prev.addActionListener(e -> { cal.add(java.util.Calendar.MONTH, -1); update(); });
            JButton next = new JButton(">"); next.addActionListener(e -> { cal.add(java.util.Calendar.MONTH, 1); update(); });
            monthYearLabel = new JLabel("", SwingConstants.CENTER);
            top.add(prev, BorderLayout.WEST); top.add(monthYearLabel, BorderLayout.CENTER); top.add(next, BorderLayout.EAST);
            add(top, BorderLayout.NORTH);
            daysPanel = new JPanel(new GridLayout(7, 7));
            String[] ds = {"CN", "T2", "T3", "T4", "T5", "T6", "T7"};
            for (String d : ds) { JLabel l = new JLabel(d, SwingConstants.CENTER); l.setForeground(Color.GRAY); daysPanel.add(l); }
            for (int i = 0; i < 42; i++) {
                JButton b = new JButton(""); b.setMargin(new Insets(2, 2, 2, 2)); b.setBackground(Color.WHITE);
                b.addActionListener(e -> { if(!b.getText().isEmpty()) { selectedDay = Integer.parseInt(b.getText()); update(); } });
                dayButtons[i] = b; daysPanel.add(b);
            }
            add(daysPanel, BorderLayout.CENTER);
            JPanel bot = new JPanel(new FlowLayout());
            hourSpinner = new JSpinner(new SpinnerNumberModel(cal.get(java.util.Calendar.HOUR_OF_DAY), 0, 23, 1));
            minuteSpinner = new JSpinner(new SpinnerNumberModel(cal.get(java.util.Calendar.MINUTE), 0, 59, 1));
            bot.add(new JLabel("Giờ:")); bot.add(hourSpinner); bot.add(new JLabel("Phút:")); bot.add(minuteSpinner);
            JButton ok = new JButton("OK");
            ok.setForeground(Color.BLACK);
            ok.addActionListener(e -> {
                if (selectedDay == -1) selectedDay = cal.get(java.util.Calendar.DAY_OF_MONTH);
                cal.set(java.util.Calendar.DAY_OF_MONTH, selectedDay);
                cal.set(java.util.Calendar.HOUR_OF_DAY, (int) hourSpinner.getValue());
                cal.set(java.util.Calendar.MINUTE, (int) minuteSpinner.getValue());
                selectedDateTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").format(cal.getTime()); dispose();
            });
            bot.add(ok); add(bot, BorderLayout.SOUTH); update();
        }
        private void update() {
            monthYearLabel.setText(new SimpleDateFormat("MM / yyyy").format(cal.getTime()));
            java.util.Calendar t = (java.util.Calendar) cal.clone(); t.set(java.util.Calendar.DAY_OF_MONTH, 1);
            int start = t.get(java.util.Calendar.DAY_OF_WEEK) - 1; int max = t.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);
            if (selectedDay == -1) selectedDay = cal.get(java.util.Calendar.DAY_OF_MONTH);
            for (int i = 0; i < 42; i++) {
                if (i >= start && i < start + max) {
                    int d = i - start + 1; dayButtons[i].setText(String.valueOf(d));
                    dayButtons[i].setBackground(d == selectedDay ? new Color(59, 130, 246) : Color.WHITE);
                    dayButtons[i].setForeground(d == selectedDay ? Color.WHITE : Color.BLACK);
                    dayButtons[i].setEnabled(true);
                } else { dayButtons[i].setText(""); dayButtons[i].setEnabled(false); dayButtons[i].setBackground(Color.WHITE); }
            }
        }
    }
}
