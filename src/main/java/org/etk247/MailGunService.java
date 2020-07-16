package org.etk247;

import com.google.common.net.HttpHeaders;

import org.apache.http.client.utils.URIBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.inject.Singleton;

import static java.net.http.HttpRequest.BodyPublishers.noBody;

@Singleton
public class MailGunService {
    private static final Logger LOG = LoggerFactory.getLogger(MailGunService.class);

    @ConfigProperty(name = "mailgun.api.url")
    private String mailgunUrl;

    @ConfigProperty(name = "mailgun.api.key")
    private String apiKey;

    @ConfigProperty(name = "mailgun.from-email")
    private String fromEmail;

    public void sendEmail(String to, String subject, String text) {
        HttpClient httpClient = HttpClient.newBuilder().build();

        URI uri;
        try {
            uri = new URIBuilder(mailgunUrl + "/messages")
                .addParameter("from", String.format("Dog Notifier <%s>", fromEmail))
                .addParameter("to", to)
                .addParameter("subject", subject)
                .addParameter("text", text)
                .build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        HttpRequest request = HttpRequest.newBuilder(uri)
            .setHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded")
            .header("content-type", "APPLICATION_FORM_URLENCODED")
            .header(HttpHeaders.AUTHORIZATION, new String(Base64.getEncoder().encode(("api:" + apiKey).getBytes(StandardCharsets.UTF_8))))
            .POST(noBody())
            .build();

        try {
            HttpResponse<?> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            if ((response.statusCode() >= 200 && response.statusCode() <= 400)) {
                LOG.info("Sucessfully sent email, {}", response.statusCode());
            } else {
                LOG.error("Got exception when sending email, status code {}, body {}", response.statusCode(), response.body());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
