package de.theitshop;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.theitshop.config.ContainerConfig;
import de.theitshop.model.config.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static de.theitshop.helpers.ConfigHelper.*;
import static org.junit.jupiter.api.Assertions.*;


public class ConfigurationTest {
    @Test
    void configIsProperlyParsedAndValidated() throws RuntimeException {
        ContainerConfig wrongConfig = new ContainerConfig();
        assertThrows(IllegalArgumentException.class, () ->  wrongConfig.parseConfig(singleServiceConfigContent()));

        ContainerConfig config = new ContainerConfig();
        ConfigServices configServices = config.parseConfig(multiServiceConfigContent());
        assertEquals(4, configServices.getServices().size());

        Service service = configServices.getServices().get(1);
        assertEquals("database", service.getName());

        LogWaitParameter logWaitParameter = new LogWaitParameter(".*database system is ready to accept connections.*", 1);
        assertEquals(logWaitParameter, service.getLogWaitParameters());
        assertEquals(List.of(5432), service.getExposedPorts());
    }

    @Test
    void servicesAreCorrectlyRankedTest() throws RuntimeException {
        ObjectMapper mapper = new ObjectMapper();
        ContainerConfig config = new ContainerConfig();
        ConfigServices configServices = config.parseConfig(multiServiceConfigContent());
        List<OrderedService> orderedServices = config.rankConfigServices(
                Set.of(), mapper.convertValue(new ArrayList<OrderedService>(), new TypeReference<>() {}),
                configServices.getServices());
        assertEquals(0, orderedServices.get(0).getRank());
        assertEquals(1, orderedServices.get(1).getRank());
        assertEquals(2, orderedServices.get(2).getRank());
        assertEquals(3, orderedServices.get(3).getRank());
    }

    @Test
    void cyclicDependenciesAreDetected() throws RuntimeException{
        ObjectMapper mapper = new ObjectMapper();
        ContainerConfig config = new ContainerConfig();
        ConfigServices configServices = config.parseConfig(cyclicDependencyConfig());

        assertThrows(IllegalArgumentException.class, () -> config.rankConfigServices(
                Set.of(), mapper.convertValue(new ArrayList<OrderedService>(), new TypeReference<>() {}),
                configServices.getServices()));
    }

    @Test
    void volumeAreCorrectlyMappedTest() throws Exception {
        ContainerConfig config = new ContainerConfig();
        ConfigServices configServices = config.parseConfig(multiServiceConfigContent());
        Service applicationService = null;
        for (Service s: configServices.getServices()){
            if (s.getName().equalsIgnoreCase("application"))
                applicationService = s;
        }
        assertNotNull(applicationService);
        assertEquals(VolumeSourceType.RESOURCE_PATH, applicationService.getVolumes().get(0).getSource());
    }
}
