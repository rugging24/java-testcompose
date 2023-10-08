package de.theitshop.container;

import de.theitshop.model.config.ExecCommandAfterContainerStartup;
import de.theitshop.model.config.Service;
import de.theitshop.model.container.ProcessedServices;
import de.theitshop.model.container.RunningContainer;
import lombok.Getter;
import lombok.NonNull;
import org.testcontainers.containers.Network;

import java.io.IOException;
import java.util.List;

public class BaseContainer {
    @Getter
    private final RunningContainer runningContainer;
    private final List<ExecCommandAfterContainerStartup> execCommandAfterContainerStartup;
    private final ProcessedServices processedServices;

    public static class Builder {
        private RunningContainer runningContainer;
        private Service service;
        private ProcessedServices processedServices;
        private final Network containerNetwork;
        private List<ExecCommandAfterContainerStartup> execCommandAfterContainerStartup;

        public Builder(@NonNull Network containerNetwork){
            this.containerNetwork = containerNetwork;
        }

        public Builder withTestService(@NonNull Service service, @NonNull ProcessedServices processedServices){
            this.service = service;
            this.processedServices = processedServices;
            this.execCommandAfterContainerStartup = service.getExecCommandAfterContainerStartup();
            return this;
        }

        public BaseContainer build(){
            ContainerInitializer initializer = new ContainerInitializerImp();
            runningContainer = initializer.getContainer(containerNetwork, service, processedServices);
            return new BaseContainer(this);
        }
    }

    public BaseContainer(Builder builder) {
        this.runningContainer = builder.runningContainer;
        this.execCommandAfterContainerStartup = builder.execCommandAfterContainerStartup;
        this.processedServices = builder.processedServices;
    }

    public void startContainer() {
        if(runningContainer.getContainer() != null && !runningContainer.getContainer().isRunning()){
            runningContainer.getContainer().start();
            try{
                new VariablePlaceholderUtils().execAfterStartupCommand(
                        runningContainer.getServiceName(), execCommandAfterContainerStartup,
                        processedServices, runningContainer.getContainer()
                );
            }catch (IOException | InterruptedException exc){
                //noinspection CallToPrintStackTrace
                exc.printStackTrace();
                throw new RuntimeException("Can not execute command after container startup");
            }
        }
    }

    public void stopContainer(){
        if(runningContainer.getContainer() != null && runningContainer.getContainer().isRunning())
            runningContainer.getContainer().stop();
    }
}
