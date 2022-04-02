package de.theitshop.model.config.containermodules;

import java.util.HashMap;
import java.util.Map;

public enum SupportedModules {
    KAFKA("kafkacontainer");

    private final String moduleName;
    SupportedModules(String moduleName){
        this.moduleName = moduleName;
    }

    private static final Map<String, SupportedModules> modules = new HashMap<>();

    static {
        for (SupportedModules m: values()){
            modules.put(m.moduleName.toLowerCase(), m);
        }
    }

    public static SupportedModules getModules(String moduleName){
        return modules.get(moduleName.toLowerCase());
    }
}
