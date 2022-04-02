package de.theitshop;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.theitshop.config.ContainerConfig;
import de.theitshop.container.BaseContainer;
import de.theitshop.model.config.ConfigServices;
import de.theitshop.model.config.OrderedService;
import de.theitshop.model.config.Service;
import de.theitshop.model.container.ProcessedServices;
import de.theitshop.model.container.RunningContainer;
import de.theitshop.networking.ContainerNetwork;
import lombok.NonNull;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.Network;

import java.util.*;
import java.util.stream.Collectors;

public class RunContainers {
    private List<OrderedService> orderedServices;
    private Map<String, BaseContainer> baseContainerMap;
    private ConfigServices configServices;
    private final ObjectMapper mapper = new ObjectMapper();
    private final ContainerConfig containerConfig = new ContainerConfig();
    private Network testNetwork;

    public RunContainers() {
        setTestNetwork(null);
        setConfigServices(null);
        setOrderedServices(getConfigServices().getServices());
    }

    public RunContainers(@NonNull String configFileName, @NonNull Network testNetwork){
        setTestNetwork(testNetwork);
        setConfigServices(configFileName);
        setOrderedServices(getConfigServices().getServices());
    }

    private void setTestNetwork(Network network){
        this.testNetwork = network != null ? network : new ContainerNetwork().getContainerNetwork();
    }

    public Network getTestNetwork(){
        return testNetwork;
    }

    private void setOrderedServices(List<Service> services){
        this.orderedServices = containerConfig.rankConfigServices(
                        Set.of(), mapper.convertValue(new ArrayList<OrderedService>(), new TypeReference<>() {}),
                        services)
                .stream().sorted().collect(Collectors.toList());
    }

    public List<OrderedService> getOrderedServices(){
        return orderedServices;
    }

    private void setConfigServices(String configFileName){
        this.configServices = containerConfig.parseConfig(containerConfig.readTestConfig(configFileName));
    }

    public ConfigServices getConfigServices(){
        return configServices;
    }

    public Map<String, BaseContainer> getBaseContainerMap(){
        return baseContainerMap;
    }

    public void startTestContainers(){
        isDockerRunning();
        baseContainerMap = new HashMap<>();
        Map<String, RunningContainer> runningContainerMap = new HashMap<>();
        ProcessedServices processedServices =  new ProcessedServices(runningContainerMap);

        for(OrderedService os: getOrderedServices()){
            BaseContainer baseContainer = new BaseContainer.Builder(getTestNetwork())
                    .withTestService(os.getService(), processedServices)
                    .build();
            baseContainer.startContainer();
            baseContainerMap.put(baseContainer.getRunningContainer().getServiceName(), baseContainer);
            runningContainerMap.put(baseContainer.getRunningContainer().getServiceName(), baseContainer.getRunningContainer());
            processedServices = new ProcessedServices(runningContainerMap);
        }
    }

    public void stopTestContainers() {
        isDockerRunning();
        Collections.reverse(getOrderedServices());
        if (getOrderedServices().size() != 0 && baseContainerMap.size() != 0){
            getOrderedServices().forEach(c -> baseContainerMap.get(c.getService().getName()).stopContainer());
        }
        baseContainerMap.clear();
    }

    protected void isDockerRunning(){
        try{
            DockerClientFactory.instance().isDockerAvailable();
        }catch (Exception exc){
            exc.printStackTrace();
            throw new RuntimeException("Docker doesn't seem to be running on this machine");
        }
    }
}
