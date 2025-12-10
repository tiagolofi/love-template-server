package com.github.tiagolofi.login;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

@RequestScoped
public class TokenJwt {

    @Inject
    Configs configs;

    public String token(Credencial credencial) { 
        String data = configs.users().get(credencial.usuario);
        String password = data != null ? data.split("\\|")[0] : null;
        String[] roles = data != null ? data.split("\\|")[1].split(",") : new String[]{};
        
        if (password == null) {
            throw new UnauthorizedException();
        }
        if (!credencial.senha.equals(password)) {
            throw new UnauthorizedException();
        }

        return Jwt
            .issuer("https://github.com.br/tiagolofi")
            .upn(credencial.usuario)
            .subject(credencial.usuario)
            .groups(Set.of(roles))
            .claim("dataHora", LocalDateTime.now())
            .expiresIn(Duration.ofHours(1))
            .innerSign()
            .encrypt();
    }

    public static class UnauthorizedException extends RuntimeException {
        public UnauthorizedException() {
            super("Credenciais inválidas.");
        }
    }

    public static class Credencial {
        public String usuario;
        public String senha;
    }

}