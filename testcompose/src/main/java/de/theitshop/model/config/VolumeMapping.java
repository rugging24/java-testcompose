package de.theitshop.model.config;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
//@JsonDeserialize(using = VolumeModeDeserializer.class)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class VolumeMapping {
    @NonNull private String host;
    @NonNull private String container;
    private String mode; // = VolumeMode.READ_ONLY.mode;
    private String source = VolumeSourceType.DOCKER_SOURCE.sourceType;
}
