package testgroup.applogic.restclient; 

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate; 
import com.fasterxml.jackson.databind.ObjectMapper; 
import testgroup.datawork.dto.NlpResponse; 
import java.util.Map; 

@Service 
public class NlpService { 

    private final RestTemplate restTemplate = new RestTemplate(); // Создаем один раз 
    private final String API_URL = 
        "http://127.0.0.1:8000/lemmatizer_app/process/"; // Адрес Django-сервиса 

    // Создаем ObjectMapper один раз для всего сервиса 
    private final ObjectMapper objectMapper = new ObjectMapper(); 
    
    @SuppressWarnings("null")
    public NlpResponse analyzeText(String text) { 
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

        if (response.getStatusCode() == HttpStatus.OK) {
            String responseBody = response.getBody();
            
            // Вместо того чтобы работать со строкой, превращаем её в объект
            return objectMapper.readValue(responseBody, NlpResponse.class);
        } else {
            throw new RuntimeException("Сервер вернул не-200 код: " + response.getStatusCode());
        }

        } catch (HttpClientErrorException e) {
            System.err.println("Сервер вернул ошибку: " + e.getResponseBodyAsString());
            throw new RuntimeException("Ошибка связи с NLP-сервисом: " + e.getMessage(), e);
        } catch (Exception e) { 
            // Ловим Jackson и другие возможные ошибки
            System.err.println("Ошибка при подготовке запроса: " + e.getMessage());
            throw new RuntimeException("Внутренняя ошибка клиента", e);
        } 
    }

} 
