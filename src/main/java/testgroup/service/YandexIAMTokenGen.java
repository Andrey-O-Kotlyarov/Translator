package testgroup.service; 

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import com.google.gson.Gson; 
import java.io.InputStream; 

public class YandexIAMTokenGen { 

    // Класс, который описывает структуру всего JSON-ответа
    class TranslationResponse {
        Translation[] translations; // Массив переводов (на случай, если мы переводили несколько фраз)
    }

    // Класс, который описывает один элемент в массиве translations
    class Translation {
        String text; // Переведенный текст
        String detectedLanguageCode; // Код языка, который был определен автоматически
    } 

    public static void gener() {
        try {
            // --- ПОЛУЧЕНИЕ IAM-ТОКЕНА ЧЕРЕЗ СИСТЕМНУЮ КОМАНДУ ---
            
            // 1. Определяем путь к исполняемому файлу yc.exe
            String pathToYc = "C:\\Users\\admin\\yandex-cloud\\bin\\yc.exe"; 

            // 2. Создаем команду. Это будет массив: [программа, аргумент]
            String[] command = {pathToYc, "iam", "create-token"};

            // 3. Запускаем процесс
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start(); 

            // Читаем вывод (stdout) из команды
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));            
            String iamToken = reader.readLine(); // Токен будет в первой строке
            
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                // Если команда не сработала, прочитаем ошибку (stderr)
                BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()));
                String errorLine;
                System.err.println("Ошибка при выполнении 'yc iam create-token':");
                while ((errorLine = errorReader.readLine()) != null) {
                    System.err.println(errorLine);
                }
                return;
            }

            System.out.println("IAM-токен успешно получен через yc: " + iamToken);

            // --- ТЕПЕРЬ ИСПОЛЬЗУЕМ ЭТОТ ТОКЕН ДЛЯ ПЕРЕВОДА ---             
            String folderId = "b1gr4ndveernv478dls8";

            //String jsonBody = String.format("{\"folderId\": \"%s\", \"texts\": [\"Привет, мир!\"], \"targetLanguageCode\": \"en\"}", folderId);
            String jsonBody = String.format(
                "{" + 
                "\"folderId\": \"%s\", " + 
                "\"texts\": [\"Hello, world!\"], " + 
                "\"targetLanguageCode\": \"ru\"" + 
                "}", 
                folderId); 

            // 1. Создаем URL и открываем соединение
            URL url = new URL(
                "https://translate.api.cloud.yandex.net/translate/v2/translate");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    
            // 2. Настраиваем соединение
            connection.setRequestMethod("POST");
            connection.setRequestProperty(
                "Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Authorization", "Bearer " + iamToken);                 
            // Указываем, что мы будем отправлять данные
            connection.setDoOutput(true); 

            // 3. Отправляем тело запроса
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.getBytes("UTF-8");
                os.write(input, 0, input.length);
            }

            // 4. ПОЛУЧАЕМ КОД ОТВЕТА
            int responseCode = connection.getResponseCode();
            System.out.println("Код ответа от Переводчика: " + responseCode);

            // 5. ВЫБИРАЕМ ПОТОК ДЛЯ ЧТЕНИЯ
            // Если код 200 - читаем из getInputStream()
            // Если код ошибки (403, 404) - читаем из getErrorStream()
            InputStream is = (responseCode == 200) 
                ? connection.getInputStream() 
                : connection.getErrorStream();

            if (is != null) {
                // 6. ЧИТАЕМ СЫРЫЕ БАЙТЫ ИЗ ПОТОКА
                ByteArrayOutputStream result = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) != -1) {
                    result.write(buffer, 0, length);
                }
        
                // 7. ЯВНО ДЕКОДИРУЕМ БАЙТЫ В СТРОКУ С ПОМОЩЬЮ UTF-8
                String responseString = result.toString("UTF-8");
        
                // 8. Парсим JSON с помощью Gson
                Gson gson = new Gson();
                TranslationResponse translationResponse = 
                    gson.fromJson(responseString, TranslationResponse.class);

                if (translationResponse.translations != null && 
                    translationResponse.translations.length > 0) {
                    String translatedText = translationResponse.translations[0].text;
                    System.out.println("Результат перевода: " + translatedText);
                } else {
                    System.out.println("В ответе нет данных о переводе.");
                    System.out.println("Полный ответ: " + responseString);
                }
            }
        
        } catch (Exception e) {
            e.printStackTrace();
        }
    } 
} 