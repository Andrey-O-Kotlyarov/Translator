package testgroup.translators; 

import javax.net.ssl.HttpsURLConnection;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;

public class YaTranslatorSec { 

    public static void translate() throws Exception {
        String apiKey = "";
        String text = URLEncoder.encode("hello", "UTF-8");
        String lang = "ru";
        String urlStr = "https://translate.yandex.net/api/v1.5/tr.json/translate?key=" + apiKey +
                        "&text=" + text + "&lang=" + lang;

        URL url = new URL(urlStr);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("POST");

        InputStream response = connection.getInputStream();
        String json = new Scanner(response).useDelimiter("\\Z").next();

        System.out.println("Ответ: " + json);
    }
}
