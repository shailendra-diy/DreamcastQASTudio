package seleniumcommand;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.Assert;
import org.testng.asserts.SoftAssert;

import io.github.bonigarcia.wdm.WebDriverManager;

public class XpathLogic {
	
	
	
	
	public static void main(String[] args) {
		WebDriverManager.chromedriver().setup();
	WebDriver	driver =new ChromeDriver();
		driver.manage().window().maximize();
		driver.get("https://event1.godreamcast.com/diy/login");
		
		SoftAssert softAssert = new SoftAssert();
		
		System.out.println(driver.getCurrentUrl());
		
		String expectedtTitle="Login Page 4";
		String actualTitle=driver.getTitle();
		
		
		System.out.println("Actual title"+ actualTitle );
		

        softAssert.assertEquals(actualTitle, expectedtTitle, "Page title is incorrect! Test Failed.");
		
		//Assert.assertEquals(expectedtTitle, actualTitle,"Title not match");
		
		
	
		
		
		
	}

}
