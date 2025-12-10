package com.github.tiagolofi.rest;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Roteiro {
    @JsonProperty
    public List<Evento> eventos;
}
