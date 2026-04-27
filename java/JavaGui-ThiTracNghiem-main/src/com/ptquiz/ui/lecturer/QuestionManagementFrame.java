package com.ptquiz.ui.lecturer;

import com.ptquiz.core.APIHelper;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestionManagementFrame extends JFrame {
    private int examId;
    private String examTitle;
    private boolean isLocked = false;
    private int maxQuestions = 0;
    private int currentMonHocId = 0;

    private JTable table;
    private DefaultTableModel model;
    private List<Question> questions = new ArrayList<>();
    private JLabel progressLabel;
    private JLabel titleLabel;
    private JButton btnAdd, btnImportWord, btnImportBank;
    
    private JPanel statsContainer;
    private JTextArea txtExamDesc;
    
    private JCheckBox chkShowAnswers, chkExamShowAnswers, chkExamShuffle;

    private final Color COLOR_PRIMARY = new Color(191, 219, 254);
    private final Color COLOR_SUCCESS = new Color(187, 247, 208);
    private final Color COLOR_WARNING = new Color(254, 215, 170);
    private final Color COLOR_DANGER = new Color(254, 202, 202);
    private final Color COLOR_PURPLE = new Color(233, 213, 255);
    private final Color COLOR_TEXT = new Color(17, 24, 39);
    private final Color COLOR_TEXT_LIGHT = new Color(55, 65, 81);
    private final Color COLOR_BG_LIGHT = new Color(248, 250, 252);
    private final Color COLOR_BORDER = new Color(226, 232, 240);

    static class Answer {
        String noidungdapan;
        int dapandung;
        public Answer(String n, int d) {
            this.noidungdapan = n;
            this.dapandung = d;
        }
    }

    static class Question {
        int id_cauhoi;
        String noidungcauhoi;
        int loai_cauhoi;
        String dokho;
        List<Answer> dapan = new ArrayList<>();
    }

    public QuestionManagementFrame(int examId, String examTitle) {
        this.examId = examId;
        this.examTitle = examTitle;

        setTitle("Quản lý câu hỏi - " + examTitle);
        setSize(1100, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(Color.WHITE);

        initComponents();
        loadQuestions();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        ((JPanel) getContentPane()).setBackground(Color.WHITE);

        JPanel mainContentWrapper = new JPanel(new BorderLayout(0, 20));
        mainContentWrapper.setBackground(Color.WHITE);
        mainContentWrapper.setBorder(new EmptyBorder(20, 30, 30, 30));

        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setBackground(Color.WHITE);
        topHeader.setBorder(new EmptyBorder(0, 0, 15, 0));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(Color.WHITE);
        titleLabel = new JLabel("Bài thi: " + examTitle);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(COLOR_TEXT);
        progressLabel = new JLabel("Đang tải dữ liệu...");
        progressLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        progressLabel.setForeground(COLOR_TEXT_LIGHT);
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(progressLabel);
        topHeader.add(titlePanel, BorderLayout.WEST);

        JPanel rightHeader = new JPanel();
        rightHeader.setLayout(new BoxLayout(rightHeader, BoxLayout.Y_AXIS));
        rightHeader.setBackground(Color.WHITE);

        JPanel settingsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        settingsPanel.setBackground(Color.WHITE);
        chkShowAnswers = new JCheckBox("Xem nhanh", true);
        chkExamShowAnswers = new JCheckBox("Hiện đáp án");
        chkExamShuffle = new JCheckBox("Xáo trộn");
        styleCheckbox(chkShowAnswers);
        styleCheckbox(chkExamShowAnswers);
        styleCheckbox(chkExamShuffle);
        chkShowAnswers.addActionListener(e -> renderTable());
        chkExamShowAnswers.addActionListener(e -> updateExamSettings());
        chkExamShuffle.addActionListener(e -> updateExamSettings());
        settingsPanel.add(chkShowAnswers);
        settingsPanel.add(chkExamShowAnswers);
        settingsPanel.add(chkExamShuffle);
        rightHeader.add(settingsPanel);
        rightHeader.add(Box.createVerticalStrut(12));

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonsPanel.setBackground(Color.WHITE);
        JButton btnBack = createButton("Quay lại", Color.WHITE);
        btnBack.addActionListener(e -> dispose());
        btnImportWord = createButton("Import", COLOR_PRIMARY);
        btnImportBank = createButton("Ngân hàng", COLOR_PURPLE);
        btnAdd = createButton("Thêm câu hỏi", COLOR_SUCCESS);
        btnImportWord.addActionListener(e -> showImportWordDialog());
        btnImportBank.addActionListener(e -> showImportBankDialog());
        btnAdd.addActionListener(e -> showAddEditDialog(null));
        buttonsPanel.add(btnBack);
        buttonsPanel.add(btnImportWord);
        buttonsPanel.add(btnImportBank);
        buttonsPanel.add(btnAdd);
        rightHeader.add(buttonsPanel);
        topHeader.add(rightHeader, BorderLayout.EAST);

        JPanel centerContainer = new JPanel();
        centerContainer.setLayout(new BoxLayout(centerContainer, BoxLayout.Y_AXIS));
        centerContainer.setBackground(Color.WHITE);

        statsContainer = new JPanel(new GridLayout(0, 3, 15, 15));
        statsContainer.setBackground(Color.WHITE);
        statsContainer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        JPanel descWrapper = new JPanel(new BorderLayout(0, 8));
        descWrapper.setBackground(Color.WHITE);
        descWrapper.setBorder(new EmptyBorder(15, 0, 0, 0));
        JLabel lblDesc = new JLabel("Miêu tả bài thi");
        lblDesc.setFont(new Font("Segoe UI", Font.BOLD, 15));
        txtExamDesc = new JTextArea("Đang tải...");
        txtExamDesc.setEditable(false);
        txtExamDesc.setLineWrap(true);
        txtExamDesc.setWrapStyleWord(true);
        txtExamDesc.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtExamDesc.setBackground(COLOR_BG_LIGHT);
        txtExamDesc.setBorder(new EmptyBorder(10, 10, 10, 10));
        descWrapper.add(lblDesc, BorderLayout.NORTH);
        descWrapper.add(txtExamDesc, BorderLayout.CENTER);

        JPanel sampleWrapper = new JPanel(new BorderLayout(0, 8));
        sampleWrapper.setBackground(Color.WHITE);
        sampleWrapper.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(15, 0, 20, 0),
            BorderFactory.createDashedBorder(COLOR_TEXT_LIGHT, 1, 3, 1, true)
        ));
        JLabel lblSample = new JLabel("Mẫu Word hỗ trợ import");
        lblSample.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblSample.setBorder(new EmptyBorder(5, 10, 0, 10));
        JTextArea sampleText = new JTextArea(
            "Câu 1: PHP là viết tắt của cụm từ nào?\nA. Personal Home Page\nB. Private Home Page\nC. Preprocessor Hypertext\nD. Programming HTML Page\nĐáp án: A\nĐộ khó: Dễ"
        );
        sampleText.setEditable(false);
        sampleText.setFont(new Font("Consolas", Font.PLAIN, 13));
        sampleText.setBackground(COLOR_BG_LIGHT);
        sampleText.setBorder(new EmptyBorder(10, 10, 10, 10));
        sampleWrapper.add(lblSample, BorderLayout.NORTH);
        sampleWrapper.add(sampleText, BorderLayout.CENTER);

        centerContainer.add(topHeader);
        centerContainer.add(statsContainer);
        centerContainer.add(descWrapper);
        centerContainer.add(sampleWrapper);

        String[] columns = {"STT", "Nội dung câu hỏi", "Đáp án", "Độ khó", "Thao tác"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return column == 4; }
        };
        table = new JTable(model);
        table.setRowHeight(50);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(COLOR_BG_LIGHT);
        table.getTableHeader().setForeground(COLOR_TEXT);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setGridColor(COLOR_BORDER);
        table.setShowVerticalLines(false);

        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(2).setPreferredWidth(250);
        table.getColumnModel().getColumn(3).setMaxWidth(100);
        table.getColumnModel().getColumn(4).setMinWidth(160);
        table.getColumnModel().getColumn(4).setMaxWidth(160);
        table.getColumnModel().getColumn(4).setCellRenderer(new ActionPanelRenderer());
        table.getColumnModel().getColumn(4).setCellEditor(new ActionPanelEditor());

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        tableScroll.getViewport().setBackground(Color.WHITE);

        mainContentWrapper.add(centerContainer, BorderLayout.NORTH);
        mainContentWrapper.add(tableScroll, BorderLayout.CENTER);
        tableScroll.setPreferredSize(new Dimension(1000, 600));

        add(new JScrollPane(mainContentWrapper), BorderLayout.CENTER);
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bg.darker(), 1),
            BorderFactory.createEmptyBorder(7, 16, 7, 16)
        ));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(bg.darker()); }
            @Override public void mouseExited(MouseEvent e) { btn.setBackground(bg); }
        });
        return btn;
    }

    private void styleCheckbox(JCheckBox chk) {
        chk.setFont(new Font("Segoe UI", Font.BOLD, 13));
        chk.setBackground(Color.WHITE);
        chk.setFocusPainted(false);
        chk.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void renderTable() {
        if (model == null) return;
        model.setRowCount(0);
        boolean showAnswers = chkShowAnswers.isSelected();
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            String answersHtml = "<html>";
            if (!showAnswers) { answersHtml += "<i style='color:#94a3b8;'>Đang ẩn</i>"; }
            else {
                if (q.loai_cauhoi == 2) {
                    answersHtml += "<div style='color:#10b981; font-weight:bold;'>[Điền từ] ";
                    for (int j = 0; j < q.dapan.size(); j++) {
                        answersHtml += q.dapan.get(j).noidungdapan + (j < q.dapan.size() - 1 ? " | " : "");
                    }
                    answersHtml += "</div>";
                } else {
                    for (Answer a : q.dapan) {
                        String color = a.dapandung == 1 ? "#10b981" : "#64748b";
                        answersHtml += "<div style='color:" + color + "; font-size:12px;'>" + (a.dapandung == 1 ? "✓ " : "○ ") + a.noidungdapan + "</div>";
                    }
                }
            }
            answersHtml += "</html>";
            model.addRow(new Object[]{i + 1, q.noidungcauhoi, answersHtml, q.dokho, q});
        }
    }

    private void updateExamSettings() {
        new Thread(() -> {
            String payload = String.format("{\"id_baithi\":%d, \"only_toggle\":true, \"hien_dapan\":%b, \"xao_tron\":%b}",
                examId, chkExamShowAnswers.isSelected(), chkExamShuffle.isSelected());
            APIHelper.sendPost("lecturer/baithi/save", payload);
        }).start();
    }

    private void loadQuestions() {
        new Thread(() -> {
            String jsonResponse = APIHelper.sendGet("lecturer/cauhoi/list?id_baithi=" + examId);
            SwingUtilities.invokeLater(() -> {
                try {
                    if (jsonResponse == null || jsonResponse.isEmpty() || jsonResponse.contains("\"error\"")) return;
                    String baithiPart = jsonResponse.substring(jsonResponse.indexOf("\"baithi\":"));
                    maxQuestions = Integer.parseInt(APIHelper.extractJsonValue(baithiPart, "tongcauhoi"));
                    currentMonHocId = Integer.parseInt(APIHelper.extractJsonValue(baithiPart, "id_monhoc"));
                    isLocked = "1".equals(APIHelper.extractJsonValue(jsonResponse, "is_locked"));
                    chkExamShowAnswers.setSelected("1".equals(APIHelper.extractJsonValue(baithiPart, "hien_dapan")));
                    chkExamShuffle.setSelected("1".equals(APIHelper.extractJsonValue(baithiPart, "xao_tron")));
                    renderExamInfo(baithiPart);
                    questions.clear();
                    int qStart = jsonResponse.indexOf("\"questions\":[");
                    if (qStart != -1) {
                        String qArray = jsonResponse.substring(qStart + 12);
                        String[] items = qArray.split("\\{\"id_cauhoi\"");
                        for (int i = 1; i < items.length; i++) {
                            String raw = "{\"id_cauhoi\"" + items[i];
                            Question q = new Question();
                            q.id_cauhoi = Integer.parseInt(APIHelper.extractJsonValue(raw, "id_cauhoi"));
                            q.noidungcauhoi = APIHelper.unescapeUnicode(APIHelper.extractJsonValue(raw, "noidungcauhoi"));
                            q.loai_cauhoi = Integer.parseInt(APIHelper.extractJsonValue(raw, "loai_cauhoi"));
                            q.dokho = APIHelper.unescapeUnicode(APIHelper.extractJsonValue(raw, "dokho"));
                            int dapanStart = raw.indexOf("\"dapan\":[");
                            if (dapanStart != -1) {
                                String dapanArray = raw.substring(dapanStart + 8);
                                String[] dItems = dapanArray.split("\\{\"id_dapan\"");
                                for (int j = 1; j < dItems.length; j++) {
                                    String dRaw = "{\"id_dapan\"" + dItems[j];
                                    q.dapan.add(new Answer(APIHelper.unescapeUnicode(APIHelper.extractJsonValue(dRaw, "noidungdapan")), Integer.parseInt(APIHelper.extractJsonValue(dRaw, "dapandung"))));
                                }
                            }
                            questions.add(q);
                        }
                    }
                    progressLabel.setText("Tiến độ: " + questions.size() + " / " + maxQuestions + " câu" + (isLocked ? " (ĐÃ KHOÁ)" : ""));
                    if (isLocked) progressLabel.setForeground(COLOR_DANGER);
                    btnAdd.setEnabled(!isLocked); btnImportWord.setEnabled(!isLocked); btnImportBank.setEnabled(!isLocked);
                    renderTable();
                } catch (Exception e) { e.printStackTrace(); }
            });
        }).start();
    }

    private void renderExamInfo(String baithiJson) {
        statsContainer.removeAll();
        String status = APIHelper.unescapeUnicode(APIHelper.extractJsonValue(baithiJson, "trangthai"));
        String statusText = status.equals("open") ? "<html><b style='color:#10b981;'>Đang mở</b></html>" : "<html><b style='color:#f59e0b;'>Đã đóng</b></html>";
        addStatCard("SỐ CÂU HỎI", maxQuestions + " câu");
        addStatCard("THỜI GIAN LÀM BÀI", APIHelper.extractJsonValue(baithiJson, "thoigianlam") + " phút");
        addStatCard("TRẠNG THÁI", statusText);
        addStatCard("THỜI GIAN MỞ", APIHelper.extractJsonValue(baithiJson, "thoigianbatdau"));
        addStatCard("THỜI GIAN ĐÓNG", APIHelper.extractJsonValue(baithiJson, "thoigianketthuc"));
        addStatCard("XÁO TRỘN", "1".equals(APIHelper.extractJsonValue(baithiJson, "xao_tron")) ? "Bật" : "Tắt");
        addStatCard("NGÀY TẠO", APIHelper.extractJsonValue(baithiJson, "ngaytao"));
        String desc = APIHelper.unescapeUnicode(APIHelper.extractJsonValue(baithiJson, "mieuta"));
        txtExamDesc.setText(desc != null && !desc.isEmpty() && !desc.equals("null") ? desc : "Chưa có miêu tả cho bài thi này.");
        statsContainer.revalidate(); statsContainer.repaint();
    }

    private void addStatCard(String label, String value) {
        JPanel card = new JPanel(new BorderLayout(0, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(COLOR_BORDER), new EmptyBorder(10, 12, 10, 12)));
        JLabel lblLabel = new JLabel(label); lblLabel.setFont(new Font("Segoe UI", Font.BOLD, 11)); lblLabel.setForeground(COLOR_TEXT_LIGHT);
        JLabel lblValue = new JLabel(value != null && !value.equals("null") ? value : "---"); lblValue.setFont(new Font("Segoe UI", Font.BOLD, 14)); lblValue.setForeground(COLOR_TEXT);
        card.add(lblLabel, BorderLayout.NORTH); card.add(lblValue, BorderLayout.CENTER); statsContainer.add(card);
    }

    private void deleteQuestion(Question q) {
        if (isLocked) { JOptionPane.showMessageDialog(this, "Bài thi đã bị khóa!"); return; }
        if (JOptionPane.showConfirmDialog(this, "Xóa câu hỏi này?", "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        new Thread(() -> {
            APIHelper.APIResponse res = APIHelper.sendPost("lecturer/cauhoi/delete", "{\"id_cauhoi\": " + q.id_cauhoi + "}");
            SwingUtilities.invokeLater(() -> { if (res.success) { JOptionPane.showMessageDialog(this, "Đã xóa!"); loadQuestions(); } });
        }).start();
    }

    private void showAddEditDialog(Question q) {
        if (isLocked) { JOptionPane.showMessageDialog(this, "Bài thi đã bị khóa!"); return; }
        if (q == null && questions.size() >= maxQuestions) { JOptionPane.showMessageDialog(this, "Đã đạt giới hạn câu hỏi!"); return; }
        JDialog dialog = new JDialog(this, q == null ? "Thêm câu hỏi mới" : "Sửa câu hỏi", true);
        dialog.setSize(750, 650); dialog.setLocationRelativeTo(this); dialog.setLayout(new BorderLayout());
        JPanel formPanel = new JPanel(); formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS)); formPanel.setBorder(new EmptyBorder(25, 25, 25, 25)); formPanel.setBackground(Color.WHITE);
        JLabel lblContent = new JLabel("Nội dung câu hỏi:"); lblContent.setFont(new Font("Segoe UI", Font.BOLD, 14)); formPanel.add(lblContent); formPanel.add(Box.createVerticalStrut(8));
        JTextArea txtContent = new JTextArea(4, 40); txtContent.setLineWrap(true); txtContent.setWrapStyleWord(true); txtContent.setFont(new Font("Segoe UI", Font.PLAIN, 14)); txtContent.setBorder(new EmptyBorder(5, 5, 5, 5)); if (q != null) txtContent.setText(q.noidungcauhoi);
        JScrollPane scrollContent = new JScrollPane(txtContent); scrollContent.setBorder(BorderFactory.createLineBorder(COLOR_BORDER)); formPanel.add(scrollContent); formPanel.add(Box.createVerticalStrut(20));
        JPanel optionsRow = new JPanel(new GridLayout(1, 2, 20, 0)); optionsRow.setBackground(Color.WHITE);
        JPanel typeSub = new JPanel(new BorderLayout(0, 5)); typeSub.setBackground(Color.WHITE);
        JLabel lblType = new JLabel("Loại câu hỏi:"); lblType.setFont(new Font("Segoe UI", Font.BOLD, 14)); typeSub.add(lblType, BorderLayout.NORTH);
        JComboBox<String> cbType = new JComboBox<>(new String[]{"Trắc nghiệm", "Điền từ"}); if (q != null && q.loai_cauhoi == 2) cbType.setSelectedIndex(1); typeSub.add(cbType, BorderLayout.CENTER);
        JPanel diffSub = new JPanel(new BorderLayout(0, 5)); diffSub.setBackground(Color.WHITE);
        JLabel lblDiff = new JLabel("Độ khó:"); lblDiff.setFont(new Font("Segoe UI", Font.BOLD, 14)); diffSub.add(lblDiff, BorderLayout.NORTH);
        JComboBox<String> cbDiff = new JComboBox<>(new String[]{"Dễ", "Trung bình", "Khó"}); if (q != null) cbDiff.setSelectedItem(q.dokho); diffSub.add(cbDiff, BorderLayout.CENTER);
        optionsRow.add(typeSub); optionsRow.add(diffSub); formPanel.add(optionsRow); formPanel.add(Box.createVerticalStrut(20));
        JPanel answersHeader = new JPanel(new BorderLayout()); answersHeader.setBackground(Color.WHITE);
        JLabel lblAns = new JLabel("Đáp án:"); lblAns.setFont(new Font("Segoe UI", Font.BOLD, 14)); answersHeader.add(lblAns, BorderLayout.WEST);
        JButton btnAddAnswer = createButton("+ Thêm đáp án", COLOR_PRIMARY); answersHeader.add(btnAddAnswer, BorderLayout.EAST); formPanel.add(answersHeader); formPanel.add(Box.createVerticalStrut(10));
        JPanel answersContainer = new JPanel(); answersContainer.setLayout(new BoxLayout(answersContainer, BoxLayout.Y_AXIS)); answersContainer.setBackground(Color.WHITE);
        JScrollPane scrollAnswers = new JScrollPane(answersContainer); scrollAnswers.setPreferredSize(new Dimension(650, 250)); scrollAnswers.setBorder(BorderFactory.createLineBorder(COLOR_BORDER)); formPanel.add(scrollAnswers);
        
        List<JRadioButton> radios = new ArrayList<>(); List<JTextField> textFields = new ArrayList<>(); ButtonGroup group = new ButtonGroup();
        Runnable[] refreshUIRef = new Runnable[1];
        refreshUIRef[0] = () -> {
            answersContainer.removeAll(); radios.clear(); boolean isMulti = cbType.getSelectedIndex() == 0;
            for (int i = 0; i < textFields.size(); i++) {
                JPanel row = new JPanel(new BorderLayout(10, 0)); row.setBackground(Color.WHITE); row.setBorder(new EmptyBorder(5, 5, 5, 5));
                JTextField tf = textFields.get(i); tf.setPreferredSize(new Dimension(0, 35)); tf.setFont(new Font("Segoe UI", Font.PLAIN, 14)); row.add(tf, BorderLayout.CENTER);
                JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); right.setBackground(Color.WHITE);
                if (isMulti) { JRadioButton rb = new JRadioButton("Đúng"); rb.setBackground(Color.WHITE); group.add(rb); radios.add(rb); right.add(rb); }
                JButton btnRem = new JButton("Xóa"); btnRem.setForeground(COLOR_DANGER); btnRem.setFont(new Font("Segoe UI", Font.BOLD, 12)); btnRem.setContentAreaFilled(false); btnRem.setBorderPainted(false);
                int idx = i; btnRem.addActionListener(e -> { textFields.remove(idx); refreshUIRef[0].run(); }); right.add(btnRem);
                row.add(right, BorderLayout.EAST); answersContainer.add(row);
            }
            answersContainer.revalidate(); answersContainer.repaint();
        };
        cbType.addActionListener(e -> refreshUIRef[0].run());
        btnAddAnswer.addActionListener(e -> { textFields.add(new JTextField()); refreshUIRef[0].run(); });
        if (q != null) { 
            for (Answer a : q.dapan) { textFields.add(new JTextField(a.noidungdapan)); } 
            refreshUIRef[0].run(); 
            if (cbType.getSelectedIndex() == 0) {
                for (int i = 0; i < q.dapan.size(); i++) { if (q.dapan.get(i).dapandung == 1 && i < radios.size()) radios.get(i).setSelected(true); }
            }
        } else {
            for(int i=0; i<4; i++) textFields.add(new JTextField());
            refreshUIRef[0].run();
        }
        dialog.add(formPanel, BorderLayout.CENTER);
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15)); bottom.setBackground(COLOR_BG_LIGHT);
        JButton btnCancel = createButton("Hủy", Color.WHITE); btnCancel.addActionListener(e -> dialog.dispose());
        JButton btnSave = createButton("Lưu câu hỏi", COLOR_SUCCESS); btnSave.setForeground(Color.BLACK);
        btnSave.addActionListener(e -> {
            boolean isMulti = cbType.getSelectedIndex() == 0;
            StringBuilder optionsJson = new StringBuilder("[");
            int correctIndex = -1;
            for (int i = 0; i < textFields.size(); i++) {
                optionsJson.append("\"").append(APIHelper.escapeJSON(textFields.get(i).getText())).append("\"");
                if (i < textFields.size() - 1) optionsJson.append(",");
                if (isMulti && radios.get(i).isSelected()) correctIndex = i;
            }
            optionsJson.append("]");
            
            if (!isMulti && textFields.size() > 0) correctIndex = 0;

            String payload = String.format("{\"id_baithi\":%d, \"id_cauhoi\":%d, \"noidungcauhoi\":\"%s\", \"dokho\":\"%s\", \"loai_cauhoi\":%d, \"options\":%s, \"correct_index\":%d}", 
                examId, q==null?0:q.id_cauhoi, APIHelper.escapeJSON(txtContent.getText()), APIHelper.escapeJSON(cbDiff.getSelectedItem().toString()), cbType.getSelectedIndex()+1, optionsJson.toString(), correctIndex);
            
            new Thread(() -> { 
                APIHelper.APIResponse res = APIHelper.sendPost("lecturer/cauhoi/save", payload); 
                SwingUtilities.invokeLater(() -> { if(res.success) { dialog.dispose(); loadQuestions(); } else { JOptionPane.showMessageDialog(dialog, res.message); } }); 
            }).start();
        });
        bottom.add(btnCancel); bottom.add(btnSave); dialog.add(bottom, BorderLayout.SOUTH); dialog.setVisible(true);
    }

    private void showImportWordDialog() {
        JFileChooser fc = new JFileChooser(); if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            new Thread(() -> {
                Map<String, String> f = new HashMap<>(); f.put("id_baithi", String.valueOf(examId));
                APIHelper.APIResponse res = APIHelper.sendMultipartPost("lecturer/cauhoi/import-word", f, "word_file", fc.getSelectedFile());
                SwingUtilities.invokeLater(() -> { if (res.success) { JOptionPane.showMessageDialog(this, "Thành công!"); loadQuestions(); } });
            }).start();
        }
    }

    private void showImportBankDialog() {
        JDialog dialog = new JDialog(this, "Chọn từ ngân hàng", true);
        dialog.setSize(500, 350); dialog.setLocationRelativeTo(this); dialog.setLayout(new BorderLayout());
        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); p.setBorder(new EmptyBorder(25, 25, 25, 25)); p.setBackground(Color.WHITE);
        
        JLabel lblHeader = new JLabel("Chọn ngân hàng (dựa trên môn học):");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 13));
        p.add(lblHeader); p.add(Box.createVerticalStrut(8));
        
        JComboBox<String> cbBanks = new JComboBox<>(); 
        cbBanks.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        p.add(cbBanks); p.add(Box.createVerticalStrut(20));

        JLabel lblCount = new JLabel("Số lượng câu hỏi theo độ khó:");
        lblCount.setFont(new Font("Segoe UI", Font.BOLD, 13));
        p.add(lblCount); p.add(Box.createVerticalStrut(10));

        JPanel cntP = new JPanel(new GridLayout(2, 3, 15, 5)); cntP.setBackground(Color.WHITE);
        cntP.add(new JLabel("Dễ", SwingConstants.CENTER));
        cntP.add(new JLabel("Trung bình", SwingConstants.CENTER));
        cntP.add(new JLabel("Khó", SwingConstants.CENTER));
        
        JSpinner sE = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1)); 
        JSpinner sM = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1)); 
        JSpinner sH = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
        cntP.add(sE); cntP.add(sM); cntP.add(sH); p.add(cntP); 
        
        dialog.add(p, BorderLayout.CENTER);

        JPanel bot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15)); bot.setBackground(Color.WHITE);
        JButton btnCan = createButton("Hủy", Color.WHITE); btnCan.addActionListener(e -> dialog.dispose());
        JButton btnOk = createButton("Xác nhận thêm", new Color(139, 92, 246));
        btnOk.setForeground(Color.BLACK); 
        
        List<Integer> bIds = new ArrayList<>();
        new Thread(() -> {
            String json = APIHelper.sendGet("lecturer/nganhang/list?id_monhoc=" + currentMonHocId);
            SwingUtilities.invokeLater(() -> {
                cbBanks.removeAllItems();
                int start = json.indexOf("\"banks\":[");
                if (start != -1) {
                    String[] items = json.substring(start + 9).split("\\{\"id_nganhang\"");
                    for (int i = 1; i < items.length; i++) {
                        String raw = "{\"id_nganhang\"" + items[i];
                        bIds.add(Integer.parseInt(APIHelper.extractJsonValue(raw, "id_nganhang")));
                        cbBanks.addItem(APIHelper.unescapeUnicode(APIHelper.extractJsonValue(raw, "ten_nganhang")));
                    }
                }
            });
        }).start();

        btnOk.addActionListener(e -> {
            if (cbBanks.getSelectedIndex() < 0) return;
            String payload = String.format("{\"id_baithi\":%d, \"id_nhch\":%d, \"counts\":{\"de\":%d, \"trungbinh\":%d, \"kho\":%d}}",
                examId, bIds.get(cbBanks.getSelectedIndex()), (int)sE.getValue(), (int)sM.getValue(), (int)sH.getValue());
            new Thread(() -> {
                APIHelper.APIResponse res = APIHelper.sendPost("lecturer/cauhoi/import-bank", payload);
                SwingUtilities.invokeLater(() -> { 
                    if(res.success) { 
                        String count = APIHelper.extractJsonValue(res.rawData, "count");
                        JOptionPane.showMessageDialog(dialog, "Đã thêm thành công " + (count != null ? count : "0") + " câu hỏi!"); 
                        dialog.dispose(); 
                        loadQuestions(); 
                    } else {
                        JOptionPane.showMessageDialog(dialog, res.message);
                    }
                });
            }).start();
        });
        bot.add(btnCan); bot.add(btnOk); dialog.add(bot, BorderLayout.SOUTH); dialog.setVisible(true);
    }

    class ActionPanelRenderer extends DefaultTableCellRenderer {
        private JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        private JButton bE = createActionBtn("Sửa", COLOR_WARNING);
        private JButton bD = createActionBtn("Xóa", COLOR_DANGER);
        public ActionPanelRenderer() { p.setBackground(Color.WHITE); p.add(bE); p.add(bD); }
        private JButton createActionBtn(String t, Color bg) {
            JButton b = new JButton(t); b.setBackground(bg); b.setForeground(Color.BLACK); b.setFont(new Font("Segoe UI", Font.BOLD, 12)); b.setPreferredSize(new Dimension(65, 32)); b.setOpaque(true); b.setBorder(BorderFactory.createLineBorder(bg.darker())); return b;
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            if (isLocked) { JLabel l = new JLabel("Chỉ xem"); l.setHorizontalAlignment(0); return l; }
            p.setBackground(s ? t.getSelectionBackground() : Color.WHITE); return p;
        }
    }

    class ActionPanelEditor extends DefaultCellEditor {
        private JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        private JButton bE = new JButton("Sửa"); private JButton bD = new JButton("Xóa");
        private Question curQ;
        public ActionPanelEditor() {
            super(new JCheckBox()); p.setBackground(Color.WHITE);
            style(bE, COLOR_WARNING); style(bD, COLOR_DANGER);
            bE.addActionListener(e -> { fireEditingStopped(); showAddEditDialog(curQ); });
            bD.addActionListener(e -> { fireEditingStopped(); deleteQuestion(curQ); });
            p.add(bE); p.add(bD);
        }
        private void style(JButton b, Color bg) { b.setBackground(bg); b.setForeground(Color.BLACK); b.setFont(new Font("Segoe UI", Font.BOLD, 12)); b.setPreferredSize(new Dimension(65, 32)); b.setOpaque(true); b.setBorder(BorderFactory.createLineBorder(bg.darker())); }
        @Override public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) { curQ = (Question) v; p.setBackground(t.getSelectionBackground()); return p; }
        @Override public Object getCellEditorValue() { return curQ; }
    }
}
