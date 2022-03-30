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
    private Map<String, BaseContainer> baseContainerMap;
    private ConfigServices configServices;
    private final ObjectMapper mapper = new ObjectMapper();
    private final ContainerConfig containerConfig = new ContainerConfig();
    private final ContainerNetwork testNetwork = new ContainerNetwork();
    private final String configFileName;


    public RunContainers() {
        this.configFileName = null;
        setConfigServices();
    }

    public RunContainers(String configFileName){
        this.configFileName = configFileName;
        setConfigServices();
    }

    private void setConfigServices(){
        this.configServices = containerConfig.parseConfig(containerConfig.readTestConfig(configFileName));
    }

    public ConfigServices getConfigServices(){
        return configServices;
    }

    public Map<String, BaseContainer> getBaseContainerMap(){
        return baseContainerMap;
    }

    public List<OrderedService> startTestContainers(){
        isDockerRunning();
        baseContainerMap = new HashMap<>();
        List<OrderedService> orderedServices = containerConfig.rankConfigServices(
                Set.of(), mapper.convertValue(new ArrayList<OrderedService>(), new TypeReference<>() {}),
                getConfigServices().getServices())
                .stream().sorted().collect(Collectors.toList());

        Map<String, RunningContainer> runningContainerMap = new HashMap<>();
        ProcessedServices processedServices =  new ProcessedServices(runningContainerMap);

        for(OrderedService os: orderedServices){
            BaseContainer baseContainer = new BaseContainer.Builder()
                    .withTestService(os.getService(), processedServices)
                    .withTestNetwork(testNetwork.getContainerNetwork())
                    .build();
            baseContainer.startContainer();
            baseContainerMap.put(baseContainer.getRunningContainer().getServiceName(), baseContainer);
            runningContainerMap.put(baseContainer.getRunningContainer().getServiceName(), baseContainer.getRunningContainer());
            processedServices = new ProcessedServices(runningContainerMap);
        }
        return orderedServices;
    }

    public void stopTestContainers(List<OrderedService> orderedServices) {
        isDockerRunning();
        Collections.reverse(orderedServices);
        if (orderedServices.size() != 0 && baseContainerMap.size() != 0){
            orderedServices.forEach(c -> baseContainerMap.get(c.getService().getName()).stopContainer());
        }
        baseContainerMap.clear();
    }

    private void isDockerRunning(){
        try{
            DockerClientFactory.instance().isDockerAvailable();
        }catch (Exception exc){
            exc.printStackTrace();
            throw new RuntimeException("Docker doesn't seem to be running on this machine");
        }
    }
}
