package de.theitshop.model.container;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class ProcessedServices {
    private Map<String, RunningContainer>  processedServices;
}
