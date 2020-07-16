package org.etk247;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AdoptableDog {

    @JsonProperty("Name")
    private final String name;

    @JsonProperty("Photos")
    private final List<String> photos;

    @JsonProperty("Status")
    private final String status;

    @JsonProperty("Breed")
    private final String breed;

    @JsonProperty("Age")
    private final String age;


    @JsonCreator
    AdoptableDog(@JsonProperty("Name") String name,
                 @JsonProperty("Photos") List<String> photos,
                 @JsonProperty("Status") String status,
                 @JsonProperty("Breed") String breed,
                 @JsonProperty("Age") String age) {
        this.name = name;
        this.photos = photos;
        this.status = status;
        this.breed = breed;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public List<String> getPhotos() {
        return photos;
    }

    public String getStatus() {
        return status;
    }

    public String getBreed() {
        return breed;
    }

    public String getAge() {
        return age;
    }

    public String toEmailLine() {
        return String.format("Dog: name: %s, image: %s, is available: %s, breed %s, age %s", name, photos.iterator().next(), status, breed, age);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdoptableDog that = (AdoptableDog) o;
        return Objects.equals(name, that.name) &&
            Objects.equals(photos, that.photos) &&
            Objects.equals(status, that.status) &&
            Objects.equals(breed, that.breed) &&
            Objects.equals(age, that.age);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, photos, status, breed, age);
    }

}
