package com.dreamcast.automation.ui.panels;

import com.dreamcast.automation.model.TestResult;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ResultPanel extends JScrollPane {

    private final DefaultTableModel model;
    private final JTable            table;

    public ResultPanel() {
        model = new DefaultTableModel(
            new String[]{"Step Name", "Status", "Message", "Screenshot"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(24);

        // Color rows by status
        table.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, focus, row, col);
                String val = v != null ? v.toString() : "";
                if ("PASS".equals(val)) c.setBackground(new Color(180, 255, 180));
                else if ("FAIL".equals(val)) c.setBackground(new Color(255, 180, 180));
                else c.setBackground(Color.WHITE);
                return c;
            }
        });

        setViewportView(table);
        setPreferredSize(new Dimension(600, 400));
    }

    public void showResults(List<TestResult> results) {
        model.setRowCount(0);
        for (TestResult r : results) {
            model.addRow(new Object[]{
                r.getStepName(),
                r.getStatus().name(),
                r.getMessage(),
                r.getScreenshotPath() != null ? r.getScreenshotPath() : ""
            });
        }
    }

    public void clear() {
        model.setRowCount(0);
    }

    public void addResult(TestResult result) {
        model.addRow(new Object[]{
            result.getStepName(),
            result.getStatus().name(),
            result.getMessage(),
            result.getScreenshotPath() != null ? result.getScreenshotPath() : ""
        });
    }
}
