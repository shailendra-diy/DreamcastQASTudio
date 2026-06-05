package dreamcastauto;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import io.github.bonigarcia.wdm.WebDriverManager;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DreamcastAutomationTool3 {

    static class Step {

        String name;
        String action;
        List<String> locators;
        String value;
        int wait;

        Step(String name,String action,List<String> locators,String value,int wait){
            this.name=name;
            this.action=action;
            this.locators=locators;
            this.value=value;
            this.wait=wait;
        }
    }

    static List<Step> steps=new ArrayList<>();
    static DefaultListModel<String> stepModel=new DefaultListModel<>();

    static JTextArea logArea=new JTextArea();

    public static void main(String[] args){

        JFrame frame=new JFrame("Selenium Automation Builder");
        frame.setSize(1200,750);
        frame.setLayout(new BorderLayout());

        // LEFT STEP PANEL

        JPanel leftPanel=new JPanel(new BorderLayout());
        leftPanel.setBorder(new TitledBorder("Steps"));

        JList<String> stepList=new JList<>(stepModel);

        leftPanel.add(new JScrollPane(stepList));

        // CENTER PANEL

        JPanel panel=new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder("Step Builder"));

        GridBagConstraints g=new GridBagConstraints();
        g.insets=new Insets(5,5,5,5);
        g.fill=GridBagConstraints.HORIZONTAL;

        int y=0;

        JTextField url=new JTextField();

        JCheckBox chrome=new JCheckBox("Chrome");
        JCheckBox edge=new JCheckBox("Edge");
        JCheckBox firefox=new JCheckBox("Firefox");

        JCheckBox parallel=new JCheckBox("Parallel Execution");
        JCheckBox maximize=new JCheckBox("Maximize Browser");

        JTextField stepName=new JTextField();

        String[] actions={

                "Textbox-sendKeys",
                "Button-click",
                "Link-click",
                "Dropdown-selectText",
                "Dropdown-selectValue",
                "Dropdown-selectIndex",
                "Mouse-hover",
                "DoubleClick",
                "RightClick",
                "ClearTextbox",
                "GetText",
                "IsDisplayed"
        };

        JComboBox<String> actionBox=new JComboBox<>(actions);

        JTextArea locatorArea=new JTextArea(4,30);

        JTextField valueField=new JTextField();

        JTextField waitField=new JTextField();

        JButton addBtn=new JButton("Add Step");
        JButton updateBtn=new JButton("Update Step");
        JButton deleteBtn=new JButton("Delete Step");
        JButton runBtn=new JButton("Run Test");

        // URL

        g.gridx=0; g.gridy=y; panel.add(new JLabel("URL"),g);
        g.gridx=1; panel.add(url,g); y++;

        // Browser

        g.gridx=0; g.gridy=y; panel.add(new JLabel("Browsers"),g);

        JPanel browserPanel=new JPanel();
        browserPanel.add(chrome);
        browserPanel.add(edge);
        browserPanel.add(firefox);

        g.gridx=1; panel.add(browserPanel,g); y++;

        g.gridx=1; panel.add(parallel,g); y++;
        g.gridx=1; panel.add(maximize,g); y++;

        // Step Name

        g.gridx=0; g.gridy=y; panel.add(new JLabel("Step Name"),g);
        g.gridx=1; panel.add(stepName,g); y++;

        // Action

        g.gridx=0; g.gridy=y; panel.add(new JLabel("Element Action"),g);
        g.gridx=1; panel.add(actionBox,g); y++;

        // Locator

        g.gridx=0; g.gridy=y; panel.add(new JLabel("XPath / CSS / ID (Multiple allowed)"),g);
        g.gridx=1; panel.add(new JScrollPane(locatorArea),g); y++;

        // Value

        g.gridx=0; g.gridy=y; panel.add(new JLabel("Value"),g);
        g.gridx=1; panel.add(valueField,g); y++;

        // Wait

        g.gridx=0; g.gridy=y; panel.add(new JLabel("Wait Seconds"),g);
        g.gridx=1; panel.add(waitField,g); y++;

        // Buttons

        JPanel btnPanel=new JPanel();
        btnPanel.add(addBtn);
        btnPanel.add(updateBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(runBtn);

        g.gridx=1; g.gridy=y; panel.add(btnPanel,g);

        // LOG PANEL

        JPanel bottom=new JPanel(new BorderLayout());
        bottom.setBorder(new TitledBorder("Execution Log"));
        bottom.add(new JScrollPane(logArea));

        frame.add(leftPanel,BorderLayout.WEST);
        frame.add(panel,BorderLayout.CENTER);
        frame.add(bottom,BorderLayout.SOUTH);

        // ACTIONS

        addBtn.addActionListener(e->{

            try{

                List<String> locators=new ArrayList<>();

                for(String l:locatorArea.getText().split("\n")){
                    if(!l.trim().isEmpty()) locators.add(l.trim());
                }

                Step step=new Step(

                        stepName.getText(),
                        actionBox.getSelectedItem().toString(),
                        locators,
                        valueField.getText(),
                        Integer.parseInt(waitField.getText())
                );

                steps.add(step);

                stepModel.addElement(step.name+" → "+step.action);

                log("Step Added");

            }
            catch(Exception ex){

                log("Add Error "+ex.getMessage());
            }

        });

        updateBtn.addActionListener(e->{

            int index=stepList.getSelectedIndex();

            if(index<0) return;

            Step step=steps.get(index);

            step.name=stepName.getText();
            step.action=actionBox.getSelectedItem().toString();
            step.value=valueField.getText();

            stepModel.set(index,step.name+" → "+step.action);

            log("Step Updated");
        });

        deleteBtn.addActionListener(e->{

            int index=stepList.getSelectedIndex();

            if(index>=0){

                steps.remove(index);
                stepModel.remove(index);

                log("Step Deleted");
            }

        });

        runBtn.addActionListener(e->{

            new Thread(()->{

                List<String> browsers=new ArrayList<>();

                if(chrome.isSelected()) browsers.add("Chrome");
                if(edge.isSelected()) browsers.add("Edge");
                if(firefox.isSelected()) browsers.add("Firefox");

                if(parallel.isSelected()){

                    for(String b:browsers){

                        new Thread(()->runTest(b,url.getText(),maximize.isSelected())).start();
                    }

                }
                else{

                    for(String b:browsers){

                        runTest(b,url.getText(),maximize.isSelected());
                    }
                }

            }).start();

        });

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    static void log(String msg){

        logArea.append(msg+"\n");
    }

    static WebDriver createDriver(String browser){

        if(browser.equals("Chrome")){
            WebDriverManager.chromedriver().setup();
            return new ChromeDriver();
        }

        if(browser.equals("Edge")){
            WebDriverManager.edgedriver().setup();
            return new EdgeDriver();
        }

        WebDriverManager.firefoxdriver().setup();
        return new FirefoxDriver();
    }

    static WebElement findElement(WebDriver driver,List<String> locators){

        for(String l:locators){

            try{

                if(l.startsWith("//")) return driver.findElement(By.xpath(l));

                if(l.startsWith("#")) return driver.findElement(By.cssSelector(l));

                return driver.findElement(By.id(l));

            }
            catch(Exception ignored){}
        }

        throw new RuntimeException("Element not found");
    }

    static void runTest(String browser,String url,boolean maximize){

        try{

            WebDriver driver=createDriver(browser);

            if(maximize) driver.manage().window().maximize();

            driver.get(url);

            int stepNo=1;

            for(Step s:steps){

                try{

                    Thread.sleep(s.wait*1000);

                    WebElement el=findElement(driver,s.locators);

                    if(s.action.contains("sendKeys")) el.sendKeys(s.value);

                    else if(s.action.contains("click")) el.click();

                    else if(s.action.contains("hover"))
                        new Actions(driver).moveToElement(el).perform();

                    else if(s.action.contains("DoubleClick"))
                        new Actions(driver).doubleClick(el).perform();

                    else if(s.action.contains("RightClick"))
                        new Actions(driver).contextClick(el).perform();

                    else if(s.action.contains("Clear")) el.clear();

                    else if(s.action.contains("GetText"))
                        log("Text: "+el.getText());

                    log(browser+" Step "+stepNo+" Passed");

                }

                catch(Exception ex){

                    log(browser+" Step "+stepNo+" FAILED → "+s.name);
                    log("Error: "+ex.getMessage());

                    break;
                }

                stepNo++;
            }

        }
        catch(Exception e){

            log("Execution Error "+e.getMessage());
        }
    }
}