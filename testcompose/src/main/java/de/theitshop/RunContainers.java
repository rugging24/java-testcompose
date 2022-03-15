package de.theitshop;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.theitshop.config.ContainerConfig;
import de.theitshop.container.BaseContainer;
import de.theitshop.model.config.ConfigServices;
import de.theitshop.model.config.OrderedService;
import de.theitshop.model.container.ProcessedServices;
import de.theitshop.model.container.RunningContainer;
import de.theitshop.networking.ContainerNetwork;
import org.testcontainers.DockerClientFactory;

import java.util.*;
import java.util.stream.Collectors;

public class RunContainers {
    public ConfigServices configServices;
    public boolean isClassTargetRunning = false;
    public boolean isMethodTargetRunning = false;
    public Map<String, BaseContainer> startedContainers = new HashMap<>();

    private final ObjectMapper mapper = new ObjectMapper();
    private final ContainerConfig containerConfig = new ContainerConfig();
    private final ContainerNetwork testNetwork = new ContainerNetwork();
    private String configFileName = null;
    private static final String defaultConfigFileName = "testcompose-bootstrap";


    public RunContainers() {
        setConfigFileName(null);
        configServices = containerConfig.parseConfig(getConfigFileName());
    }

    public void setConfigFileName(String fileName){
        this.configFileName = (fileName != null && !fileName.isBlank() && !fileName.isEmpty()) ? fileName: defaultConfigFileName;
    }

    public String getConfigFileName(){
        return configFileName;
    }

    public List<OrderedService> runTestContainers(){
        List<OrderedService> orderedServices = containerConfig.rankConfigServices(
                Set.of(), mapper.convertValue(new ArrayList<OrderedService>(), new TypeReference<>() {}),
                configServices.getServices());

        Map<String, RunningContainer> runningContainerMap = new HashMap<>();
        ProcessedServices processedServices =  new ProcessedServices(runningContainerMap);

        List<OrderedService> services = orderedServices.stream().sorted().collect(Collectors.toList());
        for(OrderedService service: services){
            BaseContainer baseContainer = new BaseContainer.Builder()
                    .withTestService(service.getService(), processedServices)
                    .withTestNetwork(testNetwork.getContainerNetwork())
                    .build();
            baseContainer.startContainer();
            startedContainers.put(baseContainer.getRunningContainer().getServiceName(), baseContainer);
            runningContainerMap.put(baseContainer.getRunningContainer().getServiceName(), baseContainer.getRunningContainer());
            processedServices = new ProcessedServices(runningContainerMap);
        }
        return orderedServices;
    }

    public void stopTestContainers(List<OrderedService> orderedServices) {
        Collections.reverse(orderedServices);
        if (orderedServices.size() != 0 && startedContainers.size() != 0){
            orderedServices.forEach(c -> startedContainers.get(c.getService().getName()).stopContainer());
        }
    }

    public boolean isDockerRunning(){
        try{
            return DockerClientFactory.instance().isDockerAvailable();
        }catch (Exception exc){
            return false;
        }
    }
}
