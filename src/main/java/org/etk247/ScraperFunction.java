package org.etk247;

import com.google.cloud.functions.Context;
import com.google.cloud.functions.RawBackgroundFunction;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Named("scraperFunction")
@ApplicationScoped
public class ScraperFunction implements RawBackgroundFunction {

    private static final Logger LOG = LoggerFactory.getLogger(ScraperFunction.class);

    private final MuddyPawsScraper scraper;
    private final StorageClient storageClient;
    private final MailGunService mailGunService;

    @ConfigProperty(name = "recipients")
    private List<String> recipients;

    @Inject
    public ScraperFunction(MuddyPawsScraper scraper,
                           StorageClient storageClient,
                           MailGunService mailGunService) {
        this.scraper = scraper;
        this.storageClient = storageClient;
        this.mailGunService = mailGunService;
    }

    @Override
    public void accept(String event, Context context) {
        LOG.error("Got event {}", event);

        Set<AdoptableDog> previousSnapshot = new HashSet<>(storageClient.downloadLastDogsSnapshot());
        Set<AdoptableDog> currentSnapshot = new HashSet<>(scraper.scrapeAdoptableDogs());

        List<AdoptableDog> newDogs = currentSnapshot
            .stream()
            .filter(not(previousSnapshot::contains))
            .collect(toList());

        LOG.info("Found {} new dogs out of {} total", newDogs.size(), currentSnapshot.size());

        storageClient.persistSnapshot(currentSnapshot);

        Predicate<AdoptableDog> fuzzyMatches = new Predicate<>() {
            private final Set<String> DOG_KEYS = previousSnapshot
                .stream()
                .map(this::getKey)
                .collect(toSet());

            @Override
            public boolean test(AdoptableDog adoptableDog) {
                return DOG_KEYS.contains(getKey(adoptableDog));
            }

            private String getKey(AdoptableDog dog) {
                return new StringJoiner("-").add(dog.getName()).add(dog.getAge()).add(dog.getBreed()).toString();
            }
        };

        List<AdoptableDog> dogsToAlertOn = currentSnapshot
            .stream()
            .filter(not(fuzzyMatches))
            .collect(toList());

        Optional.of(dogsToAlertOn)
            .filter(not(Collection::isEmpty))
            .ifPresent(dogs -> recipients.forEach(recipent -> mailGunService.sendEmail(recipent, "New Dogs On Muddy Paws!", dogsToAlertOn)));
    }
}
