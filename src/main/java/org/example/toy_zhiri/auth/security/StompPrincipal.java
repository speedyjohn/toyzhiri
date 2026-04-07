package org.example.toy_zhiri.auth.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.security.Principal;
import java.util.UUID;

/**
 * Principal для STOMP-сессии.
 * Несёт email (как имя — для совместимости с REST, где UserDetails.getUsername() = email)
 * и userId (для удобства, чтобы не дёргать UserService на каждом сообщении).
 */
@Getter
@RequiredArgsConstructor
public class StompPrincipal implements Principal {

    private final String email;
    private final UUID userId;

    @Override
    public String getName() {
        return email;
    }
}