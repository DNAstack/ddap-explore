package com.dnastack.ddap.frontend;

import static java.lang.String.format;

import com.dnastack.ddap.common.AbstractBaseE2eTest;
import com.dnastack.ddap.common.TestingPersona;
import com.dnastack.ddap.common.page.AnyDdapPage;
import com.dnastack.ddap.common.util.EnvUtil;
import com.dnastack.ddap.common.util.RetryRule;
import com.dnastack.ddap.common.util.ScreenshotUtil;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntries;

@Slf4j
public abstract class AbstractFrontendE2eTest extends AbstractBaseE2eTest {

    protected static final boolean HEADLESS = Boolean.parseBoolean(EnvUtil.optionalEnv("HEADLESS", "true"));
    protected static final Pattern URL_PARSE_PATTERN = Pattern.compile("^(https?)://(.*)$");
    protected static WebDriver driver;
    protected static AnyDdapPage ddapPage;

    @Rule
    public TestName name = new TestName();
    @Rule
    public RetryRule retry = new RetryRule(Integer.parseInt(EnvUtil.optionalEnv("E2E_TEST_RETRIES", "3")));

    @BeforeClass
    public static void driverSetup() {
        WebDriverManager.chromedriver().setup();
        driver = getChromeDriver();
        driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);

    }

    @BeforeClass
    public static void doOneTimeSetup() {
        log.info("Performing one time setup");
        configStrategy.doOnetimeSetup();
    }

    private static ChromeDriver getChromeDriver() {
        ChromeOptions options = new ChromeOptions();
        if (HEADLESS) {
            options.addArguments("headless");
        }
        options.addArguments("--disable-gpu");
        options.addArguments("window-size=1200x600");
        options.addArguments("incognito");

        return new ChromeDriver(options);
    }

    @AfterClass
    public static void cleanUp() {
        if (driver == null) {
            return;
        }
        driver.manage().deleteAllCookies();
        driver.quit();
        driver = null;
    }

    protected static <T extends AnyDdapPage> T doBrowserLogin(String realm, TestingPersona persona, Function<WebDriver, T> pageFactory) {
        try {
            return loginStrategy.performPersonaLogin(driver, persona, realm, pageFactory);
        } catch (IOException e) {
            throw new AssertionError("Unable to login", e);
        }
    }

    @After
    public void afterEach() {
        String testName = this.getClass().getSimpleName() + "." + name.getMethodName() + ".png";
        ScreenshotUtil.capture(testName, driver);
        writeBrowserConsoleLog();
    }

    private void writeBrowserConsoleLog() {
        LogEntries logs = driver.manage().logs().get("browser");
        logs.getAll().forEach((logEntry) -> {
            LocalTime loggedAt = Instant.ofEpochMilli(logEntry.getTimestamp()).atZone(ZoneId.systemDefault())
                .toLocalTime();
            log.info("Browser's console log: {} {} {}", loggedAt, logEntry.getLevel(), logEntry.getMessage());
        });
    }

    protected static String getUrlWithBasicCredentials(String original) {
        final Matcher matcher = URL_PARSE_PATTERN.matcher(original);
        if (DDAP_USERNAME == null && DDAP_PASSWORD == null) {
            return original;
        }
        if (matcher.find()) {
            return format("%s://%s:%s@%s", matcher.group(1), DDAP_USERNAME, DDAP_PASSWORD, matcher.group(2));
        } else {
            throw new IllegalArgumentException("Could not parse url: " + original);
        }
    }

}
