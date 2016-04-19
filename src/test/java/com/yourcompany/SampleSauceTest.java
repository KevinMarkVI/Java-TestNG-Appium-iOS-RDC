package com.yourcompany;

import com.saucelabs.common.SauceOnDemandAuthentication;
import com.saucelabs.common.SauceOnDemandSessionIdProvider;
import com.saucelabs.testng.SauceOnDemandAuthenticationProvider;
import com.saucelabs.testng.SauceOnDemandTestListener;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import java.lang.reflect.Method;

import io.appium.java_client.MobileElement;
import io.appium.java_client.ios.IOSDriver;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Simple TestNG test which demonstrates being instantiated via a DataProvider in order to supply multiple browser combinations.
 *
 * @author Neil Manvar
 */

@Listeners({SauceOnDemandTestListener.class})
public class SampleSauceTest implements SauceOnDemandSessionIdProvider, SauceOnDemandAuthenticationProvider {

    /**
     * Constructs a {@link com.saucelabs.common.SauceOnDemandAuthentication} instance using the supplied user name/access key.  To use the authentication
     * supplied by environment variables or from an external file, use the no-arg {@link com.saucelabs.common.SauceOnDemandAuthentication} constructor.
     */
    public SauceOnDemandAuthentication authentication = new SauceOnDemandAuthentication(System.getenv("SAUCE_USERNAME"), System.getenv("SAUCE_ACCESS_KEY"));

    /**
     * ThreadLocal variable which contains the  {@link WebDriver} instance which is used to perform browser interactions with.
     */
    private ThreadLocal<IOSDriver<WebElement>> webDriver = new ThreadLocal<IOSDriver<WebElement>>();

    /**
     * ThreadLocal variable which contains the Sauce Job Id.
     */
    private ThreadLocal<String> sessionId = new ThreadLocal<String>();

    /**
     * DataProvider that explicitly sets the browser combinations to be used.
     *
     * @param testMethod
     * @return
     */
    
    //curl command
    //curl -u KevinMarkVI:8a4b30c6-bedc-4fb6-821f-773a28670e4c -X POST -H "Content-Type: application/octet-stream" https://saucelabs.com/rest/v1/storage/KevinMarkVI/TestApp7.0.app.zip?overwrite=true --data-binary @TestApp7.0.app.zip
    
    @DataProvider(name = "hardCodedBrowsers", parallel = true)
    public static Object[][] sauceBrowserDataProvider(Method testMethod) {
        return new Object[][]{
            new Object[]{"iOS", "iPhone 6", "9.2", "https://s3.amazonaws.com/appium/TestApp8.4.app.zip", "", "portrait", "1.5.1"},
            new Object[]{"iOS", "iPhone 6 Plus", "9.2", "https://s3.amazonaws.com/appium/TestApp8.4.app.zip", "", "portrait", "1.5.1"},
            new Object[]{"iOS", "iPhone 5s", "8.4", "https://s3.amazonaws.com/appium/TestApp8.4.app.zip", "", "portrait", "1.5.1"},
            new Object[]{"iOS", "iPhone 5", "8.4", "https://s3.amazonaws.com/appium/TestApp8.4.app.zip", "", "portrait", "1.5.1"},
            new Object[]{"iOS", "iPad Retina", "9.2", "https://s3.amazonaws.com/appium/TestApp8.4.app.zip", "", "portrait", "1.5.1"},
            //Real Device
            new Object[]{"iOS", "iPhone 6 Device", "8.4", "sauce-storage:TestApp-iphoneos.zip", "", "portrait", "1.4.11"}
        };
    }

    /**
     * /**
     * Constructs a new {@link RemoteWebDriver} instance which is configured to use the capabilities defined by the platformName,
     * deviceName, platformVersion, and app and which is configured to run against ondemand.saucelabs.com, using
     * the username and access key populated by the {@link #authentication} instance.
     *
     * @param platformName Represents the platform to be run.
     * @param deviceName Represents the device to be tested on
     * @param platform Version Represents version of the platform.
     * @param app Represents the location of the app under test.
     * @return
     * @throws MalformedURLException if an error occurs parsing the url
     */
    private IOSDriver<WebElement> createDriver(String platformName, String deviceName, String platformVersion, String app, String browserName, String deviceOrientation, String appiumVersion, String methodName) throws MalformedURLException {

    	DesiredCapabilities capabilities = new DesiredCapabilities();

        capabilities.setCapability("platformName", platformName);
        capabilities.setCapability("deviceName", deviceName);
        capabilities.setCapability("platformVersion", platformVersion);
        capabilities.setCapability("app", app);
        capabilities.setCapability("browserName", browserName);
        capabilities.setCapability("deviceOrientation", deviceOrientation);
        capabilities.setCapability("appiumVersion", appiumVersion);

        capabilities.setCapability("name", "Appium-iOS-" + methodName);

        webDriver.set(new IOSDriver<WebElement>(
                new URL("http://" + authentication.getUsername() + ":" + authentication.getAccessKey() + "@ondemand.saucelabs.com:80/wd/hub"),
                capabilities));
        String id = ((RemoteWebDriver) getWebDriver()).getSessionId().toString();
        sessionId.set(id);
        System.out.println("SauceOnDemandSessionID=" + id + " job-name=" + methodName);
        return webDriver.get();
    }

    /**
     * Runs a simple test verifying we were able to launch and close the browser
     *
     * @param platformName Represents the platform to be run.
     * @param deviceName Represents the device to be tested on
     * @param platform Version Represents version of the platform.
     * @param app Represents the location of the app under test.
     * @throws app if an error occurs during the running of the test
     */
    @Test(dataProvider = "hardCodedBrowsers")
    public void launchTest(String platformName, String deviceName, String platformVersion, String app, String browserName, String deviceOrientation, String appiumVersion, Method method) throws Exception {
    	IOSDriver<WebElement> driver = createDriver(platformName, deviceName, platformVersion, app, browserName, deviceOrientation, appiumVersion, method.getName());
       
    	MobileElement fieldOne = (MobileElement) driver.findElementByAccessibilityId("TextField1");
        fieldOne.sendKeys("12");
        
        MobileElement fieldTwo = (MobileElement) driver.findElementsByClassName("UIATextField").get(1);
        fieldTwo.sendKeys("8");

        // trigger computation by using the button
        driver.findElementByAccessibilityId("ComputeSumButton").click();

        // is sum equal?
        String sum = driver.findElementsByClassName("UIAStaticText").get(0).getText();
        assertEquals(Integer.parseInt(sum), 20);
        
        driver.quit();
    }

    @Test(dataProvider = "hardCodedBrowsers")
    public void addSumTest(String platformName, String deviceName, String platformVersion, String app, String browserName, String deviceOrientation, String appiumVersion, Method method) throws Exception {
    	IOSDriver<WebElement> driver = createDriver(platformName, deviceName, platformVersion, app, browserName, deviceOrientation, appiumVersion, method.getName());
    	    
        MobileElement fieldOne = (MobileElement) driver.findElementByAccessibilityId("TextField1");
        fieldOne.sendKeys("12");

        MobileElement fieldTwo = (MobileElement) driver.findElementsByClassName("UIATextField").get(1);
        fieldTwo.sendKeys("8");

        // trigger computation by using the button
        driver.findElementByAccessibilityId("ComputeSumButton").click();

        // is sum equal?
        String sum = driver.findElementsByClassName("UIAStaticText").get(0).getText();
        assertEquals(Integer.parseInt(sum), 20);
    	
        driver.quit();
    }

    @Test(dataProvider = "hardCodedBrowsers")
    public void addSecondSumTest(String platformName, String deviceName, String platformVersion, String app, String browserName, String deviceOrientation, String appiumVersion, Method method) throws Exception {
        IOSDriver<WebElement> driver = createDriver(platformName, deviceName, platformVersion, app, browserName, deviceOrientation, appiumVersion, method.getName());
            
        MobileElement fieldOne = (MobileElement) driver.findElementByAccessibilityId("TextField1");
        fieldOne.sendKeys("12");

        MobileElement fieldTwo = (MobileElement) driver.findElementsByClassName("UIATextField").get(1);
        fieldTwo.sendKeys("8");

        // trigger computation by using the button
        driver.findElementByAccessibilityId("ComputeSumButton").click();

        // is sum equal?
        String sum = driver.findElementsByClassName("UIAStaticText").get(0).getText();
        assertEquals(Integer.parseInt(sum), 20);
        
        driver.quit();
    }

    /**
     * @return the {@link WebDriver} for the current thread
     */
    public IOSDriver<WebElement> getWebDriver() {
        System.out.println("WebDriver" + webDriver.get());
        return webDriver.get();
    }

    /**
     *
     * @return the Sauce Job id for the current thread
     */
    public String getSessionId() {
        return sessionId.get();
    }

    /*
     * @return the {@link SauceOnDemandAuthentication} instance containing the Sauce username/access key
     */
    @Override
    public SauceOnDemandAuthentication getAuthentication() {
        return authentication;
    }
}

