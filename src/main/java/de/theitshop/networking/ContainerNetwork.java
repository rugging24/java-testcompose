package de.theitshop.networking;


import lombok.Getter;
import org.testcontainers.containers.Network;

@Getter
public class ContainerNetwork {
    private Network containerNetwork;

    public ContainerNetwork(){
        setContainerNetwork(Network.newNetwork());
    }

    private void setContainerNetwork(Network network){
        this.containerNetwork = network;
    }
}
