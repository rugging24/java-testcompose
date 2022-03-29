package de.theitshop;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.theitshop.config.ContainerConfig;
import de.theitshop.model.config.ConfigServices;
import de.theitshop.model.config.LogWaitParameter;
import de.theitshop.model.config.OrderedService;
import de.theitshop.model.config.Service;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class ConfigurationTest {
    private InputStream singleServiceConfigContent() {
        String content =
                "services:\n" +
                        "  - name: database\n" +
                        "    image: \"postgres:13\"\n" +
                        "    command: \"\"\n" +
                        "    environment:\n" +
                        "      POSTGRES_USER: postgres\n" +
                        "      POSTGRES_DB: postgres_db\n" +
                        "      POSTGRES_PASSWORD: password\n" +
                        "    exposed_ports:\n" +
                        "      - 5432\n" +
                        "    log_wait_parameters:\n" +
                        "      log_line_regex: \".*database system is ready to accept connections.*\"\n" +
                        "      log_line_regex_occurrence: 1\n" +
                        "    depends_on:\n" +
                        "      - zookeeper";
        return new ByteArrayInputStream(content.getBytes());
    }

    private InputStream multiServiceConfigContent(){
        String content =
                "services:\n" +
                        "  - name: kafka\n" +
                        "    image: confluentinc/cp-kafka:6.2.1\n" +
                        "    test_containers_module:\n" +
                        "      module_name: KafkaContainer\n" +
                        "      module_parameters:\n" +
                        "        zookeeper:\n" +
                        "          external: true\n" +
                        "          connection_string: \"${{zookeeper.container_hostname}}:${{zookeeper.zookeeper_client_port}}\"\n" +
                        "    environment:\n" +
                        "      KAFKA_BROKER_ID: 1\n" +
                        "      KAFKA_AUTO_CREATE_TOPICS_ENABLE: true\n" +
                        "      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1\n" +
                        "      KAFKA_OFFSETS_TOPIC_NUM_PARTITIONS: 1\n" +
                        "      KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS: 0\n" +
                        "    exposed_ports: # ignores this setting\n" +
                        "      - 9093\n" +
                        "    log_wait_parameters: # ignored\n" +
                        "      log_line_regex: \".*Ready to serve as the new controller.*\"\n" +
                        "      log_line_regex_occurrence: 1\n" +
                        "    depends_on:\n" +
                        "      - zookeeper\n" +
                        "  - name: database\n" +
                        "    image: \"postgres:13\"\n" +
                        "    command: \"\"\n" +
                        "    environment:\n" +
                        "      POSTGRES_USER: postgres\n" +
                        "      POSTGRES_DB: postgres_db\n" +
                        "      POSTGRES_PASSWORD: password\n" +
                        "    exposed_ports:\n" +
                        "      - 5432\n" +
                        "    log_wait_parameters:\n" +
                        "      log_line_regex: \".*database system is ready to accept connections.*\"\n" +
                        "      log_line_regex_occurrence: 1\n" +
                        "  - name: application\n" +
                        "    image: \"python:3.9\"\n" +
                        "    command: \"/bin/bash -x /run_app.sh\"\n" +
                        "    environment:\n" +
                        "      DB_URL: \"${{database.postgres_user}}:${{database.postgres_password}}@${{database.container_hostname}}:5432/${{database.postgres_db}}\"\n" +
                        "      KAFKA_BOOTSTRAP_SERVERS: \"${{kafka.container_hostname}}:9092\"\n" +
                        "      KAFKA_OFFSET_RESET: \"earliest\"\n" +
                        "      KAFKA_TOPIC: \"test_kafka_topic\"\n" +
                        "    exposed_ports:\n" +
                        "      - 8000\n" +
                        "    volumes:\n" +
                        "      - host: \"docker-test-files/run_app.sh\"\n" +
                        "        container: \"/run_app.sh\"\n" +
                        "        mode: \"ro\"\n" +
                        "        source: \"resources\"\n" +
                        "      - host: \"docker-test-files/app.py\"\n" +
                        "        container: \"/app.py\"\n" +
                        "        mode: \"ro\"\n" +
                        "        source: \"resources\"\n" +
                        "    log_wait_parameters:\n" +
                        "      log_line_regex: \".*Application startup complete.*\"\n" +
                        "      log_line_regex_occurrence: 1\n" +
                        "    http_wait_parameters:\n" +
                        "      http_port: \"8000\"\n" +
                        "      response_status_code: 200\n" +
                        "      end_point: \"/ping\"\n" +
                        "    depends_on:\n" +
                        "      - database\n" +
                        "      - kafka\n" +
                        "  - name: zookeeper\n" +
                        "    image: confluentinc/cp-zookeeper:6.2.1\n" +
                        "    environment:\n" +
                        "      ZOOKEEPER_CLIENT_PORT: 2181\n" +
                        "      ZOOKEEPER_TICK_TIME: 2000\n" +
                        "    log_wait_parameters:\n" +
                        "      log_line_regex: \".*Started AdminServer on address.*\"\n" +
                        "      log_line_regex_occurrence: 1\n" +
                        "    exposed_ports:\n" +
                        "      - 2181";
        return new ByteArrayInputStream(content.getBytes());
    }

    private InputStream cyclicConfig(){
        String content =
                "services:\n" +
                        "  - name: database\n" +
                        "    image: \"postgres:13\"\n" +
                        "    command: \"\"\n" +
                        "    environment:\n" +
                        "      POSTGRES_USER: postgres\n" +
                        "      POSTGRES_DB: postgres_db\n" +
                        "      POSTGRES_PASSWORD: password\n" +
                        "    exposed_ports:\n" +
                        "      - 5432\n" +
                        "    log_wait_parameters:\n" +
                        "      log_line_regex: \".*database system is ready to accept connections.*\"\n" +
                        "      log_line_regex_occurrence: 1\n" +
                        "    depends_on:\n" +
                        "      - zookeeper\n" +
                        "  - name: zookeeper\n" +
                        "    image: confluentinc/cp-zookeeper:6.2.1\n" +
                        "    environment:\n" +
                        "      ZOOKEEPER_CLIENT_PORT: 2181\n" +
                        "      ZOOKEEPER_TICK_TIME: 2000\n" +
                        "    log_wait_parameters:\n" +
                        "      log_line_regex: \".*Started AdminServer on address.*\"\n" +
                        "      log_line_regex_occurrence: 1\n" +
                        "    exposed_ports:\n" +
                        "      - 2181\n" +
                        "    depends_on:\n" +
                        "      - database";
        return new ByteArrayInputStream(content.getBytes());
    }

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
        ConfigServices configServices = config.parseConfig(cyclicConfig());

        assertThrows(IllegalArgumentException.class, () -> config.rankConfigServices(
                Set.of(), mapper.convertValue(new ArrayList<OrderedService>(), new TypeReference<>() {}),
                configServices.getServices()));
    }
}
