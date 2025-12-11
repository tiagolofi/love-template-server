package com.github.tiagolofi.login;

import java.util.Map;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "love.template")
public interface Usuarios {
    Map<String, String> credenciais();
}
