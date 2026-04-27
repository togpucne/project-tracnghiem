package com.ptquiz.ui.student;

import com.ptquiz.core.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ResultDetailDialog extends JDialog {
    private String idLanthi;
    private JPanel contentPanel;

    public ResultDetailDialog(JFrame parent, String idLanthi, String tenBaithi) {
        super(parent, "Chi tiết bài thi: " + tenBaithi, true);
        this.idLanthi = idLanthi;

        setSize(1000, 700);
        setLocationRelativeTo(parent);

        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(249, 250, 251));

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);

        loadDetails();
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
                // Parse questions mechanically (simulating a JSON library)
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
                        qr.userTextAns = APIHelper.unescapeUnicode(extractBasic("{\"id_cauhoi\":" + qRaw, "user_text_ans"));

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

                SwingUtilities.invokeLater(() -> {
                    renderUI(qList);
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void renderUI(List<QuestionResult> qList) {
        contentPanel.removeAll();
        contentPanel.add(Box.createVerticalStrut(20));

        int stt = 1;
        for (QuestionResult qr : qList) {
            JPanel card = new JPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBackground(Color.WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(0, 40, 20, 40),
                BorderFactory.createCompoundBorder(
                    new LineBorder(getStatusColor(qr.status), 2, true),
                    new EmptyBorder(20, 20, 20, 20)
                )
            ));

            JLabel qLabel = new JLabel("<html><p style='width: 800px'><b>Câu " + stt + ":</b> " + qr.noidung + "</p></html>");
            qLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            card.add(qLabel);
            card.add(Box.createVerticalStrut(15));

            for (AnswerResult ar : qr.answers) {
                JLabel aLabel = new JLabel("<html><p style='width: 750px'>" + ar.noidung + "</p></html>");
                aLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
                aLabel.setBorder(new EmptyBorder(5, 10, 5, 10));
                aLabel.setOpaque(true);

                if (ar.selected && ar.isCorrect) {
                    aLabel.setBackground(new Color(220, 252, 231)); // Green-100
                    aLabel.setForeground(new Color(22, 101, 52)); // Green-800
                    aLabel.setText("<html>&#10004; " + aLabel.getText() + " (Đúng)</html>");
                } else if (ar.selected && !ar.isCorrect) {
                    aLabel.setBackground(new Color(254, 226, 226)); // Red-100
                    aLabel.setForeground(new Color(153, 27, 27)); // Red-800
                    aLabel.setText("<html>&#10008; " + aLabel.getText() + " (Sai)</html>");
                } else if (ar.isCorrect) {
                    aLabel.setBackground(new Color(240, 249, 255)); // Blue-50
                    aLabel.setForeground(new Color(30, 64, 175)); // Blue-800
                    aLabel.setText("<html>&#9679; " + aLabel.getText() + " (Đáp án đúng)</html>");
                } else {
                    aLabel.setBackground(Color.WHITE);
                }

                card.add(aLabel);
                card.add(Box.createVerticalStrut(5));
            }

            contentPanel.add(card);
            stt++;
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private Color getStatusColor(String status) {
        if ("correct".equals(status)) return new Color(34, 197, 94);
        if ("wrong".equals(status)) return new Color(239, 68, 68);
        return new Color(209, 213, 219);
    }

    private String extractBasic(String json, String key) {
        java.util.regex.Matcher ms = java.util.regex.Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
        if (ms.find()) return ms.group(1);
        java.util.regex.Matcher mn = java.util.regex.Pattern.compile("\"" + key + "\"\\s*:\\s*([^,}]+)").matcher(json);
        if (mn.find()) return mn.group(1).replaceAll("[\\]\\}]", "").trim();
        return "";
    }

    class QuestionResult {
        String noidung;
        String status;
        String userTextAns;
        List<AnswerResult> answers = new ArrayList<>();
    }

    class AnswerResult {
        String noidung;
        boolean selected;
        boolean isCorrect;
    }
}
