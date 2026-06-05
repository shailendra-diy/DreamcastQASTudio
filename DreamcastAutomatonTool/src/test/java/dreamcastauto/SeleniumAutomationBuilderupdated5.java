package dreamcastauto;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;

import io.github.bonigarcia.wdm.WebDriverManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.List;

public class SeleniumAutomationBuilderupdated5 extends JFrame {

    JTextField urlField,xpathField,valueField,waitField,stepNameField;
    JComboBox<String> actionBox,selectByBox;
    JCheckBox maximizeBox,parallelBox;

    JList<String> stepList;
    DefaultListModel<String> stepListModel;

    JTable resultTable;
    DefaultTableModel resultModel;

    JList<String> browserList;

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

    public SeleniumAutomationBuilderupdated5(){

        setTitle("Selenium Automation Builder");
        setSize(1400,800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JSplitPane mainSplit=new JSplitPane();
        add(mainSplit,BorderLayout.CENTER);

        JPanel leftPanel=new JPanel(new BorderLayout());
        JPanel rightPanel=new JPanel(new BorderLayout());

        mainSplit.setLeftComponent(leftPanel);
        mainSplit.setRightComponent(rightPanel);

        mainSplit.setDividerLocation(450);

        createStepBuilder();
        createStepList(leftPanel);
        createResultPanel(rightPanel);

        setVisible(true);
    }

    void createStepBuilder(){

        JPanel panel=new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Step Builder"));
        panel.setBackground(new Color(240,248,255));

        GridBagConstraints c=new GridBagConstraints();
        c.insets=new Insets(5,5,5,5);
        c.fill=GridBagConstraints.HORIZONTAL;

        urlField=new JTextField();
        xpathField=new JTextField();
        valueField=new JTextField();
        waitField=new JTextField("0");
        stepNameField=new JTextField();

        actionBox=new JComboBox<>(new String[]{
                "Click","SendKeys","Clear","GetText","GetAttribute",
                "SelectDropdown","MouseHover","Submit","Wait","Navigate","Refresh"
        });

        selectByBox=new JComboBox<>(new String[]{
                "VisibleText","Value","Index"
        });

        maximizeBox=new JCheckBox("Maximize Browser");
        parallelBox=new JCheckBox("Run Parallel");

        browserList=new JList<>(new String[]{"Chrome","Firefox","Edge"});
        browserList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        int y=0;

        c.gridx=0;c.gridy=y;panel.add(new JLabel("URL"),c);
        c.gridx=1;c.weightx=1;panel.add(urlField,c);y++;

        c.gridx=0;c.gridy=y;c.weightx=0;panel.add(new JLabel("Browsers"),c);
        c.gridx=1;panel.add(new JScrollPane(browserList),c);y++;

        c.gridx=1;panel.add(maximizeBox,c);y++;
        c.gridx=1;panel.add(parallelBox,c);y++;

        c.gridx=0;c.gridy=y;panel.add(new JLabel("Action"),c);
        c.gridx=1;panel.add(actionBox,c);y++;

        c.gridx=0;c.gridy=y;panel.add(new JLabel("Xpath"),c);
        c.gridx=1;panel.add(xpathField,c);y++;

        c.gridx=0;c.gridy=y;panel.add(new JLabel("Value"),c);
        c.gridx=1;panel.add(valueField,c);y++;

        c.gridx=0;c.gridy=y;panel.add(new JLabel("Select By"),c);
        c.gridx=1;panel.add(selectByBox,c);y++;

        c.gridx=0;c.gridy=y;panel.add(new JLabel("Wait"),c);
        c.gridx=1;panel.add(waitField,c);y++;

        c.gridx=0;c.gridy=y;panel.add(new JLabel("Step Name"),c);
        c.gridx=1;panel.add(stepNameField,c);y++;

        JPanel btnPanel=new JPanel();

        JButton addBtn=new JButton("Add Step");
        JButton updateBtn=new JButton("Update Step");
        JButton deleteBtn=new JButton("Delete Step");
        JButton runBtn=new JButton("Run");

        btnPanel.add(addBtn);
        btnPanel.add(updateBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(runBtn);

        c.gridx=0;c.gridy=y;c.gridwidth=2;
        panel.add(btnPanel,c);

        add(panel,BorderLayout.NORTH);

        addBtn.addActionListener(e->addStep());
        updateBtn.addActionListener(e->updateStep());
        deleteBtn.addActionListener(e->deleteStep());
        runBtn.addActionListener(e->runTest());

        actionBox.addActionListener(e->updateDynamicFields());

        updateDynamicFields();
    }

    void updateDynamicFields(){

        String action=(String)actionBox.getSelectedItem();

        selectByBox.setVisible(false);
        valueField.setVisible(true);

        if(action.equals("SelectDropdown")){
            selectByBox.setVisible(true);
        }

        revalidate();
        repaint();
    }

    void createStepList(JPanel panel){

        stepListModel=new DefaultListModel<>();
        stepList=new JList<>(stepListModel);

        stepList.setDragEnabled(true);
        stepList.setDropMode(DropMode.INSERT);

        stepList.addListSelectionListener(e->loadStep());

        panel.setBorder(BorderFactory.createTitledBorder("Steps"));
        panel.add(new JScrollPane(stepList),BorderLayout.CENTER);
    }

    void createResultPanel(JPanel panel){

        panel.setBorder(BorderFactory.createTitledBorder("Execution Result"));

        resultModel=new DefaultTableModel(
                new String[]{"Step No","Step Name","Status","Screenshot"},0);

        resultTable=new JTable(resultModel);
        resultTable.setRowHeight(25);

        panel.add(new JScrollPane(resultTable),BorderLayout.CENTER);
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

        refreshStepList();

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

        refreshStepList();
    }

    void deleteStep(){

        if(selectedIndex==-1)return;

        steps.remove(selectedIndex);
        refreshStepList();
        clearFields();
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

    void refreshStepList(){

        stepListModel.clear();

        int i=1;

        for(Step s:steps){

            stepListModel.addElement(i+" - "+s.name+" ["+s.action+"]");

            i++;
        }
    }

    void clearFields(){

        xpathField.setText("");
        valueField.setText("");
        waitField.setText("0");
        stepNameField.setText("");
    }

    void runTest(){

        resultModel.setRowCount(0);

        List<String> browsers=browserList.getSelectedValuesList();

        boolean parallel=parallelBox.isSelected();

        if(parallel){

            browsers.forEach(b->new Thread(()->executeBrowser(b)).start());

        }else{

            for(String b:browsers){

                executeBrowser(b);

            }

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

                try{

                    if(s.wait>0)Thread.sleep(s.wait*1000);

                    WebElement el=driver.findElement(By.xpath(s.xpath));

                    switch(s.action){

                        case "Click": el.click(); break;
                        case "SendKeys": el.sendKeys(s.value); break;
                        case "Clear": el.clear(); break;
                        case "GetText": el.getText(); break;
                        case "GetAttribute": el.getAttribute(s.value); break;

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

                            break;
                    }

                    resultModel.addRow(new Object[]{
                            stepNo,s.name,"PASS",""
                    });

                    resultTable.setRowSelectionInterval(stepNo-1,stepNo-1);
                    resultTable.setSelectionBackground(Color.GREEN);

                }catch(Exception ex){

                    String path="screenshot_"+stepNo+".png";

                    File src=((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
                    Files.copy(src.toPath(),new File(path).toPath());

                    resultModel.addRow(new Object[]{
                            stepNo,s.name,"FAIL",path
                    });

                    resultTable.setRowSelectionInterval(stepNo-1,stepNo-1);
                    resultTable.setSelectionBackground(Color.RED);

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

        SwingUtilities.invokeLater(()->new SeleniumAutomationBuilderupdated5());

    }
}