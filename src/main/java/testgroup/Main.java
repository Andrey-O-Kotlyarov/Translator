package testgroup; 

import java.util.Arrays; 
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication; 
import testgroup.service.filemanager.FileCleaner;
import testgroup.service.lemmatizer.LemmatizerOuterService;
import testgroup.service.translator.YandexTranslator; 

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);   
        System.out.println("Hello world from main!");  
        
        // очистка временных скринов
        String folderPath = 
            "C:\\Users\\weiss\\OneDrive\\Desktop\\translator\\src\\main\\resources\\static\\screens";
        int periodInMinutes = 30; 
        FileCleaner.startFileCleaning(folderPath, periodInMinutes);    
        
        // проверка получения IAM токена и работы АПИ яндекс переводчика
        String textExample = 
            "После этого убедитесь, что в коде у вас стоит заголовок Api-Key, "
            + "как я писал в предыдущем ответе."; 
        try { 
            String iamToken = YandexTranslator.generIAMToken();
            String result = YandexTranslator.translate(
                textExample, 
                "en", 
                iamToken); 
            System.out.println(result); 
        } catch (Exception e) {
            e.printStackTrace();
        }          

        // проверка лемматизатора
        LemmatizerOuterService lemmatizer = new LemmatizerOuterService(); 
        String[] lemmas = lemmatizer.lemmatizeTextExample(); 
        System.out.println("Массив лемм: \n" + Arrays.toString(lemmas)); 
        String lemmatizingWord = "каких-то"; 
        String lemma = lemmatizer.getLemma(lemmatizingWord); 
        System.out.println(lemma);
    }  

} 