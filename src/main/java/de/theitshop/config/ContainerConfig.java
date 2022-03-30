package de.theitshop.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.io.Files;
import de.theitshop.model.config.ConfigServices;
import de.theitshop.model.config.OrderedService;
import de.theitshop.model.config.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ContainerConfig {
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private static final String defaultConfigFileName = "testcompose-bootstrap";

    public InputStream readTestConfig(String configFileName){
        String fileName = (configFileName != null && !configFileName.isBlank() && !configFileName.isEmpty()) ? configFileName: defaultConfigFileName;
        String fileNameWithoutExtension = Files.getNameWithoutExtension(fileName);
        InputStream stream = this.getClass().getClassLoader().getResourceAsStream(fileNameWithoutExtension + ".yaml");
        if (stream == null){
            stream = this.getClass().getClassLoader().getResourceAsStream(fileNameWithoutExtension + ".yml");
            if (stream == null){
                throw new IllegalArgumentException("Config file not found!" + configFileName);
            }
        }
        return stream;
    }

    public ConfigServices parseConfig(InputStream stream) {
        ConfigServices configServices = null;
        yamlMapper.findAndRegisterModules();
        try{
            configServices = yamlMapper.readValue(stream, ConfigServices.class);
        }catch (IOException exc){
            exc.printStackTrace();
            throw new RuntimeException();
        }
        if (configServices == null)
            throw new RuntimeException("Config file content can not be null!");
        ConfigServices configServicesCopy= configServices;
        configServices.getServices().forEach(
                s -> {
                    if (!configServicesCopy.getServices().stream().map(Service::getName).collect(Collectors.toList()).containsAll(s.getDependsOn()))
                        throw new IllegalArgumentException("Dependent Service is not a Listed Service: " + s.getName());
                }
        );
        return configServices;
    }

    private void checkCyclicDependency(String serviceName, String dependentServiceName, List<OrderedService> processedServices, List<Service> unprocessedServices){
        Service neededService = null;
        for (Service s: unprocessedServices){
            if (s.getName().equalsIgnoreCase(dependentServiceName))
                neededService = s;
        }
        if (neededService == null){
            for (OrderedService s: processedServices){
                if (s.getService().getName().equalsIgnoreCase(dependentServiceName))
                    neededService = s.getService();
            }
        }
        assert neededService != null;
        if (neededService.getDependsOn().contains(serviceName))
            throw new IllegalArgumentException("Cyclic container relationship found for service "
                    + serviceName + " and service :" + neededService.getName());
    }

    public List<OrderedService> rankConfigServices(Set<String> serviceNames, List<OrderedService> processedServices, List<Service> unprocessedServices){
        if(unprocessedServices == null)
            throw new IllegalArgumentException("List of Services to be tested can not be null");
        if(unprocessedServices.size() == 0){
            if (serviceNames.size() != processedServices.size())
                throw new RuntimeException("Ordered Service improperly computed!");
            return processedServices;
        }else {
            Set<String> processedServiceNames = new HashSet<>(serviceNames);
            List<OrderedService> processedServicesCopy = new ArrayList<>(processedServices);
            int rank = processedServicesCopy.size();
            for (Service service: unprocessedServices){
                if (service.getDependsOn().size() == 0){
                    processedServiceNames.add(service.getName());
                    processedServicesCopy.add(new OrderedService(rank, service));
                    rank+=1;
                }else {
                    if (processedServiceNames.containsAll(service.getDependsOn())){
                        processedServiceNames.add(service.getName());
                        processedServicesCopy.add(new OrderedService(rank, service));
                        rank+=1;
                    } else
                        service.getDependsOn().forEach(dependentServiceName ->
                                checkCyclicDependency(service.getName(), dependentServiceName, processedServicesCopy, unprocessedServices));
                }
            }
            List<Service> copyOfUnprocessedServices = new ArrayList<>();
            unprocessedServices.forEach(
                    v -> {
                        if (!processedServiceNames.contains(v.getName())) copyOfUnprocessedServices.add(v);
                    }
            );
            processedServices.clear();
            processedServices.addAll(processedServicesCopy);
            return rankConfigServices(processedServiceNames, processedServices, copyOfUnprocessedServices);
        }
    }
}
