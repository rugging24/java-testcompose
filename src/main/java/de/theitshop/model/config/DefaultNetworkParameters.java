package de.theitshop.model.config;

public enum DefaultNetworkParameters {
    DEFAULT_NETWORK_NAME("bridge"),
    DEFAULT_NETWORK_SCOPE("local");

    public final String value;
    DefaultNetworkParameters(String value){
        this.value = value;
    }
}
