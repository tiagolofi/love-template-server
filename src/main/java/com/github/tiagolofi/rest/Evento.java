package com.github.tiagolofi.rest;

public record Evento(
    String titulo,
    String dia,
    String descricao,
    String data,
    boolean isPrincipal,
    String hora
) {}

