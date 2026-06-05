package dreamcastauto;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import io.github.bonigarcia.wdm.WebDriverManager;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SeleniumAutomationBuilder2 {

    static class Step {

        public String type;
        public String xpath;
        public String value;

        public Step(){}

        public Step(String type,String xpath,String value){
            this.type=type;
            this.xpath=xpath;
            this.value=value;
        }
    }

    static List<Step> steps=new ArrayList<>();

    public static void main(String[] args) {

        JFrame frame=new JFrame("Selenium Automation Builder");
        frame.setSize(700,700);
        frame.setLayout(new FlowLayout());

        JTextField urlField=new JTextField(45);

        String[] browsers={"Chrome","Edge","Firefox"};
        JComboBox<String> browserSelect=new JComboBox<>(browsers);

        String[] elementTypes={"TextField","Button","Dropdown"};
        JComboBox<String> elementSelect=new JComboBox<>(elementTypes);

        JTextField xpathField=new JTextField(45);
        JTextField valueField=new JTextField(25);

        JButton addStep=new JButton("Add Step");
        JButton runTest=new JButton("Run");
        JButton saveScript=new JButton("Save Script");
        JButton loadScript=new JButton("Load Script");

        JTextArea log=new JTextArea(20,55);
        JScrollPane scroll=new JScrollPane(log);

        frame.add(new JLabel("Enter URL"));
        frame.add(urlField);

        frame.add(new JLabel("Select Browser"));
        frame.add(browserSelect);

        frame.add(new JLabel("Element Type"));
        frame.add(elementSelect);

        frame.add(new JLabel("XPath"));
        frame.add(xpathField);

        frame.add(new JLabel("Value (for input/dropdown)"));
        frame.add(valueField);

        frame.add(addStep);
        frame.add(runTest);
        frame.add(saveScript);
        frame.add(loadScript);

        frame.add(scroll);

        addStep.addActionListener(e -> {

            String type=elementSelect.getSelectedItem().toString();
            String xpath=xpathField.getText();
            String value=valueField.getText();

            steps.add(new Step(type,xpath,value));

            log.append("Step Added → "+type+" : "+xpath+"\n");

            xpathField.setText("");
            valueField.setText("");

        });

        runTest.addActionListener(e -> {

            try{

                WebDriver driver;

                String browser=browserSelect.getSelectedItem().toString();

                if(browser.equals("Chrome")){
                    WebDriverManager.chromedriver().setup();
                    driver=new ChromeDriver();
                }
                else if(browser.equals("Edge")){
                    WebDriverManager.edgedriver().setup();
                    driver=new EdgeDriver();
                }
                else{
                    WebDriverManager.firefoxdriver().setup();
                    driver=new FirefoxDriver();
                }

                driver.manage().window().maximize();

                driver.get(urlField.getText());

                for(Step step:steps){

                    WebElement element=driver.findElement(By.xpath(step.xpath));

                    if(step.type.equals("TextField")){
                        element.sendKeys(step.value);
                    }

                    else if(step.type.equals("Button")){
                        element.click();
                    }

                    else if(step.type.equals("Dropdown")){
                        element.sendKeys(step.value);
                    }

                    Thread.sleep(1000);

                }

                log.append("Execution Completed\n");

            }

            catch(Exception ex){
                log.append("Execution Error: "+ex.getMessage()+"\n");
            }

        });

        saveScript.addActionListener(e -> {

            try{

                ObjectMapper mapper=new ObjectMapper();

                mapper.writeValue(new File("testScript.json"),steps);

                log.append("Script Saved Successfully\n");

            }

            catch(Exception ex){
                log.append("Save Error: "+ex.getMessage()+"\n");
            }

        });

        loadScript.addActionListener(e -> {

            try{

                ObjectMapper mapper=new ObjectMapper();

                steps=Arrays.asList(
                        mapper.readValue(
                                new File("testScript.json"),
                                Step[].class
                        )
                );

                log.append("Script Loaded Successfully\n");

            }

            catch(Exception ex){
                log.append("Load Error: "+ex.getMessage()+"\n");
            }

        });

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}