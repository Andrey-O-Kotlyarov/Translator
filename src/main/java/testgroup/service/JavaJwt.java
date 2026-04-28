package testgroup.service; 

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader; 
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Security;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Date;

public class JavaJwt { 

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KeyInfo {

        public String id;
        public String service_account_id;
        public String private_key;
    }

    public static void get() throws Exception {

        String content = new String(Files.readAllBytes(Paths.get("C:\\Users\\admin\\Downloads\\authorized_key.json")));
        KeyInfo keyInfo = (new ObjectMapper()).readValue(content, KeyInfo.class);

        String privateKeyString = keyInfo.private_key;
        String serviceAccountId = keyInfo.service_account_id;
        String keyId = keyInfo.id; 

        PemObject privateKeyPem;
        try (PemReader reader = new PemReader(new StringReader(privateKeyString))) {
            privateKeyPem = reader.readPemObject();
            if (privateKeyPem == null) {
                throw new IllegalArgumentException("Не удалось прочитать приватный ключ из PEM");
            }
        }

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyPem.getContent()));

        Instant now = Instant.now();

        // Формирование JWT.
        String encodedToken = Jwts.builder()
                .setHeaderParam("kid", keyId)
                .setIssuer(serviceAccountId)
                .setAudience("https://iam.api.cloud.yandex.net/iam/v1/tokens")
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(3600)))
                .signWith(privateKey, SignatureAlgorithm.PS256)
                .compact();
        System.out.println(encodedToken);
    } 
}
