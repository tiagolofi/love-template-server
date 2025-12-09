package com.github.tiagolofi.filter;

import java.io.IOException;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.jboss.logging.Logger;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.Provider;

/**
 * Filter que captura informações detalhadas sobre os usuários que acessam a página /love-calendar.
 * Registra dados como IP, User-Agent, headers, localização geográfica e informações da máquina servidor.
 */
@Provider
public class LoveCalendarRequestFilter implements ContainerRequestFilter {

    private static final Logger logger = Logger.getLogger(LoveCalendarRequestFilter.class);
    private static final String LOVE_CALENDAR_PATH = "/templates/love-calendar";
    private String serverID;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();

        // Aplicar filter apenas para requisições de /love-calendar
        if (path.contains(LOVE_CALENDAR_PATH)) {
            logUserAccess(requestContext);
        }
    }

    /**
     * Registra informações detalhadas sobre o acesso do usuário
     */
    private void logUserAccess(ContainerRequestContext requestContext) {
        if (serverID == null) {
            serverID = getServerIdentifier();
        }

        StringBuilder logMessage = new StringBuilder();
        logMessage.append("\n");
        logMessage.append("========== LOVE CALENDAR ACCESS LOG ==========\n");
        logMessage.append("Timestamp: ").append(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)).append("\n");

        // ID do servidor
        logMessage.append("Server ID: ").append(serverID).append("\n");

        // Informações da requisição
        logMessage.append("Method: ").append(requestContext.getMethod()).append("\n");
        logMessage.append("URI: ").append(requestContext.getUriInfo().getRequestUri()).append("\n");
        logMessage.append("Path: ").append(requestContext.getUriInfo().getPath()).append("\n");

        // IP do cliente
        String clientIP = getClientIP(requestContext);
        logMessage.append("Client IP: ").append(clientIP).append("\n");

        // User-Agent e informações do dispositivo
        String userAgent = requestContext.getHeaderString("User-Agent");
        logMessage.append("User-Agent: ").append(userAgent != null ? userAgent : "NOT PROVIDED").append("\n");
        logMessage.append("Device Info: ").append(getDeviceInfo(userAgent)).append("\n");

        // Informações de localização
        logMessage.append("Location: ").append(getLocationInfo(requestContext)).append("\n");

        // Accept language
        String acceptLanguage = requestContext.getHeaderString("Accept-Language");
        logMessage.append("Accept-Language: ").append(acceptLanguage != null ? acceptLanguage : "NOT PROVIDED").append("\n");

        // Referrer
        String referrer = requestContext.getHeaderString("Referer");
        logMessage.append("Referrer: ").append(referrer != null ? referrer : "DIRECT ACCESS").append("\n");

        // Accept media types
        logMessage.append("Accept Media Types: ").append(requestContext.getAcceptableMediaTypes()).append("\n");

        // Query parameters
        if (!requestContext.getUriInfo().getQueryParameters().isEmpty()) {
            logMessage.append("Query Parameters: ").append(requestContext.getUriInfo().getQueryParameters()).append("\n");
        }

        // Todos os headers
        logMessage.append("Headers: {\n");
        requestContext.getHeaders().keySet().stream()
                .sorted()
                .forEach(headerName -> {
                    String headerValue = requestContext.getHeaderString(headerName);
                    logMessage.append("  ").append(headerName).append(": ").append(headerValue).append("\n");
                });
        logMessage.append("}\n");

        // Content-Type da requisição
        MediaType mediaType = requestContext.getMediaType();
        logMessage.append("Content-Type: ").append(mediaType != null ? mediaType : "NOT SPECIFIED").append("\n");

        // Length da requisição
        logMessage.append("Content-Length: ").append(requestContext.getLength()).append("\n");

        logMessage.append("=============================================\n");

        logger.infof(logMessage.toString());
    }

    /**
     * Extrai o IP do cliente, considerando proxies e load balancers
     */
    private String getClientIP(ContainerRequestContext requestContext) {
        // Tenta vários headers que podem conter o IP real em ambientes com proxy
        String[] headerCandidates = {
            "X-Forwarded-For",
            "CF-Connecting-IP",
            "True-Client-IP",
            "X-Client-IP",
            "X-Real-IP"
        };

        for (String header : headerCandidates) {
            String ip = requestContext.getHeaderString(header);
            if (ip != null && !ip.isEmpty()) {
                // X-Forwarded-For pode conter múltiplos IPs, pega o primeiro
                return ip.split(",")[0].trim();
            }
        }

        // Fallback para remoteAddr se nenhum header for encontrado
        return requestContext.getSecurityContext() != null &&
               requestContext.getSecurityContext().getUserPrincipal() != null ?
               "Authenticated User" : "UNKNOWN";
    }

    /**
     * Obtém um identificador único da máquina servidor
     */
    public static String getServerIdentifier() {
        try {
            // Combina hostname com um identificador único da JVM
            String hostname = InetAddress.getLocalHost().getHostName();
            // "-" + Integer.toHexString(System.identityHashCode(System.class));
            return hostname;
        } catch (Exception e) {
            return "SERVER-" + System.getenv("HOSTNAME");
        }
    }

    /**
     * Tenta extrair informações de localização do IP usando headers comuns
     */
    private String getLocationInfo(ContainerRequestContext requestContext) {
        StringBuilder location = new StringBuilder();

        // Headers que podem conter informações de localização (comuns em CDNs como Cloudflare)
        String[] locationHeaders = {
            "CF-IPCountry",     // Cloudflare: país
            "X-Country-Code",   // Código do país
            "X-City",           // Cidade
            "X-State",          // Estado/Província
            "X-Latitude",       // Latitude
            "X-Longitude",      // Longitude
            "X-Metro-Code"      // Código metropolitano
        };

        for (String header : locationHeaders) {
            String value = requestContext.getHeaderString(header);
            if (value != null && !value.isEmpty()) {
                if (location.length() > 0) {
                    location.append(", ");
                }
                location.append(header).append(": ").append(value);
            }
        }

        return location.length() > 0 ? location.toString() : "NOT AVAILABLE";
    }

    /**
     * Extrai informações do dispositivo/cliente a partir do User-Agent
     */
    private String getDeviceInfo(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "NOT PROVIDED";
        }

        StringBuilder deviceInfo = new StringBuilder();

        // Detecta SO
        if (userAgent.contains("Windows")) {
            deviceInfo.append("OS: Windows");
            if (userAgent.contains("Windows NT 10.0")) {
                deviceInfo.append(" 10/11");
            }
        } else if (userAgent.contains("Mac")) {
            deviceInfo.append("OS: macOS");
        } else if (userAgent.contains("Linux")) {
            deviceInfo.append("OS: Linux");
        } else if (userAgent.contains("Android")) {
            deviceInfo.append("OS: Android");
        } else if (userAgent.contains("iPhone") || userAgent.contains("iPad")) {
            deviceInfo.append("OS: iOS");
        }

        deviceInfo.append(" | ");

        // Detecta browser
        if (userAgent.contains("Chrome")) {
            deviceInfo.append("Browser: Chrome");
        } else if (userAgent.contains("Firefox")) {
            deviceInfo.append("Browser: Firefox");
        } else if (userAgent.contains("Safari")) {
            deviceInfo.append("Browser: Safari");
        } else if (userAgent.contains("Edge")) {
            deviceInfo.append("Browser: Edge");
        } else if (userAgent.contains("OPR")) {
            deviceInfo.append("Browser: Opera");
        } else {
            deviceInfo.append("Browser: Unknown");
        }

        // Tipo de dispositivo
        if (userAgent.contains("Mobile")) {
            deviceInfo.append(" | Type: Mobile");
        } else if (userAgent.contains("Tablet")) {
            deviceInfo.append(" | Type: Tablet");
        } else {
            deviceInfo.append(" | Type: Desktop");
        }

        return deviceInfo.toString();
    }
}
