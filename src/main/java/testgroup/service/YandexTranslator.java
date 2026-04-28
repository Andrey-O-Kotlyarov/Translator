package testgroup.service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets; 
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.tomcat.util.json.JSONParser; 
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class YandexTranslator { 
    /* 
    final static String IAM_TOKEN = "/тут мой ключ/";
    final static String folder_id = "/тут мой айди/";
    
    private static final String URL_TEMPLATE = 
        "https://translate.yandex.net/api/v1.5/tr.json/translate?key=%s&text=%s&lang=%s";

    public static void translate(String text, String lang) throws Exception {
        
        HttpClient httpClient = HttpClientBuilder.create().build();
        String result = "";
        try {
            HttpPost request = new HttpPost("https://translate.api.cloud.yandex.net/translate/v2/translate");
            String body = String.format("{\"targetLanguageCode\":\"%s\",\"texts\":\"%s\",\"folderId\":\"%s\"}", lang, text, folder_id);
            StringEntity params = new StringEntity(body, "UTF-8");
            params.setContentType("charset=UTF-8");
            request.addHeader("content-type", "application/json");
            String auth = String.format("Bearer %s", IAM_TOKEN);
            request.addHeader("content-type", "application/json");
            request.addHeader("Authorization", auth);
            request.setEntity(params);
            HttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String retSrc = EntityUtils.toString(entity);
                Object jsob_obj = new JSONParser().parse(retSrc);
                JSONObject json_res = (JSONObject) jsob_obj;
                JSONArray res_translate = (JSONArray) json_res.get("translations");
                JSONObject res_json_obj = (JSONObject) res_translate.get(0);
                result = (String) res_json_obj.get("text");
                System.out.println(res_translate);
            }
        } catch (Exception ignored) {
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
    }
    */
} 