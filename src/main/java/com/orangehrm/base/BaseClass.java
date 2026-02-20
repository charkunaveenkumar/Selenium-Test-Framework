package com.orangehrm.base;

import java.io.FileInputStream;

import java.io.IOException;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.asserts.SoftAssert;

import com.orangehrm.actiondriver.ActionDriver;
import com.orangehrm.utilities.ExtentManager;

import io.github.bonigarcia.wdm.WebDriverManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BaseClass {

	protected static Properties prop;
	//protected static WebDriver driver;
	// changes made after 3.04 in selenium java frame work class
	//private static ActionDriver actionDriver;
	
	//changes made to create threadlocal class to run in parallel mode
	
	private static ThreadLocal<WebDriver> driver=new ThreadLocal<>();
	private static ThreadLocal<ActionDriver> actionDriver=new ThreadLocal<>();
	
	public static final Logger logger = LogManager.getLogger(BaseClass.class);
	
	protected ThreadLocal<SoftAssert> softAssert = ThreadLocal.withInitial(SoftAssert::new);
	
	//Getter method for soft assert
	public SoftAssert getSoftAssert() {
		return softAssert.get();
	}
	
	

	@BeforeSuite
	public void loadConfig() throws IOException {
		prop = new Properties();
		FileInputStream fis = new FileInputStream(System.getProperty("user.dir")+"/src/main/resources/config.properties");
		prop.load(fis);
	    logger.info("config.properties file loaded");
	    
	    //start the Extent report
	    //ExtentManager.getReporter();  //This has been implemented in TestListener
	}

	@BeforeMethod
	public synchronized void setup() throws IOException {
		System.out.println("Setting up the WebDriver for:" + this.getClass().getSimpleName());
		launchBrowser();
		configureBrowser();
		staticWait(2);
		logger.info("WebDriver Initialized and Browser Maximized");
		logger.trace("This is a Trace message");
		logger.error("This is a Error message");
		logger.debug("This is a debug message");
		logger.fatal("This is a fatal message");
		logger.warn("This is a warn message");
		

		/* // changes made after 3.04 in selenium java frame work class
		// Initialize the actionDriver object only once
		if (actionDriver == null) {
			actionDriver = new ActionDriver(driver);
			//System.out.println("ActionDriver instance is created");
			logger.info("ActionDriver instance is created. "+Thread.currentThread().getId());
		} */
		
		//Initialize ActionDriver for the current Thread
		actionDriver.set(new ActionDriver(getDriver()));
		getDriver().manage().window().setSize(new Dimension(1920, 1080));//problem solve for running in headless mode
		logger.info("ActionDriver Initialized for Thread: "+Thread.currentThread().getId());

	}

	// Initialize the WebDriver based on browser defined in config.properties
	private synchronized void launchBrowser() {

		String browser = prop.getProperty("browser");

		if (browser.equalsIgnoreCase("chrome")) {
			
  //Create chromeoptions
			WebDriverManager.chromedriver().setup();
			ChromeOptions options = new ChromeOptions();
			options.addArguments("--headless=new"); // Run chrome in headless mode
			options.addArguments("--disable-gpu"); //Run GPU for headless mode
			options.addArguments("--use-angle=swiftshader");
			options.addArguments("--window-size=1920,1080");//set window size
			options.addArguments("--disable-save-password-bubble");
			options.addArguments("--disable-notifications");//Disable browser notifications
			options.addArguments("--no-sandbox");//Needed for CI/CD environments
			options.addArguments("--disable-dev-shm-usage");//Resolve issues in resources 
			options.addArguments("--remote-allow-o-rigins=*");
			
			//driver = new ChromeDriver(options);
			driver.set(new ChromeDriver(options)); //New changes as per Thread
	
			ExtentManager.registerDriver(getDriver());
			logger.info("ChromeDriver Instance is created");
		} else if (browser.equalsIgnoreCase("firefox")) {
			
			//create  FireFoxOptions
			FirefoxOptions options = new FirefoxOptions();
			options.addArguments("--headless"); // Run Firefox in headless mode
			options.addArguments("--disable-gpu"); //Run GPU for headless mode
			options.addArguments("--width=1920");//set browser width
			options.addArguments("--height=1080");//set browser height
			options.addArguments("--disable-save-password-bubble");
			options.addArguments("--disable-notifications");//Disable browser notifications
			options.addArguments("--no-sandbox");//Needed for CI/CD environments
			options.addArguments("--disable-dev-shm-usage");//Resolve issues in resources
			
			//driver = new FirefoxDriver();
			driver.set(new FirefoxDriver(options)); //New changes as per Thread
			ExtentManager.registerDriver(getDriver());
			logger.info("FirefoxDriver Instance is created");
		} else if (browser.equalsIgnoreCase("edge")) {
			//crete EdgeOptions
			EdgeOptions options = new EdgeOptions();
			options.addArguments("--headless"); // Run Firefox in headless mode
			options.addArguments("--disable-gpu"); //Run GPU for headless mode
			options.addArguments("--width=1920");//set browser width
			options.addArguments("--height=1080");//set browser height
			options.addArguments("--disable-save-password-bubble");
			options.addArguments("--disable-notifications");//Disable browser notifications
			options.addArguments("--no-sandbox");//Needed for CI/CD environments
			options.addArguments("--disable-dev-shm-usage");//Resolve issues in resources
			
			//driver = new EdgeDriver();
			driver.set( new EdgeDriver(options));  //New changes as per Thread
			ExtentManager.registerDriver(getDriver());
			logger.info("EdgeDriver Instance is created");
		} else {
			throw new IllegalArgumentException("Browser Not Supported:" + browser);
		}
	}

	// configure browser settings such as implicit wait, maximize browser and
	// navigate to URL
	private  void configureBrowser() {
		// Implicit Wait
		int implicitWait = Integer.parseInt(prop.getProperty("implicitWait"));
		getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));

		// maximize the browser
		getDriver().manage().window().maximize();

		// Navigate to URL
		try {
			getDriver().get(prop.getProperty("URL"));
		} catch (Exception e) {
			System.out.println("Failed to navigate to the URL" + e.getMessage());
		}
	}

	@AfterMethod
	public  synchronized void teardown() {
		if (getDriver() != null) {
			try {
				getDriver().quit();
			} catch (Exception e) {
				System.out.println("unable to quit the driver" + e.getMessage());
			}
		}
		//System.out.println("WebDriver instance is closed");
		logger.info("WebDriver instance is closed");
		driver.remove();
		actionDriver.remove();
		//driver = null;
		//actionDriver = null;
		
		//ExtentManager.endTest();  //This has been implemented in TestListener
	}


	 /* // Driver getting method 
	  public WebDriver getDriver() { 
	 return driver; }  */

	// Getter Method for WebDriver
	public static WebDriver getDriver() {
		if(driver.get()==null) {
			System.out.println("WebDriver is not initialized");
			throw new IllegalStateException("WebDriver is not initialized");
		}
		return driver.get();
	}
	
	// Getter Method for ActionDriver
		public static ActionDriver getActionDriver() {
			if(actionDriver.get()==null) {
				System.out.println("ActionDriver is not initialized");
				throw new IllegalStateException("ActionDriver is not initialized");
			}
			return actionDriver.get();
		}
		
		 //Getter method for prop
		 
		 public static Properties getProp() {
		  return prop;
		  }

	// Driver setter method
	public void setDriver(ThreadLocal<WebDriver> driver) {
		BaseClass.driver = driver;
	}


	// static wait for pause
	public void staticWait(int seconds) {
		LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(seconds));
	}

}
