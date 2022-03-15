package de.theitshop.model.config;

public enum VolumeSourceType {
    LOCAL_SOURCE ("local"),
    DOCKER_SOURCE("docker");

    public final String sourceType;
    VolumeSourceType(String sourceType){
        this.sourceType = sourceType;
    }
}
