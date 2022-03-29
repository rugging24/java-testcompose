package de.theitshop;

import de.theitshop.model.config.OrderedService;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.List;

public final class RunContainersByClass extends RunContainers
        implements BeforeAllCallback, AfterAllCallback {

    private List<OrderedService> containerOrderedServices;
    /**
     * Callback that is invoked once <em>after</em> all tests in the current
     * container.
     *
     * @param context the current extension context; never {@code null}
     */
    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        if (isDockerRunning() && containerOrderedServices!= null){
            stopTestContainers(containerOrderedServices);
            containerOrderedServices.clear();
        }else {
            throw new RuntimeException("Docker client is not available !!!");
        }
    }

    /**
     * Callback that is invoked once <em>before</em> all tests in the current
     * container.
     *
     * @param context the current extension context; never {@code null}
     */
    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        if (isDockerRunning()){
            containerOrderedServices = runTestContainers();
        }else {
            throw new RuntimeException("Docker client is not available !!!");
        }
    }
}
