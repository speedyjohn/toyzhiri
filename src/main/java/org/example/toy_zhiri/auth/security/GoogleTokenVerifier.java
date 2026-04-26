package org.example.toy_zhiri.auth.security;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.example.toy_zhiri.exception.AuthException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

/**
 * Верификатор Google ID Token.
 * Проверяет подпись токена по публичным ключам Google и валидирует audience.
 */
@Slf4j
@Component
public class GoogleTokenVerifier {

    @Value("${app.google.client-id}")
    private String googleClientId;

    private GoogleIdTokenVerifier verifier;

    /**
     * Инициализирует верификатор после старта приложения.
     * Ключи Google кэшируются внутри библиотеки автоматически.
     */
    @PostConstruct
    public void init() {
        this.verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                new GsonFactory()
        )
                .setAudience(Collections.singletonList(googleClientId))
                .build();
    }

    /**
     * Верифицирует ID Token от Google и возвращает полезную нагрузку.
     *
     * @param idTokenString ID Token в виде строки
     * @return payload с данными пользователя (email, name, sub и т.д.)
     * @throws AuthException если токен невалиден, истёк или произошла ошибка связи
     */
    public GoogleIdToken.Payload verify(String idTokenString) {
        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken == null) {
                throw new AuthException("Невалидный Google ID Token");
            }

            return idToken.getPayload();

        } catch (GeneralSecurityException e) {
            log.error("Ошибка проверки подписи Google ID Token", e);
            throw new AuthException("Ошибка проверки Google токена");
        } catch (IOException e) {
            log.error("Ошибка IO при проверке Google ID Token", e);
            throw new AuthException("Не удалось связаться с серверами Google");
        }
    }
}