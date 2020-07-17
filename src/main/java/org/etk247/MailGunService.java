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
import java.util.List;

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

    public void sendEmail(String to, String subject, List<AdoptableDog> dogs) {
        HttpClient httpClient = HttpClient.newBuilder().build();


        URI uri;
        try {
            uri = new URIBuilder(mailgunUrl + "/messages")
                .addParameter("from", String.format("Dog Notifier <%s>", fromEmail))
                .addParameter("to", to)
                .addParameter("subject", subject)
                .addParameter("html", getHtml(dogs))
                .build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        String encodedAuth = Base64.getEncoder().encodeToString(("api:" + apiKey).getBytes(StandardCharsets.UTF_8));
        HttpRequest request = HttpRequest.newBuilder(uri)
            .setHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded")
            .header("Authorization", "Basic " + encodedAuth)
            .POST(noBody())
            .build();

        try {
            HttpResponse<?> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if ((response.statusCode() >= 200 && response.statusCode() <= 400)) {
                LOG.info("Sucessfully sent email, {}, {}", response.statusCode(), response.body());
            } else {
                throw new RuntimeException(String.format("Got exception when sending email, status code %s, body %s", response.statusCode(), response.body()));
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private String getHtml(List<AdoptableDog> dogs) {
        return new EmailBuilder()
            .withRows(dogs)
            .withColumn("Name", AdoptableDog::getName)
            .withColumn("Image", (dog) -> String.format("<img src=\"%s\" style=\"width:128px;height:128px;\">\n", dog.getPhotos().iterator().next()))
            .withColumn("Age", AdoptableDog::getAge)
            .withColumn("Breed", AdoptableDog::getBreed)
            .withColumn("Status", AdoptableDog::getStatus)
            .buildHtml();
    }
}
