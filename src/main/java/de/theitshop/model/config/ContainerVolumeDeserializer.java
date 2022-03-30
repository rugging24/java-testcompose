package de.theitshop.model.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import org.testcontainers.containers.BindMode;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ContainerVolumeDeserializer extends JsonDeserializer<ContainerVolume> {
    private static final String HOST = "host";
    private static final String SOURCE = "source";
    private static final String CONTAINER = "container";
    private static final String MODE = "mode";
    private static final String VOLUME_RW_ACCESS = "rw";
    private static final String VOLUME_RO_ACCESS = "ro";
    private static final String FILE_MOUNT_SRC = "filesystem";
    private static final String RESOURCE_MOUNT_SRC = "resources";


    @Override
    public ContainerVolume deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
        JsonNode node = parser.getCodec().readTree(parser);
        String host = checkHostPath(node.get(HOST), node.get(SOURCE));
        String container = node.get(CONTAINER) != null ? node.get(CONTAINER).asText() : "/";
        BindMode mode = checkFileMode(node.get(MODE));
        VolumeSourceType source = checkFileSources(node.get(SOURCE));
        ContainerVolume containerVolume = new ContainerVolume();
        containerVolume.setHost(host);
        containerVolume.setContainer(container);
        containerVolume.setMode(mode);
        containerVolume.setSource(source);
        return containerVolume;
    }

    private String checkHostPath(JsonNode hostNode, JsonNode sourceNode){
        if (sourceNode.asText().equalsIgnoreCase(FILE_MOUNT_SRC)) {
            if ((new File(hostNode.asText())).exists()) return hostNode.asText();
            else throw new IllegalArgumentException("The File:" + hostNode.asText() + " does not exists");
        }else {
            InputStream stream = this.getClass().getClassLoader().getResourceAsStream(hostNode.asText());
            if (stream == null) throw new IllegalArgumentException("The File:" + hostNode.asText() + " does not exists");
            return hostNode.asText();
        }
    }
    private VolumeSourceType checkFileSources(JsonNode node){
        if (node == null) return VolumeSourceType.FILESYSTEM_PATH;
        if (node.asText().equalsIgnoreCase(RESOURCE_MOUNT_SRC)) return VolumeSourceType.RESOURCE_PATH;
        else return VolumeSourceType.FILESYSTEM_PATH;
    }

    private BindMode checkFileMode(JsonNode node){
        if (node == null) return VolumeMode.READ_ONLY.mode;
        if (node.asText().equalsIgnoreCase(VOLUME_RW_ACCESS))
            return VolumeMode.READ_WRITE.mode;
        else
            return VolumeMode.READ_ONLY.mode;
    }
}
