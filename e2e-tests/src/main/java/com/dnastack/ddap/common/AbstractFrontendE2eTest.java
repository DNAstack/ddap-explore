package com.dnastack.ddap.common;

import com.dnastack.ddap.common.page.AnyDdapPage;
import com.dnastack.ddap.common.page.ICLoginPage;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;

@Slf4j
public abstract class AbstractFrontendE2eTest extends AbstractBaseE2eTest {

    protected static final boolean HEADLESS = Boolean.parseBoolean(optionalEnv("HEADLESS", "true"));
    protected static final Pattern URL_PARSE_PATTERN = Pattern.compile("^(https?)://(.*)$");

    protected WebDriver driver;
    protected AnyDdapPage ddapPage;

    @Rule
    public TestName name = new TestName();
    @Rule
    public RetryRule retry = new RetryRule(Integer.parseInt(optionalEnv("E2E_TEST_RETRIES", "3")));

    @BeforeClass
    public static void driverSetup() {
        WebDriverManager.chromedriver().setup();
    }

    private ChromeDriver getChromeDriver() {
        ChromeOptions options = new ChromeOptions();
        if (HEADLESS) {
            options.addArguments("headless");
        }
        options.addArguments("--disable-gpu");
        options.addArguments("window-size=1200x600");
        options.addArguments("incognito");

        return new ChromeDriver(options);
    }

    @After
    public void afterEach() {
        String testName = this.getClass().getSimpleName() + "." + name.getMethodName() + ".png";
        Screenshot.capture(testName, driver);

        if (driver != null) {
            driver.manage().deleteAllCookies();
            driver.quit();
            driver = null;
        }
    }

    @Before
    public void beforeEach() {
        driver = getChromeDriver();
        driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);

        ICLoginPage icLoginPage = startLogin(getRealm());
        ddapPage = login(icLoginPage);
    }

    protected abstract AnyDdapPage login(ICLoginPage icLoginPage);

    protected abstract String getRealm();

    protected ICLoginPage startLogin(String realm) {
        driver.get(getUrlWithBasicCredentials(URI.create(DDAP_BASE_URL).resolve(format("/api/v1alpha/%s/identity/login", realm)).toString()));
        return new ICLoginPage(driver);
    }

    private static String getUrlWithBasicCredentials(String original) {
        final Matcher matcher = URL_PARSE_PATTERN.matcher(original);
        if (matcher.find()) {
            return format("%s://%s:%s@%s", matcher.group(1), DDAP_USERNAME, DDAP_PASSWORD, matcher.group(2));
        } else {
            throw new IllegalArgumentException("Could not parse url: " + original);
        }
    }

}
