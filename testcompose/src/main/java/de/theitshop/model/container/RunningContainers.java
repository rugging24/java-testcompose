package de.theitshop.model.container;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class RunningContainers {
    private List<RunningContainer> runningContainers;
}
