package com.dreamcast.automation.ui;

import com.dreamcast.automation.executor.ParallelExecutor;
import com.dreamcast.automation.model.TestResult;
import com.dreamcast.automation.model.TestStep;
import com.dreamcast.automation.ui.panels.ResultPanel;
import com.dreamcast.automation.ui.panels.StepBuilderPanel;
import com.dreamcast.automation.ui.panels.StepListPanel;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class MainWindow extends JFrame {

    private final JTextField    urlField     = new JTextField("https://example.com", 30);
    private final JCheckBox     chromeBox    = new JCheckBox("Chrome",  true);
    private final JCheckBox     firefoxBox   = new JCheckBox("Firefox", false);
    private final JCheckBox     edgeBox      = new JCheckBox("Edge",    false);

    private final StepListPanel stepListPanel;
    private final ResultPanel   resultPanel  = new ResultPanel();

    public MainWindow() {
        super("Dreamcast Automation Builder");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 750);
        setLayout(new BorderLayout(5, 5));

        stepListPanel = new StepListPanel(this::onStepSelected);

        StepBuilderPanel builderPanel = new StepBuilderPanel(
            stepListPanel::addStep,
            step -> stepListPanel.updateStep(stepListPanel.getSelectedIndex(), step),
            stepListPanel::deleteSelected
        );

        add(buildTopBar(), BorderLayout.NORTH);

        JSplitPane center = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, stepListPanel, resultPanel);
        center.setDividerLocation(650);

        JSplitPane main = new JSplitPane(JSplitPane.VERTICAL_SPLIT, builderPanel, center);
        main.setDividerLocation(200);
        add(main, BorderLayout.CENTER);

        setVisible(true);
    }

    private JPanel buildTopBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        panel.setBorder(BorderFactory.createEtchedBorder());

        panel.add(new JLabel("URL:"));
        panel.add(urlField);
        panel.add(new JLabel("Browsers:"));
        panel.add(chromeBox);
        panel.add(firefoxBox);
        panel.add(edgeBox);

        JButton runBtn   = new JButton("Run Tests");
        JButton clearBtn = new JButton("Clear Results");

        runBtn.setBackground(new Color(0, 180, 80));
        runBtn.setForeground(Color.WHITE);
        runBtn.setFont(runBtn.getFont().deriveFont(Font.BOLD));

        runBtn.addActionListener(e -> runTests());
        clearBtn.addActionListener(e -> resultPanel.clear());

        panel.add(runBtn);
        panel.add(clearBtn);
        return panel;
    }

    private void runTests() {
        List<TestStep> steps = stepListPanel.getSteps();
        if (steps.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Add some test steps first.", "No Steps", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<String> browsers = new java.util.ArrayList<>();
        if (chromeBox.isSelected())  browsers.add("chrome");
        if (firefoxBox.isSelected()) browsers.add("firefox");
        if (edgeBox.isSelected())    browsers.add("edge");
        if (browsers.isEmpty()) browsers.add("chrome");

        String url = urlField.getText().trim();
        resultPanel.clear();

        new Thread(() -> {
            List<TestResult> results = ParallelExecutor.runOnBrowsers(
                browsers, url, steps, "screenshots",
                (browser, res) -> SwingUtilities.invokeLater(() ->
                    res.forEach(resultPanel::addResult))
            );
            SwingUtilities.invokeLater(() -> {
                long pass = results.stream().filter(TestResult::isPassed).count();
                long fail = results.size() - pass;
                JOptionPane.showMessageDialog(this,
                    "Completed: PASS=" + pass + "  FAIL=" + fail,
                    "Run Complete", JOptionPane.INFORMATION_MESSAGE);
            });
        }).start();
    }

    private void onStepSelected(TestStep step) {
        // Step selection handled internally by StepBuilderPanel
    }
}
