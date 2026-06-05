package dreamcastauto;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;
import io.github.bonigarcia.wdm.WebDriverManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.List;

public class SeleniumAutomationBuilderupdated6 extends JFrame {

    JTextField urlField,xpathField,valueField,waitField,stepNameField;
    JComboBox<String> actionBox,selectByBox;

    JCheckBox maximizeBox,parallelBox;

    JList<String> browserList;
    DefaultListModel<String> stepModel;
    JList<String> stepList;

    DefaultTableModel resultModel;
    JTable resultTable;

    List<Step> steps=new ArrayList<>();

    int selectedIndex=-1;

    class Step{
        String name;
        String action;
        String xpath;
        String value;
        String selectBy;
        int wait;
    }

    public SeleniumAutomationBuilderupdated6(){

        setTitle("Selenium Automation Builder");
        setSize(1500,850);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        buildUI();

        setVisible(true);
    }

    void buildUI(){

        JPanel builder=new JPanel(new GridBagLayout());
        builder.setBorder(BorderFactory.createTitledBorder("Step Builder"));

        GridBagConstraints c=new GridBagConstraints();
        c.insets=new Insets(6,6,6,6);
        c.fill=GridBagConstraints.HORIZONTAL;

        urlField=new JTextField();
        xpathField=new JTextField();
        valueField=new JTextField();
        waitField=new JTextField("0");
        stepNameField=new JTextField();

        actionBox=new JComboBox<>(new String[]{
                "Click","SendKeys","Clear","GetText","GetAttribute",
                "GetCurrentURL","SelectDropdown","MouseHover"
        });

        selectByBox=new JComboBox<>(new String[]{
                "VisibleText","Value","Index"
        });

        maximizeBox=new JCheckBox("Maximize Browser");
        parallelBox=new JCheckBox("Parallel Run");

        browserList=new JList<>(new String[]{"Chrome","Firefox","Edge"});
        browserList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        int y=0;

        c.gridx=0;c.gridy=y;builder.add(new JLabel("URL"),c);
        c.gridx=1;c.weightx=1;builder.add(urlField,c);y++;

        c.gridx=0;c.gridy=y;c.weightx=0;builder.add(new JLabel("Browsers"),c);
        c.gridx=1;builder.add(new JScrollPane(browserList),c);y++;

        c.gridx=1;builder.add(maximizeBox,c);y++;
        c.gridx=1;builder.add(parallelBox,c);y++;

        c.gridx=0;c.gridy=y;builder.add(new JLabel("Action"),c);
        c.gridx=1;builder.add(actionBox,c);y++;

        c.gridx=0;c.gridy=y;builder.add(new JLabel("Xpath"),c);
        c.gridx=1;builder.add(xpathField,c);y++;

        c.gridx=0;c.gridy=y;builder.add(new JLabel("Value"),c);
        c.gridx=1;builder.add(valueField,c);y++;

        c.gridx=0;c.gridy=y;builder.add(new JLabel("Select By"),c);
        c.gridx=1;builder.add(selectByBox,c);y++;

        c.gridx=0;c.gridy=y;builder.add(new JLabel("Wait (sec)"),c);
        c.gridx=1;builder.add(waitField,c);y++;

        c.gridx=0;c.gridy=y;builder.add(new JLabel("Step Name"),c);
        c.gridx=1;builder.add(stepNameField,c);y++;

        JPanel buttons=new JPanel();

        JButton add=new JButton("Add Step");
        JButton update=new JButton("Update Step");
        JButton delete=new JButton("Delete Step");
        JButton run=new JButton("Run Test");

        buttons.add(add);
        buttons.add(update);
        buttons.add(delete);
        buttons.add(run);

        c.gridx=0;c.gridy=y;c.gridwidth=2;
        builder.add(buttons,c);

        add(builder,BorderLayout.NORTH);

        actionBox.addActionListener(e->updateFields());

        add.addActionListener(e->addStep());
        update.addActionListener(e->updateStep());
        delete.addActionListener(e->deleteStep());
        run.addActionListener(e->runTest());

        JSplitPane center=new JSplitPane();

        add(center,BorderLayout.CENTER);

        stepModel=new DefaultListModel<>();
        stepList=new JList<>(stepModel);

        stepList.addListSelectionListener(e->loadStep());

        JPanel left=new JPanel(new BorderLayout());
        left.setBorder(BorderFactory.createTitledBorder("Steps"));

        left.add(new JScrollPane(stepList));

        center.setLeftComponent(left);

        resultModel=new DefaultTableModel(
                new String[]{
                        "StepNo","Browser","StepName",
                        "Action","Value","Output",
                        "Status","Screenshot"
                },0);

        resultTable=new JTable(resultModel);

        JPanel right=new JPanel(new BorderLayout());
        right.setBorder(BorderFactory.createTitledBorder("Result"));

        right.add(new JScrollPane(resultTable));

        center.setRightComponent(right);

        center.setDividerLocation(400);

        updateFields();
    }

    void updateFields(){

        String action=(String)actionBox.getSelectedItem();

        selectByBox.setVisible(false);

        if(action.equals("SelectDropdown"))
            selectByBox.setVisible(true);

        revalidate();
    }

    void addStep(){

        Step s=new Step();

        s.name=stepNameField.getText();
        s.action=(String)actionBox.getSelectedItem();
        s.xpath=xpathField.getText();
        s.value=valueField.getText();
        s.selectBy=(String)selectByBox.getSelectedItem();
        s.wait=Integer.parseInt(waitField.getText());

        steps.add(s);

        refreshSteps();

        clearFields();
    }

    void updateStep(){

        if(selectedIndex==-1)return;

        Step s=steps.get(selectedIndex);

        s.name=stepNameField.getText();
        s.action=(String)actionBox.getSelectedItem();
        s.xpath=xpathField.getText();
        s.value=valueField.getText();
        s.selectBy=(String)selectByBox.getSelectedItem();
        s.wait=Integer.parseInt(waitField.getText());

        refreshSteps();
    }

    void deleteStep(){

        if(selectedIndex==-1)return;

        steps.remove(selectedIndex);

        refreshSteps();
    }

    void loadStep(){

        selectedIndex=stepList.getSelectedIndex();

        if(selectedIndex==-1)return;

        Step s=steps.get(selectedIndex);

        stepNameField.setText(s.name);
        actionBox.setSelectedItem(s.action);
        xpathField.setText(s.xpath);
        valueField.setText(s.value);
        selectByBox.setSelectedItem(s.selectBy);
        waitField.setText(""+s.wait);
    }

    void refreshSteps(){

        stepModel.clear();

        int i=1;

        for(Step s:steps){

            stepModel.addElement(i+" - "+s.name+" ["+s.action+"]");

            i++;
        }
    }

    void clearFields(){

        xpathField.setText("");
        valueField.setText("");
        stepNameField.setText("");
        waitField.setText("0");
    }

    void runTest(){

        resultModel.setRowCount(0);

        List<String> browsers=browserList.getSelectedValuesList();

        if(parallelBox.isSelected()){

            browsers.forEach(b->new Thread(()->executeBrowser(b)).start());

        }else{

            browsers.forEach(this::executeBrowser);

        }
    }

    void executeBrowser(String browser){

        WebDriver driver=null;

        try{

            if(browser.equals("Chrome")){
                WebDriverManager.chromedriver().setup();
                driver=new ChromeDriver();
            }

            if(browser.equals("Firefox")){
                WebDriverManager.firefoxdriver().setup();
                driver=new FirefoxDriver();
            }

            if(browser.equals("Edge")){
                WebDriverManager.edgedriver().setup();
                driver=new EdgeDriver();
            }

            if(maximizeBox.isSelected())
                driver.manage().window().maximize();

            driver.get(urlField.getText());

            int stepNo=1;

            for(Step s:steps){

                String output="";

                try{

                    if(s.wait>0)Thread.sleep(s.wait*1000);

                    if(s.action.equals("GetCurrentURL")){

                        output=driver.getCurrentUrl();

                    }else{

                        WebElement el=driver.findElement(By.xpath(s.xpath));

                        switch(s.action){

                            case "Click":
                                el.click();
                                break;

                            case "SendKeys":
                                el.sendKeys(s.value);
                                break;

                            case "Clear":
                                el.clear();
                                break;

                            case "GetText":
                                output=el.getText();
                                break;

                            case "GetAttribute":
                                output=el.getAttribute(s.value);
                                break;

                            case "MouseHover":
                                new Actions(driver).moveToElement(el).perform();
                                break;

                            case "SelectDropdown":

                                Select select=new Select(el);

                                if(s.selectBy.equals("VisibleText"))
                                    select.selectByVisibleText(s.value);

                                if(s.selectBy.equals("Value"))
                                    select.selectByValue(s.value);

                                if(s.selectBy.equals("Index"))
                                    select.selectByIndex(Integer.parseInt(s.value));
                        }
                    }

                    resultModel.addRow(new Object[]{
                            stepNo,browser,s.name,
                            s.action,s.value,
                            output,"PASS",""
                    });

                }catch(Exception ex){

                    String path="fail_"+browser+"_"+stepNo+".png";

                    File src=((TakesScreenshot)driver)
                            .getScreenshotAs(OutputType.FILE);

                    Files.copy(src.toPath(),new File(path).toPath());

                    resultModel.addRow(new Object[]{
                            stepNo,browser,s.name,
                            s.action,s.value,
                            output,"FAIL",path
                    });
                }

                stepNo++;
            }

        }catch(Exception e){

            e.printStackTrace();

        }finally{

            if(driver!=null)
                driver.quit();
        }
    }

    public static void main(String[] args){

        SwingUtilities.invokeLater(SeleniumAutomationBuilderupdated6::new);
    }
}