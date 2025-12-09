package com.github.tiagolofi.rest;

import java.util.ArrayList;
import java.util.List;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@RequestScoped
@Path("/templates")
public class TemplatesResource {

    @CheckedTemplate(requireTypeSafeExpressions = false)
    public static class Templates {
        public static native TemplateInstance loveCalendar(List<Evento> eventos);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("love-calendar")
    public TemplateInstance get() {
        return Templates.loveCalendar(eventos());
    }

    private List<Evento> eventos() {
        List<Evento> eventos = new ArrayList<>();
        // dia 26
        eventos.add(new Evento("Chegada e Boas Vindas", "26", "", "26 de dezembro", true, "00h"));
        eventos.add(new Evento("Chegada e Boas Vindas", "26", 
        "Beijos e abraços no Aeroporto Internacional de Brasília", "26 de dezembro", false, "19h"));

        // dia 27
        eventos.add(new Evento("Comemoração de Aniversário", "27", "", "27 de dezembro", true, "00h"));
        
        // dia 28
        eventos.add(new Evento("Passeios", "28", "", "28 de dezembro", true, "00h"));
        eventos.add(new Evento("Passeio no Lago Paranoá", "28", 
        "Observar o Lago Paranoá e a decoração de natal no Pontão do Lago Sul", "28 de dezembro", false, "17h30"));
        
        // dia 29
        eventos.add(new Evento("Domingo Livre", "29", "Domingo Livre", "29 de dezembro", true, "00h"));

        // dia 30
        eventos.add(new Evento("O que mais fazer em uma segunda feira chuvosa pela manhã?", "30", "", "30 de dezembro", true, "00h"));
        
        // dia 31
        eventos.add(new Evento("Reveillon", "31", "", "31 de dezembro", true, "00h"));
        
        // dia 01
        eventos.add(new Evento("A definir", "1", "", "1 de janeiro", true, "00h"));
        
        // dia 02
        eventos.add(new Evento("A definir", "2", "", "2 de janeiro", true, "00h"));
        
        // dia 03
        eventos.add(new Evento("Te vejo em breve...", "3", "", "3 de janeiro", true, "00h"));

        return eventos;
    }

}
