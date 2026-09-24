package com.raynald.budget_tracker.security;

import com.raynald.budget_tracker.entity.User;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final long expirySeconds;

    public TokenService(JwtEncoder jwtEncoder,
                        @Value("${app.jwt.expiry-seconds}") long expirySeconds) {
        this.jwtEncoder = jwtEncoder;
        this.expirySeconds = expirySeconds;
    }

    public String createToken(User user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("budget-tracker")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expirySeconds))
                .subject(user.getId().toString())   // who this token belongs to
                .claim("email", user.getEmail())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public long getExpirySeconds() {
        return expirySeconds;
    }
}