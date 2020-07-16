package org.etk247;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import static java.util.stream.Collectors.toList;

@ApplicationScoped
public class MuddyPawsScraper {

    private static final Logger LOG = LoggerFactory.getLogger(MuddyPawsScraper.class);

    @ConfigProperty(name = "muddypaws.url")
    private String muddyPawsUrl;

    public List<AdoptableDog> scrapeAdoptableDogs() {
        try {
            LOG.info("Starting scrape of {}", muddyPawsUrl);
            Document doc = Jsoup.connect(muddyPawsUrl).get();
            LOG.info("Finished scrape of {}", doc.toString());

            Elements dogNodes = doc.body()
                .select(".Main-content")
                .select(".card");

            return dogNodes
                .stream()
                .map(this::adoptableDogFromNode)
                .collect(toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private AdoptableDog adoptableDogFromNode(Element dogNode) {
        AdoptableDog.Builder dogBuilder = new AdoptableDog.Builder();

        Elements imageNode = dogNode.select(".card-images");

        String imageLink = imageNode.select("a").first().attr("href");
        dogBuilder.withImageLink(imageLink);

        Boolean available = Boolean.valueOf(imageNode.select(".available").text());
        dogBuilder.withAvailable(available);

        Element bodyNode = dogNode.select(".card-body").first();

        String name = bodyNode.select(".card-title").text();
        dogBuilder.withName(name);

        List<String> details = bodyNode.select(".text")
            .stream()
            .map(Element::text)
            .collect(toList());
        dogBuilder.withDetails(details);

        return dogBuilder.build();
    }
}
