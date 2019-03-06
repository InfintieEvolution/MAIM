package AIS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static java.lang.Double.NaN;

public class Antibody {

    private double[] features;
    private double radius;
    private String label;
    private double fitness;
    private Antigen[] antigens;
    private HashMap<Antigen,Double> connectedAntigen;
    private HashMap<String, ArrayList<Antigen>> connectedAntigenOfLabel;
    private double boundAntigensCount;
    private double totalInteraction;
    private int correctClassificationCount;
    private boolean connectedAntigensSet;
    private double correctInteraction;
    private double accuracy;
    private AIS ais;

    public Antibody(double[] features, double radius, String label, Antigen[] antigens, AIS ais){
        this.features = features;
        this.radius = radius;
        this.label = label;
        this.fitness = 0.0;
        this.antigens = antigens;
        this.connectedAntigen = new HashMap<>();
        this.boundAntigensCount = 0;
        //this.totalInteraction = 0.0;
        this.correctClassificationCount = 0;
        this.connectedAntigensSet = false;
        this.ais = ais;
        this.accuracy = 0.0;
        this.correctInteraction = 0.0;
        this.connectedAntigenOfLabel = new HashMap<>();
    }

    public void setConnectedAntigens(){
        //Connected antigens has been calculated before, we only need to re-add the connected antigen
        if(this.connectedAntigensSet){
            for (Antigen antigen:connectedAntigen.keySet()){
                antigen.getConnectedAntibodies().add(this);
                antigen.setTotalInteraction(antigen.getTotalInteraction() + this.getConnectedAntigen().get(antigen));
                antigen.addInteraction(this, this.getConnectedAntigen().get(antigen));
            }
        }else{  //first time calculating fitness
            for (Antigen antigen:antigens){
                double distance = eucledeanDistance(this.features,antigen.getAttributes());
                if (distance <= this.radius) {
                    antigen.getConnectedAntibodies().add(this);
                    double weight = calcualteWeight(antigen,distance);
                    totalInteraction += weight;
                    connectedAntigen.put(antigen,weight);

                    //interaction of antigen
                    antigen.setTotalInteraction(antigen.getTotalInteraction() + weight);
                    antigen.addInteraction(this,weight);

                    this.connectedAntigensSet = true;
                    this.boundAntigensCount++;

                    if(connectedAntigenOfLabel.containsKey(antigen.getLabel())){
                        connectedAntigenOfLabel.get(antigen.getLabel()).add(antigen);
                    }else{
                        connectedAntigenOfLabel.put(antigen.getLabel(),new ArrayList<>(){{add(antigen);}});
                    }
                    if(antigen.getLabel().equals(this.label)){
                        correctClassificationCount +=1;
                        correctInteraction += weight;
                    }
                }
            }
        }
    }
    public double calcualteWeight(Antigen antigen,double distance){
        //double distance = this.getConnectedAntigen().get(antigen);
        double weight = 1/distance;

        return weight;
    }

    /*public void calculateFitness(){
        if(this.boundAntigensCount == 0){
            this.fitness = 0.0;
        }else{
            double accuracy = (double) correctClassificationCount/(boundAntigensCount);
            double sharingFactor = 0.0;
            double totalInteraction = 0.0;
            for(Antigen antigen: connectedAntigen.keySet()){
                double weight = calcualteWeight(antigen);
                totalInteraction+=weight;
                double totalAntibodyWeight = 0.0;
                for(Antibody antibody:antigen.getConnectedAntibodies()){
                    totalAntibodyWeight += antibody.calcualteWeight(antigen);
                }
                sharingFactor += Math.pow(weight,2)/totalAntibodyWeight;
            }
            //System.out.println(sharingFactor);
            this.fitness = ((accuracy *sharingFactor)/(totalInteraction));
        }
    }*/
    public void calculateFitness(){
        if(this.boundAntigensCount == 0){
            this.fitness = 0.0;
        }else{
            this.accuracy = (double) correctClassificationCount/(boundAntigensCount);
            double sharingFactor = 0.0;
            for(Antigen antigen: connectedAntigen.keySet()){
                double weight = connectedAntigen.get(antigen);
                //double totalInteractionAntigen = antigen.getTotalInteraction();
                //double totalAntibodyWeight = 0.0;

                sharingFactor += Math.pow(weight,2)/antigen.getInteractionMap().get(this.getAis());
                //sharingFactor += Math.pow(weight,2)/antigen.getTotalInteraction();

            }

            double sum = 0.0;

            for(String label: ais.getAntibodyMap().keySet()){
                sum += ais.getAntibodyMap().get(label).size();
            }
            double representation = ais.getAntibodyMap().get(this.label).size()/sum;
            double weightedAccuracy = (1 + correctInteraction) /(connectedAntigenOfLabel.keySet().size()+totalInteraction);
            //double weightedAccuracy = correctInteraction / totalInteraction;
            this.fitness = (sharingFactor*weightedAccuracy)/ totalInteraction;
            //this.fitness = (accuracy * (totalInteraction/boundAntigensCount));
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
            this.fitness = (sharingFactor*Math.pow(accuracy,2))/totalInteraction;
        }
    }
*/
    public double eucledeanDistance(double[] featureSet1, double[] featureSet2){

        double eucledeanDistance = 0.0;
        for (int i=0;i<featureSet1.length;i++){
            if(featureSet1[i] == -1.0 || featureSet2[i] == -1.0){
                continue;
            }
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

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public void setConnectedAntigen(HashMap<Antigen, Double> connectedAntigen) {
        this.connectedAntigen = connectedAntigen;
    }

    public double getTotalInteraction() {
        return totalInteraction;
    }

    public void setTotalInteraction(double totalInteraction) {
        this.totalInteraction = totalInteraction;
    }

    public void setAis(AIS ais) {
        this.ais = ais;
    }

    public AIS getAis() {
        return ais;
    }

    @Override
    public String toString() {
        return
                "features=" + Arrays.toString(features) +
                ", radius=" + radius +
                ", label='" + label + '\'';
    }
}
