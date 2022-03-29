package de.theitshop.model.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderedService implements Serializable, Comparable<OrderedService> {
    private Integer rank;
    private Service service;

    @Override
    public int compareTo(@NotNull OrderedService service) {
        return this.rank - service.rank;
    }
}
