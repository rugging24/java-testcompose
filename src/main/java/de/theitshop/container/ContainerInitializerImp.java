package de.theitshop.container;


import de.theitshop.model.config.Service;
import de.theitshop.model.config.ContainerVolume;
import de.theitshop.model.container.ProcessedServices;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContainerInitializerImp implements ContainerInitializer {
    VariablePlaceholderUtils variablePlaceholderUtils = new VariablePlaceholderUtils();

    @Override
    public Map<String, String> containerEnvironmentVariables(Service service, ProcessedServices processedServices) {
        Map<String, String> runningEnvironmentVariables = new HashMap<>();
        if(!service.getEnvironment().isEmpty()){
            service.getEnvironment().forEach(
                    (k,v) -> {
                        Map.Entry<String, String> env = variablePlaceholderUtils.removeVariablePlaceholder(
                                service.getName(), k, String.valueOf(v), processedServices, null);
                        runningEnvironmentVariables.put(env.getKey().toUpperCase(), env.getValue());
                    }
            );
        }
        return runningEnvironmentVariables;
    }

    @Override
    public Integer[] containerExposedPorts(Service service) {
        List<Integer> exposedPorts = new ArrayList<>(List.of());
        if(service.getExposedPorts() != null && service.getExposedPorts().size() > 0){
            exposedPorts.addAll(service.getExposedPorts());
        }
        return exposedPorts.toArray(new Integer[0]);
    }

    @Override
    public String containerStartupCommand(Service service) {
        String command = null;
        if (service.getCommand() !=null && !service.getCommand().isEmpty() && !service.getCommand().isBlank()){
            command = service.getCommand();
        }
        return command;
    }

    @Override
    public List<ContainerVolume> containerAttachedVolumes(Service service) {
        List<ContainerVolume> attachedVolumes = List.of();
        if (service.getVolumes() != null){
            attachedVolumes = service.getVolumes();
        }
        return attachedVolumes;
    }

    @Override
    public WaitStrategy containerLogWaiter(Service service) {
        WaitStrategy waiter = Wait.defaultWaitStrategy();
        if(service.getLogWaitParameters() != null){
            waiter = Wait.forLogMessage(
                    service.getLogWaitParameters().getLogLineRegex(),
                    service.getLogWaitParameters().getLogLineRegexOccurrence()
            );
        }
        return waiter;
    }

    @Override
    public WaitStrategy containerServiceHTTPWaiter(Service service) {
        WaitStrategy waiter = Wait.defaultWaitStrategy();
        if (service.getHttpWaitParameters() !=null){
            waiter = Wait.forHttp(service.getHttpWaitParameters().getEndPoint())
                    .forPort(service.getHttpWaitParameters().getHttpPort())
                    .forStatusCode(service.getHttpWaitParameters().getResponseStatusCode());
        }
        return waiter;
    }
}
