package org.etk247;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.quarkus.arc.profile.IfBuildProfile;

import static java.util.Collections.emptyList;

@ApplicationScoped
public class StorageClient {

    private static final Logger LOG = LoggerFactory.getLogger(StorageClient.class);

    @ConfigProperty(name = "gcp.project.name")
    private String project;

    @ConfigProperty(name = "gcp.bucket.name")
    public String snapshotBucket;

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

        Blob mostRecentFilePointer = storageService.get(BlobId.of(snapshotBucket, MOST_RECENT_FILENAME));

        return Optional.ofNullable(mostRecentFilePointer).map(
            (pointer -> {
                LOG.info("Found pointer, getting updated file");
                String previousFileName = new String(mostRecentFilePointer.getContent(), StandardCharsets.UTF_8);

                LOG.info("Newest filename {}", previousFileName);
                Optional<Blob> snapshot = Optional.ofNullable(storageService.get(BlobId.of(snapshotBucket, previousFileName)));

                return snapshot
                    .map(blob -> {
                        try {
                            return objectMapper.readValue(new String(blob.getContent(), StandardCharsets.UTF_8), new TypeReference<List<AdoptableDog>>() {});
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .orElse(emptyList());
            })
        ).orElse(emptyList());
    }

    public void persistSnapshot(Collection<AdoptableDog> dogs) {
        LOG.info("Persisting snapshot of {} dogs", dogs.size());

        BlobId mostRecentBlobId = BlobId.of(snapshotBucket, MOST_RECENT_FILENAME);
        storageService.delete(mostRecentBlobId); //Remove old metadata. A bit hacky, but we're running this like every 10 minutes, so nbd

        BlobInfo blobInfo = BlobInfo.newBuilder(mostRecentBlobId).build();
        String newFileName = "SNAPSHOT-" + Instant.now().toEpochMilli();
        BlobInfo newFileBlobInfo = BlobInfo.newBuilder(BlobId.of(snapshotBucket, newFileName)).build();
        try {
            storageService.create(newFileBlobInfo, objectMapper.writeValueAsBytes(dogs));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        storageService.create(blobInfo, newFileName.getBytes(StandardCharsets.UTF_8));
    }

    @ApplicationScoped
    @IfBuildProfile("prod")
    Storage remoteStorage() {
        return StorageOptions.newBuilder().setProjectId(project).build().getService();
    }
}
