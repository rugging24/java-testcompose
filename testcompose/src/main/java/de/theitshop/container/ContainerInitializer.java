package de.theitshop.container;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import de.theitshop.model.config.Service;
import de.theitshop.model.config.VolumeMapping;
import de.theitshop.model.container.ProcessedServices;
import de.theitshop.model.container.RunningContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.images.PullPolicy;
import org.testcontainers.utility.DockerImageName;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public interface ContainerInitializer {
    String randomPortName = "JAVA_DOCKER_RANDOM_PORT";

    Map<String, String> containerEnvironmentVariables(Service service, ProcessedServices processedServices);
    Integer[] containerExposedPorts(Service service);
    String containerStartupCommand(Service service);
    List<VolumeMapping> containerAttachedVolumes(Service service);
    WaitStrategy containerLogWaiter(Service service);
    WaitStrategy containerServiceHTTPWaiter(Service service);

    default RunningContainer getContainer(Service service, ProcessedServices processedServices){
        GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse(service.getImage()));
        container.withImagePullPolicy(PullPolicy.alwaysPull());

        Map<String, String> runningEnvironmentVariables = containerEnvironmentVariables(service, processedServices);
        container.withEnv(runningEnvironmentVariables);
        container.withExposedPorts(containerExposedPorts(service));

        if (runningEnvironmentVariables.containsKey(randomPortName)){
            List<String> fixedExternalPort = Arrays.asList(runningEnvironmentVariables.get(randomPortName).split("<=>"));
            if (fixedExternalPort.size() == 2){
                int hostPort = Integer.parseInt(fixedExternalPort.get(0));
                int containerExposedPort = Integer.parseInt(fixedExternalPort.get(1));
                container.withCreateContainerCmdModifier(cmd -> cmd.withHostConfig(
                        new HostConfig().withPortBindings(new PortBinding(Ports.Binding.bindPort(containerExposedPort), new ExposedPort(hostPort)))
                ));
            }
        }

        String command = containerStartupCommand(service);
        if (command !=null && !command.isEmpty()) container.withCommand();
//        containerAttachedVolumes(service).forEach(
//                v -> container.withFileSystemBind(v.getHost(), v.getContainer(), v.getMode())
//        );

        container.waitingFor(containerServiceHTTPWaiter(service));
        container.waitingFor(containerLogWaiter(service));
        System.out.println(processedServices.getProcessedServices().keySet());
        if (service.getDependsOn() != null){
            service.getDependsOn().forEach(
                    k -> container.dependsOn(
                            processedServices.getProcessedServices().get(k).getContainer()
                    )
            );
        }
        container.withCreateContainerCmdModifier(cmd -> cmd.withHostName(service.getName()));
        return new RunningContainer(
                container, runningEnvironmentVariables, service.getName()
        );
    }
}
