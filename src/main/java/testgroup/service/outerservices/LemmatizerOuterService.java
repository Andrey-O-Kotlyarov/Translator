package testgroup.service.outerservices; 

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate; 
import com.fasterxml.jackson.databind.ObjectMapper; 
import testgroup.model.dto.LemmatizerResponse;
import testgroup.model.dto.Token; 
import java.util.List;
import java.util.Map; 

@Service 
public class LemmatizerOuterService { 

    private final RestTemplate restTemplate = new RestTemplate(); // Создаем один раз 
    private final String API_URL = 
        "http://127.0.0.1:8000/lemmatizer_app/process/"; // Адрес Django-сервиса 
    private final String API_URL_LEMMA = 
        "http://127.0.0.1:8000/lemmatizer_app/get_lemma/"; // другой метод Django-сервиса 
    private final String textExample = 
        "Слышал про такого? Я чуть не подавился остатками гречки. Слышал ли я про такого? " + 
        "В девятнадцатом году я сидел с этим Далио в Давосе, в ресторации с дурацким " + 
        "названием, которое сейчас не вспомню. Он заказал стейк «вэл дан» — кусок мраморной " + 
        "подошвы за тысячу франков. Полтора часа я смотрел, как он пилит её тупым ножом, и " + 
        "кивал про радикальную прозрачность и табличку для собственных ошибок. И старался не " + 
        "ржать в открытую над его «генеальностью». — Слышал, — сказал я в трубку. — Краем уха.";

    // Создаем ObjectMapper один раз для всего сервиса 
    private final ObjectMapper objectMapper = new ObjectMapper(); 
    
    @SuppressWarnings("null")
    public LemmatizerResponse analyzeText(String text) { 
        try {
        // 1. Создаем объект с данными
        Map<String, String> requestBody = Map.of("text", text);

        // 2. ЯВНО превращаем Map в JSON-строку
        String jsonBody = objectMapper.writeValueAsString(requestBody);
        // Теперь jsonBody выглядит так: {"text":"Ваш текст"}

        // 3. Устанавливаем заголовок
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 4. Создаем HttpEntity, передавая JSON-строку
        HttpEntity<String> requestEntity = new HttpEntity<>(jsonBody, headers);

        // 5. Выполняем запрос
        ResponseEntity<String> response = restTemplate.exchange(
            API_URL,
            HttpMethod.POST,
            requestEntity,
            String.class
        );

        if (response.getStatusCode() != HttpStatus.OK) { 
            throw new RuntimeException("Сервер вернул не-200 код: " + response.getStatusCode()); 
        } 
        
        String responseBody = response.getBody(); 
        // Вместо того чтобы работать со строкой, превращаем её в объект
        return objectMapper.readValue(responseBody, LemmatizerResponse.class);

        } catch (HttpClientErrorException e) {
            System.err.println("Сервер вернул ошибку: " + e.getResponseBodyAsString());
            throw new RuntimeException("Ошибка связи с NLP-сервисом: " + e.getMessage(), e);
        } catch (Exception e) { 
            // Ловим Jackson и другие возможные ошибки
            System.err.println("Ошибка при подготовке запроса: " + e.getMessage());
            throw new RuntimeException("Внутренняя ошибка клиента", e);
        } 
    } 

    public String getLemma(String word) {
        try {
             // Для GET-запросов с параметрами удобнее использовать getForEntity
            // Мы передаем параметр 'word' прямо в URL
            ResponseEntity<String> response = restTemplate.getForEntity(
                API_URL_LEMMA + "?word={word}", // URL с плейсхолдером {word}
                String.class,                   // Ожидаемый тип ответа
                word                            // Значение, которое подставится вместо {word}
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody(); // Это и будет строка с леммой
            } else {
                throw new RuntimeException("Ошибка при вызове метода get_lemma: " 
                    + response.getStatusCode());
            }
        } catch (HttpClientErrorException e) {
            System.err.println("Ошибка от сервера при лемматизации: " 
                + e.getResponseBodyAsString());
            throw new RuntimeException("Ошибка связи при вызове get_lemma", e);
        }
    }

    public String[] lemmatizeText(String text) { 
        LemmatizerResponse nlpResponse = analyzeText(text); 
        List<Token> tokens = nlpResponse.getTokens(); 
        String[] lemmasArray = new String[tokens.size()]; 

        for (int i = 0; i < tokens.size(); i++) {
            lemmasArray[i] = tokens.get(i).getLemma();
        } 

        return lemmasArray; 
    } 

    public String[] lemmatizeTextExample() { 
        LemmatizerResponse nlpResponse = analyzeText(textExample); 
        List<Token> tokens = nlpResponse.getTokens(); 
        String[] lemmasArray = new String[tokens.size()]; 

        for (int i = 0; i < tokens.size(); i++) {
            lemmasArray[i] = tokens.get(i).getLemma();
        } 

        return lemmasArray; 
    } 

} 
