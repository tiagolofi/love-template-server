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
    Usuarios usuarios;

    public String token(Credencial credencial) { 
        String data = usuarios.credenciais().get(credencial.usuario);
        String password = data != null ? data.split("\\|")[0] : null;
        String[] roles = data != null ? data.split("\\|")[1].split(",") : new String[]{};
        
        if (password == null) {
            throw new CredencialInvalidaException();
        }
        if (!credencial.senha.equals(password)) {
            throw new CredencialInvalidaException();
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

    public static class CredencialInvalidaException extends RuntimeException {
        public CredencialInvalidaException() {
            super("Credenciais inválidas.");
        }
    }

    public static class Credencial {
        public String usuario;
        public String senha;
    }

}