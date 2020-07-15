package org.etk247;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class AdoptableDog {

    @JsonProperty("name")
    private final String name;

    @JsonProperty("image_link")
    private final String imageLink;

    @JsonProperty("available")
    private final boolean available;

    @JsonProperty("details")
    private final List<String> details;

    @JsonCreator
    AdoptableDog(@JsonProperty("name") String name,
                 @JsonProperty("image_link") String imageLink,
                 @JsonProperty("available") boolean available,
                 @JsonProperty("details") List<String> details,
                 @JsonProperty("age") String age) {
        this.name = name;
        this.imageLink = imageLink;
        this.available = available;
        this.details = details;
    }

    public String getName() {
        return name;
    }

    public String getImageLink() {
        return imageLink;
    }

    public boolean isAvailable() {
        return available;
    }

    public List<String> getDetails() {
        return details;
    }

    public String toEmailLine() {
        return String.format("Dog: name: %s, image: https://www.muddypawsrescue.org%s, is available: %s, details %s", name, imageLink, available, details);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdoptableDog that = (AdoptableDog) o;
        return available == that.available &&
            Objects.equals(name, that.name) &&
            Objects.equals(imageLink, that.imageLink) &&
            Objects.equals(details, that.details);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, imageLink, available, details);
    }

    public static class Builder {
        private String name;
        private String imageLink;
        private Boolean available;
        private final List<String> details = new ArrayList<>();
        private String age;

        public Builder() {
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withImageLink(String imageLink) {
            this.imageLink = imageLink;
            return this;
        }

        public Builder withAvailable(Boolean available) {
            this.available = available;
            return this;
        }

        public Builder withDetail(String detail) {
            this.details.add(detail);
            return this;
        }

        public Builder withDetails(Collection<String> details) {
            this.details.addAll(details);
            return this;
        }

        public AdoptableDog build() {
            return new AdoptableDog(
                name,
                imageLink,
                available,
                details,
                age
            );
        }
    }
}
