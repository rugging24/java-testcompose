package de.theitshop.container;

import de.theitshop.model.config.ExecCommandAfterContainerStartup;
import de.theitshop.model.container.ProcessedServices;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VariablePlaceholderUtils {
    private final Pattern pattern = Pattern.compile("\\$\\{\\{([^}]*)}}");
    private static final String openingBracket = "\\$\\{\\{";
    public static final String SELF = "self";
    private static final String CONTAINER_HOSTNAME = "container_hostname";
    private static final String MAPPED_PORT = "external_port";
    private static final String HOST_NETWORK_ALIAS = "container_host_alias";
    private static final String HOST_ADDRESS = "container_host_address";

    public Map.Entry<String, String> removeVariablePlaceholder(String serviceName, String key, String value,
                                                               ProcessedServices processedServices, GenericContainer<?> container){
        String replacementVariable = value;
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

                    if (processedServices != null && processedServices.getProcessedServices() != null && !processedServices.getProcessedServices().isEmpty()
                        && processedServices.getProcessedServices().containsKey(placeholderVariablePrefix)){
                        dependentServiceName = placeholderVariablePrefix;
                        dependentServiceContainer = processedServices.getProcessedServices().get(placeholderVariablePrefix).getContainer();
                        dependentServiceContainer.getEnvMap().forEach((k,v) -> dependentServiceEnvVariables.put(k.toLowerCase(), v));
                    } else if (container.isRunning()){
                        container.getEnvMap().forEach((k,v) -> dependentServiceEnvVariables.put(k.toLowerCase(), v));
                    }
                    replacementVariable = replacementVariable.replace(
                            matches.group(),
                            commonPlaceholders(placeholderVariablePrefix, placeholderDependentServiceEnvVariable,
                                    dependentServiceName,serviceName, dependentServiceContainer,
                                    dependentServiceEnvVariables, container)
                    );
                }
            }
        }
        return  Map.entry(key, replacementVariable);
    }

    private String commonPlaceholders(String placeholderPrefix, String placeholder, String dependentServiceName,
                                      String currentServiceName, GenericContainer<?> dependentServiceContainer,
                                      Map<String, String> dependentServiceEnvVariables, GenericContainer<?> container) {
        String variable;
        if (placeholderPrefix.equalsIgnoreCase(SELF) && placeholder.equalsIgnoreCase(CONTAINER_HOSTNAME)) {
            variable = currentServiceName;
        } else if (placeholderPrefix.equalsIgnoreCase(dependentServiceName) && placeholder.equalsIgnoreCase(CONTAINER_HOSTNAME)) {
            variable = dependentServiceName;
        } else if (placeholderPrefix.equalsIgnoreCase(dependentServiceName) && placeholder.toLowerCase().startsWith(MAPPED_PORT)) {
            String port = placeholder.toLowerCase().replace(MAPPED_PORT + "_", "");
            variable = dependentServiceContainer.getMappedPort(Integer.parseInt(port)).toString();
        } else if (placeholderPrefix.equalsIgnoreCase(SELF) && placeholder.toLowerCase().startsWith(MAPPED_PORT)) {
            int containerPort = Integer.parseInt(placeholder.toLowerCase().replace(MAPPED_PORT + "_", ""));
            variable = container.isRunning() ? String.valueOf(container.getMappedPort(containerPort)) : null;
        } else if (placeholderPrefix.equalsIgnoreCase(SELF) && placeholder.equalsIgnoreCase(HOST_NETWORK_ALIAS)){
            variable = container.isRunning() ? container.getNetworkAliases().get(0) : null;
        } else if (placeholderPrefix.equalsIgnoreCase(SELF) && placeholder.equalsIgnoreCase(HOST_ADDRESS)){
            variable = container.isRunning() ? container.getHost() : null;
        } else {
            variable = dependentServiceEnvVariables.get(placeholder);
        }
        return variable;
    }

    public void execAfterStartupCommand(String serviceName, List<ExecCommandAfterContainerStartup> commands,
                                        ProcessedServices processedServices, GenericContainer<?> container)
            throws IOException, InterruptedException {
        List<String> cmdToExec = new ArrayList<>();
        if (commands != null && commands.size() > 0){
            commands.forEach(cmd -> cmdToExec.add(removeVariablePlaceholder(serviceName,
                    cmd.getName(), cmd.getCommand(), processedServices, container).getValue()));

            final Pattern cmdSpaceSeparatorRegex = Pattern.compile("('.*?'|\\\".*?\\\"|\\S+)");
            for(String cmd: cmdToExec){
                Matcher match = cmdSpaceSeparatorRegex.matcher(cmd);
                List<String> matchedCmd = new ArrayList<>();
                while (match.find()) matchedCmd.add(match.group());
                Container.ExecResult result = container.execInContainer(matchedCmd.toArray(new String[0]));
                if (result.getExitCode() != 0) throw new IllegalStateException(result.getStderr());
            }
        }
    }
}
