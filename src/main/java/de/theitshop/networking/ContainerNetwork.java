package de.theitshop.networking;


import org.testcontainers.containers.Network;

public class ContainerNetwork {
    private Network containerNetwork;

    public ContainerNetwork(){
        setContainerNetwork(Network.newNetwork());
    }

    private void setContainerNetwork(Network network){
        this.containerNetwork = network;
    }

    public Network getContainerNetwork(){
        return containerNetwork;
    }
}
