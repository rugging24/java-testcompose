package de.theitshop.container;

import de.theitshop.model.container.ProcessedServices;
import org.testcontainers.containers.GenericContainer;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class VariablePlaceholderUtils {
    private final Pattern pattern = Pattern.compile("\\$\\{\\{([^}]*)}}");
    private static final String openingBracket = "\\$\\{\\{";
    private String externalFixedPort;

    public Map.Entry<String, String> removeVariablePlaceholder(String serviceName, String key, String value, ProcessedServices processedServices){
        String replacementVariable = value;
        setExternalFixedPort(null);
        if (replacementVariable.contains("${{")) {
            Matcher matches = pattern.matcher(replacementVariable);
            while (matches.find()) {
                String matchedGroup = matches.group();
                if (matchedGroup.startsWith("${{") && matchedGroup.endsWith("}}")) {
                    matchedGroup = matchedGroup.replaceFirst(openingBracket, "");
                    matchedGroup = matchedGroup.substring(0, matchedGroup.length() - 2);
                    List<String> tempVariable = Arrays.asList(matchedGroup.split("\\."));
                    if(tempVariable.size() != 2){
                        throw new IllegalArgumentException(
                                "Placeholder variable should be of the format serviceName.placeholder"
                        );
                    }

                    GenericContainer<?> dependentServiceContainer = null;
                    String dependentServiceName = null;
                    Map<String, String> dependentServiceEnvVariables = new HashMap<>();

                    String placeholderVariablePrefix = tempVariable.get(0).toLowerCase();
                    String placeholderDependentServiceEnvVariable = tempVariable.get(1).toLowerCase();

                    if (processedServices.getProcessedServices() != null && !processedServices.getProcessedServices().isEmpty()){
                        if(!placeholderVariablePrefix.equalsIgnoreCase("self")){
                            dependentServiceName = placeholderVariablePrefix;
                            dependentServiceContainer = processedServices.getProcessedServices().get(placeholderVariablePrefix).getContainer();
                            dependentServiceContainer.getEnv().forEach(v -> {
                                        List<String> envVar= Arrays.asList(v.split("="));
                                        if (envVar.size() > 0) {
                                            dependentServiceEnvVariables.put(envVar.get(0).toLowerCase(), envVar.get(1).toLowerCase());
                                        }
                                    });
                        }

                        replacementVariable = replacementVariable.replace(
                                matches.group(),
                                commonPlaceholders(placeholderVariablePrefix, placeholderDependentServiceEnvVariable,
                                        dependentServiceName,serviceName, dependentServiceContainer,
                                        dependentServiceEnvVariables)
                        );
                    }
                }
            }
        }
        return  Map.entry(key, replacementVariable);
    }

    private void setExternalFixedPort(String port){
        this.externalFixedPort = port;
    }

    public String getExternalFixedPort() {
        return externalFixedPort;
    }

    private String commonPlaceholders(String placeholderPrefix, String placeholder, String dependentServiceName,
                                      String currentServiceName, GenericContainer<?> dependentServiceContainer,
                                      Map<String, String> dependentServiceEnvVariables) {
        String variable = null;
        if (placeholderPrefix.equalsIgnoreCase("self") && placeholder.equalsIgnoreCase("container_hostname")) {
            variable = currentServiceName;
        } else if (placeholderPrefix.equalsIgnoreCase(dependentServiceName) && placeholder.equalsIgnoreCase("container_hostname")) {
            variable = dependentServiceName;
        } else if (placeholderPrefix.equalsIgnoreCase(dependentServiceName) && placeholder.toLowerCase().startsWith("external_port")) {
            String port = placeholder.toLowerCase().replace("external_port_", "");
            variable = dependentServiceContainer.getMappedPort(Integer.parseInt(port)).toString();
        } else if (placeholderPrefix.equalsIgnoreCase("self") && placeholder.toLowerCase().startsWith("external_port")) {
            int hostPort = 0;
            String containerPort = placeholder.toLowerCase().replace("external_port_", "");
            try(ServerSocket serverSocket = new ServerSocket(0)){
                hostPort = serverSocket.getLocalPort();
            }catch (IOException exc){
                System.out.println(exc.getMessage());
            }
            variable = String.valueOf(hostPort);
            setExternalFixedPort(variable + "<=>" + containerPort);
        } else {
            variable = dependentServiceEnvVariables.get(placeholder);
        }
        return variable;
    }
}
