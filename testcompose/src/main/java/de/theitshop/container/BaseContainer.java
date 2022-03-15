package de.theitshop.container;

import de.theitshop.model.config.Service;
import de.theitshop.model.container.ProcessedServices;
import de.theitshop.model.container.RunningContainer;
import org.testcontainers.containers.Network;

public class BaseContainer {
    private final RunningContainer runningContainer;

    public static class Builder {
        private RunningContainer runningContainer;
        private Service service;
        private ProcessedServices processedServices;
        private Network containerNetwork;

        public Builder withTestService(Service service, ProcessedServices processedServices){
            this.service = service;
            this.processedServices = processedServices;
            return this;
        }

        public Builder withTestNetwork(Network containerNetwork) {
            this.containerNetwork = containerNetwork;
            return this;
        }

        public BaseContainer build(){
            ContainerInitializer initializer = new ContainerInitializerImp();
            runningContainer = initializer.getContainer(service, processedServices);
            runningContainer.getContainer().withNetwork(containerNetwork);
            return new BaseContainer(this);
        }
    }

    public BaseContainer(Builder builder) {
        this.runningContainer = builder.runningContainer;
    }

    public void startContainer(){
        if(runningContainer.getContainer() != null && !runningContainer.getContainer().isRunning()){
            runningContainer.getContainer().start();
        }
    }

    public void stopContainer(){
        if(runningContainer.getContainer() != null && runningContainer.getContainer().isRunning()){
            runningContainer.getContainer().stop();
        }
    }

    public RunningContainer getRunningContainer(){
        return runningContainer;
    }
}
