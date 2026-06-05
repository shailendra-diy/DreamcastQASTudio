package dreamcastauto;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.Dimension;
import java.util.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;
import io.github.bonigarcia.wdm.WebDriverManager;

public class SmartAutomationBuilderudpated2 {

	static DefaultTableModel model;
	static JTable table;
	static java.util.List<Map<String, String>> steps = new ArrayList<>();

	public static void main(String[] args) {

		JFrame frame = new JFrame("Selenium Automation Builder");
		frame.setSize(1200, 700);
		frame.setLayout(new BorderLayout());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Color bg = new Color(235, 245, 255);

		JPanel top = new JPanel(new GridBagLayout());
		top.setBorder(BorderFactory.createTitledBorder("Browser Setup"));
		top.setBackground(bg);

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(5, 5, 5, 5);
		c.fill = GridBagConstraints.HORIZONTAL;

		JTextField url = new JTextField();
		JComboBox<String> browser = new JComboBox<>(new String[] { "Chrome", "Firefox", "Edge" });
		JCheckBox max = new JCheckBox("Maximize");

		c.gridx = 0;
		c.gridy = 0;
		top.add(new JLabel("URL"), c);
		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 2;
		top.add(url, c);
		c.gridwidth = 1;

		c.gridx = 0;
		c.gridy = 1;
		top.add(new JLabel("Browser"), c);
		c.gridx = 1;
		c.gridy = 1;
		top.add(browser, c);
		c.gridx = 2;
		c.gridy = 1;
		top.add(max, c);

		frame.add(top, BorderLayout.NORTH);

		JPanel left = new JPanel(new GridBagLayout());
		left.setBorder(BorderFactory.createTitledBorder("Step Builder"));
		left.setBackground(bg);

		GridBagConstraints s = new GridBagConstraints();
		s.insets = new Insets(5, 5, 5, 5);
		s.fill = GridBagConstraints.HORIZONTAL;

		JComboBox<String> action = new JComboBox<>(new String[] { "textbox", "click", "dropdown", "mouseHover",
				"doubleClick", "rightClick", "getText", "getAttribute" });

		JComboBox<String> selectType = new JComboBox<>(
				new String[] { "selectByVisibleText", "selectByValue", "selectByIndex" });

		selectType.setVisible(false);

		JTextField xpath = new JTextField();
		JTextField value = new JTextField();
		JTextField wait = new JTextField("1");
		JTextField stepName = new JTextField();

		s.gridx = 0;
		s.gridy = 0;
		left.add(new JLabel("Action"), s);
		s.gridx = 1;
		s.gridy = 0;
		left.add(action, s);

		s.gridx = 0;
		s.gridy = 1;
		left.add(new JLabel("Select Type"), s);
		s.gridx = 1;
		s.gridy = 1;
		left.add(selectType, s);

		s.gridx = 0;
		s.gridy = 2;
		left.add(new JLabel("XPath"), s);
		s.gridx = 1;
		s.gridy = 2;
		left.add(xpath, s);

		s.gridx = 0;
		s.gridy = 3;
		left.add(new JLabel("Value"), s);
		s.gridx = 1;
		s.gridy = 3;
		left.add(value, s);

		s.gridx = 0;
		s.gridy = 4;
		left.add(new JLabel("Wait"), s);
		s.gridx = 1;
		s.gridy = 4;
		left.add(wait, s);

		s.gridx = 0;
		s.gridy = 5;
		left.add(new JLabel("Step Name"), s);
		s.gridx = 1;
		s.gridy = 5;
		left.add(stepName, s);

		JButton add = new JButton("Add Step");
		JButton update = new JButton("Update Step");
		update.setEnabled(false);

		s.gridx = 0;
		s.gridy = 6;
		left.add(add, s);
		s.gridx = 1;
		s.gridy = 6;
		left.add(update, s);

		frame.add(left, BorderLayout.WEST);

		model = new DefaultTableModel(new String[] { "Step", "Action", "XPath", "Value", "Wait" }, 0);

		table = new JTable(model);
		table.setRowHeight(25);

		JScrollPane scroll = new JScrollPane(table);
		scroll.setBorder(BorderFactory.createTitledBorder("Steps"));

		frame.add(scroll, BorderLayout.CENTER);

		JTextArea result = new JTextArea();
		result.setLineWrap(true);
		result.setWrapStyleWord(true);

		JScrollPane resultPane = new JScrollPane(result);
		resultPane.setPreferredSize(new Dimension(200, 150));
		resultPane.setBorder(BorderFactory.createTitledBorder("Execution Result"));

		frame.add(resultPane, BorderLayout.SOUTH);

		action.addActionListener(e -> {

			String a = (String) action.getSelectedItem();
			selectType.setVisible(a.equals("dropdown"));
			frame.revalidate();

		});

		add.addActionListener(e -> {

			Map<String, String> step = new HashMap<>();

			step.put("action", (String) action.getSelectedItem());
			step.put("xpath", xpath.getText());
			step.put("value", value.getText());
			step.put("wait", wait.getText());
			step.put("name", stepName.getText());
			step.put("selectType", (String) selectType.getSelectedItem());

			steps.add(step);

			model.addRow(new Object[] { stepName.getText(), action.getSelectedItem(), xpath.getText(), value.getText(),
					wait.getText() });

			xpath.setText("");
			value.setText("");
			stepName.setText("");

		});

		JPopupMenu menu = new JPopupMenu();
		JMenuItem delete = new JMenuItem("Delete");
		JMenuItem edit = new JMenuItem("Edit");

		menu.add(edit);
		menu.add(delete);

		table.setComponentPopupMenu(menu);

		delete.addActionListener(e -> {

			int r = table.getSelectedRow();
			if (r >= 0) {

				steps.remove(r);
				model.removeRow(r);

			}

		});

		edit.addActionListener(e -> {

			int r = table.getSelectedRow();

			Map<String, String> st = steps.get(r);

			action.setSelectedItem(st.get("action"));
			xpath.setText(st.get("xpath"));
			value.setText(st.get("value"));
			wait.setText(st.get("wait"));
			stepName.setText(st.get("name"));

		});

		JButton run = new JButton("Run Automation");
		frame.add(run, BorderLayout.EAST);

		run.addActionListener(e -> {

			new Thread(() -> {

				WebDriver driver = null;

				try {

					String b = (String) browser.getSelectedItem();

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

					if (max.isSelected())
						driver.manage().window().maximize();

					driver.get(url.getText());

					Actions act = new Actions(driver);

					for (int i = 0; i < steps.size(); i++) {

						Map<String, String> st = steps.get(i);

						try {

							Thread.sleep(Integer.parseInt(st.get("wait")) * 1000);

							WebElement el = driver.findElement(By.xpath(st.get("xpath")));

							switch (st.get("action")) {

							case "textbox":
								el.sendKeys(st.get("value"));
								break;
							case "click":
								el.click();
								break;

							case "dropdown":

								Select sel = new Select(el);

								switch (st.get("selectType")) {

								case "selectByVisibleText":
									sel.selectByVisibleText(st.get("value"));
									break;

								case "selectByValue":
									sel.selectByValue(st.get("value"));
									break;

								case "selectByIndex":
									sel.selectByIndex(Integer.parseInt(st.get("value")));
									break;

								}

								break;

							case "mouseHover":
								act.moveToElement(el).perform();
								break;
							case "doubleClick":
								act.doubleClick(el).perform();
								break;
							case "rightClick":
								act.contextClick(el).perform();
								break;

							case "getText":
								result.append("TEXT: " + el.getText() + "\n");
								break;

							case "getAttribute":
								result.append("ATTR: " + el.getAttribute(st.get("value")) + "\n");
								break;

							}

							table.setRowSelectionInterval(i, i);
							table.setSelectionBackground(Color.GREEN);

							result.append("STEP PASS: " + st.get("name") + "\n");

						} catch (Exception ex) {

							table.setRowSelectionInterval(i, i);
							table.setSelectionBackground(Color.RED);

							result.append("STEP FAIL: " + st.get("name") + "\n");

						}

					}

				} catch (Exception er) {

					JTextArea area = new JTextArea(er.toString());
					area.setLineWrap(true);
					area.setWrapStyleWord(true);

					JScrollPane sp = new JScrollPane(area);
					sp.setPreferredSize(new Dimension(400, 200));

					JOptionPane.showMessageDialog(null, sp, "Error", JOptionPane.ERROR_MESSAGE);

				}

			}).start();

		});

		frame.setVisible(true);

	}

}