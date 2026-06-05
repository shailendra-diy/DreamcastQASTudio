package dreamcastauto;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.Select;
import io.github.bonigarcia.wdm.WebDriverManager;

public class AutomationSwingTool {

    static DefaultTableModel model;
    static java.util.List<Map<String,String>> steps = new ArrayList<>();

    public static void main(String[] args) {
        JFrame frame = new JFrame("Automation Tool");
        frame.setSize(900,600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new GridLayout(6,2,5,5));

        JTextField urlField = new JTextField();
        JComboBox<String> browserBox = new JComboBox<>(new String[]{"Chrome","Firefox","Edge"});
        JCheckBox maximizeBox = new JCheckBox("Maximize Browser");

        JComboBox<String> actionBox = new JComboBox<>(new String[]{"textbox","click","dropdown"});
        JTextField xpathField = new JTextField();
        JTextField valueField = new JTextField();
        JTextField waitField = new JTextField("1");
        JTextField nameField = new JTextField();

        JButton addStepBtn = new JButton("Add Step");
        JButton runBtn = new JButton("Run");

        topPanel.add(new JLabel("URL:")); topPanel.add(urlField);
        topPanel.add(new JLabel("Browser:")); topPanel.add(browserBox);
        topPanel.add(maximizeBox); topPanel.add(new JLabel(""));
        topPanel.add(new JLabel("Action:")); topPanel.add(actionBox);
        topPanel.add(new JLabel("XPath:")); topPanel.add(xpathField);
        topPanel.add(new JLabel("Value:")); topPanel.add(valueField);
        topPanel.add(new JLabel("Wait (sec):")); topPanel.add(waitField);
        topPanel.add(new JLabel("Step Name:")); topPanel.add(nameField);

        frame.add(topPanel, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[]{"Step Name","Action","XPath","Value","Wait"},0);
        JTable table = new JTable(model);
        JScrollPane scroll = new JScrollPane(table);
        frame.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        bottom.add(addStepBtn);
        bottom.add(runBtn);
        frame.add(bottom, BorderLayout.SOUTH);

        addStepBtn.addActionListener(e -> {
            String action = (String)actionBox.getSelectedItem();
            String xpath = xpathField.getText();
            String value = valueField.getText();
            String wait = waitField.getText();
            String name = nameField.getText();

            Map<String,String> step = new HashMap<>();
            step.put("action",action);
            step.put("xpath",xpath);
            step.put("value",value);
            step.put("wait",wait);
            step.put("name",name);

            steps.add(step);

            model.addRow(new Object[]{name,action,xpath,value,wait});

            xpathField.setText("");
            valueField.setText("");
            waitField.setText("1");
            nameField.setText("");
        });

        runBtn.addActionListener(e -> {
            String url = urlField.getText();
            String browser = (String)browserBox.getSelectedItem();
            boolean max = maximizeBox.isSelected();
            new Thread(() -> runAutomation(url,browser,max)).start();
        });

        JPopupMenu popup = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Delete Step");
        popup.add(deleteItem);

        table.setComponentPopupMenu(popup);
        deleteItem.addActionListener(e -> {
            int row = table.getSelectedRow();
            if(row >= 0){
                steps.remove(row);
                model.removeRow(row);
            }
        });

        frame.setVisible(true);
    }

    static void runAutomation(String url, String browser, boolean max){
        WebDriver driver=null;
        try {
            if(browser.equalsIgnoreCase("Chrome")){
                WebDriverManager.chromedriver().setup();
                driver=new ChromeDriver();
            } else if(browser.equalsIgnoreCase("Firefox")){
                WebDriverManager.firefoxdriver().setup();
                driver=new FirefoxDriver();
            } else if(browser.equalsIgnoreCase("Edge")){
                WebDriverManager.edgedriver().setup();
                driver=new EdgeDriver();
            }

            if(max) driver.manage().window().maximize();
            driver.get(url);

            for(Map<String,String> s:steps){
                int waitTime = Integer.parseInt(s.get("wait"));
                Thread.sleep(waitTime*1000);

                WebElement el = driver.findElement(By.xpath(s.get("xpath")));
                String action = s.get("action");
                String value = s.get("value");

                if(action.equals("textbox")){
                    el.sendKeys(value);
                } else if(action.equals("click")){
                    el.click();
                } else if(action.equals("dropdown")){
                    new Select(el).selectByVisibleText(value);
                }
            }
            JOptionPane.showMessageDialog(null,"Execution Finished");

        } catch(Exception e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,"Error: "+e.getMessage());
        }
    }
}