package org.etk247;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Singleton;

import io.quarkus.arc.profile.IfBuildProfile;

@ApplicationScoped
public class StorageClient {

    private static final Logger LOG = LoggerFactory.getLogger(StorageClient.class);

    private static final String PROJECT = "muddy-paws-scraper";
    public static final String SNAPSHOT_BUCKET = "muddy-paws-scraper-snapshot";
    public static final String MOST_RECENT_FILENAME = "most_recent.txt";

    private final ObjectMapper objectMapper;
    private final Storage storageService;

    @Inject
    public StorageClient(ObjectMapper objectMapper, Storage storageService) {
        this.objectMapper = objectMapper;
        this.storageService = storageService;
    }

    public List<AdoptableDog> downloadLastDogsSnapshot() {
        LOG.info("Downloading latest snapshot");

        Blob mostRecentFilePointer = storageService.get(BlobId.of(SNAPSHOT_BUCKET, MOST_RECENT_FILENAME));

        return Optional.ofNullable(mostRecentFilePointer).map(
            (pointer -> {
                LOG.info("Found pointer, getting updated file");
                String previousFileName = new String(mostRecentFilePointer.getContent(), StandardCharsets.UTF_8);

                LOG.info("Newest filename {}", previousFileName);
                Blob snapshot = storageService.get(BlobId.of(SNAPSHOT_BUCKET, previousFileName));

                try {
                    return objectMapper.readValue(new String(snapshot.getContent(), StandardCharsets.UTF_8), new TypeReference<List<AdoptableDog>>() {
                    });
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            })
        ).orElse(Collections.emptyList());
    }

    public void persistSnapshot(List<AdoptableDog> dogs) {
        LOG.info("Persisting snapshot of {} dogs", dogs.size());

        BlobId blobId = BlobId.of(SNAPSHOT_BUCKET, MOST_RECENT_FILENAME);
        storageService.delete(blobId); //Remove old metadata. A bit hacky, but we're running this like every 10 minutes, so nbd

        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        try {
            storageService.create(blobInfo, objectMapper.writeValueAsBytes(dogs));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @ApplicationScoped
    @IfBuildProfile("prod")
    Storage remoteStorage() {
        return StorageOptions.newBuilder().setProjectId(PROJECT).build().getService();
    }
}
