package Island;

public class IslandConnection {

    private Island sendToIsland;
    private Island receiveFromIsland;
    private Island masterIsland;

    public IslandConnection(Island sendingIsland, Island receivingIsland, Island masterIsland) {
        this.sendToIsland = sendingIsland;
        this.receiveFromIsland = receivingIsland;
        this.masterIsland = masterIsland;
    }

    /**
     * The island current island is sending migrants to.
     * @return Island
     */
    public Island getSendToIsland() {
        return sendToIsland;
    }

    /**
     * This island current island is receiving immigrants from.
     * @return Island
     */
    public Island getReceiveFromIsland() {
        return receiveFromIsland;
    }

    public Island getMasterIsland() {
        return masterIsland;
    }

    @Override
    public String toString() {
        return "IslandConnection{" +
                "sendToIsland=" + sendToIsland +
                ", receiveFromIsland=" + receiveFromIsland +
                ", masterIsland=" + masterIsland +
                '}';
    }
}
