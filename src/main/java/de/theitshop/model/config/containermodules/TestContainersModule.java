package de.theitshop.model.config.containermodules;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.theitshop.model.config.containermodules.kafka.Zookeeper;
import lombok.Getter;
import lombok.ToString;

@Getter(onMethod_={@JsonIgnore})
@ToString
public class TestContainersModule {
    private static final String MODULE_NAME_FIELD = "module_name";
    private static final String MODULE_PARAMETERS_FIELD = "module_parameters";
    @JsonIgnore
    private final ObjectMapper mapper = new ObjectMapper();

    @JsonProperty(MODULE_NAME_FIELD)
    private final String moduleName;

    @JsonProperty(MODULE_PARAMETERS_FIELD)
    private final ContainerModuleParameters moduleParameters;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public TestContainersModule(@JsonProperty(MODULE_NAME_FIELD) String moduleName,
                                @JsonProperty(MODULE_PARAMETERS_FIELD) Object moduleParameters){
        this.moduleName = moduleName;
        switch (SupportedModules.getModules(moduleName)){
            case KAFKA:
                this.moduleParameters = mapper.convertValue(moduleParameters, new TypeReference<Zookeeper>(){});
                break;
            default:
                this.moduleParameters = null;
                break;
        }
    }
}
