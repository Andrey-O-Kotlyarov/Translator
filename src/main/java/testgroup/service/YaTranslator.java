package testgroup.service; 

import javax.net.ssl.HttpsURLConnection;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder; 

public class YaTranslator { 

    private static final String API_KEY = "ваш_api_ключ"; 

    private static String translate(String lang, String input) throws IOException {
        String urlStr = "https://translate.yandex.net/api/v1.5/tr.json/translate?key=" + API_KEY;
        URL urlObj = new URL(urlStr);
        HttpsURLConnection connection = (HttpsURLConnection) urlObj.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
        dataOutputStream.writeBytes("text=" + URLEncoder.encode(input, "UTF-8") + "&lang=" + lang);

        InputStream response = connection.getInputStream();
        String json = new java.util.Scanner(response).useDelimiter("\\A").next();

        // Извлекаем переведённый текст из JSON
        int start = json.indexOf("[\"");
        int end = json.indexOf("\"]");
        if (start != -1 && end != -1) {
            return json.substring(start + 2, end);
        }
        return "Ошибка перевода";
    }

} 