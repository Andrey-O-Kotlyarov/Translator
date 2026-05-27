package testgroup; 

import java.util.Arrays;
import java.util.List; 
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication; 
import testgroup.applogic.filemanager.FileCleaner;
import testgroup.applogic.restclient.NlpService; 
import testgroup.applogic.translator.YandexIAMTokenGen;
import testgroup.datawork.dto.NlpResponse;
import testgroup.datawork.dto.Token;

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
        
        // проверка получения IAM токена и работы АПИ яндекс переводчика
        try {
            String result = 
            YandexIAMTokenGen.gener("После этого убедитесь, что в коде у вас стоит заголовок Api-Key, как я писал в предыдущем ответе.", "en"); 
            System.out.println(result); 
        } catch (Exception e) {
            e.printStackTrace();
        }          

        NlpService nlp = new NlpService(); 
        NlpResponse nlpResponse = nlp.analyzeText("Слышал про такого? Я чуть не подавился остатками гречки. Слышал ли я про такого? В девятнадцатом году я сидел с этим Далио в Давосе, в ресторации с дурацким названием, которое сейчас не вспомню. Он заказал стейк «вэл даун» — кусок мраморной подошвы за тысячу франков. Полтора часа я смотрел, как он пилит её тупым ножом, и кивал про радикальную прозрачность и табличку для собственных ошибок. И старался не ржать в открытую над его «генеальностью». — Слышал, — сказал я в трубку. — Краем уха.\r\n" ); 
        List<Token> tokens = nlpResponse.getTokens(); 
        String[] lemmasArray = new String[tokens.size()]; 

        for (int i = 0; i < tokens.size(); i++) {
            lemmasArray[i] = tokens.get(i).getLemma();
        } 
        
        System.out.println("Массив лемм:");
        System.out.println(Arrays.toString(lemmasArray));
    }  
} 