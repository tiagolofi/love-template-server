package com.github.tiagolofi.rest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tiagolofi.filter.LoveCalendarRequestFilter;
import com.github.tiagolofi.login.TokenJwt;
import com.github.tiagolofi.login.TokenJwt.Credencial;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@RequestScoped
@Path("/templates")
public class TemplatesResource {

    @Inject
    LoveCalendarRequestFilter filtro;

    @Inject
    TokenJwt tokenJwt;

    @Inject
    JsonWebToken token;

    @Inject
    Logger log;

    @CheckedTemplate(requireTypeSafeExpressions = false)
    public static class Templates {
        public static native TemplateInstance loveCalendar(String nome, Roteiro roteiro);
        public static native TemplateInstance login();
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/login")
    public String login(Credencial credencial) {
        return tokenJwt.token(credencial);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/login")
    public TemplateInstance getLogin() {
        return Templates.login();
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/love-calendar")
    @RolesAllowed({"user"})
    public TemplateInstance get() throws IOException {

        File file = new File("eventos.txt");
        if (!file.exists() || !file.canRead()) {
            return Templates.loveCalendar("Usuário", new Roteiro());
        }

        String base64 = Files.readString(file.toPath()).replaceAll("\\s+", "");
        byte[] decodedBytes = Base64.getDecoder().decode(base64);
        String json = new String(decodedBytes);
        ObjectMapper mapper = new ObjectMapper();
        Roteiro roteiro = mapper.readValue(json, Roteiro.class);

        String nome = capitalize(token.getSubject());

        return Templates.loveCalendar(nome, roteiro);
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

}
