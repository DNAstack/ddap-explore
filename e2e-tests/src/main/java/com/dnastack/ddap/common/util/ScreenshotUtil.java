package com.dnastack.ddap.common.util;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

public class ScreenshotUtil {

    protected static final String SCREENSHOT_DIR = EnvUtil.optionalEnv("E2E_SCREENSHOT_DIR", "target");

    public static void capture(String filename, WebDriver driver) {
        try {
            File destiny = new File(SCREENSHOT_DIR, filename);
            FileUtils.copyFile(((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE), destiny);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

}