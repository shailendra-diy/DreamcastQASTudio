package dreamcastauto;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;
import io.github.bonigarcia.wdm.WebDriverManager;

public class UltimateAutomationBuilderupdated4 {

	static JTable stepTable;
	static JTable resultTable;

	static DefaultTableModel stepModel;
	static DefaultTableModel resultModel;

	static List<Step> steps = new ArrayList<>();

	static JTextField urlField;
	static JTextField xpathField;
	static JTextField valueField;
	static JTextField waitField;
	static JTextField nameField;

	static JComboBox<String> actionBox;
	static JComboBox<String> browserBox;

	static int selectedStepIndex = -1;

	public static void main(String[] args) {

		JFrame frame = new JFrame("Ultimate Selenium Automation Builder");
		frame.setSize(1400, 800);
		frame.setLayout(new BorderLayout());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.add(createStepBuilder(), BorderLayout.NORTH);

		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createStepPanel(), createResultPanel());

		split.setDividerLocation(600);
		frame.add(split, BorderLayout.CENTER);

		frame.setVisible(true);

	}

	static JPanel createStepBuilder() {

		JPanel p = new JPanel(new GridBagLayout());
		p.setBorder(BorderFactory.createTitledBorder("Step Builder"));

		GridBagConstraints g = new GridBagConstraints();
		g.insets = new Insets(5, 5, 5, 5);
		g.fill = GridBagConstraints.HORIZONTAL;

		urlField = new JTextField();

		browserBox = new JComboBox<>(new String[] { "Chrome", "Firefox", "Edge" });

		actionBox = new JComboBox<>(getAllActions());

		xpathField = new JTextField();
		valueField = new JTextField();
		waitField = new JTextField("1");
		nameField = new JTextField();

		g.gridx = 0;
		g.gridy = 0;
		p.add(new JLabel("URL"), g);
		g.gridx = 1;
		g.gridy = 0;
		p.add(urlField, g);

		g.gridx = 2;
		p.add(new JLabel("Browser"), g);
		g.gridx = 3;
		p.add(browserBox, g);

		g.gridx = 0;
		g.gridy = 1;
		p.add(new JLabel("Action"), g);
		g.gridx = 1;
		p.add(actionBox, g);

		g.gridx = 2;
		p.add(new JLabel("XPath"), g);
		g.gridx = 3;
		p.add(xpathField, g);

		g.gridx = 0;
		g.gridy = 2;
		p.add(new JLabel("Value"), g);
		g.gridx = 1;
		p.add(valueField, g);

		g.gridx = 2;
		p.add(new JLabel("Wait"), g);
		g.gridx = 3;
		p.add(waitField, g);

		g.gridx = 0;
		g.gridy = 3;
		p.add(new JLabel("Step Name"), g);
		g.gridx = 1;
		p.add(nameField, g);

		JButton add = new JButton("Add Step");
		JButton update = new JButton("Update Step");
		JButton delete = new JButton("Delete Step");
		JButton run = new JButton("Run");

		g.gridx = 0;
		g.gridy = 4;
		p.add(add, g);
		g.gridx = 1;
		p.add(update, g);
		g.gridx = 2;
		p.add(delete, g);
		g.gridx = 3;
		p.add(run, g);

		add.addActionListener(e -> addStep());
		update.addActionListener(e -> updateStep());
		delete.addActionListener(e -> deleteStep());
		run.addActionListener(e -> runAutomation());

		return p;
	}

	static JScrollPane createStepPanel() {

		stepModel = new DefaultTableModel(new String[] { "#", "Step Name", "Action", "XPath", "Value" }, 0);

		stepTable = new JTable(stepModel);
		stepTable.setRowHeight(25);

		stepTable.getSelectionModel().addListSelectionListener(e -> loadStep());

		enableDragDrop(stepTable);

		return new JScrollPane(stepTable);
	}

	static JScrollPane createResultPanel() {

		resultModel = new DefaultTableModel(new String[] { "Step", "Result", "Message" }, 0);

		resultTable = new JTable(resultModel);
		resultTable.setRowHeight(25);

		return new JScrollPane(resultTable);
	}

	static void addStep() {

		Step s = new Step();

		s.name = nameField.getText();
		s.action = (String) actionBox.getSelectedItem();
		s.xpath = xpathField.getText();
		s.value = valueField.getText();
		s.wait = Integer.parseInt(waitField.getText());

		steps.add(s);

		refreshStepTable();

	}

	static void updateStep() {

		if (selectedStepIndex < 0)
			return;

		Step s = steps.get(selectedStepIndex);

		s.name = nameField.getText();
		s.action = (String) actionBox.getSelectedItem();
		s.xpath = xpathField.getText();
		s.value = valueField.getText();
		s.wait = Integer.parseInt(waitField.getText());

		refreshStepTable();

	}

	static void deleteStep() {

		int row = stepTable.getSelectedRow();

		if (row >= 0) {

			steps.remove(row);
			refreshStepTable();

		}

	}

	static void loadStep() {

		int row = stepTable.getSelectedRow();
		if (row < 0)
			return;

		selectedStepIndex = row;

		Step s = steps.get(row);

		nameField.setText(s.name);
		xpathField.setText(s.xpath);
		valueField.setText(s.value);
		waitField.setText("" + s.wait);
		actionBox.setSelectedItem(s.action);

	}

	static void refreshStepTable() {

		stepModel.setRowCount(0);

		for (int i = 0; i < steps.size(); i++) {

			Step s = steps.get(i);

			stepModel.addRow(new Object[] { i + 1, s.name, s.action, s.xpath, s.value });

		}

	}

	static void runAutomation() {

		resultModel.setRowCount(0);

		ExecutorService pool = Executors.newFixedThreadPool(3);

		pool.submit(() -> executeTest());

		pool.shutdown();

	}

	static void executeTest() {

		WebDriver driver = null;

		try {

			String b = (String) browserBox.getSelectedItem();

			if (b.equals("Chrome")) {
				WebDriverManager.chromedriver().setup();
				driver = new ChromeDriver();
			}

			if (b.equals("Firefox")) {
				WebDriverManager.firefoxdriver().setup();
				driver = new FirefoxDriver();
			}

			if (b.equals("Edge")) {
				WebDriverManager.edgedriver().setup();
				driver = new EdgeDriver();
			}

			driver.get(urlField.getText());

			Actions actions = new Actions(driver);

			for (Step s : steps) {

				try {

					Thread.sleep(s.wait * 1000);

					WebElement el = driver.findElement(By.xpath(s.xpath));

					executeAction(driver, actions, el, s);

					resultModel.addRow(new Object[] { s.name, "PASS", "" });

					colorRow(resultTable, resultModel.getRowCount() - 1, Color.GREEN);

				} catch (Exception ex) {

					takeScreenshot(driver, s.name);

					resultModel.addRow(new Object[] { s.name, "FAIL", ex.getMessage() });

					colorRow(resultTable, resultModel.getRowCount() - 1, Color.RED);

				}

			}

			generateReport();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	static void executeAction(WebDriver driver, Actions actions, WebElement el, Step s) {

		switch (s.action) {

		case "click":
			el.click();
			break;
		case "sendKeys":
			el.sendKeys(s.value);
			break;
		case "clear":
			el.clear();
			break;
		case "mouseHover":
			actions.moveToElement(el).perform();
			break;
		case "doubleClick":
			actions.doubleClick(el).perform();
			break;
		case "rightClick":
			actions.contextClick(el).perform();
			break;
		case "submit":
			el.submit();
			break;
		case "getText":
			System.out.println(el.getText());
			break;
		case "getAttribute":
			System.out.println(el.getAttribute(s.value));
			break;

		case "selectByText":
			new Select(el).selectByVisibleText(s.value);
			break;

		case "selectByValue":
			new Select(el).selectByValue(s.value);
			break;

		case "selectByIndex":
			new Select(el).selectByIndex(Integer.parseInt(s.value));
			break;

		}

	}

	static void takeScreenshot(WebDriver driver, String name) {

		try {

			File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

			File dest = new File("screenshots/" + name + ".png");

			dest.getParentFile().mkdirs();

			src.renameTo(dest);

		} catch (Exception e) {
		}

	}

	static void generateReport() {

		System.out.println("Report Generated");

	}

	static void colorRow(JTable table, int row, Color c) {

		table.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {

			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row2, int col) {

				Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row2, col);

				if (row2 == row)
					comp.setBackground(c);

				return comp;

			}

		});

	}

	static void enableDragDrop(JTable table) {

		table.setDragEnabled(true);
		table.setDropMode(DropMode.INSERT_ROWS);

	}

	static String[] getAllActions() {

		return new String[] { "click", "sendKeys", "clear", "submit", "mouseHover", "doubleClick", "rightClick",
				"getText", "getAttribute", "selectByText", "selectByValue", "selectByIndex", "isDisplayed", "isEnabled",
				"isSelected", "scrollTo", "scrollIntoView", "waitVisible", "waitClickable", "waitPresence",
				"switchFrame", "switchWindow", "alertAccept", "alertDismiss", "navigateBack", "navigateForward",
				"refresh", "executeJS", "uploadFile", "downloadFile", "dragAndDrop", "moveByOffset", "pressKey",
				"releaseKey", "takeScreenshot", "verifyText", "verifyAttribute", "verifyTitle", "verifyURL",
				"assertText", "assertAttribute", "assertTitle", "assertURL", "getCssValue", "getLocation", "getSize",
				"getTagName", "clearAndType", "focus", "blur", "highlight" };

	}

	static class Step {

		String name;
		String action;
		String xpath;
		String value;
		int wait;

	}

}