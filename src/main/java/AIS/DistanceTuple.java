package AIS;

public class DistanceTuple {

    private double distance;
    private Antibody antibody;

    public DistanceTuple(double distance, Antibody antibody) {
        this.distance = distance;
        this.antibody = antibody;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public Antibody getAntibody() {
        return antibody;
    }

    public void setAntibody(Antibody antibody) {
        this.antibody = antibody;
    }
}
