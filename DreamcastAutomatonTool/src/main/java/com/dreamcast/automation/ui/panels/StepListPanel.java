package com.dreamcast.automation.ui.panels;

import com.dreamcast.automation.model.TestStep;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class StepListPanel extends JScrollPane {

    private final DefaultTableModel model;
    private final JTable            table;
    private final List<TestStep>    steps = new ArrayList<>();

    public StepListPanel(Consumer<TestStep> onSelect) {
        model = new DefaultTableModel(new String[]{"#", "Step Name", "Action", "XPath", "Value"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(24);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = table.getSelectedRow();
                if (row >= 0 && row < steps.size()) onSelect.accept(steps.get(row));
            }
        });

        setViewportView(table);
        setPreferredSize(new java.awt.Dimension(600, 400));
    }

    public void addStep(TestStep step) {
        steps.add(step);
        refresh();
    }

    public void updateStep(int index, TestStep step) {
        if (index >= 0 && index < steps.size()) {
            steps.set(index, step);
            refresh();
        }
    }

    public void deleteSelected() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            steps.remove(row);
            refresh();
        }
    }

    public int getSelectedIndex() {
        return table.getSelectedRow();
    }

    public List<TestStep> getSteps() {
        return new ArrayList<>(steps);
    }

    public void clearAll() {
        steps.clear();
        refresh();
    }

    private void refresh() {
        model.setRowCount(0);
        for (int i = 0; i < steps.size(); i++) {
            TestStep s = steps.get(i);
            model.addRow(new Object[]{i + 1, s.getName(), s.getAction(), s.getXpath(), s.getValue()});
        }
    }
}
