package de.theitshop;

import de.theitshop.model.config.OrderedService;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.List;

public final class RunContainersByMethod extends RunContainers implements BeforeEachCallback, AfterEachCallback {

    private List<OrderedService> containerOrderedServices;
    /**
     * Callback that is invoked <em>after</em> an individual test and any
     * user-defined teardown methods for that test have been executed.
     *
     * @param context the current extension context; never {@code null}
     */
    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        if (isDockerRunning() && containerOrderedServices != null){
            stopTestContainers(containerOrderedServices);
            containerOrderedServices.clear();
        }else {
            throw new RuntimeException("Docker client is not available !!!");
        }
    }

    /**
     * Callback that is invoked <em>before</em> an individual test and any
     * user-defined setup methods for that test have been executed.
     *
     * @param context the current extension context; never {@code null}
     */
    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        if (isDockerRunning()){
            containerOrderedServices = runTestContainers();
        }else {
            throw new RuntimeException("Docker client is not available !!!");
        }
    }
}
