package AIS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Antibody {

    private double[] features;
    private double radius;
    private String label;
    private double fitness;
    private Antigen[] antigens;
    private HashMap<Antigen,Double> connectedAntigen;
    private double boundAntigensCount;
    private double totalWeight;
    private int correctClassificationCount;
    public Antibody(double[] features, double radius, String label, Antigen[] antigens){
        this.features = features;
        this.radius = radius;
        this.label = label;
        this.fitness = 0.0;
        this.antigens = antigens;
        this.connectedAntigen = new HashMap<>();
        this.boundAntigensCount = 0;
        this.totalWeight = 0.0;
        this.correctClassificationCount = 0;
    }

    public void setConnectedAntigens(){
        for (Antigen antigen:antigens){
            double distance = eucledeanDistance(this.features,antigen.getAttributes());
            if (distance <= this.radius) {
                antigen.getConnectedAntibodies().add(this);
                totalWeight += distance;
                if(this.fitness == 0.0){
                    connectedAntigen.put(antigen,distance);
                }
                this.boundAntigensCount++;
                if(antigen.getLabel().equals(this.label)){
                    correctClassificationCount +=1;
                }
            }
        }
    }

    public void calculateFitness(){
        if(this.boundAntigensCount == 0){
            this.fitness = 0.0;
        }else{
            double accuracy = (double) correctClassificationCount/(boundAntigensCount);
            double sharingFactor = 0.0;
            for(Antigen antigen: connectedAntigen.keySet()){
                sharingFactor += antigen.getConnectedAntibodies().size();
            }
            //System.out.println(sharingFactor);
            this.fitness = (accuracy * (boundAntigensCount/totalWeight));
        }
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

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public HashMap<Antigen, Double> getConnectedAntigen() {
        return connectedAntigen;
    }

    public void setConnectedAntigen(HashMap<Antigen, Double> connectedAntigen) {
        this.connectedAntigen = connectedAntigen;
    }

    @Override
    public String toString() {
        return
                "features=" + Arrays.toString(features) +
                ", radius=" + radius +
                ", label='" + label + '\'';
    }
}
