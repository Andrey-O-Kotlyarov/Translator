package testgroup.service.translator; 

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import com.google.gson.Gson; 
import java.io.InputStream; 

public class YandexTranslator { 

    // Класс, который описывает структуру всего JSON-ответа
    class TranslationResponse {
        Translation[] translations; // Массив переводов (на случай, если мы переводили несколько фраз)
    }


    // Класс, который описывает один элемент в массиве translations
    class Translation {
        String text; // Переведенный текст
        String detectedLanguageCode; // Код языка, который был определен автоматически
    } 
    
    
    // метод для получения iam токена с помощью утилиты yc в командной строке
    public static String generIAMToken() { 
        String iamToken = ""; 

        try { 
            // --- ПОЛУЧЕНИЕ IAM-ТОКЕНА ЧЕРЕЗ СИСТЕМНУЮ КОМАНДУ ---             
            // 1. Определяем путь к исполняемому файлу yc.exe
            String pathToYc = 
                "C:\\Users\\weiss\\OneDrive\\Desktop\\language_teacher\\winProgs\\yandex-cloud\\bin\\yc.exe"; 

            // 2. задаем свой OAuth токен
            String yourOAuthToken = "y0__wgBENmiuwcYwd0TILmk_54XMJCy6uYIeafYCXxjgPnN_jOiaoA3PdRGuGo";     

            // 3. Создаем команду для командной строки, которая вызовет утилиту yc и установит OAuth токен
            // команды для cli создаются в виде массива [программа, аргумент] 
            // и передаются в командную строку с помощью ProcessBuilder:             
            String[] configCommand = {pathToYc, "config", "set", "token", yourOAuthToken};
            ProcessBuilder configBuilder = new ProcessBuilder(configCommand);
            Process configProcess = configBuilder.start();
            int configExitCode = configProcess.waitFor(); // Ждем завершения 

            // 4. Если команда установки OAut токена не сработала, прочитаем ошибку (stderr)
            if (configExitCode != 0) { 
                BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(configProcess.getErrorStream()));
                String errorLine;
                System.err.println("Ошибка при выполнении 'yc iam create-token':");
                while ((errorLine = errorReader.readLine()) != null) {
                    System.err.println(errorLine);
                }
                return "Ошибка при выполнении 'yc iam create-token'";
            }

            // 5. А если сработала, то отправляем следующую команду - для получения iam токена
            // и читаем ее вывод 
            if (configExitCode == 0) {
                String[] iamCommand = {pathToYc, "iam", "create-token"};
                ProcessBuilder iamBuilder = new ProcessBuilder(iamCommand);
                Process iamProcess = iamBuilder.start(); 
                BufferedReader reader = 
                    new BufferedReader(new InputStreamReader(iamProcess.getInputStream()));

                // В последней строке вывода будет iam токен
                String line;
                while ((line = reader.readLine()) != null) {
                    iamToken = line; 
                }
            } 
            System.out.println("IAM-токен успешно получен через yc: " + iamToken); 

        } catch (Exception e) {
            e.printStackTrace();
        } 
        return iamToken;
    }


    // метод для получения перевода 
    public static String translate(String sourceText, String targetLangCode, String iamToken) {
        
        try {
            String translatedText = "";             

            // --- ИСПОЛЬЗУЕМ ПОЛУЧЕННЫЙ ТОКЕН ДЛЯ ПЕРЕВОДА ---             
            String folderId = "b1gr4ndveernv478dls8";

            //String jsonBody = String.format("{\"folderId\": \"%s\", \"texts\": [\"Привет, мир!\"], \"targetLanguageCode\": \"en\"}", folderId);
            String jsonBody = String.format(
                "{" + 
                "\"folderId\": \"%s\", " + 
                "\"texts\": [\"" + sourceText + "\"], " + 
                "\"targetLanguageCode\": \"" + targetLangCode + "\"" + 
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
                System.out.println("Ответ от сервера: " + responseString);

                // 8. Парсим JSON с помощью Gson
                Gson gson = new Gson();
                TranslationResponse translationResponse = 
                    gson.fromJson(responseString, TranslationResponse.class);

                if (translationResponse.translations != null && 
                    translationResponse.translations.length > 0) {
                    translatedText = translationResponse.translations[0].text;
                    //System.out.println("Результат перевода: " + translatedText);
                    return translatedText;
                } else {
                    System.out.println("В ответе нет данных о переводе.");
                    System.out.println("Полный ответ: " + responseString);
                    return "В ответе нет данных о переводе."; 
                }
            }        
        } catch (Exception e) {
            e.printStackTrace();
        }
        return targetLangCode;         
    } 

} 