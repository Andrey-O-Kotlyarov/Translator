package testgroup.service; 

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

// Для HTTP-запроса
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

// Для парсинга JSON (новое)
import org.json.JSONObject;

public class YandexIAMTokenGenerator {

    public static void generate() throws Exception {
        // --- 1. Параметры --- 
        // ID сервисного аккаунта (например, ajey0c39s8****)
        String serviceAccountId = "aje4b13jumf6qqtotric";
        
        // ID авторизованного ключа (например, ajeq3404egolesqnebuqr)
        String keyId = "ajej3404egolsqnebqur";
        
        // Путь к файлу с закрытым ключом (JSON, который вы скачали)
        String privateKeyFilePath = "C:\\Users\\admin\\Downloads\\authorized_key.json";

        // --- 2. Чтение закрытого ключа из файла (СУПЕР-ОЧИСТКА) ---
        String keyContent = new String(Files.readAllBytes(Paths.get(privateKeyFilePath)));
        JSONObject jsonObject = new JSONObject(keyContent);

        // Извлекаем строку с ключом
        String privateKeyFull = jsonObject.getString("private_key");

        // --- НОВЫЙ СПОСОБ ОЧИСТКИ ---
        // 1. Находим позицию маркера начала ключа
        int startIndex = privateKeyFull.indexOf("BEGIN PRIVATE KEY");

        if (startIndex == -1) {
            // Если маркер не найден, выводим ошибку и останавливаемся
            System.out.println("ОШИБКА: В строке ключа не найден маркер 'BEGIN PRIVATE KEY'.");
            System.out.println("Содержимое переменной: " + privateKeyFull);
            return; // Останавливаем выполнение программы
        }

        // 2. Находим позицию маркера конца ключа
        int endIndex = privateKeyFull.indexOf("END PRIVATE KEY");

        if (endIndex == -1) {
            System.out.println("ОШИБКА: В строке ключа не найден маркер 'END PRIVATE KEY'.");
            return;
        }

        // 3. Извлекаем только ту часть, которая МЕЖДУ маркерами
        // Добавляем длину строки "BEGIN PRIVATE KEY" (18 символов), чтобы пропустить сам заголовок
        String keyBetweenMarkers = privateKeyFull.substring(startIndex + 18, endIndex);

        // 4. Убираем все переносы строк и лишние пробелы внутри извлеченного ключа
        String privateKeyBase64 = keyBetweenMarkers.replaceAll("[\\s]+", "");
        privateKeyBase64 = privateKeyBase64.replaceAll("[^A-Za-z0-9+/=]", ""); 
        System.out.println("Ключ успешно извлечен и очищен."); 

        // --- 3. Формирование JWT ---
        Date now = new Date();

        // ВЫВОДИМ НАЧАЛО И КОНЕЦ КЛЮЧА В КОНСОЛЬ
        System.out.println("=== НАЧАЛО ДИАГНОСТИКИ КЛЮЧА ===");
        System.out.println("Первый 100 символов ключа: " + privateKeyBase64.substring(0, Math.min(100, privateKeyBase64.length())));
        System.out.println("Последний 50 символов ключа: " + privateKeyBase64.substring(Math.max(0, privateKeyBase64.length() - 50)));
        System.out.println("=== КОНЕЦ ДИАГНОСТИКИ КЛЮЧА ===");

        // Если вы видите здесь не случайный набор букв и цифр (B64-алфавит),
        // а что-то вроде "api.iam.v1" или "service-account-keys", значит,
        // вы извлекаете из JSON не то поле.
        
        String jwt = Jwts.builder()
                .setHeaderParam("kid", keyId)
                .setHeaderParam("alg", "PS256")
                .setIssuer(serviceAccountId)
                .setAudience("https://iam.api.cloud.yandex.net/iam/v1/tokens")
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + 3600 * 1000))
                .signWith(getPrivateKey(privateKeyBase64), SignatureAlgorithm.PS256)
                .compact();

        System.out.println("Сформированный JWT: " + jwt);

        // --- 4. Обмен JWT на IAM-токен ---

        //String jsonBody = String.format("{\"jwt\": \"%s\"}", jwt);
        //String jsonBody = ""; 
        //String jsonBody = String.format("{\"jwt\": \"%s\", \"serviceAccountId\": \"%s\"}", jwt, serviceAccountId);
        //String jsonBody = String.format("{\"serviceAccountId\": \"%s\"}", serviceAccountId); 
        //String jsonBody = String.format("{\"identity\": {\"jwt\": \"%s\"}}", jwt);
        String jsonBody = String.format("{\"identity\": {\"jwt\": \"%s\"}}", jwt);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://iam.api.cloud.yandex.net/iam/v1/tokens"))
                .header("Content-Type", "application/json")
                //.header("Authorization", "Bearer " + jwt)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody)) 
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Код ответа: " + response.statusCode());
        System.out.println("Тело ответа: " + response.body());
    }

    // Метод для преобразования PEM-строки в PrivateKey объект (ОСТАЛСЯ ПРЕЖНИМ) 
    private static PrivateKey getPrivateKey(String privateKeyBase64) throws Exception {
        // Убираем все пробелы и переносы строк, которые могли случайно попасть
        String cleanBase64 = privateKeyBase64.replaceAll("[\\s]+", "");

        // Декодируем Base64-строку в массив байтов
        byte[] encoded = Base64.getDecoder().decode(cleanBase64);

        // Создаем спецификацию ключа (PKCS#8)
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);

        // Получаем фабрику для RSA-ключей и генерируем сам ключ
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(keySpec);
    }


}