package testgroup.translators;

import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import com.google.gson.annotations.SerializedName;
import java.util.List; 

public class YandexTranslateExample {

    private static final String API_KEY = "ваш_api_ключ";
    private static final String URL = "https://translate.yandex.net/api/v1.5/tr.json/translate";
    private static final String FOLDER_ID = "b1ghs3b9bv"; // <-- ВСТАВЬТЕ СВОЙ ID КАТАЛОГА


    public static void translate() throws Exception {

        // 1. Создаем тело запроса в формате JSON
        String jsonInput = "{"
                + "\"folderId\": \"" + FOLDER_ID + "\","
                + "\"texts\": [\"Hello, world!\"],"
                + "\"targetLanguageCode\": \"ru\""
                + "}";

        byte[] inputBytes = jsonInput.getBytes(StandardCharsets.UTF_8);

        URL url = new URL(URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // 2. Настраиваем соединение
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Content-Length", String.valueOf(inputBytes.length));
        
        // 3. Добавляем заголовок авторизации с вашим ключом
        conn.setRequestProperty("Authorization", "Api-Key " + API_KEY);
        
        conn.setDoOutput(true);

        // 4. Отправляем тело запроса
        try (OutputStream os = conn.getOutputStream()) {
            os.write(inputBytes);
        }

        // 5. Получаем и парсим ответ
        int responseCode = conn.getResponseCode();
        System.out.println("Response Code: " + responseCode);
        
        try (InputStream is = conn.getInputStream()) {
            Gson gson = new Gson();
            TranslateResponse response = gson.fromJson(new InputStreamReader(is), TranslateResponse.class);
            
            if (response != null && response.translations != null && !response.translations.isEmpty()) {
                System.out.println("Перевод: " + response.translations.get(0).text);
            }
        }
    }

    
    
    public class TranslateResponse { 
        @SerializedName("translations")
        List<TranslationItem> translations;

        public static class TranslationItem {
            @SerializedName("text")
            String text;
        
            @SerializedName("detectedLanguageCode")
            String detectedLanguageCode;
        } 
    } 
} 