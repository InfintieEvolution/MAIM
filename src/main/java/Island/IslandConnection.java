package Island;

public class IslandConnection {

    private Island sendToIsland;
    private Island receiveFromIsland;

    public IslandConnection(Island sendingIsland, Island receivingIsland) {
        this.sendToIsland = sendingIsland;
        this.receiveFromIsland = receivingIsland;
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

    @Override
    public String toString() {
        return "IslandConnection{" +
                "sendToIsland=" + sendToIsland +
                ", receiveFromIsland=" + receiveFromIsland +
                '}';
    }
}
