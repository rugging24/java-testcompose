package de.theitshop.container;


import de.theitshop.model.config.Service;
import de.theitshop.model.config.VolumeMapping;
import de.theitshop.model.container.ProcessedServices;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitStrategy;

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
                        Map.Entry<String, String> env = variablePlaceholderUtils.removeVariablePlaceholder(service.getName(), k, (String) v, processedServices);
                        runningEnvironmentVariables.put(env.getKey().toUpperCase(), env.getValue());
                        if (variablePlaceholderUtils.getExternalFixedPort() !=null){
                            runningEnvironmentVariables.put(randomPortName, variablePlaceholderUtils.getExternalFixedPort());
                        }
                    }
            );
        }
        return runningEnvironmentVariables;
    }

    @Override
    public Integer[] containerExposedPorts(Service service) {
        if(service.getExposedPorts().isEmpty()){
            throw new IllegalArgumentException("Container exposed ports can not be an empty list");
        }
        return service.getExposedPorts().toArray(new Integer[0]);
    }

    @Override
    public String containerStartupCommand(Service service) {
        String command = null;
        if (service.getCommand() !=null && !service.getCommand().isEmpty()){
            command = service.getCommand();
        }
        return command;
    }

    @Override
    public List<VolumeMapping> containerAttachedVolumes(Service service) {
        List<VolumeMapping> attachedVolumes = List.of();
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
