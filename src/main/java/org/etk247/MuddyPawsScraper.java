package org.etk247;

import com.google.common.net.HttpHeaders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.client.utils.URIBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import static java.net.http.HttpRequest.BodyPublishers.noBody;
import static java.util.stream.Collectors.toList;

@ApplicationScoped
public class MuddyPawsScraper {

    private static final Logger LOG = LoggerFactory.getLogger(MuddyPawsScraper.class);

    @ConfigProperty(name = "muddypaws.url")
    private String muddyPawsUrl;

    private final ObjectMapper objectMapper;

    @Inject
    public MuddyPawsScraper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<AdoptableDog> scrapeAdoptableDogs() {
        LOG.info("Starting scrape of {}", muddyPawsUrl);

        HttpRequest request = HttpRequest.newBuilder(getUri())
            .GET()
            .build();

        try {
            return HttpClient.newHttpClient()
                .sendAsync(request, BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(this::readJson)
                .get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

    }

    private List<AdoptableDog> readJson(String body) {
        try {
            return objectMapper.readValue(body, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private URI getUri() {
        URI uri;
        try {
            uri = new URI(muddyPawsUrl);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return uri;
    }
}
