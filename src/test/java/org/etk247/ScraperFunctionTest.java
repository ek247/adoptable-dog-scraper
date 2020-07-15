package org.etk247;

import com.github.tomakehurst.wiremock.WireMockServer;

import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.inject.Inject;

import io.quarkus.test.junit.QuarkusTest;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

@QuarkusTest
public class ScraperFunctionTest {

    @Inject
    ScraperFunction scraperFunction;

    @ConfigProperty(name = "mailgun.api.url")
    private String mailgunUrl;

    private final WireMockServer muddyPawsWireMockServer = new WireMockServer(options().bindAddress("127.0.0.1").port(8082));
    private final WireMockServer mailGunWireMockServer = new WireMockServer(8083);

    @BeforeEach
    public void startServers() {
        muddyPawsWireMockServer.start();
        mailGunWireMockServer.start();
    }

    @AfterEach
    public void stopServers() {
        muddyPawsWireMockServer.stop();
        mailGunWireMockServer.stop();
    }

    @Test
    public void testScraper() throws IOException {
        muddyPawsWireMockServer.stubFor(get(urlEqualTo("/dogs"))
            .willReturn(aResponse()
                .withStatus(200)
                .withBody(IOUtils.toString(getClass().getClassLoader().getResourceAsStream("test-dogs.html"), StandardCharsets.UTF_8))
            ));
        scraperFunction.accept("test", null);

        muddyPawsWireMockServer.verify(1, getRequestedFor(urlEqualTo("/dogs")));
        mailGunWireMockServer.verify(1, postRequestedFor(
            urlPathEqualTo("/mailgun/messages"))
            .withQueryParam("from", equalTo("test@test.com"))
            .withQueryParam("text", equalTo("Dog: name: Blanca, image: https://www.muddypawsrescue.org/adoptable?dog=2527, is available: false, details [10 years, Looks like a Mixed Breed (Medium) mix]\n" +
                "Dog: name: Juno, image: https://www.muddypawsrescue.org/adoptable?dog=2563, is available: false, details [2 years, Looks like a Terrier, Pit Bull/Mixed Breed (Large) mix]\n" +
                "Dog: name: Tyr, image: https://www.muddypawsrescue.org/adoptable?dog=2610, is available: false, details [9 years, Looks like a Bulldog, English/Mixed Breed (Large) mix]\n" +
                "Dog: name: Harlie, image: https://www.muddypawsrescue.org/adoptable?dog=2611, is available: false, details [2 years, Looks like a Catahoula Leopard Dog/Mixed Breed (Large) mix]\n" +
                "Dog: name: Tank, image: https://www.muddypawsrescue.org/adoptable?dog=2590, is available: false, details [4 years, Looks like a Mastiff/Mixed Breed (Large) mix]\n" +
                "Dog: name: Sid, image: https://www.muddypawsrescue.org/adoptable?dog=2594, is available: false, details [3 years, Looks like a Chihuahua/Mixed Breed (Small) mix]\n" +
                "Dog: name: Rambo, image: https://www.muddypawsrescue.org/adoptable?dog=2552, is available: false, details [2 years, Looks like a Pyrenees, Great/Retriever, Golden mix]\n" +
                "Dog: name: Barney, image: https://www.muddypawsrescue.org/adoptable?dog=2500, is available: false, details [8 years, Looks like a Shepherd/Mixed Breed (Medium) mix]\n" +
                "Dog: name: Rootbeer, image: https://www.muddypawsrescue.org/adoptable?dog=2569, is available: false, details [3 months, Looks like a Rottweiler/Shepherd mix]\n" +
                "Dog: name: Reba, image: https://www.muddypawsrescue.org/adoptable?dog=2466, is available: false, details [2 years, Looks like a Beagle/Shepherd mix]\n" +
                "Dog: name: Marty, image: https://www.muddypawsrescue.org/adoptable?dog=2578, is available: false, details [3 months, Looks like a Shepherd/Mixed Breed (Medium) mix]\n" +
                "Dog: name: Butter, image: https://www.muddypawsrescue.org/adoptable?dog=2568, is available: false, details [3 months, Looks like a Rottweiler/Shepherd mix]\n" +
                "Dog: name: Bruce, image: https://www.muddypawsrescue.org/adoptable?dog=2609, is available: false, details [6 months, Looks like a Chihuahua/Mixed Breed (Small) mix]"))
        );
    }

}
