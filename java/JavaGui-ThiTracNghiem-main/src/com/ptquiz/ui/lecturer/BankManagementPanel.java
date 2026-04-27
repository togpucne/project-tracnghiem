package com.ptquiz.ui.lecturer;

import com.ptquiz.core.APIHelper;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

public class BankManagementPanel extends JPanel {
    private JPanel leftBankList;
    private JPanel rightQuestionContainer;
    private JTable questionTable;
    private DefaultTableModel questionModel;
    private List<BankItem> banks = new ArrayList<>();
    private List<QuestionModel> currentQuestions = new ArrayList<>();
    private BankItem selectedBank = null;
    
    private final Color COLOR_PRIMARY = new Color(191, 219, 254);
    private final Color COLOR_SUCCESS = new Color(187, 247, 208);
    private final Color COLOR_WARNING = new Color(254, 215, 170);
    private final Color COLOR_DANGER = new Color(254, 202, 202);
    private final Color COLOR_PURPLE = new Color(233, 213, 255);
    private final Color COLOR_TEXT = new Color(17, 24, 39);
    private final Color COLOR_TEXT_LIGHT = new Color(75, 85, 99);
    private final Color COLOR_BG_LIGHT = new Color(248, 250, 252);
    private final Color COLOR_BORDER = new Color(226, 232, 240);

    static class Answer {
        int id_dapan;
        String noidungdapan;
        int dapandung;
        Answer() {}
        Answer(String n, int d) { this.noidungdapan = n; this.dapandung = d; }
    }

    static class QuestionModel {
        int id_cauhoi;
        String noidungcauhoi;
        int loai_cauhoi;
        String dokho;
        String trangthai;
        List<Answer> dapan = new ArrayList<>();
    }

    static class BankItem {
        String id, name, mieuta, trangthai, qCount;
        List<SubjectItem> subjects = new ArrayList<>();
        String getPrimarySubjectId() { return subjects.isEmpty() ? "0" : subjects.get(0).id; }
        String getPrimarySubjectName() { return subjects.isEmpty() ? "Chưa có môn học" : subjects.get(0).name; }
    }
    
    private List<SubjectItem> accessibleSubjects = new ArrayList<>();
    static class SubjectItem {
        String id, name, lecturer;
    }

    public BankManagementPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(30, 30, 30, 30));

        initComponents();
        loadBanks();
    }

    private void initComponents() {
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setBackground(Color.WHITE);
        topHeader.setBorder(new EmptyBorder(0, 0, 20, 0));

        JPanel titleGrp = new JPanel();
        titleGrp.setLayout(new BoxLayout(titleGrp, BoxLayout.Y_AXIS));
        titleGrp.setBackground(Color.WHITE);
        JLabel title = new JLabel("Ngân hàng câu hỏi");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(COLOR_TEXT);
        JLabel sub = new JLabel("Tạo ngân hàng, gắn môn học và quản lý câu hỏi theo độ khó.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sub.setForeground(COLOR_TEXT_LIGHT);
        titleGrp.add(title);
        titleGrp.add(sub);
        topHeader.add(titleGrp, BorderLayout.WEST);

        JButton addBankBtn = createButton("+ Thêm ngân hàng", new Color(37, 99, 235));
        addBankBtn.setForeground(Color.BLACK); 
        addBankBtn.addActionListener(e -> showBankDialog(null));
        topHeader.add(addBankBtn, BorderLayout.EAST);
        add(topHeader, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(350);
        splitPane.setBorder(null);
        splitPane.setBackground(Color.WHITE);

        JPanel leftPanel = new JPanel(new BorderLayout(0, 15));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(new EmptyBorder(0, 0, 0, 20));
        JLabel lblList = new JLabel("Danh sách ngân hàng");
        lblList.setFont(new Font("Segoe UI", Font.BOLD, 16));
        leftPanel.add(lblList, BorderLayout.NORTH);

        leftBankList = new JPanel();
        leftBankList.setLayout(new BoxLayout(leftBankList, BoxLayout.Y_AXIS));
        leftBankList.setBackground(Color.WHITE);
        JScrollPane leftScroll = new JScrollPane(leftBankList);
        leftScroll.setBorder(null);
        leftScroll.getVerticalScrollBar().setUnitIncrement(16);
        leftPanel.add(leftScroll, BorderLayout.CENTER);
        splitPane.setLeftComponent(leftPanel);

        rightQuestionContainer = new JPanel(new BorderLayout(0, 15));
        rightQuestionContainer.setBackground(Color.WHITE);
        rightQuestionContainer.setBorder(new EmptyBorder(0, 20, 0, 0));
        
        showEmptyRightPanel();
        splitPane.setRightComponent(rightQuestionContainer);

        add(splitPane, BorderLayout.CENTER);
    }

    private void showEmptyRightPanel() {
        rightQuestionContainer.removeAll();
        JLabel empty = new JLabel("Chọn một ngân hàng để xem câu hỏi", SwingConstants.CENTER);
        empty.setForeground(COLOR_TEXT_LIGHT);
        rightQuestionContainer.add(empty, BorderLayout.CENTER);
        rightQuestionContainer.revalidate();
        rightQuestionContainer.repaint();
    }

    private void showBankDetailsPanel() {
        rightQuestionContainer.removeAll();
        JPanel rHeader = new JPanel(new BorderLayout());
        rHeader.setBackground(Color.WHITE);
        
        JPanel rTitleGrp = new JPanel();
        rTitleGrp.setLayout(new BoxLayout(rTitleGrp, BoxLayout.Y_AXIS));
        rTitleGrp.setBackground(Color.WHITE);
        JLabel rTitle = new JLabel(selectedBank.name);
        rTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        JLabel rSub = new JLabel(selectedBank.getPrimarySubjectName() + " | Tổng số " + selectedBank.qCount + " câu hỏi");
        rSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        rSub.setForeground(COLOR_TEXT_LIGHT);
        rTitleGrp.add(rTitle);
        rTitleGrp.add(rSub);
        rHeader.add(rTitleGrp, BorderLayout.WEST);

        JPanel rBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rBtns.setBackground(Color.WHITE);
        JButton btnAddQ = createButton("+ Thêm câu hỏi", COLOR_SUCCESS);
        JButton btnImport = createButton("Import Word", COLOR_WARNING);
        btnAddQ.addActionListener(e -> showAddEditQuestionDialog(null));
        btnImport.addActionListener(e -> showImportDialog());
        rBtns.add(btnAddQ);
        rBtns.add(btnImport);
        rHeader.add(rBtns, BorderLayout.EAST);

        rightQuestionContainer.add(rHeader, BorderLayout.NORTH);

        String[] cols = {"STT", "Nội dung câu hỏi", "Đáp án", "Độ khó", "Thao tác"};
        questionModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 4; }
        };
        questionTable = new JTable(questionModel);
        questionTable.setRowHeight(50);
        questionTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        questionTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        questionTable.getTableHeader().setBackground(COLOR_BG_LIGHT);
        questionTable.setShowVerticalLines(false);
        questionTable.setGridColor(COLOR_BORDER);
        
        questionTable.getColumnModel().getColumn(0).setMaxWidth(50);
        questionTable.getColumnModel().getColumn(2).setPreferredWidth(200);
        questionTable.getColumnModel().getColumn(3).setMaxWidth(100);
        questionTable.getColumnModel().getColumn(4).setMinWidth(140);
        questionTable.getColumnModel().getColumn(4).setMaxWidth(140);
        
        questionTable.getColumnModel().getColumn(4).setCellRenderer(new ActionPanelRenderer());
        questionTable.getColumnModel().getColumn(4).setCellEditor(new ActionPanelEditor());

        JScrollPane tableScroll = new JScrollPane(questionTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        tableScroll.getViewport().setBackground(Color.WHITE);
        rightQuestionContainer.add(tableScroll, BorderLayout.CENTER);

        rightQuestionContainer.revalidate();
        rightQuestionContainer.repaint();
        loadBankQuestions();
    }

    private void loadBanks() {
        new Thread(() -> {
            String json = APIHelper.sendGet("lecturer/nganhang/list");
            SwingUtilities.invokeLater(() -> {
                if (json == null || json.trim().isEmpty()) {
                    leftBankList.removeAll();
                    leftBankList.add(new JLabel("Không thể kết nối máy chủ."));
                    leftBankList.revalidate(); leftBankList.repaint();
                    return;
                }
                
                System.out.println("FULL SERVER RESPONSE: " + json);
                leftBankList.removeAll();
                banks.clear();
                accessibleSubjects.clear();
                
                try {
                    // 1. Parse TOP-LEVEL subjects first (needed to match bank subjects)
                    int sStart = json.indexOf("\"subjects\":[");
                    if (sStart != -1) {
                        String sFullPart = json.substring(sStart + 11);
                        int sFullEnd = sFullPart.indexOf("]");
                        if (sFullEnd != -1) {
                            sFullPart = sFullPart.substring(0, sFullEnd + 1);
                            int curS = 0;
                            while (true) {
                                int sBegin = sFullPart.indexOf("{", curS);
                                if (sBegin == -1) break;
                                int sFinal = sFullPart.indexOf("}", sBegin);
                                if (sFinal == -1) break;
                                String sRaw = sFullPart.substring(sBegin, sFinal + 1);
                                SubjectItem s = new SubjectItem();
                                s.id = APIHelper.extractJsonValue(sRaw, "id_monhoc");
                                s.name = APIHelper.unescapeUnicode(APIHelper.extractJsonValue(sRaw, "tenmonhoc"));
                                if (s.name == null) s.name = APIHelper.unescapeUnicode(APIHelper.extractJsonValue(sRaw, "ten_monhoc"));
                                s.lecturer = APIHelper.unescapeUnicode(APIHelper.extractJsonValue(sRaw, "ten"));
                                if (s.id != null && s.name != null) accessibleSubjects.add(s);
                                curS = sFinal + 1;
                            }
                        }
                    }

                    // 2. Parse banks
                    int banksStart = json.indexOf("\"banks\":[");
                    if (banksStart != -1) {
                        String banksPart = json.substring(banksStart + 8);
                        int banksEnd = banksPart.lastIndexOf("]");
                        if (banksEnd != -1) banksPart = banksPart.substring(0, banksEnd + 1);

                        int cur = 0;
                        while (true) {
                            int bStart = banksPart.indexOf("{\"id_nganhang\"", cur);
                            if (bStart == -1) break;
                            
                            int bEnd = -1;
                            int depth = 0;
                            for (int i = bStart; i < banksPart.length(); i++) {
                                char c = banksPart.charAt(i);
                                if (c == '{') depth++;
                                else if (c == '}') {
                                    depth--;
                                    if (depth == 0) { bEnd = i; break; }
                                }
                            }
                            if (bEnd == -1) break;

                            String raw = banksPart.substring(bStart, bEnd + 1);
                            BankItem b = new BankItem();
                            b.id = APIHelper.extractJsonValue(raw, "id_nganhang");
                            b.name = APIHelper.unescapeUnicode(APIHelper.extractJsonValue(raw, "ten_nganhang"));
                            if (b.name == null || b.name.isEmpty()) b.name = APIHelper.unescapeUnicode(APIHelper.extractJsonValue(raw, "tenmonhoc"));
                            b.qCount = APIHelper.extractJsonValue(raw, "soluongcauhoi");
                            b.mieuta = APIHelper.unescapeUnicode(APIHelper.extractJsonValue(raw, "mieuta"));
                            b.trangthai = APIHelper.extractJsonValue(raw, "trangthai");
                            
                            // Try to match subject from 'tenmonhoc' field in bank if 'subjects' array is missing
                            String bankSubName = APIHelper.unescapeUnicode(APIHelper.extractJsonValue(raw, "tenmonhoc"));
                            if (bankSubName == null || bankSubName.isEmpty()) bankSubName = APIHelper.unescapeUnicode(APIHelper.extractJsonValue(raw, "ds_monhoc"));
                            
                            if (bankSubName != null && !bankSubName.isEmpty()) {
                                for (SubjectItem as : accessibleSubjects) {
                                    if (bankSubName.contains(as.name) || as.name.contains(bankSubName)) {
                                        b.subjects.add(as);
                                        break; 
                                    }
                                }
                            }

                            // Fallback to nested subjects array if present
                            int subStart = raw.indexOf("\"subjects\":[");
                            if (subStart != -1) {
                                int subEnd = raw.indexOf("]", subStart);
                                if (subEnd != -1) {
                                    String subPart = raw.substring(subStart + 11, subEnd + 1);
                                    int curSub = 0;
                                    while (true) {
                                        int innerSStart = subPart.indexOf("{", curSub);
                                        if (innerSStart == -1) break;
                                        int sEnd = subPart.indexOf("}", innerSStart);
                                        if (sEnd == -1) break;
                                        String sRaw = subPart.substring(innerSStart, sEnd + 1);
                                        SubjectItem s = new SubjectItem();
                                        s.id = APIHelper.extractJsonValue(sRaw, "id_monhoc");
                                        s.name = APIHelper.unescapeUnicode(APIHelper.extractJsonValue(sRaw, "ten_monhoc"));
                                        if (s.name == null) s.name = APIHelper.unescapeUnicode(APIHelper.extractJsonValue(sRaw, "tenmonhoc"));
                                        if (s.id != null && s.name != null) {
                                            boolean exists = false;
                                            for(SubjectItem existing : b.subjects) if(existing.id.equals(s.id)) exists = true;
                                            if(!exists) b.subjects.add(s);
                                        }
                                        curSub = sEnd + 1;
                                    }
                                }
                            }
                            
                            banks.add(b);
                            leftBankList.add(createBankCard(b));
                            leftBankList.add(Box.createVerticalStrut(12));
                            cur = bEnd + 1;
                        }
                    }
                    
                    if (banks.isEmpty()) {
                        leftBankList.add(new JLabel("Chưa có ngân hàng nào."));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                leftBankList.revalidate(); leftBankList.repaint();
            });
        }).start();
    }

    private JPanel createBankCard(BankItem b) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER, 1),
            new EmptyBorder(15, 15, 15, 15)
        ));
        card.setMaximumSize(new Dimension(500, 120)); 
        card.setPreferredSize(new Dimension(300, 120));

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBackground(Color.WHITE);
        
        JLabel name = new JLabel(b.name);
        name.setFont(new Font("Segoe UI", Font.BOLD, 15));
        JLabel subj = new JLabel(b.getPrimarySubjectName());
        subj.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subj.setForeground(COLOR_PRIMARY.darker());
        JLabel stats = new JLabel("<html><b>" + b.qCount + "</b> câu hỏi</html>");
        stats.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        stats.setForeground(COLOR_TEXT_LIGHT);

        info.add(name);
        info.add(Box.createVerticalStrut(4));
        info.add(subj);
        info.add(Box.createVerticalStrut(8));
        info.add(stats);
        card.add(info, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        actions.setBackground(Color.WHITE);
        JButton btnEdit = createMiniButton("Sửa", COLOR_WARNING);
        JButton btnDel = createMiniButton("Xóa", COLOR_DANGER);
        btnEdit.addActionListener(e -> showBankDialog(b));
        btnDel.addActionListener(e -> deleteBank(b));
        actions.add(btnEdit);
        actions.add(btnDel);
        card.add(actions, BorderLayout.SOUTH);

        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                selectedBank = b;
                showBankDetailsPanel();
            }
            @Override public void mouseEntered(MouseEvent e) { card.setBackground(COLOR_BG_LIGHT); }
            @Override public void mouseExited(MouseEvent e) { card.setBackground(Color.WHITE); }
        });

        return card;
    }

    private void loadBankQuestions() {
        if (selectedBank == null) return;
        new Thread(() -> {
            String json = APIHelper.sendGet("lecturer/nganhang/cauhoi/list?id_nganhang=" + selectedBank.id);
            SwingUtilities.invokeLater(() -> {
                questionModel.setRowCount(0);
                currentQuestions.clear();
                try {
                    int qStart = json.indexOf("\"questions\":[");
                    if (qStart != -1) {
                        String qPart = json.substring(qStart + 12);
                        int qEnd = qPart.lastIndexOf("]");
                        if (qEnd != -1) qPart = qPart.substring(0, qEnd + 1);

                        int cur = 0;
                        int count = 0;
                        while (true) {
                            int bStart = qPart.indexOf("{", cur);
                            if (bStart == -1) break;
                            
                            int bEnd = -1;
                            int depth = 0;
                            for (int i = bStart; i < qPart.length(); i++) {
                                char c = qPart.charAt(i);
                                if (c == '{') depth++;
                                else if (c == '}') {
                                    depth--;
                                    if (depth == 0) { bEnd = i; break; }
                                }
                            }
                            if (bEnd == -1) break;

                            String raw = qPart.substring(bStart, bEnd + 1);
                            QuestionModel q = new QuestionModel();
                            q.id_cauhoi = Integer.parseInt(APIHelper.extractJsonValue(raw, "id_cauhoi_nganhang"));
                            q.noidungcauhoi = APIHelper.unescapeUnicode(APIHelper.extractJsonValue(raw, "noidungcauhoi"));
                            q.loai_cauhoi = Integer.parseInt(APIHelper.extractJsonValue(raw, "loai_cauhoi"));
                            q.dokho = APIHelper.unescapeUnicode(APIHelper.extractJsonValue(raw, "dokho"));
                            q.trangthai = APIHelper.extractJsonValue(raw, "trangthai");

                            int dStart = raw.indexOf("\"dapan\":[");
                            if (dStart != -1) {
                                String dPart = raw.substring(dStart + 8);
                                int dEnd = dPart.indexOf("]");
                                if (dEnd != -1) dPart = dPart.substring(0, dEnd + 1);
                                
                                int dCur = 0;
                                while (true) {
                                    int sStart = dPart.indexOf("{", dCur);
                                    if (sStart == -1) break;
                                    int sEnd = dPart.indexOf("}", sStart);
                                    if (sEnd == -1) break;
                                    
                                    String dRaw = dPart.substring(sStart, sEnd + 1);
                                    String nd = APIHelper.unescapeUnicode(APIHelper.extractJsonValue(dRaw, "noidungdapan"));
                                    int isCorrect = Integer.parseInt(APIHelper.extractJsonValue(dRaw, "dapandung"));
                                    q.dapan.add(new Answer(nd, isCorrect));
                                    dCur = sEnd + 1;
                                }
                            }
                            
                            currentQuestions.add(q);
                            renderQuestionRow(q, ++count);
                            cur = bEnd + 1;
                        }
                        
                        for (Component c : rightQuestionContainer.getComponents()) {
                            if (c instanceof JPanel) {
                                for (Component subC : ((JPanel)c).getComponents()) {
                                    if (subC instanceof JPanel) {
                                        for (Component subSubC : ((JPanel)subC).getComponents()) {
                                            if (subSubC instanceof JLabel && ((JLabel)subSubC).getText().contains("Tổng số")) {
                                                ((JLabel)subSubC).setText(selectedBank.getPrimarySubjectName() + " | Tổng số " + currentQuestions.size() + " câu hỏi");
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) { e.printStackTrace(); }
            });
        }).start();
    }

    private void renderQuestionRow(QuestionModel q, int index) {
        String answersHtml = "<html>";
        if (q.loai_cauhoi == 2) {
            answersHtml += "<div style='color:#10b981; font-weight:bold;'>[Điền từ] ";
            for (int j = 0; j < q.dapan.size(); j++) {
                answersHtml += q.dapan.get(j).noidungdapan;
                if (j < q.dapan.size() - 1) answersHtml += " | ";
            }
            answersHtml += "</div>";
        } else {
            for (Answer a : q.dapan) {
                String color = a.dapandung == 1 ? "#10b981" : "#64748b";
                String weight = a.dapandung == 1 ? "bold" : "normal";
                String check = a.dapandung == 1 ? "✓ " : "○ ";
                answersHtml += "<div style='color:" + color + "; font-weight:" + weight + "; font-size:11px;'>" + check + a.noidungdapan + "</div>";
            }
        }
        answersHtml += "</html>";
        questionModel.addRow(new Object[]{index, q.noidungcauhoi, answersHtml, q.dokho, q});
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bg.darker(), 1),
            BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createMiniButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setBackground(bg);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bg.darker(), 1),
            BorderFactory.createEmptyBorder(3, 8, 3, 8)
        ));
        return btn;
    }

    private void showBankDialog(BankItem b) {
        Window parent = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parent, b == null ? "Thêm ngân hàng câu hỏi" : "Sửa ngân hàng câu hỏi", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(800, 650);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(25, 25, 25, 25));
        p.setBackground(Color.WHITE);

        JPanel row1 = new JPanel(new GridLayout(1, 2, 20, 0));
        row1.setBackground(Color.WHITE);
        
        JPanel gName = new JPanel(new BorderLayout(0, 5));
        gName.setBackground(Color.WHITE);
        gName.add(new JLabel("Tên ngân hàng:"), BorderLayout.NORTH);
        JTextField nameField = new JTextField(b != null ? b.name : "");
        gName.add(nameField, BorderLayout.CENTER);
        
        JPanel gStatus = new JPanel(new BorderLayout(0, 5));
        gStatus.setBackground(Color.WHITE);
        gStatus.add(new JLabel("Trạng thái:"), BorderLayout.NORTH);
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Đang hoạt động", "Ngừng hoạt động"});
        if (b != null && "inactive".equals(b.trangthai)) statusCombo.setSelectedIndex(1);
        gStatus.add(statusCombo, BorderLayout.CENTER);
        
        row1.add(gName); row1.add(gStatus);
        p.add(row1);
        p.add(Box.createVerticalStrut(15));

        p.add(new JLabel("Mô tả:"));
        p.add(Box.createVerticalStrut(5));
        JTextArea descArea = new JTextArea(4, 20);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setText(b != null ? b.mieuta : "");
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setPreferredSize(new Dimension(0, 100));
        p.add(descScroll);
        p.add(Box.createVerticalStrut(15));

        p.add(new JLabel("Môn học (Chọn môn học duy nhất cho ngân hàng này):"));
        p.add(Box.createVerticalStrut(10));
        
        JPanel subGrid = new JPanel(new GridLayout(0, 2, 10, 10));
        subGrid.setBackground(Color.WHITE);
        ButtonGroup group = new ButtonGroup();
        List<JRadioButton> rbs = new ArrayList<>();
        
        for (SubjectItem s : accessibleSubjects) {
            JPanel card = new JPanel(new BorderLayout(10, 5));
            card.setBackground(COLOR_BG_LIGHT);
            card.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
            
            JRadioButton rb = new JRadioButton();
            rb.setBackground(COLOR_BG_LIGHT);
            group.add(rb);
            rbs.add(rb);
            
            JPanel info = new JPanel();
            info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
            info.setBackground(COLOR_BG_LIGHT);
            JLabel sName = new JLabel(s.name);
            sName.setFont(new Font("Segoe UI", Font.BOLD, 13));
            JLabel sLect = new JLabel(s.lecturer);
            sLect.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            sLect.setForeground(COLOR_TEXT_LIGHT);
            info.add(sName); info.add(sLect);
            
            card.add(rb, BorderLayout.WEST);
            card.add(info, BorderLayout.CENTER);
            
            if (b != null && s.id.equals(b.getPrimarySubjectId())) rb.setSelected(true);
            subGrid.add(card);
        }
        
        JScrollPane gridScroll = new JScrollPane(subGrid);
        gridScroll.setBorder(null);
        p.add(gridScroll);

        dialog.add(p, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        bottom.setBackground(Color.WHITE);
        JButton btnCancel = createButton("Đóng", Color.WHITE);
        btnCancel.addActionListener(e -> dialog.dispose());
        JButton save = createButton("Lưu ngân hàng", COLOR_SUCCESS);
        save.addActionListener(e -> {
            if (nameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập tên ngân hàng!");
                return;
            }
            int selectedIdx = -1;
            for (int i=0; i<rbs.size(); i++) if (rbs.get(i).isSelected()) selectedIdx = i;
            if (selectedIdx == -1) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng chọn môn học!");
                return;
            }
            
            String status = statusCombo.getSelectedIndex() == 0 ? "active" : "inactive";
            String payload = String.format("{\"id_nganhang\":%s, \"ten_nganhang\":\"%s\", \"mieuta\":\"%s\", \"trangthai\":\"%s\", \"subject_ids\":[%s]}",
                (b == null ? "0" : b.id), APIHelper.escapeJSON(nameField.getText()), APIHelper.escapeJSON(descArea.getText()), status, accessibleSubjects.get(selectedIdx).id);
            
            new Thread(() -> {
                APIHelper.APIResponse res = APIHelper.sendPost("lecturer/nganhang/save", payload);
                SwingUtilities.invokeLater(() -> {
                    if (res.success) { 
                        JOptionPane.showMessageDialog(dialog, "Lưu ngân hàng thành công!");
                        dialog.dispose(); 
                        loadBanks(); 
                    }
                    else JOptionPane.showMessageDialog(dialog, res.message);
                });
            }).start();
        });
        bottom.add(btnCancel);
        bottom.add(save);
        dialog.add(bottom, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void deleteBank(BankItem b) {
        if (JOptionPane.showConfirmDialog(this, "Xóa ngân hàng '" + b.name + "'?") != JOptionPane.YES_OPTION) return;
        new Thread(() -> {
            APIHelper.APIResponse res = APIHelper.sendPost("lecturer/nganhang/delete", "{\"id_nganhang\":" + b.id + "}");
            SwingUtilities.invokeLater(() -> {
                if (res.success) { loadBanks(); showEmptyRightPanel(); }
                else JOptionPane.showMessageDialog(this, res.message);
            });
        }).start();
    }

    private void showAddEditQuestionDialog(final QuestionModel q) {
        Window parent = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parent, q == null ? "Thêm câu hỏi" : "Sửa câu hỏi", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(850, 650);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(new EmptyBorder(25, 25, 25, 25));
        formPanel.setBackground(Color.WHITE);

        final List<JTextField> tfs = new ArrayList<>();
        final List<JRadioButton> rbs = new ArrayList<>();
        final ButtonGroup group = new ButtonGroup();
        final JPanel ansContainer = new JPanel();
        ansContainer.setLayout(new BoxLayout(ansContainer, BoxLayout.Y_AXIS));
        ansContainer.setBackground(Color.WHITE);

        final JComboBox<String> cbSubject = new JComboBox<>();
        if (selectedBank.subjects.isEmpty()) {
            cbSubject.addItem("--- Trống ---");
        } else {
            for (SubjectItem s : selectedBank.subjects) cbSubject.addItem(s.name);
        }
        
        final JComboBox<String> cbType = new JComboBox<>(new String[]{"Trắc nghiệm", "Điền từ"});
        if (q != null && q.loai_cauhoi == 2) cbType.setSelectedIndex(1);
        
        final JComboBox<String> cbDiff = new JComboBox<>(new String[]{"Dễ", "Trung bình", "Khó"});
        if (q != null) cbDiff.setSelectedItem(q.dokho);
        
        final JComboBox<String> cbStatus = new JComboBox<>(new String[]{"Đang hoạt động", "Khóa"});
        if (q != null && "inactive".equals(q.trangthai)) cbStatus.setSelectedIndex(1);

        final Runnable[] refresh = { null };
        refresh[0] = () -> {
            ansContainer.removeAll();
            rbs.clear();
            boolean isMulti = cbType.getSelectedIndex() == 0;
            for (int i = 0; i < tfs.size(); i++) {
                final int idx = i;
                JPanel row = new JPanel(new BorderLayout(10, 0));
                row.setBackground(Color.WHITE);
                row.add(tfs.get(i), BorderLayout.CENTER);
                
                JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
                right.setBackground(Color.WHITE);
                
                if (isMulti) {
                    JRadioButton rb = new JRadioButton("Đúng");
                    rb.setBackground(Color.WHITE);
                    group.add(rb); rbs.add(rb);
                    right.add(rb);
                }
                
                JButton btnDel = new JButton("Xóa");
                btnDel.setForeground(COLOR_DANGER);
                btnDel.setFocusPainted(false);
                btnDel.addActionListener(e -> {
                    if (tfs.size() <= 1) return;
                    tfs.remove(idx);
                    refresh[0].run();
                });
                right.add(btnDel);
                
                row.add(right, BorderLayout.EAST);
                ansContainer.add(row);
                ansContainer.add(Box.createVerticalStrut(5));
            }
            ansContainer.revalidate(); ansContainer.repaint();
        };

        JPanel headerRow = new JPanel(new GridLayout(1, 4, 15, 0));
        headerRow.setBackground(Color.WHITE);
        headerRow.setMaximumSize(new Dimension(2000, 55));
        headerRow.add(createLabeledPanel("Môn học", cbSubject));
        headerRow.add(createLabeledPanel("Loại câu hỏi", cbType));
        headerRow.add(createLabeledPanel("Độ khó", cbDiff));
        headerRow.add(createLabeledPanel("Trạng thái", cbStatus));
        formPanel.add(headerRow);
        formPanel.add(Box.createVerticalStrut(15));

        formPanel.add(new JLabel("Nội dung câu hỏi:"));
        final JTextArea txtContent = new JTextArea(4, 40);
        txtContent.setLineWrap(true);
        txtContent.setWrapStyleWord(true);
        if (q != null) txtContent.setText(q.noidungcauhoi);
        formPanel.add(new JScrollPane(txtContent));
        formPanel.add(Box.createVerticalStrut(15));

        JPanel ansHeader = new JPanel(new BorderLayout());
        ansHeader.setBackground(Color.WHITE);
        ansHeader.add(new JLabel("Đáp án:"), BorderLayout.WEST);
        JButton btnAddA = createMiniButton("+ Thêm đáp án", COLOR_PRIMARY);
        btnAddA.addActionListener(e -> { tfs.add(new JTextField()); refresh[0].run(); });
        ansHeader.add(btnAddA, BorderLayout.EAST);
        formPanel.add(ansHeader);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(new JScrollPane(ansContainer));

        cbType.addActionListener(e -> refresh[0].run());

        if (q != null) {
            for (Answer a : q.dapan) tfs.add(new JTextField(a.noidungdapan));
            refresh[0].run();
            if (q.loai_cauhoi == 1) {
                for (int i = 0; i < q.dapan.size(); i++) {
                    if (i < rbs.size() && q.dapan.get(i).dapandung == 1) rbs.get(i).setSelected(true);
                }
            }
        } else {
            tfs.add(new JTextField()); tfs.add(new JTextField());
            refresh[0].run();
        }

        dialog.add(formPanel, BorderLayout.CENTER);
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton save = createButton("Lưu", COLOR_SUCCESS);
        save.addActionListener(e -> {
            int subIdx = cbSubject.getSelectedIndex();
            if (subIdx == -1 || cbSubject.getSelectedItem().toString().contains("Trống")) { 
                JOptionPane.showMessageDialog(dialog, "Vui lòng gán môn học cho ngân hàng trước!"); 
                return; 
            }
            if (txtContent.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(dialog, "Vui lòng nhập nội dung!"); return; }

            StringBuilder opts = new StringBuilder("[");
            for (int i = 0; i < tfs.size(); i++) {
                opts.append("\"").append(APIHelper.escapeJSON(tfs.get(i).getText())).append("\"");
                if (i < tfs.size() - 1) opts.append(",");
            }
            opts.append("]");

            int correct = -1;
            for (int i = 0; i < rbs.size(); i++) if (rbs.get(i).isSelected()) { correct = i; break; }
            
            if (cbType.getSelectedIndex() == 0 && correct == -1) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng chọn đáp án đúng cho câu hỏi trắc nghiệm!");
                return;
            }
            if (correct == -1) correct = 0; // Fallback for 'fill in' or if no RB exists

            String selSubId = selectedBank.subjects.get(subIdx).id;
            String status = (cbStatus.getSelectedIndex() == 0) ? "active" : "inactive";
            String payload = String.format("{\"id_nganhang\":\"%s\", \"id_monhoc\":\"%s\", \"id_cauhoi_nganhang\":\"%s\", \"noidungcauhoi\":\"%s\", \"dokho\":\"%s\", \"trangthai\":\"%s\", \"loai_cauhoi\":\"%d\", \"options\":%s, \"correct_index\":\"%d\"}",
                    (selectedBank.id == null || selectedBank.id.isEmpty() ? "0" : selectedBank.id),
                    selSubId, (q == null ? "0" : String.valueOf(q.id_cauhoi)), APIHelper.escapeJSON(txtContent.getText()),
                    cbDiff.getSelectedItem().toString().toLowerCase().replace("trung bình", "trungbinh").replace("dễ", "de").replace("khó", "kho"),
                    status, cbType.getSelectedIndex() + 1, opts.toString(), correct);

            new Thread(() -> {
                APIHelper.APIResponse res = APIHelper.sendPost("lecturer/nganhang/cauhoi/save", payload);
                SwingUtilities.invokeLater(() -> {
                    if (res.success) { dialog.dispose(); loadBankQuestions(); loadBanks(); }
                    else JOptionPane.showMessageDialog(dialog, res.message);
                });
            }).start();
        });
        bottom.add(save);
        dialog.add(bottom, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void deleteQuestion(QuestionModel q) {
        if (JOptionPane.showConfirmDialog(this, "Xóa câu hỏi?") != JOptionPane.YES_OPTION) return;
        new Thread(() -> {
            APIHelper.APIResponse res = APIHelper.sendPost("lecturer/nganhang/cauhoi/delete", "{\"id_cauhoi_nganhang\":" + q.id_cauhoi + "}");
            SwingUtilities.invokeLater(() -> {
                if (res.success) { loadBankQuestions(); loadBanks(); }
                else JOptionPane.showMessageDialog(this, res.message);
            });
        }).start();
    }

    private void showImportDialog() {
        JFileChooser jfc = new JFileChooser();
        if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            new Thread(() -> {
                Map<String, String> f = new HashMap<>();
                if (selectedBank != null) f.put("id_nhch", selectedBank.id);
                APIHelper.APIResponse res = APIHelper.sendMultipartPost("lecturer/nganhang/import-word", f, "word_file", jfc.getSelectedFile());
                SwingUtilities.invokeLater(() -> {
                    if (res.success) { loadBankQuestions(); loadBanks(); }
                    JOptionPane.showMessageDialog(this, res.message);
                });
            }).start();
        }
    }

    private JPanel createLabeledPanel(String label, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setBackground(Color.WHITE);
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        p.add(l, BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    class ActionPanelRenderer extends javax.swing.table.DefaultTableCellRenderer {
        private JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        private JButton bEdit = createMiniButton("Sửa", COLOR_WARNING);
        private JButton bDel = createMiniButton("Xóa", COLOR_DANGER);
        public ActionPanelRenderer() { p.setBackground(Color.WHITE); p.add(bEdit); p.add(bDel); }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            p.setBackground(s ? t.getSelectionBackground() : Color.WHITE); return p;
        }
    }

    class ActionPanelEditor extends DefaultCellEditor {
        private JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        private JButton bEdit = createMiniButton("Sửa", COLOR_WARNING);
        private JButton bDel = createMiniButton("Xóa", COLOR_DANGER);
        private QuestionModel curQ;
        public ActionPanelEditor() {
            super(new JCheckBox()); p.setBackground(Color.WHITE);
            bEdit.addActionListener(e -> { fireEditingStopped(); showAddEditQuestionDialog(curQ); });
            bDel.addActionListener(e -> { fireEditingStopped(); deleteQuestion(curQ); });
            p.add(bEdit); p.add(bDel);
        }
        @Override public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) {
            curQ = (QuestionModel) v; p.setBackground(t.getSelectionBackground()); return p;
        }
        @Override public Object getCellEditorValue() { return curQ; }
    }
}
