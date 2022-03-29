package de.theitshop.model.config.containermodules.kafka;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import de.theitshop.model.config.containermodules.ContainerModuleParameters;
import de.theitshop.model.container.ProcessedServices;
import lombok.Data;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Zookeeper implements ContainerModuleParameters {
    private ZookeeperParameters zookeeper;

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ZookeeperParameters {
        private boolean external;
        private String connectionString;
    }

    @Override
    public GenericContainer<?> moduleContainer(String serviceName, DockerImageName imageName, ProcessedServices processedServices) {
        KafkaContainer kafka = new KafkaContainer(imageName);
        Map.Entry<String, String> entry = variablePlaceholderUtils.removeVariablePlaceholder(
                serviceName, "connectionString", getZookeeper().connectionString,
                processedServices, null
        );
        if (!getZookeeper().external){
            kafka.withExternalZookeeper(entry.getValue());
        }else {
            kafka.withEmbeddedZookeeper();
        }
        return kafka;
    }

    @Override
    public String moduleContainerHostConnString(GenericContainer<?> container) {
        KafkaContainer kafka = (KafkaContainer) container;
        return kafka.getBootstrapServers();
    }
}
