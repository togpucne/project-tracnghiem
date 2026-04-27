package com.ptquiz.ui.lecturer;

import com.ptquiz.core.APIHelper;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ResultViewPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;

    public ResultViewPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(40, 40, 40, 40));

        JLabel title = new JLabel("Kết quả thi của sinh viên");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setBorder(new EmptyBorder(0, 0, 20, 0));
        add(title, BorderLayout.NORTH);

        String[] columns = {"STT", "Sinh viên", "Đề thi", "Ngày thi", "Điểm", "Chi tiết"};
        model = new DefaultTableModel(columns, 0);
        table = new JTable(model);
        table.setRowHeight(40);
        
        add(new JScrollPane(table), BorderLayout.CENTER);

        loadData();
    }

    private void loadData() {
        // This would typically hit an endpoint like history/all or lecturer/results
        // For now, using a placeholder if endpoint doesn't exist
        new Thread(() -> {
            String json = APIHelper.sendGet("history/list"); // Reuse for demo or specific lecturer endpoint
            if (json == null || json.isEmpty()) return;

            SwingUtilities.invokeLater(() -> {
                model.setRowCount(0);
                // Implementation similar to HistoryPanel but for all students (if admin/lecturer)
                model.addRow(new Object[]{"1", "Nguyễn Văn A", "Cấu trúc dữ liệu", "2026-04-26", "8.5", "Xem"});
                model.addRow(new Object[]{"2", "Trần Thị B", "Lập trình Java", "2026-04-26", "9.0", "Xem"});
            });
        }).start();
    }
}
