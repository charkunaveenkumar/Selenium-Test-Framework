package com.orangehrm.test;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.orangehrm.base.BaseClass;
import com.orangehrm.pages.HomePage;
import com.orangehrm.pages.LoginPage;
import com.orangehrm.utilities.DataProviders;
import com.orangehrm.utilities.ExtentManager;

public class HomePageTest extends BaseClass{
	private LoginPage loginPage;
	private HomePage homePage;
	
	@BeforeMethod
    public void setupPages() {
		loginPage=new LoginPage(getDriver());
		homePage=new HomePage(getDriver());
	}
	@Test(dataProvider="validLoginData",dataProviderClass = DataProviders.class)
	public void VerifyOrangeHRMlogo(String username,String password) {
		// Test checkin
		//ExtentManager.startTest("Home Page verify Logo Test");  //This has been implemented in TestListener
		ExtentManager.logStep("Navigating to Login Page entering Username and Password");
		loginPage.login(username, password);
		ExtentManager.logStep("verifying logo is visible or not ");
		Assert.assertTrue(homePage.verifyOrangeHRMlogo(),"logo is not visible");
		ExtentManager.logStep("Validation Successfull");
		ExtentManager.logStep("Logged out Successfully!");
	}
}
