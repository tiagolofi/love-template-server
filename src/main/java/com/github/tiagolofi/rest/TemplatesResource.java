package com.github.tiagolofi.rest;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
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
        public static native TemplateInstance loveCalendar(String nome, List<Evento> eventos);
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
    public TemplateInstance get() throws JacksonException, DatabindException, IOException {
        log.info("Token JWT recebido: " + token.getRawToken());
        return Templates.loveCalendar("teste", eventos());
    }

    private List<Evento> eventos() throws StreamReadException, DatabindException, IOException {
        File file = new File("roteiro.json");
        if (file.exists() && file.canRead()) {
            ObjectMapper mapper = new ObjectMapper();
            Roteiro roteiro = mapper.readValue(file, Roteiro.class);
            return roteiro.eventos;
        }
        return null;
    }

    // private List<Evento> eventos() {
    //     List<Evento> eventos = new ArrayList<>();
    //     // dia 26
    //     eventos.add(new Evento("Chegada e Boas Vindas", "26", "", "26 de dezembro", true, "00h"));
    //     eventos.add(new Evento("Chegada e Boas Vindas", "26", 
    //     "Beijos e abraços no Aeroporto Internacional de Brasília", "26 de dezembro", false, "19h"));

    //     // dia 27
    //     eventos.add(new Evento("Comemoração de Aniversário", "27", "", "27 de dezembro", true, "00h"));
        
    //     // dia 28
    //     eventos.add(new Evento("Passeios", "28", "", "28 de dezembro", true, "00h"));
    //     eventos.add(new Evento("Passeio no Lago Paranoá", "28", 
    //     "Observar o Lago Paranoá e a decoração de natal no Pontão do Lago Sul", "28 de dezembro", false, "17h30"));
        
    //     // dia 29
    //     eventos.add(new Evento("Domingo Livre", "29", "Domingo Livre", "29 de dezembro", true, "00h"));

    //     // dia 30
    //     eventos.add(new Evento("O que mais fazer em uma segunda feira chuvosa pela manhã?", "30", "", "30 de dezembro", true, "00h"));
        
    //     // dia 31
    //     eventos.add(new Evento("Reveillon", "31", "", "31 de dezembro", true, "00h"));
        
    //     // dia 01
    //     eventos.add(new Evento("A definir", "1", "", "1 de janeiro", true, "00h"));
        
    //     // dia 02
    //     eventos.add(new Evento("A definir", "2", "", "2 de janeiro", true, "00h"));
        
    //     // dia 03
    //     eventos.add(new Evento("Te vejo em breve...", "3", "", "3 de janeiro", true, "00h"));

    //     return eventos;
    // }

}
