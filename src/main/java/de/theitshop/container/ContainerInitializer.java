package de.theitshop.container;

import de.theitshop.model.config.Service;
import de.theitshop.model.config.ContainerVolume;
import de.theitshop.model.config.VolumeSourceType;
import de.theitshop.model.container.ProcessedServices;
import de.theitshop.model.container.RunningContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.images.PullPolicy;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Map;

public interface ContainerInitializer {

    Logger logger = LoggerFactory.getLogger(ContainerInitializer.class);

    Map<String, String> containerEnvironmentVariables(Service service, ProcessedServices processedServices);
    Integer[] containerExposedPorts(Service service);
    String containerStartupCommand(Service service);
    List<ContainerVolume> containerAttachedVolumes(Service service);
    WaitStrategy containerLogWaiter(Service service);
    WaitStrategy containerServiceHTTPWaiter(Service service);

    default RunningContainer getContainer(Network network, Service service, ProcessedServices processedServices){
        Map<String, String> configEnvironmentVariables = containerEnvironmentVariables(service, processedServices);
        String command = containerStartupCommand(service);
        List<ContainerVolume> containerVolumes = containerAttachedVolumes(service);
        WaitStrategy httpWaitStrategy = containerServiceHTTPWaiter(service);
        WaitStrategy logMessageWaitStrategy = containerLogWaiter(service);

        DockerImageName imageName = DockerImageName.parse(service.getImage());

        GenericContainer<?> container = null;
        if (service.getTestContainersModule() !=null){
            container = service.getTestContainersModule()
                    .getModuleParameters()
                    .moduleContainer(service.getName(), imageName, processedServices);
        }else {
            container = new GenericContainer<>(imageName);
            container.withImagePullPolicy(PullPolicy.defaultPolicy());
        }
        container.withNetwork(network);

        container.withEnv(configEnvironmentVariables);
        container.withExposedPorts(containerExposedPorts(service));

        if (command !=null) container.withCommand(command);

        for (ContainerVolume v: containerVolumes){
            if (v.getSource().equals(VolumeSourceType.RESOURCE_PATH))
                container.withClasspathResourceMapping(v.getHost(), v.getContainer(), v.getMode());
            else
                container.withFileSystemBind(v.getHost(), v.getContainer(), v.getMode());
        }

        container.waitingFor(httpWaitStrategy);
        container.waitingFor(logMessageWaitStrategy);

        if (service.getDependsOn() != null){
            for (String k: service.getDependsOn()){
                container.dependsOn(
                        processedServices.getProcessedServices().get(k).getContainer()
                );
            }
        }

        container.withCreateContainerCmdModifier(cmd -> cmd.withHostName(service.getName()));
        container.withLogConsumer(new Slf4jLogConsumer(logger));
        return new RunningContainer(
                container, configEnvironmentVariables, service.getName()
        );
    }
}
