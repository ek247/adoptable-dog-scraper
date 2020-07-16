package org.etk247;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

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
