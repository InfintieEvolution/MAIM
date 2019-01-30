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
    //private double totalWeight;
    private int correctClassificationCount;
    private boolean connectedAntigensSet;
    private AIS ais;
    public Antibody(double[] features, double radius, String label, Antigen[] antigens, AIS ais){
        this.features = features;
        this.radius = radius;
        this.label = label;
        this.fitness = 0.0;
        this.antigens = antigens;
        this.connectedAntigen = new HashMap<>();
        this.boundAntigensCount = 0;
        //this.totalWeight = 0.0;
        this.correctClassificationCount = 0;
        this.connectedAntigensSet = false;
        this.ais = ais;
    }

    public void setConnectedAntigens(){
        //Connected antigens has been calculated before, we only need to re-add the connected antigen
        if(this.connectedAntigensSet){
            for (Antigen antigen:connectedAntigen.keySet()){
                antigen.getConnectedAntibodies().add(this);
            }
        }else{  //first time calculating fitness
            for (Antigen antigen:antigens){
                double distance = eucledeanDistance(this.features,antigen.getAttributes());
                if (distance <= this.radius) {
                    antigen.getConnectedAntibodies().add(this);
                    //totalWeight += distance;
                    connectedAntigen.put(antigen,distance);
                    this.connectedAntigensSet = true;
                    this.boundAntigensCount++;
                    if(antigen.getLabel().equals(this.label)){
                        correctClassificationCount +=1;
                    }
                }
            }
        }
    }
    public double calcualteWeight(Antigen antigen){
        double distance = this.getConnectedAntigen().get(antigen);
        double weight = 1/distance;

        return weight;
    }

    /*public void calculateFitness(){
        if(this.boundAntigensCount == 0){
            this.fitness = 0.0;
        }else{
            double accuracy = (double) correctClassificationCount/(boundAntigensCount);
            double sharingFactor = 0.0;
            double totalWeight = 0.0;
            for(Antigen antigen: connectedAntigen.keySet()){
                double weight = calcualteWeight(antigen);
                totalWeight+=weight;
                double totalAntibodyWeight = 0.0;
                for(Antibody antibody:antigen.getConnectedAntibodies()){
                    totalAntibodyWeight += antibody.calcualteWeight(antigen);
                }
                sharingFactor += Math.pow(weight,2)/totalAntibodyWeight;
            }
            //System.out.println(sharingFactor);
            this.fitness = ((accuracy *sharingFactor)/(totalWeight));
        }
    }*/
    public void calculateFitness(){
        if(this.boundAntigensCount == 0){
            this.fitness = 0.0;
        }else{
            double accuracy = (double) correctClassificationCount/(boundAntigensCount);
            double sharingFactor = 0.0;
            double totalWeight = 0.0;
            for(Antigen antigen: connectedAntigen.keySet()){
                double weight = calcualteWeight(antigen);
                totalWeight+=weight;
                double totalAntibodyWeight = 0.0;

                //sharing factor
                /*for(Antibody antibody:antigen.getConnectedAntibodies()){
                    totalAntibodyWeight += antibody.calcualteWeight(antigen);
                }*/
                sharingFactor += Math.pow(weight,2)/totalAntibodyWeight;
            }
            //System.out.println(sharingFactor);

            //this.fitness = (sharingFactor*accuracy)/totalWeight;
            this.fitness = (accuracy * (totalWeight/boundAntigensCount));
        }
    }
/*
    public void calculateFitness(){
        if(this.boundAntigensCount == 0){
            this.fitness = 0.0;
        }else{
            double accuracy = (double) correctClassificationCount/(boundAntigensCount);
            double sharingFactor = 0.0;
            for(Antigen antigen: connectedAntigen.keySet()){
                double weight = this.connectedAntigen.get(antigen);
                double totalAntibodyWeight = 0.0;
                for(Antibody antibody:antigen.getConnectedAntibodies()){
                    totalAntibodyWeight += antibody.connectedAntigen.get(antigen);
                }
                sharingFactor += weight/totalAntibodyWeight;
            }
            //System.out.println(sharingFactor);
            this.fitness = (sharingFactor*Math.pow(accuracy,2))/totalWeight;
        }
    }
*/
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
