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
import java.util.*;

public class ContainerConfig {
    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    public ConfigServices parseConfig(String configFileName) {
        String fileNameWithoutExtension = Files.getNameWithoutExtension(configFileName);
        InputStream stream = this.getClass().getClassLoader().getResourceAsStream(fileNameWithoutExtension + ".yaml");
        if (stream == null){
            stream = this.getClass().getClassLoader().getResourceAsStream(fileNameWithoutExtension + ".yml");
            if (stream == null){
                throw new IllegalArgumentException("Config file not found!" + configFileName);
            }
        }
        ConfigServices configServices = null;

        yamlMapper.findAndRegisterModules();
        try{
            configServices = yamlMapper.readValue(stream, ConfigServices.class);
        }catch (IOException exc){
            // ToDo; provide proper logging utils
            System.out.println(exc.getMessage());
        }
        if (configServices == null){
            throw new RuntimeException("Config file content can not be null!");
        }
        return configServices;
    }

    public List<OrderedService> rankConfigServices(Set<String> serviceNames, List<OrderedService> processedServices, List<Service> unprocessedServices){
        if(unprocessedServices == null){
            throw new IllegalArgumentException("List of Services to be tested can not be null");
        }
        if(unprocessedServices.size() == 0){
            if (serviceNames.size() != processedServices.size()){
                throw new RuntimeException("Ordered Service improperly computed!");
            }
            return jsonMapper.convertValue(processedServices, new TypeReference<>() {
            });
        }else {
            Set<String> processedServiceNames = new HashSet<>(serviceNames);
            Set<String> passes = new HashSet<>();
            List<OrderedService> tempProcessedServiceSet = processedServices;
            int rank = tempProcessedServiceSet.size();
            for (Service service: unprocessedServices){
                passes.add(service.getName());
                if (service.getDependsOn().size() == 0){
                    processedServiceNames.add(service.getName());
                    tempProcessedServiceSet.add(jsonMapper.convertValue(new OrderedService(rank, service), OrderedService.class));
                }else {
                    Set<String> passesCopy = new HashSet<>(passes);
                    if (processedServiceNames.containsAll(service.getDependsOn())){
                        processedServiceNames.add(service.getName());
                        tempProcessedServiceSet.add(jsonMapper.convertValue(new OrderedService(rank, service), OrderedService.class));
                    } else if (!processedServiceNames.containsAll(service.getDependsOn()) && passesCopy.retainAll(service.getDependsOn())){
                        throw new IllegalArgumentException("Cyclic container relationship found for " + service.getName());
                    }
                }
                rank+=1 ;
            }
            List<Service> copyOfUnprocessedServices = new ArrayList<>();
            unprocessedServices.forEach(
                    v -> {
                        if (!processedServiceNames.contains(v.getName())){
                            copyOfUnprocessedServices.add(v);
                        }
                    }
            );
            processedServices = jsonMapper.convertValue(tempProcessedServiceSet, new TypeReference<>() {});
            return rankConfigServices(processedServiceNames, processedServices, copyOfUnprocessedServices);
        }
    }
}
