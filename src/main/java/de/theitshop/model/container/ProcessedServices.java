package de.theitshop.model.container;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessedServices {
    private Map<String, RunningContainer>  processedServices;
}
