package de.theitshop.model.config;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Network {
    private String name = DefaultNetworkParameters.DEFAULT_NETWORK_NAME.value;
    private String driver = "bridge";
    private Boolean autoCreate = true;
    private Boolean useRandomNetwork = true;
}
