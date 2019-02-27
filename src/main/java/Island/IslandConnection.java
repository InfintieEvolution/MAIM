package Island;

public class IslandConnection {

    private Island sendingIsland;
    private Island receivingIsland;

    public IslandConnection(Island sendingIsland, Island receivingIsland) {
        this.sendingIsland = sendingIsland;
        this.receivingIsland = receivingIsland;
    }

    public Island getSendingIsland() {
        return sendingIsland;
    }

    public Island getReceivingIsland() {
        return receivingIsland;
    }
}
