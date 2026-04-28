package testgroup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication; 
import testgroup.service.FileCleaner;
import testgroup.service.YandexIAMTokenGen;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);   
        System.out.println("Hello world from main!");  
        
        // очистка временных скринов
        String folderPath = 
        "C:\\Users\\admin\\Desktop\\translator\\src\\main\\resources\\static\\screens";
        int periodInMinutes = 30; 
        FileCleaner.startFileCleaning(folderPath, periodInMinutes); 

        try {
            YandexIAMTokenGen.gener(); 
        } catch (Exception e) {
            e.printStackTrace();
        } 

    } 
} 