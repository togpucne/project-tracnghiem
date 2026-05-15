package com.ptquiz.ui.student;

import com.ptquiz.core.*;
import com.ptquiz.ui.main.Home;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.Timer;
import java.util.TimerTask;

public class ExamScreen extends JFrame {
    private String idBaithi;
    private JFrame parentFrame;
    private String idLanthi = "";
    private int remainingSeconds = 0;
    private Timer timer;
    private JLabel timerLabel;
    private JPanel questionsPanel;
    private JPanel navGrid;
    private Map<String, String> selectedAnswers = new HashMap<>();
    private Set<String> flaggedQuestions = new HashSet<>();
    private Map<String, JPanel> questionCards = new HashMap<>();
    private Map<String, List<JPanel>> optionPanels = new HashMap<>();
    private Preferences prefs = Preferences.userNodeForPackage(ExamScreen.class);

    private String titleBaithi;

    public ExamScreen(JFrame parentFrame, String idBaithi, String titleBaithi) {
        this.parentFrame = parentFrame;
        this.idBaithi = idBaithi;
        this.titleBaithi = titleBaithi;
        
        // Clear old state
        flaggedQuestions.clear();
        selectedAnswers.clear();

        setTitle("Làm bài thi");
        setSize(1300, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitExam();
            }
        });

        // Initialize UI
        JPanel mainPanel = new JPanel(new BorderLayout(20, 0));
        mainPanel.setBackground(new Color(249, 250, 251));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(mainPanel);

        questionsPanel = new JPanel();
        questionsPanel.setLayout(new BoxLayout(questionsPanel, BoxLayout.Y_AXIS));
        questionsPanel.setBackground(new Color(249, 250, 251));
        
        JScrollPane scrollPane = new JScrollPane(questionsPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(new Color(249, 250, 251));
        rightPanel.setPreferredSize(new Dimension(350, 0));
        mainPanel.add(rightPanel, BorderLayout.EAST);

        // Right side contents
        JPanel stickyPanel = new JPanel();
        stickyPanel.setLayout(new BoxLayout(stickyPanel, BoxLayout.Y_AXIS));
        stickyPanel.setBackground(Color.WHITE);
        stickyPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(229, 231, 235), 1, true),
            new EmptyBorder(20, 20, 20, 20)
        ));

        // Timer
        JLabel timeTitle = new JLabel("Thời gian làm bài", SwingConstants.CENTER);
        timeTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        timeTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        timerLabel = new JLabel("00:00", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        timerLabel.setForeground(new Color(220, 38, 38)); // Red for timer
        timerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Nav Grid
        navGrid = new JPanel(new WrapLayout(FlowLayout.LEFT, 10, 10));
        navGrid.setBackground(Color.WHITE);
        
        JScrollPane navScroll = new JScrollPane(navGrid);
        navScroll.setBorder(null);
        navScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
        // Submit
        JButton submitBtn = new JButton("Nộp bài");
        submitBtn.setBackground(Color.WHITE);
        submitBtn.setForeground(new Color(34, 197, 94)); // Green-500
        submitBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        submitBtn.setFocusPainted(false);
        submitBtn.setOpaque(true);
        submitBtn.setContentAreaFilled(false);
        submitBtn.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(34, 197, 94), 1, true),
            new EmptyBorder(12, 0, 12, 0)));
        submitBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        submitBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        submitBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        submitBtn.addActionListener(e -> submitExam());

        JButton exitBtn = new JButton("Thoát");
        exitBtn.setBackground(Color.WHITE);
        exitBtn.setForeground(new Color(239, 68, 68)); // Red-500
        exitBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        exitBtn.setFocusPainted(false);
        exitBtn.setOpaque(true);
        exitBtn.setContentAreaFilled(false);
        exitBtn.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(239, 68, 68), 1, true),
            new EmptyBorder(12, 0, 12, 0)));
        exitBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        exitBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        exitBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exitBtn.addActionListener(e -> exitExam());

        stickyPanel.add(timeTitle);
        stickyPanel.add(Box.createVerticalStrut(10));
        stickyPanel.add(timerLabel);
        stickyPanel.add(Box.createVerticalStrut(20));
        stickyPanel.add(navScroll);
        stickyPanel.add(Box.createVerticalStrut(20));
        stickyPanel.add(submitBtn);
        stickyPanel.add(Box.createVerticalStrut(10));
        stickyPanel.add(exitBtn);

        rightPanel.add(stickyPanel, BorderLayout.CENTER);

        restoreAnswers();
        loadData();
    }

    private void loadData() {
        new Thread(() -> {
            String jsonResponse = APIHelper.sendGet("exam/questions?id=" + idBaithi);
            if (jsonResponse == null || jsonResponse.isEmpty() || jsonResponse.contains("\"error\"")) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Không thể tải đề thi hoặc đề thi đã bị thao tác lỗi!");
                    closeNormally();
                });
                return;
            }

            try {
                idLanthi = extractBasic(jsonResponse, "id_lanthi");
                String thoigianlamStr = extractBasic(jsonResponse, "thoigianlam");
                String elapsedStr = extractBasic(jsonResponse, "elapsed_seconds");
                
                String thoigianconlaiStr = extractBasic(jsonResponse, "thoigianconlai");
                String cautraloiTamStr = extractBasic(jsonResponse, "cautraloi_tam");
                
                int totalMinutes = Integer.parseInt(thoigianlamStr);
                int elapsed = Integer.parseInt(elapsedStr);
                
                if (!thoigianconlaiStr.isEmpty() && !thoigianconlaiStr.equals("null")) {
                    remainingSeconds = Integer.parseInt(thoigianconlaiStr);
                } else {
                    remainingSeconds = (totalMinutes * 60) - elapsed;
                }
                
                if (!cautraloiTamStr.isEmpty() && !cautraloiTamStr.equals("null")) {
                    for (String pair : cautraloiTamStr.split("\\|")) {
                        if (pair.contains(":")) {
                            String[] kv = pair.split(":");
                            selectedAnswers.put(kv[0], kv[1]);
                        }
                    }
                }
                
                if (remainingSeconds <= 0) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "Thời gian làm bài đã hết! Hệ thống sẽ tự động nộp bài.");
                        doSubmit();
                    });
                    return;
                }

                // Parse questions mechanically
                List<Question> qList = new ArrayList<>();
                int cauhoiStart = jsonResponse.indexOf("\"cauhoi\":[");
                if (cauhoiStart != -1) {
                    String cauhoiStr = jsonResponse.substring(cauhoiStart);
                    String[] questionsRaw = cauhoiStr.split("\"id_cauhoi\":");
                    for (int i = 1; i < questionsRaw.length; i++) {
                        String qRaw = questionsRaw[i];
                        String idCauhoi = qRaw.substring(0, qRaw.indexOf(",")).trim().replace("\"", "");
                        String noiDung = extractBasic("{\"id_cauhoi\":" + qRaw, "noidung");
                        
                        Question q = new Question();
                        q.id = idCauhoi;
                        q.noidung = APIHelper.unescapeUnicode(noiDung);
                        
                        String[] dapanBlocks = qRaw.split("\"id_dapan\":");
                        for (int j = 1; j < dapanBlocks.length; j++) {
                            String dRaw = dapanBlocks[j];
                            String idDapan = dRaw.substring(0, dRaw.indexOf(",")).trim().replace("\"", "");
                            String dNoidung = extractBasic("{\"id_dapan\":" + dRaw, "noidungdapan");
                            
                            Answer ans = new Answer();
                            ans.id = idDapan;
                            ans.noidung = APIHelper.unescapeUnicode(dNoidung);
                            q.answers.add(ans);
                        }
                        qList.add(q);
                    }
                }

                SwingUtilities.invokeLater(() -> {
                    buildQuestionsUI(qList);
                    startTimer();
                    setVisible(true);
                });

            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Lỗi phân tích đề thi.");
                    closeNormally();
                });
            }
        }).start();
    }

    private void buildQuestionsUI(List<Question> qList) {
        questionsPanel.removeAll();
        navGrid.removeAll();
        int stt = 1;
        questionCards.clear();
        optionPanels.clear();
        for (Question q : qList) {
            JPanel card = new JPanel();
            questionCards.put(q.id, card);
            card.setLayout(new BorderLayout(0, 15));
            card.setBackground(Color.WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(0, 0, 25, 0),
                new RoundedBorder(new Color(229, 231, 235), 20, 1) // Modern Soft Corners
            ));
            card.setBorder(BorderFactory.createCompoundBorder(
                card.getBorder(),
                new EmptyBorder(30, 30, 30, 30)
            ));

            // Header: [Badge] [Question Text] ... [Flag]
            JPanel header = new JPanel(new BorderLayout(15, 0));
            header.setOpaque(false);

            // Pill Badge for "Câu X"
            JLabel badge = new JLabel("Câu " + stt);
            badge.setFont(new Font("Segoe UI", Font.BOLD, 12));
            badge.setForeground(new Color(59, 130, 246)); // Blue-500
            badge.setBackground(new Color(239, 246, 255));
            badge.setOpaque(true);
            badge.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(191, 219, 254), 1, true),
                new EmptyBorder(4, 12, 4, 12)
            ));
            
            JPanel badgeWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            badgeWrapper.setOpaque(false);
            badgeWrapper.add(badge);
            header.add(badgeWrapper, BorderLayout.NORTH);

            JLabel qLabel = new JLabel("<html><p style='width: 800px'>" + q.noidung + "</p></html>");
            qLabel.setFont(new Font("Segoe UI", Font.PLAIN, 17));
            qLabel.setForeground(new Color(31, 41, 55));
            header.add(qLabel, BorderLayout.CENTER);

            // Flag Button (Top Right)
            JButton flagBtn = new JButton(new FlagIcon(flaggedQuestions.contains(q.id), 24));
            flagBtn.setBorderPainted(false);
            flagBtn.setContentAreaFilled(false);
            flagBtn.setFocusPainted(false);
            flagBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            final String currentQId = q.id;
            flagBtn.addActionListener(e -> {
                if (flaggedQuestions.contains(currentQId)) flaggedQuestions.remove(currentQId);
                else flaggedQuestions.add(currentQId);
                flagBtn.setIcon(new FlagIcon(flaggedQuestions.contains(currentQId), 24));
                updateNavButton(currentQId);
                saveFlagState();
            });
            
            JPanel flagWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            flagWrapper.setOpaque(false);
            flagWrapper.add(flagBtn);
            header.add(flagWrapper, BorderLayout.EAST);

            card.add(header, BorderLayout.NORTH);

            // Options Body
            JPanel optionsBody = new JPanel();
            optionsBody.setLayout(new BoxLayout(optionsBody, BoxLayout.Y_AXIS));
            optionsBody.setOpaque(false);
            
            ButtonGroup bg = new ButtonGroup();
            List<JPanel> pList = new ArrayList<>();
            for (Answer a : q.answers) {
                JPanel optionCard = new JPanel(new BorderLayout(15, 0));
                optionCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
                optionCard.setPreferredSize(new Dimension(0, 55));
                optionCard.setCursor(new Cursor(Cursor.HAND_CURSOR));
                pList.add(optionCard);

                JRadioButton rb = new JRadioButton("<html>" + a.noidung + "</html>");
                rb.setFont(new Font("Segoe UI", Font.PLAIN, 15));
                rb.setOpaque(false);
                rb.setFocusPainted(false);
                bg.add(rb);

                if (a.id.equals(selectedAnswers.get(q.id))) {
                    rb.setSelected(true);
                    styleOptionCard(optionCard, true);
                } else {
                    styleOptionCard(optionCard, false);
                }

                optionCard.add(rb, BorderLayout.CENTER);
                
                // Make clicking the card select the radio
                optionCard.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        rb.setSelected(true);
                        handleAnswerSelection(q.id, a.id, pList, optionCard);
                    }
                });
                rb.addActionListener(e -> handleAnswerSelection(q.id, a.id, pList, optionCard));

                optionsBody.add(optionCard);
                optionsBody.add(Box.createVerticalStrut(10));
            }
            optionPanels.put(q.id, pList);
            card.add(optionsBody, BorderLayout.CENTER);
            
            questionsPanel.add(card);

            JButton navBtn = new JButton(String.valueOf(stt));
            navBtn.setName("nav_" + q.id);
            navBtn.setFocusPainted(false);
            navBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            styleNavBtn(navBtn, q.id);
            
            navBtn.addActionListener(e -> {
                ((JComponent) card.getParent()).scrollRectToVisible(card.getBounds());
            });
            navGrid.add(navBtn);
            
            stt++;
        }
        
        questionsPanel.revalidate();
        questionsPanel.repaint();
        navGrid.revalidate();
        navGrid.repaint();
    }

    private void handleAnswerSelection(String qId, String aId, List<JPanel> allPanels, JPanel selectedPanel) {
        saveAnswer(qId, aId);
        for (JPanel p : allPanels) styleOptionCard(p, p == selectedPanel);
        updateNavButton(qId);
        syncDraftToServer(false);
    }

    private void styleOptionCard(JPanel p, boolean selected) {
        int radius = 15;
        if (selected) {
            p.setBackground(new Color(239, 246, 255)); // Sea Blue-50
            p.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(new Color(59, 130, 246), radius, 2), // Blue border
                new EmptyBorder(12, 20, 12, 20)
            ));
        } else {
            p.setBackground(Color.WHITE);
            p.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(new Color(229, 231, 235), radius, 1),
                new EmptyBorder(12, 20, 12, 20)
            ));
        }
    }

    private void styleNavBtn(JButton btn, String qId) {
        boolean flagged = flaggedQuestions.contains(qId);
        boolean answered = selectedAnswers.containsKey(qId);

        btn.setPreferredSize(new Dimension(50, 50));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        
        int radius = 12;
        if (answered) {
            btn.setBackground(new Color(239, 246, 255)); // Sea Blue background
            btn.setForeground(Color.BLACK);
            btn.setBorder(new RoundedBorder(new Color(59, 130, 246), radius, 2)); // Blue border
        } else {
            btn.setBackground(Color.WHITE);
            btn.setForeground(Color.BLACK);
            btn.setBorder(new RoundedBorder(new Color(229, 231, 235), radius, 1));
        }

        // Add flag indicator to Nav Button
        if (flagged) {
            btn.setIcon(new FlagIcon(true, 12));
            btn.setHorizontalTextPosition(SwingConstants.RIGHT);
            btn.setIconTextGap(2);
        } else {
            btn.setIcon(null);
        }
        
        btn.setVerticalTextPosition(SwingConstants.CENTER);
        btn.setHorizontalAlignment(SwingConstants.CENTER);
    }

    private void updateNavButton(String qId) {
        for (Component c : navGrid.getComponents()) {
            if (c instanceof JButton && ("nav_" + qId).equals(c.getName())) {
                styleNavBtn((JButton) c, qId);
                break;
            }
        }
    }

    private void updateFlagBtnStyle(JButton btn, String qId) {
        if (flaggedQuestions.contains(qId)) {
            btn.setText("<html><font color='red' size='5'>■</font> Đã cờ</html>");
            btn.setBackground(new Color(254, 226, 226));
            btn.setForeground(new Color(220, 38, 38));
            btn.setBorder(BorderFactory.createLineBorder(new Color(220, 38, 38)));
        } else {
            btn.setText("<html><font color='gray' size='5'>□</font> Gắn cờ</html>");
            btn.setBackground(Color.WHITE);
            btn.setForeground(new Color(107, 114, 128));
            btn.setBorder(BorderFactory.createLineBorder(new Color(209, 213, 219)));
        }
    }

    private void startTimer() {
        updateTimerLabel();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            int tickCount = 0;
            @Override
            public void run() {
                remainingSeconds--;
                tickCount++;
                if (tickCount % 10 == 0) {
                    syncDraftToServer(false); // Background sync
                }
                SwingUtilities.invokeLater(() -> {
                    updateTimerLabel();
                    if (remainingSeconds <= 0) {
                        timer.cancel();
                        JOptionPane.showMessageDialog(ExamScreen.this, "Hết thời gian làm bài! Hệ thống sẽ tự động nộp bài.");
                        doSubmit();
                    }
                });
            }
        }, 1000, 1000);
    }

    private void updateTimerLabel() {
        if (remainingSeconds < 0) remainingSeconds = 0;
        int m = remainingSeconds / 60;
        int s = remainingSeconds % 60;
        timerLabel.setText(String.format("%02d:%02d", m, s));
        if (remainingSeconds <= 60) {
            timerLabel.setForeground(new Color(220, 38, 38)); // Danger
        } else if (remainingSeconds <= 300) {
            timerLabel.setForeground(new Color(245, 158, 11)); // Warning
        } else {
            timerLabel.setForeground(new Color(31, 41, 55)); // Normal
        }
    }

    private String getPrefKey() {
        return "exam_" + idBaithi; 
    }

    private void saveAnswer(String qId, String ansId) {
        selectedAnswers.put(qId, ansId);
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : selectedAnswers.entrySet()) {
            sb.append(e.getKey()).append(":").append(e.getValue()).append(",");
        }
        prefs.put(getPrefKey(), sb.toString());
    }

    private void restoreAnswers() {
        String data = prefs.get(getPrefKey(), "");
        if (!data.isEmpty()) {
            for (String pair : data.split(",")) {
                if (pair.contains(":")) {
                    String[] kv = pair.split(":");
                    selectedAnswers.put(kv[0], kv[1]);
                }
            }
        }
        
        String flags = prefs.get(getPrefKey() + "_flags", "");
        if (!flags.isEmpty()) {
            for (String id : flags.split(",")) {
                if (!id.isEmpty()) flaggedQuestions.add(id);
            }
        }
    }

    private void saveFlagState() {
        StringBuilder sb = new StringBuilder();
        for (String id : flaggedQuestions) {
            sb.append(id).append(",");
        }
        prefs.put(getPrefKey() + "_flags", sb.toString());
    }

    private void exitExam() {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Bạn có chắc muốn thoát bài thi? Dữ liệu hiện tại và thời gian đếm ngược sẽ được lưu lại.", 
            "Xác nhận thoát", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            syncDraftToServer(true); // Blocking sync before closing
            closeNormally();
        }
    }
    
    private void closeNormally() {
        if (timer != null) timer.cancel();
        dispose();
        if (parentFrame != null) {
            if (parentFrame instanceof Home) {
                ((Home) parentFrame).refreshExams();
            }
            parentFrame.setVisible(true);
        }
    }

    private void syncDraftToServer(boolean blocking) {
        if (idLanthi == null || idLanthi.isEmpty()) return;
        StringBuilder payload = new StringBuilder();
        payload.append("{");
        payload.append("\"id_lanthi\":").append(idLanthi).append(",");
        payload.append("\"thoigianconlai\":").append(remainingSeconds).append(",");
        
        // Add flagged questions for API security research testing
        payload.append("\"flagged_questions\":[");
        boolean firstFlag = true;
        for (String qId : flaggedQuestions) {
            if (!firstFlag) payload.append(",");
            payload.append("\"").append(qId).append("\"");
            firstFlag = false;
        }
        payload.append("],");

        payload.append("\"answers\":{");
        
        boolean first = true;
        for (Map.Entry<String, String> e : selectedAnswers.entrySet()) {
            if (!first) payload.append(",");
            payload.append("\"").append(e.getKey()).append("\":\"").append(e.getValue()).append("\"");
            first = false;
        }
        payload.append("}");
        payload.append("}");
        
        if (blocking) {
            APIHelper.sendPost("exam/sync-draft", payload.toString());
        } else {
            new Thread(() -> {
                APIHelper.sendPost("exam/sync-draft", payload.toString());
            }).start();
        }
    }

    private void submitExam() {
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn chắc chắn muốn nộp bài?\nThời gian vẫn còn: " + timerLabel.getText(), "Xác nhận nộp bài", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            doSubmit();
        }
    }

    private void doSubmit() {
        if (timer != null) timer.cancel();
        
        StringBuilder payload = new StringBuilder();
        payload.append("{");
        payload.append("\"id_lanthi\":").append(idLanthi).append(",");
        payload.append("\"id_baithi\":").append(idBaithi).append(",");
        payload.append("\"answers\":{");
        
        boolean first = true;
        for (Map.Entry<String, String> e : selectedAnswers.entrySet()) {
            if (!first) payload.append(",");
            payload.append("\"").append(e.getKey()).append("\":\"").append(e.getValue()).append("\"");
            first = false;
        }
        payload.append("}");
        payload.append("}");

        new Thread(() -> {
            APIHelper.APIResponse res = APIHelper.sendPost("exam/submit", payload.toString());
            SwingUtilities.invokeLater(() -> {
                if (res.success) {
                    String diem = extractBasic(res.rawData, "diem");
                    String socau = extractBasic(res.rawData, "socaudung");
                    
                    // Clear local persistence for this exam
                    prefs.remove(getPrefKey());
                    prefs.remove(getPrefKey() + "_flags");
                    flaggedQuestions.clear(); 
                    
                    JOptionPane.showMessageDialog(this, "Nộp bài thành công!\nBạn đúng " + socau + " câu.\nĐiểm số: " + diem + " / 10");
                    closeNormally();
                    if(parentFrame instanceof Home) {
                        ((Home)parentFrame).switchView("HISTORY");
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi khi nộp bài: " + res.message);
                    if(remainingSeconds > 0 && timer != null) {
                        startTimer();
                    }
                }
            });
        }).start();
    }

    private String extractBasic(String json, String key) {
        java.util.regex.Matcher ms = java.util.regex.Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
        if (ms.find()) return ms.group(1);

        java.util.regex.Matcher mn = java.util.regex.Pattern.compile("\"" + key + "\"\\s*:\\s*([^,}]+)").matcher(json);
        if (mn.find()) return mn.group(1).replaceAll("[\\]\\}]", "").trim();

        return "";
    }

    // Custom Rounded Border for a modern, soft look
    class RoundedBorder extends javax.swing.border.AbstractBorder {
        private Color color;
        private int radius;
        private int thickness;
        RoundedBorder(Color color, int radius, int thickness) {
            this.color = color;
            this.radius = radius;
            this.thickness = thickness;
        }
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.drawRoundRect(x + thickness/2, y + thickness/2, width - thickness, height - thickness, radius, radius);
            g2.dispose();
        }
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius/2, radius/2, radius/2, radius/2);
        }
    }

    // Custom Icon Class to DRAW the flag manually
    class FlagIcon implements Icon {
        private boolean active;
        private int size;
        public FlagIcon(boolean active, int size) { this.active = active; this.size = size; }
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(active ? new Color(239, 68, 68) : new Color(209, 213, 219));
            // Draw flag pole
            g2.fillRect(x + 4, y + 2, 2, size - 4);
            // Draw flag triangle
            int[] px = {x + 6, x + size - 4, x + 6};
            int[] py = {y + 2, y + size/2 - 1, y + size/2 + 2};
            g2.fillPolygon(px, py, 3);
            g2.dispose();
        }
        @Override public int getIconWidth() { return size; }
        @Override public int getIconHeight() { return size; }
    }

    class Question {
        String id;
        String noidung;
        List<Answer> answers = new ArrayList<>();
    }

    class Answer {
        String id;
        String noidung;
    }
}
