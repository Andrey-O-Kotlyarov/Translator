package testgroup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication; 
import testgroup.applogic.filemanager.FileCleaner;
import testgroup.applogic.translator.YandexIAMTokenGen;

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

        /* 
        try {
            YandexIAMTokenGen.gener("После этого убедитесь, что в коде у вас стоит заголовок Api-Key, как я писал в предыдущем ответе.", "en"); 
        } catch (Exception e) {
            e.printStackTrace();
        } 
        */

    }  
} 