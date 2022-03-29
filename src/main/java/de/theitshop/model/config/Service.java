package de.theitshop.model.config;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import de.theitshop.model.config.containermodules.TestContainersModule;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Service {
    @NonNull private String name;
    @NonNull private String image;
    private List<Integer> exposedPorts;
    private String command;
    private List<ExecCommandAfterContainerStartup> execCommandAfterContainerStartup = List.of();
    private Map<String, Object> environment = Map.of();
    private List<ContainerVolume> volumes;
    private LogWaitParameter logWaitParameters;
    private HttpWaitParameter httpWaitParameters;
    private List<String> dependsOn = List.of();
    private TestContainersModule testContainersModule;
}
