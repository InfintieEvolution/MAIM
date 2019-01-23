package AIS;

import java.util.Arrays;

public class Antibody {

    private double[] features;
    private double radius;
    private String label;
    private double fitness;

    public Antibody(double[] features, double radius, String label){
        this.features = features;
        this.radius = radius;
        this.label = label;
        this.fitness = 0.0;
    }

    public void calculateFitness(Antigen[] antigens){
        double fitness = 0.0;

        int i=0;
        for (Antigen antigen:antigens){
          double distance = eucledeanDistance(this.features,antigen.getAttributes());

          if (distance > this.radius){
              //return;
          }else{
              System.out.println(distance);
              i++;
          }
        }
        System.out.println(i);
        this.fitness = fitness;
    }

    public double eucledeanDistance(double[] featureSet1, double[] featureSet2){

        double eucledeanDistance = 0.0;
        for (int i=0;i<featureSet1.length;i++){
            eucledeanDistance += Math.pow(featureSet1[i] - featureSet2[i],2);
        }

        eucledeanDistance = Math.sqrt(eucledeanDistance);
        return eucledeanDistance;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
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

    @Override
    public String toString() {
        return
                "features=" + Arrays.toString(features) +
                ", radius=" + radius +
                ", label='" + label + '\'';
    }
}
