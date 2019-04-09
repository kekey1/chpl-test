package gov.healthit.chpl.aqa.stepDefinitions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.shared.utils.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.support.events.AbstractWebDriverEventListener;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.support.events.WebDriverEventListener;

import cucumber.api.java.After;
import cucumber.api.java.Before;

/**
 * Class Hooks definition.
 */
public class Hooks {

    private static File dir;
    private static EventFiringWebDriver driver;
    private static final int DELAY = 30;
    private static String screenshotPath;
    private static String downloadPath = System.getProperty("downloadPath");

    /**
     * Launch ChromeDriver.
     */
    @Before
    public void openBrowser() {
        /* To run chrome with the developer tools window automatically opened re-able these lines.
         *
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--auto-open-devtools-for-tabs");
        driver = new ChromeDriver(chromeOptions);
        driver.manage().window().maximize(); // does not work on CI machine, sometimes useful locally
         */
        if (StringUtils.isEmpty(downloadPath)) {
            String tempDirectory;
            try {
                tempDirectory = Files.createTempDirectory("download-files").toString();
                // Print the path to the newly created directory
            } catch (final IOException e) {
                // If temp directory creation failed, create new directory in target folder
                // user.dir - User working directory, make new directories in user's working directory
                File file = new File("target", "download-files");
                file.mkdirs();
                tempDirectory = System.getProperty("user.dir") + File.separator + "target" + File.separator + "download-files";
            }
            downloadPath = tempDirectory;
        }
        dir = new File(downloadPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        HashMap<String, Object> chromePrefs = new HashMap<String, Object>();
        chromePrefs.put("profile.default_content_settings.popups", 0);
        chromePrefs.put("download.default_directory", downloadPath);
        /**
         * Save Chrome Options
         */
        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("prefs", chromePrefs);
        chromePrefs.put("safebrowsing.enabled", "true");
        options.addArguments("--safebrowsing-disable-download-protection");
        options.addArguments("disable-popup-blocking");
        options.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);

        screenshotPath = System.getProperty("user.dir") + File.separator + "test-output";
        driver = new EventFiringWebDriver(new ChromeDriver(options));
        driver.manage().timeouts().implicitlyWait(DELAY, TimeUnit.SECONDS);
        WebDriverEventListener errorListener = new AbstractWebDriverEventListener() {
            @Override
            public void onException(final Throwable throwable, final WebDriver activeDriver) {
                try {
                    takeScreenshot();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        driver.register(errorListener);
    }

    /**
     * Close browser windows and terminate WebDriver session.
     */
    @After
    public void afterMethod() {
        driver.quit();
    }

    public static EventFiringWebDriver getDriver() {
        return driver;
    }

    public static File getDownloadDirectory() {
        return dir;
    }

    /**
     * Take a screenshot.
     * @param hash a string that will be inserted into the filename to avoid overwriting images.
     * All "non-word characters" will be replaced with "_"
     * @throws Exception if there is an exception
     */
    public static void takeScreenshot(final String hash) throws Exception {
        File scrFile = ((TakesScreenshot) getDriver()).getScreenshotAs(OutputType.FILE);
        File outFile = new File(screenshotPath + File.separator + "failed-test-" + hash.replaceAll("\\W+", "_") + ".png");
        FileUtils.copyFile(scrFile, outFile);
    }

    /**
     * Take a screenshot. Generates hash based on current time.
     * @throws Exception if screenshot cannot be created
     */
    public static void takeScreenshot() throws Exception {
        String hash = String.valueOf(new Date().getTime());
        takeScreenshot(hash);
    }
}
