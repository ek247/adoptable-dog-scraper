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
                .withBody(IOUtils.toString(getClass().getClassLoader().getResourceAsStream("test-dogs.json"), StandardCharsets.UTF_8))
            ));
        scraperFunction.accept("test", null);

        muddyPawsWireMockServer.verify(1, getRequestedFor(urlEqualTo("/dogs")));
        mailGunWireMockServer.verify(1, postRequestedFor(
            urlPathEqualTo("/mailgun/messages"))
            .withQueryParam("from", equalTo("Dog Notifier <test@test.com>"))
            .withQueryParam("text", equalTo("Dog: name: Marty, image: https://www.shelterluv.com/sites/default/files/animal_pics/12799/2020/06/28/18/20200628180446.png, is available: Waitlist Full / Pending Adoption, breed Shepherd/Mixed Breed (Medium), age 3 months\n" +
                "Dog: name: Sid, image: https://www.shelterluv.com/sites/default/files/animal_pics/12799/2020/07/10/08/20200710083812.png, is available: Waitlist Full / Pending Adoption, breed Chihuahua/Mixed Breed (Small), age 3 years\n" +
                "Dog: name: Rambo, image: https://www.shelterluv.com/sites/default/files/animal_pics/12799/2020/07/09/16/20200709161139.png, is available: Available, breed Pyrenees, Great/Retriever, Golden, age 2 years\n" +
                "Dog: name: Kayla, image: https://www.shelterluv.com/sites/default/files/animal_pics/12799/2020/07/10/13/20200710130803.png, is available: Waitlist Full / Pending Adoption, breed Pyrenees, Great/Mixed Breed (Large), age a year\n" +
                "Dog: name: Tyr, image: https://www.shelterluv.com/sites/default/files/animal_pics/12799/2020/07/10/13/20200710130215.png, is available: Available, breed Bulldog, English/Mixed Breed (Large), age 9 years\n" +
                "Dog: name: Bruce, image: https://www.shelterluv.com/sites/default/files/animal_pics/12799/2020/07/10/12/20200710125911.png, is available: Waitlist Full / Pending Adoption, breed Chihuahua/Mixed Breed (Small), age 6 months\n" +
                "Dog: name: Barney, image: https://www.shelterluv.com/sites/default/files/animal_pics/12799/2020/06/12/13/20200612134311.png, is available: Available, breed Shepherd/Mixed Breed (Medium), age 8 years\n" +
                "Dog: name: Blanca, image: https://www.shelterluv.com/sites/default/files/animal_pics/12799/2020/07/04/08/20200704081826.png, is available: Available, breed Mixed Breed (Medium), age 10 years\n" +
                "Dog: name: Harlie, image: https://www.shelterluv.com/sites/default/files/animal_pics/12799/2020/07/10/13/20200710130502.png, is available: Waitlist Full / Pending Adoption, breed Catahoula Leopard Dog/Mixed Breed (Large), age 2 years\n" +
                "Dog: name: Tank, image: https://www.shelterluv.com/sites/default/files/animal_pics/12799/2020/06/27/21/20200627210616.png, is available: Available, breed Mastiff/Mixed Breed (Large), age 4 years\n" +
                "Dog: name: Juno, image: https://www.shelterluv.com/sites/default/files/animal_pics/12799/2020/07/09/16/20200709161110.png, is available: Available, breed Terrier, Pit Bull/Mixed Breed (Large), age 2 years\n" +
                "Dog: name: Reba, image: https://www.shelterluv.com/sites/default/files/animal_pics/12799/2020/06/04/09/20200604093137.png, is available: Available, breed Beagle/Shepherd, age 2 years\n" +
                "Dog: name: Hercules, image: https://www.shelterluv.com/sites/default/files/animal_pics/12799/2020/07/10/08/20200710084414.png, is available: Waitlist Full / Pending Adoption, breed Retriever, Labrador/Mixed Breed (Large), age 6 years\n" +
                "Dog: name: Rootbeer, image: https://www.shelterluv.com/sites/default/files/animal_pics/12799/2020/07/07/17/20200707172139.png, is available: Waitlist Full / Pending Adoption, breed Rottweiler/Shepherd, age 3 months\n" +
                "Dog: name: London, image: https://www.shelterluv.com/sites/default/files/animal_pics/12799/2020/07/10/09/20200710095543.png, is available: Available, breed Terrier, Jack Russell/Mix, age 6 years"))
        );
    }

}
