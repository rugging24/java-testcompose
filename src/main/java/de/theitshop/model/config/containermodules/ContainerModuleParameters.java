package de.theitshop.model.config.containermodules;

import de.theitshop.container.VariablePlaceholderUtils;
import de.theitshop.model.container.ProcessedServices;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public interface ContainerModuleParameters {
    VariablePlaceholderUtils variablePlaceholderUtils = new VariablePlaceholderUtils();
    GenericContainer<?> moduleContainer(String serviceName, DockerImageName imageName, ProcessedServices processedServices);
    String moduleContainerHostConnString(GenericContainer<?> container);
}
