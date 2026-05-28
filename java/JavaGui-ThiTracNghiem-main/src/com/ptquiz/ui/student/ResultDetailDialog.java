package com.ptquiz.ui.student;

import com.ptquiz.core.*;
import com.ptquiz.ui.main.Home;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public class ResultDetailDialog extends JFrame {
    private String idLanthi;
    private JPanel contentPanel;

    public ResultDetailDialog(JFrame parent, String idLanthi, String tenBaithi) {
        setTitle("Chi tiết kết quả: " + tenBaithi);
        this.idLanthi = idLanthi;

        setSize(1200, 900);
        setLocationRelativeTo(parent);
        setExtendedState(JFrame.MAXIMIZED_BOTH); 
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        contentPanel = new JPanel();
        // Dùng FlowLayout với căn trái nhưng sẽ ép chiều rộng Card sau
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(243, 244, 246));

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(25);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);

        loadDetails();
        setVisible(true);
    }

    private void loadDetails() {
        new Thread(() -> {
            String jsonResponse = APIHelper.sendGet("result/detail?id=" + idLanthi);
            if (jsonResponse == null || jsonResponse.isEmpty() || jsonResponse.contains("\"error\"")) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Không thể tải chi tiết bài thi.");
                    dispose();
                });
                return;
            }

            try {
                List<QuestionResult> qList = new ArrayList<>();
                int questionsStart = jsonResponse.indexOf("\"questions\":[");
                if (questionsStart != -1) {
                    String questionsPart = jsonResponse.substring(questionsStart);
                    String[] qBlocks = questionsPart.split("\"id_cauhoi\":");
                    for (int i = 1; i < qBlocks.length; i++) {
                        String qRaw = qBlocks[i];
                        QuestionResult qr = new QuestionResult();
                        qr.noidung = APIHelper.unescapeUnicode(extractBasic("{\"id_cauhoi\":" + qRaw, "noidungcauhoi"));
                        qr.status = extractBasic("{\"id_cauhoi\":" + qRaw, "status");
                        qr.loigiaiChitiet = APIHelper.unescapeUnicode(extractBasic("{\"id_cauhoi\":" + qRaw, "loigiai_chitiet"));

                        String[] aBlocks = qRaw.split("\"id_dapan\":");
                        for (int j = 1; j < aBlocks.length; j++) {
                            String aRaw = aBlocks[j];
                            AnswerResult ar = new AnswerResult();
                            ar.noidung = APIHelper.unescapeUnicode(extractBasic("{\"id_dapan\":" + aRaw, "noidungdapan"));
                            ar.selected = "true".equals(extractBasic("{\"id_dapan\":" + aRaw, "selected"));
                            ar.isCorrect = "true".equals(extractBasic("{\"id_dapan\":" + aRaw, "dapandung"));
                            qr.answers.add(ar);
                        }
                        qList.add(qr);
                    }
                }

                SwingUtilities.invokeLater(() -> renderUI(qList));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void renderUI(List<QuestionResult> qList) {
        contentPanel.removeAll();
        contentPanel.setBorder(new EmptyBorder(30, 60, 30, 60)); // Tăng padding bên ngoài cho thoáng

        for (int i = 0; i < qList.size(); i++) {
            QuestionResult qr = qList.get(i);
            int stt = i + 1;
            Color accentColor = getStatusColor(qr.status);
            
            // Card chính
            JPanel card = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(Color.WHITE);
                    // Tăng độ bo lên 30px cho mềm mại
                    g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 30, 30));
                    
                    // Vạch màu chỉ thị bên trái
                    g2.setColor(accentColor);
                    g2.fill(new RoundRectangle2D.Double(0, 0, 10, getHeight(), 30, 30));
                    g2.fillRect(5, 0, 5, getHeight());
                    g2.dispose();
                }
            };
            card.setLayout(new BorderLayout());
            card.setOpaque(false);
            card.setAlignmentX(Component.CENTER_ALIGNMENT);
            // Ép card rộng 95% diện tích màn hình để ĐỀU NHAU
            card.setMaximumSize(new Dimension(1400, Integer.MAX_VALUE));
            card.setBorder(new EmptyBorder(30, 40, 30, 30));

            JPanel inner = new JPanel();
            inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
            inner.setOpaque(false);
            inner.setAlignmentX(Component.LEFT_ALIGNMENT);

            // Nội dung câu hỏi
            JLabel qLabel = new JLabel("<html><body style='width: 1000px'><b>Câu " + stt + ":</b> " + qr.noidung + "</body></html>");
            qLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
            qLabel.setForeground(new Color(17, 24, 39));
            qLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            inner.add(qLabel);
            inner.add(Box.createVerticalStrut(25));

            // Danh sách đáp án
            char labelChar = 'A';
            for (AnswerResult ar : qr.answers) {
                JPanel optionRow = new JPanel(new BorderLayout(15, 0));
                optionRow.setOpaque(true);
                optionRow.setAlignmentX(Component.LEFT_ALIGNMENT);
                optionRow.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(229, 231, 235), 1, true),
                    new EmptyBorder(12, 18, 12, 18)
                ));

                String text = "<html><b>" + labelChar + ".</b> " + ar.noidung;
                if (ar.selected) text += " <i style='color: #6B7280'>(Bạn chọn)</i>";
                text += "</html>";
                
                JLabel aLabel = new JLabel(text);
                aLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));

                if (ar.isCorrect) {
                    optionRow.setBackground(new Color(236, 253, 245));
                    optionRow.setBorder(new LineBorder(new Color(16, 185, 129), 1, true));
                    aLabel.setForeground(new Color(6, 95, 70));
                } else if (ar.selected && !ar.isCorrect) {
                    optionRow.setBackground(new Color(254, 242, 242));
                    optionRow.setBorder(new LineBorder(new Color(239, 68, 68), 1, true));
                    aLabel.setForeground(new Color(153, 27, 27));
                } else {
                    optionRow.setBackground(Color.WHITE);
                }

                optionRow.add(aLabel, BorderLayout.CENTER);
                inner.add(optionRow);
                inner.add(Box.createVerticalStrut(12));
                labelChar++;
            }

            // Lời giải chi tiết
            if (qr.loigiaiChitiet != null && !qr.loigiaiChitiet.isEmpty() && !qr.loigiaiChitiet.equals("null")) {
                inner.add(Box.createVerticalStrut(20));
                
                JPanel solBox = new JPanel(new BorderLayout(10, 8));
                solBox.setBackground(new Color(249, 250, 251));
                solBox.setBorder(new EmptyBorder(20, 25, 20, 25));
                solBox.setAlignmentX(Component.LEFT_ALIGNMENT);
                
                JLabel solTitle = new JLabel("<html><b>(!) GIẢI THÍCH CHI TIẾT</b></html>");
                solTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
                solTitle.setForeground(new Color(75, 85, 99));
                solBox.add(solTitle, BorderLayout.NORTH);

                // SECURITY CHECK: Only premium users can see detailed solutions
                if (UserSession.premiumStatus == 1) {
                    String formattedSol = qr.loigiaiChitiet.replace("\\n", "<br>").replace("\n", "<br>");
                    JLabel solText = new JLabel("<html><body style='width: 900px'>" + formattedSol + "</body></html>");
                    solText.setFont(new Font("Segoe UI", Font.PLAIN, 15));
                    solText.setForeground(new Color(31, 41, 55));
                    solBox.add(solText, BorderLayout.CENTER);
                } else {
                    JLabel lockLabel = new JLabel("<html><body style='width: 900px; color: #DC2626;'><i>Tính năng này chỉ dành cho tài khoản <b>PREMIUM</b>. Vui lòng nâng cấp để xem lời giải chi tiết.</i></body></html>");
                    lockLabel.setFont(new Font("Segoe UI", Font.ITALIC, 15));
                    solBox.add(lockLabel, BorderLayout.CENTER);
                    
                    JButton upgradeBtn = new JButton("Nâng cấp ngay");
                    upgradeBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    upgradeBtn.setBackground(new Color(251, 191, 36));
                    upgradeBtn.addActionListener(e -> {
                        Window parent = SwingUtilities.getWindowAncestor(ResultDetailDialog.this);
                        dispose();
                        if (parent instanceof Home) {
                            ((Home) parent).switchView("PREMIUM");
                        }
                    });
                    solBox.add(upgradeBtn, BorderLayout.SOUTH);
                }
                
                JPanel solWrapper = new JPanel(new BorderLayout()) {
                   @Override protected void paintComponent(Graphics g) {
                       Graphics2D g2 = (Graphics2D) g.create();
                       g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                       g2.setColor(new Color(243, 244, 246));
                       g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));
                       g2.dispose();
                   }
                };
                solWrapper.setOpaque(false);
                solWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
                solWrapper.add(solBox);
                inner.add(solWrapper);
            }

            card.add(inner, BorderLayout.CENTER);
            contentPanel.add(card);
            contentPanel.add(Box.createVerticalStrut(40)); // Khoảng cách giữa các card
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private Color getStatusColor(String status) {
        if ("correct".equals(status)) return new Color(16, 185, 129); // Modern Green
        if ("wrong".equals(status)) return new Color(239, 68, 68); // Modern Red
        return new Color(209, 213, 219);
    }

    private String extractBasic(String json, String key) {
        java.util.regex.Matcher ms = java.util.regex.Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
        if (ms.find()) return ms.group(1);
        java.util.regex.Matcher mn = java.util.regex.Pattern.compile("\"" + key + "\"\\s*:\\s*([^,}]+)").matcher(json);
        if (mn.find()) return mn.group(1).replaceAll("[\\]\\}]", "").trim();
        return "";
    }

    static class QuestionResult {
        String noidung;
        String status;
        String loigiaiChitiet;
        List<AnswerResult> answers = new ArrayList<>();
    }

    static class AnswerResult {
        String noidung;
        boolean selected;
        boolean isCorrect;
    }
}
