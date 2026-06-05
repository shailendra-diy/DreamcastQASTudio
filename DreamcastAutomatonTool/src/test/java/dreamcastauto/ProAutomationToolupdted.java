package dreamcastauto;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;
import io.github.bonigarcia.wdm.WebDriverManager;

public class ProAutomationToolupdted{

    static DefaultTableModel model;
    static java.util.List<Map<String,String>> steps = new ArrayList<>();

    public static void main(String[] args) {
        JFrame frame = new JFrame("Pro Selenium Automation Builder");
        frame.setSize(1000,700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Top Panel for URL, Browser, Maximize
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBorder(BorderFactory.createTitledBorder("Browser Configuration"));
        topPanel.setBackground(new Color(230, 240, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField urlField = new JTextField();
        JComboBox<String> browserBox = new JComboBox<>(new String[]{"Chrome","Firefox","Edge"});
        JCheckBox maximizeBox = new JCheckBox("Maximize Browser");

        gbc.gridx=0; gbc.gridy=0; topPanel.add(new JLabel("URL:"),gbc);
        gbc.gridx=1; gbc.gridy=0; gbc.gridwidth=2; topPanel.add(urlField,gbc);
        gbc.gridwidth=1;
        gbc.gridx=0; gbc.gridy=1; topPanel.add(new JLabel("Browser:"),gbc);
        gbc.gridx=1; gbc.gridy=1; topPanel.add(browserBox,gbc);
        gbc.gridx=2; gbc.gridy=1; topPanel.add(maximizeBox,gbc);

        frame.add(topPanel, BorderLayout.NORTH);

        // Center Panel for Steps
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("Automation Steps"));
        centerPanel.setBackground(new Color(245, 250, 255));

        // Dynamic Action Panel
        JPanel actionPanel = new JPanel(new GridBagLayout());
        actionPanel.setBackground(new Color(245, 250, 255));
        GridBagConstraints aGbc = new GridBagConstraints();
        aGbc.insets = new Insets(5,5,5,5);
        aGbc.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<String> actionBox = new JComboBox<>(new String[]{
                "textbox","click","dropdown","mouseHover","getText","getAttribute","doubleClick","rightClick"
        });
        JTextField xpathField = new JTextField();
        JTextField valueField = new JTextField();
        JTextField waitField = new JTextField("1");
        JTextField nameField = new JTextField();

        // Dropdown selection type for dropdown action
        JComboBox<String> dropdownTypeBox = new JComboBox<>(new String[]{"selectByVisibleText","selectByValue","selectByIndex"});
        dropdownTypeBox.setVisible(false);

        JButton addStepBtn = new JButton("Add Step");
        JButton updateStepBtn = new JButton("Update Step");
        updateStepBtn.setEnabled(false);

        aGbc.gridx=0;aGbc.gridy=0; actionPanel.add(new JLabel("Action:"),aGbc);
        aGbc.gridx=1;aGbc.gridy=0; actionPanel.add(actionBox,aGbc);
        aGbc.gridx=0;aGbc.gridy=1; actionPanel.add(new JLabel("XPath:"),aGbc);
        aGbc.gridx=1;aGbc.gridy=1; actionPanel.add(xpathField,aGbc);
        aGbc.gridx=0;aGbc.gridy=2; actionPanel.add(new JLabel("Value:"),aGbc);
        aGbc.gridx=1;aGbc.gridy=2; actionPanel.add(valueField,aGbc);
        aGbc.gridx=0;aGbc.gridy=3; actionPanel.add(new JLabel("Wait (sec):"),aGbc);
        aGbc.gridx=1;aGbc.gridy=3; actionPanel.add(waitField,aGbc);
        aGbc.gridx=0;aGbc.gridy=4; actionPanel.add(new JLabel("Step Name:"),aGbc);
        aGbc.gridx=1;aGbc.gridy=4; actionPanel.add(nameField,aGbc);
        aGbc.gridx=1;aGbc.gridy=5; actionPanel.add(dropdownTypeBox,aGbc);
        aGbc.gridx=0;aGbc.gridy=6; actionPanel.add(addStepBtn,aGbc);
        aGbc.gridx=1;aGbc.gridy=6; actionPanel.add(updateStepBtn,aGbc);

        centerPanel.add(actionPanel,BorderLayout.NORTH);

        // Table for Steps
        model = new DefaultTableModel(new String[]{"Step Name","Action","XPath","Value","Wait","Dropdown Type"},0);
        JTable table = new JTable(model);
        JScrollPane scroll = new JScrollPane(table);
        centerPanel.add(scroll, BorderLayout.CENTER);

        // Popup menu for delete/update
        JPopupMenu popup = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Delete Step");
        JMenuItem editItem = new JMenuItem("Edit Step");
        popup.add(editItem);
        popup.add(deleteItem);
        table.setComponentPopupMenu(popup);

        deleteItem.addActionListener(e->{
            int row = table.getSelectedRow();
            if(row>=0){
                steps.remove(row);
                model.removeRow(row);
            }
        });

        editItem.addActionListener(e->{
            int row = table.getSelectedRow();
            if(row>=0){
                Map<String,String> step = steps.get(row);
                actionBox.setSelectedItem(step.get("action"));
                xpathField.setText(step.get("xpath"));
                valueField.setText(step.get("value"));
                waitField.setText(step.get("wait"));
                nameField.setText(step.get("name"));
                dropdownTypeBox.setSelectedItem(step.getOrDefault("dropdownType","selectByVisibleText"));
                addStepBtn.setEnabled(false);
                updateStepBtn.setEnabled(true);
                updateStepBtn.putClientProperty("row",row);
            }
        });

        // Action listener to show dropdown type only for dropdown action
        actionBox.addActionListener(e->{
            String act = (String)actionBox.getSelectedItem();
            dropdownTypeBox.setVisible(act.equals("dropdown"));
        });

        addStepBtn.addActionListener(e->{
            addStep(actionBox,xpathField,valueField,waitField,nameField,dropdownTypeBox,null);
        });

        updateStepBtn.addActionListener(e->{
            int row = (int)updateStepBtn.getClientProperty("row");
            addStep(actionBox,xpathField,valueField,waitField,nameField,dropdownTypeBox,row);
            addStepBtn.setEnabled(true);
            updateStepBtn.setEnabled(false);
        });

        frame.add(centerPanel, BorderLayout.CENTER);

        // Run Button
        JPanel bottom = new JPanel();
        JButton runBtn = new JButton("Run Automation");
        bottom.add(runBtn);
        bottom.setBackground(new Color(200,230,255));
        frame.add(bottom, BorderLayout.SOUTH);

        runBtn.addActionListener(e->{
            String url = urlField.getText();
            String browser = (String)browserBox.getSelectedItem();
            boolean max = maximizeBox.isSelected();
            new Thread(()->runAutomation(url,browser,max)).start();
        });

        frame.setVisible(true);
    }

    static void addStep(JComboBox<String> actionBox,JTextField xpathField,JTextField valueField,
                        JTextField waitField,JTextField nameField,JComboBox<String> dropdownTypeBox,Integer rowIndex){

        String action = (String)actionBox.getSelectedItem();
        String xpath = xpathField.getText();
        String value = valueField.getText();
        String wait = waitField.getText();
        String name = nameField.getText();
        String dropdownType = (String)dropdownTypeBox.getSelectedItem();

        Map<String,String> step = new HashMap<>();
        step.put("action",action);
        step.put("xpath",xpath);
        step.put("value",value);
        step.put("wait",wait);
        step.put("name",name);
        step.put("dropdownType",dropdownType);

        if(rowIndex==null){
            steps.add(step);
            model.addRow(new Object[]{name,action,xpath,value,wait,dropdownType});
        }else{
            steps.set(rowIndex,step);
            for(int i=0;i<6;i++) model.setValueAt(model.getValueAt(rowIndex,i),rowIndex,i); // refresh
            model.setValueAt(name,rowIndex,0);
            model.setValueAt(action,rowIndex,1);
            model.setValueAt(xpath,rowIndex,2);
            model.setValueAt(value,rowIndex,3);
            model.setValueAt(wait,rowIndex,4);
            model.setValueAt(dropdownType,rowIndex,5);
        }

        // Clear fields
        xpathField.setText("");
        valueField.setText("");
        waitField.setText("1");
        nameField.setText("");
    }

    static void runAutomation(String url,String browser,boolean max){
        WebDriver driver=null;
        try{
            if(browser.equalsIgnoreCase("Chrome")){
                WebDriverManager.chromedriver().setup();
                driver=new ChromeDriver();
            }else if(browser.equalsIgnoreCase("Firefox")){
                WebDriverManager.firefoxdriver().setup();
                driver=new FirefoxDriver();
            }else if(browser.equalsIgnoreCase("Edge")){
                WebDriverManager.edgedriver().setup();
                driver=new EdgeDriver();
            }

            if(max) driver.manage().window().maximize();
            driver.get(url);

            Actions actions = new Actions(driver);

            for(Map<String,String> s:steps){
                int waitTime = Integer.parseInt(s.get("wait"));
                Thread.sleep(waitTime*1000);
                WebElement el = null;
                if(!s.get("action").equals("getText") && !s.get("action").equals("getAttribute")){
                    el = driver.findElement(By.xpath(s.get("xpath")));
                }
                switch(s.get("action")){
                    case "textbox": el.sendKeys(s.get("value")); break;
                    case "click": el.click(); break;
                    case "dropdown":
                        Select sel = new Select(el);
                        switch(s.get("dropdownType")){
                            case "selectByVisibleText": sel.selectByVisibleText(s.get("value")); break;
                            case "selectByValue": sel.selectByValue(s.get("value")); break;
                            case "selectByIndex": sel.selectByIndex(Integer.parseInt(s.get("value"))); break;
                        }
                        break;
                    case "mouseHover": actions.moveToElement(el).perform(); break;
                    case "doubleClick": actions.doubleClick(el).perform(); break;
                    case "rightClick": actions.contextClick(el).perform(); break;
                    case "getText": el = driver.findElement(By.xpath(s.get("xpath"))); System.out.println("Text: "+el.getText()); break;
                    case "getAttribute": el = driver.findElement(By.xpath(s.get("xpath"))); System.out.println("Attribute: "+el.getAttribute(s.get("value"))); break;
                }
            }
            JOptionPane.showMessageDialog(null,"Automation Finished Successfully!");
        }catch(Exception e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,"Error: "+e.getMessage());
        }
    }
}