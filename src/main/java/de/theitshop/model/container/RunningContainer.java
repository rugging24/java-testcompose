package de.theitshop.model.container;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.testcontainers.containers.GenericContainer;

import java.io.Serializable;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class RunningContainer implements Serializable {
    /**
     * Running containers should contain the following
     *  - container object
     *  - container special environment variables
     *  - container service name
     */
    private GenericContainer<?> container;
    private Map<String, String> configEnvironmentVariables;
    private String serviceName;
}
