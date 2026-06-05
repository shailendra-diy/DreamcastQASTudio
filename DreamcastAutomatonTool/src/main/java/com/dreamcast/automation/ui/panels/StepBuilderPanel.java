package com.dreamcast.automation.ui.panels;

import com.dreamcast.automation.model.StepAction;
import com.dreamcast.automation.model.TestStep;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class StepBuilderPanel extends JPanel {

    private final JTextField  nameField   = new JTextField(15);
    private final JTextField  xpathField  = new JTextField(20);
    private final JTextField  valueField  = new JTextField(15);
    private final JTextField  waitField   = new JTextField("0", 4);
    private final JComboBox<String> actionBox = new JComboBox<>(StepAction.allLabels());

    public StepBuilderPanel(Consumer<TestStep> onAdd, Consumer<TestStep> onUpdate, Runnable onDelete) {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("Step Builder"));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 5, 4, 5);
        g.fill   = GridBagConstraints.HORIZONTAL;

        addRow(g, 0, "Step Name", nameField);
        addRow(g, 1, "Action",    actionBox);
        addRow(g, 2, "XPath",     xpathField);
        addRow(g, 3, "Value",     valueField);
        addRow(g, 4, "Wait (sec)",waitField);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JButton addBtn    = new JButton("Add Step");
        JButton updateBtn = new JButton("Update Step");
        JButton deleteBtn = new JButton("Delete Step");

        addBtn.addActionListener(e -> onAdd.accept(buildStep()));
        updateBtn.addActionListener(e -> onUpdate.accept(buildStep()));
        deleteBtn.addActionListener(e -> onDelete.run());

        buttons.add(addBtn);
        buttons.add(updateBtn);
        buttons.add(deleteBtn);

        g.gridx = 0; g.gridy = 5; g.gridwidth = 4;
        add(buttons, g);
    }

    public void loadStep(TestStep step) {
        nameField.setText(step.getName());
        xpathField.setText(step.getXpath());
        valueField.setText(step.getValue());
        waitField.setText(String.valueOf(step.getWaitSeconds()));
        actionBox.setSelectedItem(step.getAction());
    }

    public void clear() {
        nameField.setText("");
        xpathField.setText("");
        valueField.setText("");
        waitField.setText("0");
        actionBox.setSelectedIndex(0);
    }

    private TestStep buildStep() {
        int wait = 0;
        try { wait = Integer.parseInt(waitField.getText().trim()); } catch (NumberFormatException ignored) {}
        return new TestStep(
            nameField.getText().trim(),
            (String) actionBox.getSelectedItem(),
            xpathField.getText().trim(),
            valueField.getText().trim(),
            wait
        );
    }

    private void addRow(GridBagConstraints g, int row, String label, JComponent field) {
        g.gridwidth = 1;
        g.gridx = 0; g.gridy = row; add(new JLabel(label), g);
        g.gridx = 1; add(field, g);
    }
}
