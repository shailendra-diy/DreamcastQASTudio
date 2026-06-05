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
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DreamcastAutomationBuilder {

    static class Step {

        String action;
        List<String> xpaths;
        String textValue;
        String waitTime;
        String description;

        Step(String action, List<String> xpaths, String textValue, String waitTime, String description) {

            this.action = action;
            this.xpaths = xpaths;
            this.textValue = textValue;
            this.waitTime = waitTime;
            this.description = description;
        }
    }

    static List<Step> steps = new ArrayList<>();
    static DefaultListModel<String> stepModel = new DefaultListModel<>();
    static JTextArea resultArea = new JTextArea();

    public static void main(String[] args) {

        JFrame frame = new JFrame("Dreamcast Automation Builder");
        frame.setSize(1200,750);
        frame.setLayout(new BorderLayout());

        JPanel header = new JPanel();
        header.setBackground(new Color(20,120,220));

        JLabel brand = new JLabel("Dreamcast Automation Builder");
        brand.setFont(new Font("Arial",Font.BOLD,22));
        brand.setForeground(Color.WHITE);

        header.add(brand);

        JPanel urlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JTextField urlField = new JTextField(40);
        JCheckBox maximize = new JCheckBox("Maximize Browser");

        String[] browsers={"Chrome","Edge","Firefox"};
        JComboBox<String> browserSelect = new JComboBox<>(browsers);

        urlPanel.add(new JLabel("URL"));
        urlPanel.add(urlField);
        urlPanel.add(new JLabel("Browser"));
        urlPanel.add(browserSelect);
        urlPanel.add(maximize);

        JPanel stepPanel = new JPanel(new GridBagLayout());
        stepPanel.setBorder(new TitledBorder("Add / Edit Step"));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5,5,5,5);
        g.fill = GridBagConstraints.HORIZONTAL;

        String[] actions={
                "TextField","Button","Dropdown","Link",
                "Checkbox","Radio","VerifyText","AssertText",
                "Hover","Wait"
        };

        JComboBox<String> actionBox = new JComboBox<>(actions);

        JTextArea xpathArea = new JTextArea(3,35);
        JScrollPane xpathScroll = new JScrollPane(xpathArea);

        JTextField textValueField = new JTextField(20);
        JTextField waitField = new JTextField(20);

        JTextField descField = new JTextField(20);

        JLabel textLabel = new JLabel("Text / Value");
        JLabel waitLabel = new JLabel("Wait Time (ms)");

        JButton addBtn = new JButton("Add Step");
        JButton updateBtn = new JButton("Update Step");
        JButton deleteBtn = new JButton("Delete Step");

        g.gridx=0; g.gridy=0;
        stepPanel.add(new JLabel("Action"),g);

        g.gridx=1;
        stepPanel.add(actionBox,g);

        g.gridx=0; g.gridy=1;
        stepPanel.add(new JLabel("XPath(s)"),g);

        g.gridx=1;
        stepPanel.add(xpathScroll,g);

        g.gridx=0; g.gridy=2;
        stepPanel.add(textLabel,g);

        g.gridx=1;
        stepPanel.add(textValueField,g);

        g.gridx=0; g.gridy=3;
        stepPanel.add(waitLabel,g);

        g.gridx=1;
        stepPanel.add(waitField,g);

        g.gridx=0; g.gridy=4;
        stepPanel.add(new JLabel("Description"),g);

        g.gridx=1;
        stepPanel.add(descField,g);

        g.gridx=0; g.gridy=5;
        stepPanel.add(addBtn,g);

        g.gridx=1;
        stepPanel.add(updateBtn,g);

        g.gridx=2;
        stepPanel.add(deleteBtn,g);

        JList<String> stepList = new JList<>(stepModel);

        JScrollPane stepScroll = new JScrollPane(stepList);
        stepScroll.setBorder(new TitledBorder("Steps"));

        resultArea.setEditable(false);
        resultArea.setLineWrap(true);

        JScrollPane resultScroll = new JScrollPane(resultArea);
        resultScroll.setBorder(new TitledBorder("Execution Results"));

        JPanel btnPanel = new JPanel();

        JButton runBtn = new JButton("Run Script");
        JButton saveBtn = new JButton("Save Script");
        JButton loadBtn = new JButton("Load Script");

        btnPanel.add(runBtn);
        btnPanel.add(saveBtn);
        btnPanel.add(loadBtn);

        JPanel center = new JPanel(new GridLayout(1,2));
        center.add(stepPanel);
        center.add(stepScroll);

        JPanel north = new JPanel(new BorderLayout());
        north.add(header,BorderLayout.NORTH);
        north.add(urlPanel,BorderLayout.SOUTH);

        frame.add(north,BorderLayout.NORTH);
        frame.add(center,BorderLayout.CENTER);
        frame.add(resultScroll,BorderLayout.SOUTH);
        frame.add(btnPanel,BorderLayout.PAGE_END);

        actionBox.addActionListener(e->{

            String act = actionBox.getSelectedItem().toString();

            textValueField.setVisible(false);
            waitField.setVisible(false);
            textLabel.setVisible(false);
            waitLabel.setVisible(false);

            if(act.equals("TextField") || act.equals("Dropdown") ||
               act.equals("VerifyText") || act.equals("AssertText")){

                textValueField.setVisible(true);
                textLabel.setVisible(true);
            }

            if(act.equals("Wait")){

                waitField.setVisible(true);
                waitLabel.setVisible(true);
            }

        });

        Runnable refresh = () -> {

            stepModel.clear();

            int i=1;

            for(Step s:steps){

                stepModel.addElement(i+". "+s.action+" | "+s.description);
                i++;
            }
        };

        addBtn.addActionListener(e -> {

            List<String> xp = Arrays.asList(xpathArea.getText().split("\n"));

            steps.add(new Step(

                    actionBox.getSelectedItem().toString(),
                    xp,
                    textValueField.getText(),
                    waitField.getText(),
                    descField.getText()
            ));

            refresh.run();

            xpathArea.setText("");
            textValueField.setText("");
            waitField.setText("");
            descField.setText("");
        });

        stepList.addListSelectionListener(e -> {

            int i = stepList.getSelectedIndex();

            if(i>=0){

                Step s = steps.get(i);

                actionBox.setSelectedItem(s.action);
                xpathArea.setText(String.join("\n",s.xpaths));
                textValueField.setText(s.textValue);
                waitField.setText(s.waitTime);
                descField.setText(s.description);
            }
        });

        updateBtn.addActionListener(e -> {

            int i = stepList.getSelectedIndex();

            if(i>=0){

                Step s = steps.get(i);

                s.action = actionBox.getSelectedItem().toString();
                s.xpaths = Arrays.asList(xpathArea.getText().split("\n"));
                s.textValue = textValueField.getText();
                s.waitTime = waitField.getText();
                s.description = descField.getText();

                refresh.run();
            }
        });

        deleteBtn.addActionListener(e -> {

            int i = stepList.getSelectedIndex();

            if(i>=0){

                steps.remove(i);
                refresh.run();
            }
        });

        runBtn.addActionListener(e -> {

            resultArea.setText("");

            new Thread(() -> {

                try{

                    WebDriver driver;

                    String b = browserSelect.getSelectedItem().toString();

                    if(b.equals("Chrome")){

                        WebDriverManager.chromedriver().setup();
                        driver = new ChromeDriver();
                    }
                    else if(b.equals("Edge")){

                        WebDriverManager.edgedriver().setup();
                        driver = new EdgeDriver();
                    }
                    else{

                        WebDriverManager.firefoxdriver().setup();
                        driver = new FirefoxDriver();
                    }

                    if(maximize.isSelected())
                        driver.manage().window().maximize();

                    driver.get(urlField.getText());

                    List<String> logs = new ArrayList<>();

                    int stepNo = 1;

                    for(Step s:steps){

                        boolean pass=false;

                        if(s.action.equals("Wait")){

                            Thread.sleep(Long.parseLong(s.waitTime));
                            pass=true;
                        }
                        else{

                            for(String xp:s.xpaths){

                                try{

                                    WebElement el = driver.findElement(By.xpath(xp));

                                    switch(s.action){

                                        case "TextField":
                                            el.sendKeys(s.textValue);
                                            break;

                                        case "Dropdown":
                                            el.sendKeys(s.textValue);
                                            break;

                                        case "VerifyText":
                                            if(!el.getText().contains(s.textValue))
                                                throw new Exception();
                                            break;

                                        case "AssertText":
                                            if(!el.getText().equals(s.textValue))
                                                throw new Exception();
                                            break;

                                        case "Button":
                                        case "Link":
                                            el.click();
                                            break;

                                        case "Checkbox":
                                        case "Radio":
                                            if(!el.isSelected())
                                                el.click();
                                            break;

                                        case "Hover":
                                            new Actions(driver).moveToElement(el).perform();
                                            break;
                                    }

                                    pass=true;
                                    break;

                                }catch(Exception ignored){}
                            }
                        }

                        if(pass){

                            logs.add("Step "+stepNo+" PASS");
                            resultArea.append("Step "+stepNo+" PASS\n");
                        }
                        else{

                            String ss=captureScreenshot(driver,"step_"+stepNo);

                            logs.add("Step "+stepNo+" FAIL screenshot:"+ss);
                            resultArea.append("Step "+stepNo+" FAIL\n");
                        }

                        stepNo++;
                    }

                    generateHtmlReport(logs);

                }catch(Exception ex){

                    resultArea.append("ERROR "+ex.getMessage());
                }

            }).start();
        });

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    static String captureScreenshot(WebDriver driver,String name){

        try{

            File src = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);

            String path="screenshots/"+name+".png";

            File dest=new File(path);
            dest.getParentFile().mkdirs();

            Files.copy(src.toPath(),dest.toPath());

            return path;

        }catch(Exception e){

            return "";
        }
    }

    static void generateHtmlReport(List<String> logs){

        try{

            FileWriter fw=new FileWriter("report.html");

            fw.write("<html><head><title>Dreamcast Automation Report</title></head><body>");
            fw.write("<h1>Dreamcast Automation Execution</h1>");

            for(String log:logs){

                fw.write("<p>"+log+"</p>");
            }

            fw.write("</body></html>");

            fw.close();

        }catch(Exception ignored){}
    }
}