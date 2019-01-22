package AIS;

public class Antibody {

    private double[] features;
    private double radius;
    private String label;

    public Antibody(double[] features, double radius, String label){
        this.features = features;
        this.radius = radius;
        this.label = label;
    }

    public double[] getFeatures() {
        return features;
    }

    public void setFeatures(double[] features) {
        this.features = features;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

}
