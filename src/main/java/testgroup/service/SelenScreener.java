package testgroup.service;

import java.nio.file.Files;
import java.nio.file.Path;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;

@Service
public class SelenScreener {     
    private static String chromeDriverExe = 
        "C:\\Users\\admin\\Downloads\\chromedriver-win64\\chromedriver-win64\\chromedriver.exe"; 
    private static String smallSizeOfScreen = "1280,10000"; 
    private static String bigSizeOfScreen = "1280,14500";

    //метод для сканирования сайта в файл .png
    public static void screenPage(String sourceURL, String destPictureFile, boolean bigSize) {         
        // устанавливаем размер скана
        String sizeOfScreen = smallSizeOfScreen; 
        if (bigSize) { 
            sizeOfScreen = bigSizeOfScreen; 
        } 

        // Set the path for the ChromeDriver
        System.setProperty("webdriver.chrome.driver", chromeDriverExe); 

        // Настройки Headless Chrome
        ChromeOptions options = new ChromeOptions(); 
        options.addArguments("--headless=new"); 
        options.addArguments("--disable-gpu"); 
        options.addArguments("--log-level=3"); 
        options.addArguments("--window-size=" + sizeOfScreen);
        options.addArguments("--blink-settings=imagesEnabled=false"); 

        // Initialize WebDriver
        WebDriver driver = new ChromeDriver(options);

        try {
            // Navigate to the target web page
            driver.get(sourceURL);

            // Делаем скриншот
            TakesScreenshot screenshotTaker = (TakesScreenshot) driver;
            byte[] screenshotBytes = screenshotTaker.getScreenshotAs(OutputType.BYTES);

            // Сохраняем скриншот в файл
            Files.write(Path.of(destPictureFile), screenshotBytes);             
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Turnoff the browser
            driver.quit();
        }
    }      
} 