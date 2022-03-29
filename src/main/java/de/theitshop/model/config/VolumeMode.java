package de.theitshop.model.config;

import org.testcontainers.containers.BindMode;

public enum VolumeMode {
    READ_ONLY("ro"),
    READ_WRITE("rw");

    public final BindMode mode;

    VolumeMode(String mode){
        if (mode.equalsIgnoreCase("rw")) {
            this.mode = BindMode.READ_WRITE;
        } else {
            this.mode = BindMode.READ_ONLY;
        }
    }
}
