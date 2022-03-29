package de.theitshop.model.config;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.testcontainers.containers.BindMode;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonDeserialize(using = VolumeModeDeserializer.class)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ContainerVolume {
    @NonNull private String host;
    private String container;
    private BindMode mode = VolumeMode.READ_ONLY.mode;
    private VolumeSourceType source = VolumeSourceType.FILESYSTEM_PATH;
}
