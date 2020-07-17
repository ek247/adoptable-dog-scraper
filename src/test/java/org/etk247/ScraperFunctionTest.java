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
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.MatcherAssert.assertThat;

@QuarkusTest
public class ScraperFunctionTest {

    @Inject
    ScraperFunction scraperFunction;

    @ConfigProperty(name = "mailgun.api.url")
    private String mailgunUrl;

    private final WireMockServer muddyPawsWireMockServer = new WireMockServer(options().port(8082));
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
    public void whenDogs_sendsEmail_doesNotResendOnSecondRequest() throws IOException {
        muddyPawsWireMockServer.stubFor(get(urlEqualTo("/dogs"))
            .willReturn(aResponse()
                .withStatus(200)
                .withBody(IOUtils.toString(getClass().getClassLoader().getResourceAsStream("test-dogs.json"), StandardCharsets.UTF_8))
            ));
        mailGunWireMockServer.stubFor(post(urlPathEqualTo("/messages"))
            .willReturn(aResponse()
                .withStatus(200)));

        scraperFunction.accept("test", null);

        muddyPawsWireMockServer.verify(1, getRequestedFor(urlEqualTo("/dogs")));
        mailGunWireMockServer.verify(1, postRequestedFor(
            urlPathEqualTo("/messages"))
            .withQueryParam("from", equalTo("Dog Notifier <test@test.com>"))
            .withQueryParam("html", equalTo("<html><body><table><tr> <th> Name </th>\n" +
                "<th> Image </th>\n" +
                "<th> Age </th>\n" +
                "<th> Breed </th>\n" +
                "<th> Status </th> </tr><tr> <th> Marty </th>\n" +
                "<th> <img src=\"https://www.shelterluv.com/sites/default/files/animal_pics/12799/2020/06/28/18/20200628180446.png\" style=\"width:128px;height:128px;\">\n" +
                " </th>\n" +
                "<th> 3 months </th>\n" +
                "<th> Shepherd/Mixed Breed (Medium) </th>\n" +
                "<th> Waitlist Full / Pending Adoption </th> </tr>\n" +
                "<tr> <th> Sid </th>\n" +
                "<th> <img src=\"https://www.shelterluv.com/sites/default/files/animal_pics/12799/2020/07/10/08/20200710083812.png\" style=\"width:128px;height:128px;\">\n" +
                " </th>\n" +
                "<th> 3 years </th>\n" +
                "<th> Chihuahua/Mixed Breed (Small) </th>\n" +
                "<th> Waitlist Full / Pending Adoption </th> </tr>\n" +
                "<tr> <th> Rambo </th>\n" +
                "<th> <img src=\"https://www.shelterluv.com/sites/default/files/animal_pics/12799/2020/07/09/16/20200709161139.png\" style=\"width:128px;height:128px;\">\n" +
                " </th>\n" +
                "<th> 2 years </th>\n" +
                "<th> Pyrenees, Great/Retriever, Golden </th>\n" +
                "<th> Available </th> </tr>\n" +
                "<tr> <th> Kayla </th>\n" +
                "<th> <img src=\"https://www.shelterluv.com/sites/default/files/animal_pics/12799/2020/07/10/13/20200710130803.png\" style=\"width:128px;height:128px;\">\n" +
                " </th>\n" +
                "<th> a year </th>\n" +
                "<th> Pyrenees, Great/Mixed Breed (Large) </th>\n" +
                "<th> Waitlist Full / Pending Adoption </th> </tr>\n" +
                "<tr> <th> Tyr </th>\n" +
                "<th> <img src=\"https://www.shelterluv.com/sites/default/files/animal_pics/12799/2020/07/10/13/20200710130215.png\" style=\"width:128px;height:128px;\">\n" +
                " </th>\n" +
                "<th> 9 years </th>\n" +
                "<th> Bulldog, English/Mixed Breed (Large) </th>\n" +
                "<th> Available </th> </tr>\n" +
                "<tr> <th> Bruce </th>\n" +
                "<th> <img src=\"https://www.shelterluv.com/sites/default/files/animal_pics/12799/2020/07/10/12/20200710125911.png\" style=\"width:128px;height:128px;\">\n" +
                " </th>\n" +
                "<th> 6 months </th>\n" +
                "<th> Chihuahua/Mixed Breed (Small) </th>\n" +
                "<th> Waitlist Full / Pending Adoption </th> </tr>\n" +
                "<tr> <th> Barney </th>\n" +
                "<th> <img src=\"https://www.shelterluv.com/sites/default/files/animal_pics/12799/2020/06/12/13/20200612134311.png\" style=\"width:128px;height:128px;\">\n" +
                " </th>\n" +
                "<th> 8 years </th>\n" +
                "<th> Shepherd/Mixed Breed (Medium) </th>\n" +
                "<th> Available </th> </tr>\n" +
                "<tr> <th> Blanca </th>\n" +
                "<th> <img src=\"https://www.shelterluv.com/sites/default/files/animal_pics/12799/2020/07/04/08/20200704081826.png\" style=\"width:128px;height:128px;\">\n" +
                " </th>\n" +
                "<th> 10 years </th>\n" +
                "<th> Mixed Breed (Medium) </th>\n" +
                "<th> Available </th> </tr>\n" +
                "<tr> <th> Harlie </th>\n" +
                "<th> <img src=\"https://www.shelterluv.com/sites/default/files/animal_pics/12799/2020/07/10/13/20200710130502.png\" style=\"width:128px;height:128px;\">\n" +
                " </th>\n" +
                "<th> 2 years </th>\n" +
                "<th> Catahoula Leopard Dog/Mixed Breed (Large) </th>\n" +
                "<th> Waitlist Full / Pending Adoption </th> </tr>\n" +
                "<tr> <th> Tank </th>\n" +
                "<th> <img src=\"https://www.shelterluv.com/sites/default/files/animal_pics/12799/2020/06/27/21/20200627210616.png\" style=\"width:128px;height:128px;\">\n" +
                " </th>\n" +
                "<th> 4 years </th>\n" +
                "<th> Mastiff/Mixed Breed (Large) </th>\n" +
                "<th> Available </th> </tr>\n" +
                "<tr> <th> Juno </th>\n" +
                "<th> <img src=\"https://www.shelterluv.com/sites/default/files/animal_pics/12799/2020/07/09/16/20200709161110.png\" style=\"width:128px;height:128px;\">\n" +
                " </th>\n" +
                "<th> 2 years </th>\n" +
                "<th> Terrier, Pit Bull/Mixed Breed (Large) </th>\n" +
                "<th> Available </th> </tr>\n" +
                "<tr> <th> Reba </th>\n" +
                "<th> <img src=\"https://www.shelterluv.com/sites/default/files/animal_pics/12799/2020/06/04/09/20200604093137.png\" style=\"width:128px;height:128px;\">\n" +
                " </th>\n" +
                "<th> 2 years </th>\n" +
                "<th> Beagle/Shepherd </th>\n" +
                "<th> Available </th> </tr>\n" +
                "<tr> <th> Hercules </th>\n" +
                "<th> <img src=\"https://www.shelterluv.com/sites/default/files/animal_pics/12799/2020/07/10/08/20200710084414.png\" style=\"width:128px;height:128px;\">\n" +
                " </th>\n" +
                "<th> 6 years </th>\n" +
                "<th> Retriever, Labrador/Mixed Breed (Large) </th>\n" +
                "<th> Waitlist Full / Pending Adoption </th> </tr>\n" +
                "<tr> <th> Rootbeer </th>\n" +
                "<th> <img src=\"https://www.shelterluv.com/sites/default/files/animal_pics/12799/2020/07/07/17/20200707172139.png\" style=\"width:128px;height:128px;\">\n" +
                " </th>\n" +
                "<th> 3 months </th>\n" +
                "<th> Rottweiler/Shepherd </th>\n" +
                "<th> Waitlist Full / Pending Adoption </th> </tr>\n" +
                "<tr> <th> London </th>\n" +
                "<th> <img src=\"https://www.shelterluv.com/sites/default/files/animal_pics/12799/2020/07/10/09/20200710095543.png\" style=\"width:128px;height:128px;\">\n" +
                " </th>\n" +
                "<th> 6 years </th>\n" +
                "<th> Terrier, Jack Russell/Mix </th>\n" +
                "<th> Available </th> </tr></table></body></html>"))
        );

        scraperFunction.accept("test", null);
        muddyPawsWireMockServer.verify(2, getRequestedFor(urlEqualTo("/dogs")));
        mailGunWireMockServer.verify(1, postRequestedFor(urlPathEqualTo("/messages")));
    }

    @Test
    public void whenNoDogs_SendsNoEmail() throws IOException {
        muddyPawsWireMockServer.stubFor(get(urlEqualTo("/dogs"))
            .willReturn(aResponse()
                .withStatus(200)
                .withBody("[]")
            ));

        scraperFunction.accept("test", null);

        muddyPawsWireMockServer.verify(1, getRequestedFor(urlEqualTo("/dogs")));
    }

}
