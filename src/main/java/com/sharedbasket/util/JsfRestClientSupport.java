package com.sharedbasket.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;

/**
 * Shared helpers for JSF beans that call this application's own REST endpoints over HTTP,
 */
public final class JsfRestClientSupport {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JsfRestClientSupport() {
    }

    public static <T> T resolveBean(Class<T> beanType) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext == null) {
            throw new IllegalStateException("FacesContext is not available");
        }

        ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
        WebApplicationContext applicationContext =
            WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
        return applicationContext.getBean(beanType);
    }

    public static HttpServletRequest getCurrentRequest() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext == null) {
            return null;
        }

        Object request = facesContext.getExternalContext().getRequest();
        return request instanceof HttpServletRequest ? (HttpServletRequest) request : null;
    }

    public static HttpHeaders buildJsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpServletRequest request = getCurrentRequest();
        String cookie = request == null ? null : request.getHeader("Cookie");
        if (cookie != null && !cookie.trim().isEmpty()) {
            headers.add(HttpHeaders.COOKIE, cookie);
        }
        return headers;
    }

    public static String apiBaseUrl(String pathSuffix) {
        String configuredBaseUrl = System.getProperty("sharedbasket.api-base-url");
        if (configuredBaseUrl == null || configuredBaseUrl.trim().isEmpty()) {
            configuredBaseUrl = System.getenv("SHAREDBASKET_API_BASE_URL");
        }
        if (configuredBaseUrl != null && !configuredBaseUrl.trim().isEmpty()) {
            String baseUrl = configuredBaseUrl.trim();
            return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) + pathSuffix : baseUrl + pathSuffix;
        }

        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            throw new IllegalStateException("HttpServletRequest is not available");
        }

        String scheme = request.getScheme();
        String host = request.getServerName();
        int port = request.getServerPort();
        String contextPath = request.getContextPath() == null ? "" : request.getContextPath();

        boolean defaultPort = ("http".equalsIgnoreCase(scheme) && port == 80)
            || ("https".equalsIgnoreCase(scheme) && port == 443);
        String base = scheme + "://" + host + (defaultPort ? "" : ":" + port) + contextPath;
        return base + pathSuffix;
    }

    public static String extractErrorMessage(Exception ex) {
        if (ex instanceof HttpStatusCodeException) {
            String body = ((HttpStatusCodeException) ex).getResponseBodyAsString();
            if (body != null && !body.trim().isEmpty()) {
                String message = tryExtractMessageField(body);
                return message != null ? message : body;
            }
        }
        return ex.getMessage();
    }

    private static String tryExtractMessageField(String jsonBody) {
        try {
            JsonNode node = OBJECT_MAPPER.readTree(jsonBody);
            return node.hasNonNull("message") ? node.get("message").asText() : null;
        } catch (Exception parseEx) {
            return null;
        }
    }
}
