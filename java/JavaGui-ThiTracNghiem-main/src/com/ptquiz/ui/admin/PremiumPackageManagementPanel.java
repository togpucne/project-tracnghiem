package com.ptquiz.ui.admin;

import com.ptquiz.core.APIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PremiumPackageManagementPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private JButton btnAdd;
    private String currentStatus = "active";
    private JButton btnActiveTab, btnTrashTab;

    public PremiumPackageManagementPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(30, 40, 30, 40));

        initComponents();
        loadData();
    }

    private void initComponents() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(0, 0, 20, 0));

        JPanel titleBox = new JPanel(new GridLayout(0, 1));
        titleBox.setBackground(Color.WHITE);

        JLabel title = new JLabel("Quản lý Gói Premium");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(Color.BLACK);
        titleBox.add(title);

        JLabel subtitle = new JLabel("Tạo, sửa, sao chép và xóa gói Premium qua API bảo mật.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(Color.BLACK);
        titleBox.add(subtitle);

        btnAdd = new JButton("+ THÊM GÓI MỚI");
        styleStandardButton(btnAdd, new Color(79, 70, 229));
        btnAdd.setForeground(Color.BLACK);
        btnAdd.addActionListener(e -> showPackageDialog(-1, "", "10000", "2", ""));

        JPanel tabContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        tabContainer.setBackground(Color.WHITE);
        tabContainer.setBorder(new EmptyBorder(10, 0, 10, 0));

        btnActiveTab = new JButton("Đang bán");
        btnTrashTab = new JButton("Thùng rác");
        styleTabButton(btnActiveTab, true);
        styleTabButton(btnTrashTab, false);

        btnActiveTab.addActionListener(e -> switchTab("active"));
        btnTrashTab.addActionListener(e -> switchTab("inactive"));

        tabContainer.add(btnActiveTab);
        tabContainer.add(btnTrashTab);

        header.add(titleBox, BorderLayout.WEST);
        header.add(btnAdd, BorderLayout.EAST);
        
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setBackground(Color.WHITE);
        northPanel.add(header, BorderLayout.NORTH);
        northPanel.add(tabContainer, BorderLayout.SOUTH);
        
        add(northPanel, BorderLayout.NORTH);

        String[] columns = {"STT", "ID", "Tên gói", "Giá (VNĐ)", "Thời hạn (ngày)", "Miêu tả", "Hành động"};
        model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) {
                return column == 6;
            }
        };

        table = new JTable(model);
        table.setRowHeight(55);
        table.setForeground(Color.BLACK);
        table.getTableHeader().setForeground(Color.BLACK);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.setGridColor(new Color(229, 231, 235));
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(0, 1));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        centerRenderer.setForeground(Color.BLACK);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(1).setMinWidth(0);
        table.getColumnModel().getColumn(1).setMaxWidth(0);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(6).setMinWidth(250);
        table.getColumnModel().getColumn(6).setCellRenderer(new ActionPanelRenderer());
        table.getColumnModel().getColumn(6).setCellEditor(new ActionPanelEditor());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(new Color(229, 231, 235), 1));
        scroll.getViewport().setBackground(Color.WHITE);
        add(scroll, BorderLayout.CENTER);
    }

    public void loadData() {
        new Thread(() -> {
            String json = APIHelper.sendGet("admin/goipremium?status=" + currentStatus);
            SwingUtilities.invokeLater(() -> {
                if (json == null || json.isEmpty()) return;
                model.setRowCount(0);
                try {
                    String dataStr = APIHelper.extractJsonValue(json, "data");
                    List<String> items = APIHelper.splitJsonArray(dataStr);
                    int index = 1;
                    for (String item : items) {
                        String id = APIHelper.extractJsonValue(item, "id_goi");
                        String name = APIHelper.unescapeUnicode(APIHelper.extractJsonValue(item, "ten_goi"));
                        String price = APIHelper.extractJsonValue(item, "gia");
                        String days = APIHelper.extractJsonValue(item, "thoihan_ngay");
                        String desc = APIHelper.unescapeUnicode(APIHelper.extractJsonValue(item, "mieuta"));
                        model.addRow(new Object[]{index++, id, name, price, days, desc, ""});
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                table.repaint();
                table.revalidate();
            });
        }).start();
    }

    private void showPackageDialog(int id, String name, String price, String days, String description) {
        boolean editMode = id > 0;
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), editMode ? "Chỉnh sửa gói Premium" : "Thêm gói Premium", true);
        dialog.setSize(620, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Color.WHITE);

        JPanel header = new JPanel(new GridLayout(0, 1, 0, 8));
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(25, 30, 10, 30));
        JLabel title = new JLabel(editMode ? "Chỉnh sửa gói Premium" : "Thêm gói Premium");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.BLACK);
        header.add(title);
        JLabel subtitle = new JLabel("Cập nhật gói cho người dùng admin qua API và đồng bộ với web.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(107, 114, 128));
        header.add(subtitle);
        dialog.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(10, 30, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.weightx = 1.0;

        JTextField txtName = new JTextField(name);
        JTextField txtPrice = new JTextField(price);
        JTextField txtDays = new JTextField(days);
        JTextArea txtDesc = new JTextArea(description);
        txtDesc.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtDesc.setLineWrap(true);
        txtDesc.setWrapStyleWord(true);

        gbc.gridy = 0;
        gbc.gridx = 0;
        body.add(createInputGroup("Tên gói", txtName), gbc);
        gbc.gridx = 1;
        body.add(createInputGroup("Giá (VNĐ)", txtPrice), gbc);

        gbc.gridy = 1;
        gbc.gridx = 0;
        body.add(createInputGroup("Thời hạn (ngày)", txtDays), gbc);
        gbc.gridx = 1;
        body.add(createTextAreaGroup("Miêu tả", txtDesc), gbc);

        dialog.add(body, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        footer.setBackground(new Color(249, 250, 251));
        footer.setBorder(new LineBorder(new Color(229, 231, 235), 1));

        JButton btnCancel = new JButton("HỦY");
        styleStandardButton(btnCancel, Color.WHITE);
        btnCancel.setForeground(Color.BLACK);
        btnCancel.addActionListener(e -> dialog.dispose());

        JButton btnSave = new JButton(editMode ? "CẬP NHẬT" : "LƯU GÓI");
        styleStandardButton(btnSave, new Color(187, 247, 208));
        btnSave.setForeground(Color.BLACK);
        btnSave.addActionListener(e -> {
            String packageName = txtName.getText().trim();
            String priceValue = txtPrice.getText().trim();
            String daysValue = txtDays.getText().trim();
            String descValue = txtDesc.getText().trim();

            if (packageName.isEmpty() || priceValue.isEmpty() || daysValue.isEmpty() || descValue.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng điền đầy đủ thông tin gói Premium.");
                return;
            }

            int priceInt;
            int daysInt;
            try {
                priceInt = Integer.parseInt(priceValue.replaceAll("\\D", ""));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Giá gói phải là số nguyên hợp lệ.");
                return;
            }
            try {
                daysInt = Integer.parseInt(daysValue.replaceAll("\\D", ""));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Thời hạn phải là số ngày hợp lệ.");
                return;
            }

            if (priceInt < 10000) {
                JOptionPane.showMessageDialog(dialog, "Giá gói phải lớn hơn hoặc bằng 10.000 VNĐ.");
                return;
            }
            if (daysInt <= 1) {
                JOptionPane.showMessageDialog(dialog, "Thời hạn phải lớn hơn 1 ngày.");
                return;
            }

            String payload = String.format(
                    "{\"ten_goi\":\"%s\",\"gia\":%d,\"thoihan_ngay\":%d,\"mieuta\":\"%s\"}",
                    APIHelper.escapeJSON(packageName), priceInt, daysInt, APIHelper.escapeJSON(descValue)
            );

            new Thread(() -> {
                APIHelper.APIResponse response;
                if (editMode) {
                    String patchPayload = String.format(
                            "{\"id_goi\":%d,\"ten_goi\":\"%s\",\"gia\":%d,\"thoihan_ngay\":%d,\"mieuta\":\"%s\"}",
                            id, APIHelper.escapeJSON(packageName), priceInt, daysInt, APIHelper.escapeJSON(descValue)
                    );
                    response = APIHelper.sendPatch("admin/goipremium", patchPayload);
                } else {
                    response = APIHelper.sendPost("admin/goipremium", payload);
                }

                SwingUtilities.invokeLater(() -> {
                    if (response.success) {
                        dialog.dispose();
                        loadData();
                    } else {
                        JOptionPane.showMessageDialog(dialog, response.message, "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                });
            }).start();
        });

        footer.add(btnCancel);
        footer.add(btnSave);
        dialog.add(footer, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private JPanel createInputGroup(String labelText, JTextField textField) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(Color.WHITE);
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panel.add(label, BorderLayout.NORTH);
        textField.setPreferredSize(new Dimension(0, 38));
        panel.add(textField, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createTextAreaGroup(String labelText, JTextArea textArea) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(Color.WHITE);
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panel.add(label, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setPreferredSize(new Dimension(0, 120));
        scroll.setBorder(new LineBorder(new Color(229, 231, 235), 1));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void styleStandardButton(JButton button, Color bg) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(bg);
        button.setBorder(new LineBorder(new Color(209, 213, 219), 1));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(180, 38));
    }

    private void styleTabButton(JButton button, boolean active) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        if (active) {
            button.setBackground(new Color(238, 242, 255));
            button.setForeground(new Color(79, 70, 229));
            button.setBorder(new LineBorder(new Color(79, 70, 229), 2));
        } else {
            button.setBackground(Color.WHITE);
            button.setForeground(new Color(107, 114, 128));
            button.setBorder(new LineBorder(new Color(229, 231, 235), 1));
        }
        button.setPreferredSize(new Dimension(120, 35));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void switchTab(String status) {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
        table.clearSelection();
        this.currentStatus = status;
        styleTabButton(btnActiveTab, status.equals("active"));
        styleTabButton(btnTrashTab, status.equals("inactive"));
        btnAdd.setVisible(status.equals("active"));
        loadData();
    }

    private JPanel buildActionPanel(int row) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        panel.setBackground(Color.WHITE);

        int id = Integer.parseInt(model.getValueAt(row, 1).toString());

        if (currentStatus.equals("active")) {
            JButton btnEdit = new JButton("SỬA");
            styleActionButton(btnEdit, new Color(79, 70, 229));
            btnEdit.addActionListener(e -> {
                String name = model.getValueAt(row, 2).toString();
                String price = model.getValueAt(row, 3).toString();
                String days = model.getValueAt(row, 4).toString();
                String desc = model.getValueAt(row, 5).toString();
                showPackageDialog(id, name, price, days, desc);
            });

            JButton btnCopy = new JButton("SAO CHÉP");
            styleActionButton(btnCopy, new Color(16, 185, 129));
            btnCopy.addActionListener(e -> {
                new Thread(() -> {
                    APIHelper.APIResponse response = APIHelper.sendPost("admin/goipremium", "{\"copy_id\": " + id + "}");
                    SwingUtilities.invokeLater(() -> {
                        if (response.success) {
                            loadData();
                        } else {
                            JOptionPane.showMessageDialog(this, response.message, "Lỗi bảo mật API", JOptionPane.ERROR_MESSAGE);
                        }
                    });
                }).start();
            });

            JButton btnDelete = new JButton("XÓA");
            styleActionButton(btnDelete, new Color(239, 68, 68));
            btnDelete.addActionListener(e -> {
                int choice = JOptionPane.showConfirmDialog(this,
                        "Bạn có chắc muốn chuyển gói này vào thùng rác?", "Xác nhận", JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.YES_OPTION) {
                    new Thread(() -> {
                        APIHelper.APIResponse response = APIHelper.sendDelete("admin/goipremium", "{\"id_goi\": " + id + "}");
                        SwingUtilities.invokeLater(() -> {
                            if (response.success) {
                                loadData();
                            } else {
                                JOptionPane.showMessageDialog(this, response.message, "Lỗi", JOptionPane.ERROR_MESSAGE);
                            }
                        });
                    }).start();
                }
            });

            panel.add(btnEdit);
            panel.add(btnCopy);
            panel.add(btnDelete);
        } else {
            JButton btnRestore = new JButton("KHÔI PHỤC");
            styleActionButton(btnRestore, new Color(16, 185, 129));
            btnRestore.setPreferredSize(new Dimension(120, 30));
            btnRestore.addActionListener(e -> {
                new Thread(() -> {
                    APIHelper.APIResponse response = APIHelper.sendPatch("admin/goipremium", "{\"action\": \"restore\", \"id_goi\": " + id + "}");
                    SwingUtilities.invokeLater(() -> {
                        if (response.success) {
                            loadData();
                        } else {
                            JOptionPane.showMessageDialog(this, response.message, "Lỗi trùng lặp API", JOptionPane.ERROR_MESSAGE);
                        }
                    });
                }).start();
            });
            panel.add(btnRestore);
        }

        return panel;
    }

    private void styleActionButton(JButton button, Color color) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(color);
        button.setBackground(Color.WHITE);
        button.setBorder(new LineBorder(color, 1));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(90, 30));
    }

    class ActionPanelRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                                 boolean hasFocus, int row, int column) {
            return buildActionPanel(row);
        }
    }

    class ActionPanelEditor extends DefaultCellEditor {
        public ActionPanelEditor() {
            super(new JCheckBox());
        }

        @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            return buildActionPanel(row);
        }

        @Override public Object getCellEditorValue() {
            return "";
        }

        @Override public boolean stopCellEditing() {
            return super.stopCellEditing();
        }
    }
}
