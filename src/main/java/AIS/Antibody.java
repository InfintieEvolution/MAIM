package AIS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Antibody {

    private double[] features;
    private double radius;
    private double fitness;
    private Antigen[] antigens;
    private HashMap<Antigen,Double> connectedAntigen;
    private HashMap<String, ArrayList<Antigen>> connectedAntigenOfLabel;
    private double boundAntigensCount;
    private double totalInteraction;
    private boolean connectedAntigensSet;
    private double accuracy;
    private AIS ais;
    private double weightedAccuracy;
    private double[] featuresWeights;
    private HashMap<String,Double> classDistributionMap;
    private String label;
    public Antibody(double[] features, double radius, Antigen[] antigens, AIS ais, double[] featuresWeights){
        this.features = features;
        this.radius = radius;
        this.fitness = 0.0;
        this.antigens = antigens;
        this.connectedAntigen = new HashMap<>();
        this.boundAntigensCount = 0;
        this.connectedAntigensSet = false;
        this.ais = ais;
        this.accuracy = 0.0;
        this.connectedAntigenOfLabel = new HashMap<>();
        this.featuresWeights = new double[features.length];
        this.classDistributionMap = new HashMap<>();
        //setConnectedAntigens();
    }
    public void setConnectedAntigens(){
        //Connected antigens has been calculated before, we only need to re-add the connected antigen
        if(!this.connectedAntigensSet){  //first time calculating fitness
            double totalClassDistribution = 0.0;
            for (Antigen antigen:antigens){
                double distance = eucledeanDistance(this.features,antigen.getAttributes());
                if (distance <= this.radius) {
                    double weight = calcualteWeight(antigen,distance);
                    totalInteraction += weight;
                    connectedAntigen.put(antigen,weight);

                    this.connectedAntigensSet = true;
                    this.boundAntigensCount++;

                    if(connectedAntigenOfLabel.containsKey(antigen.getLabel())){
                        connectedAntigenOfLabel.get(antigen.getLabel()).add(antigen);
                    }else{
                        connectedAntigenOfLabel.put(antigen.getLabel(),new ArrayList<>(){{add(antigen);}});
                    }

                    //add to class distribution.
                    classDistributionMap.put(antigen.getLabel(), classDistributionMap.getOrDefault(antigen.getLabel(), 1.0) + weight);
                }
            }
            String mostRepresentedClass = null;
            double highestDistribution = 0.0;
            for(String label:classDistributionMap.keySet()){
                double distribution = classDistributionMap.get(label);
                if(distribution > highestDistribution){
                    highestDistribution = distribution;
                    mostRepresentedClass = label;
                }
                classDistributionMap.put(label,distribution/(classDistributionMap.keySet().size()+totalInteraction));
            }

            if(mostRepresentedClass == null){
                this.label = antigens[0].getLabel();
            }else{
                this.label = mostRepresentedClass;
            }
        }
    }

    public void setInteraction(){
        for (Antigen antigen:connectedAntigen.keySet()){
            antigen.getConnectedAntibodies().add(this);
            antigen.addInteraction(this.getConnectedAntigen().get(antigen)); //add interaction weight from this antibody to the connected antigen
            antigen.addInteraction(this, this.getConnectedAntigen().get(antigen));
        }
    }

    public double calcualteWeight(Antigen antigen,double distance){
        double weight = 1/distance;

        return weight;
    }

    public void calculateFitness(){
        if(this.boundAntigensCount == 0){
            this.fitness = 0.0;
        }else{
            double sharingFactor = 0.0;
            for(Antigen antigen: connectedAntigen.keySet()){
                double weight = connectedAntigen.get(antigen);
                sharingFactor += (Math.pow(weight,2)/(antigen.getTotalInteraction()))*classDistributionMap.get(antigen.getLabel()); //part of the antigen that belongs to the antibody
            }
            this.fitness = ((sharingFactor)/(totalInteraction));
        }
    }

    /**
     * Checks if a antibody is connected to any nearby antigen.
     * @return
     */
    public boolean isConnected(){
        for (Antigen antigen:antigens){
            double distance = eucledeanDistance(this.features,antigen.getAttributes());
            if (distance <= this.radius) {
                return true;
            }
        }
        return false;
    }

    public double calculateAffinity(){
        double affinity = 0.0;
        for (Antigen antigen:antigens){
            double distance = eucledeanDistance(this.features,antigen.getAttributes());
            if (distance <= this.radius) {
                affinity += 1/distance;
            }
        }
        return affinity;
    }

    /*public double checkAccuracy(){
        int totalClassifications = 0;
        int correctClassifications = 0;
        for (Antigen antigen:antigens){
            double distance = eucledeanDistance(this.features,antigen.getAttributes());
            if (distance <= this.radius) {
                if(antigen.getLabel().equals(this.getLabel())){
                    correctClassifications++;
                }
                totalClassifications ++;
            }
        }
        double accuracy = (double)correctClassifications/totalClassifications;
        this.accuracy = accuracy;
        return accuracy;
    }*/

    public double eucledeanDistance(double[] featureSet1, double[] featureSet2){

        double eucledeanDistance = 0.0;
        for (int i=0;i<featureSet1.length;i++){
            /*if(!featuresWeights[i]){
                continue;
            }*/
            eucledeanDistance += /*this.featuresWeights[i]**/(Math.pow(featureSet1[i] - featureSet2[i],2));
        }

        eucledeanDistance = Math.sqrt(eucledeanDistance);
        return eucledeanDistance;
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

    public Antigen[] getAntigens() {
        return antigens;
    }

    public void setAntigens(Antigen[] antigens) {
        this.antigens = antigens;
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

    public double getWeightedAccuracy() {
        return weightedAccuracy;
    }

    public void setWeightedAccuracy(double weightedAccuracy) {
        this.weightedAccuracy = weightedAccuracy;
    }

    public boolean isConnectedAntigensSet() {
        return connectedAntigensSet;
    }
    public void setConnectedAntigensSet(boolean connectedAntigensSet) {
        this.connectedAntigensSet = connectedAntigensSet;
    }
    public double[] getFeaturesWeights() {
        return featuresWeights;
    }

    public void setFeaturesWeights(double[] inactiveFeatures) {
        this.featuresWeights = inactiveFeatures;
    }

    public HashMap<String, Double> getClassDistributionMap() {
        return classDistributionMap;
    }

    public void setClassDistributionMap(HashMap<String, Double> classDistributionMap) {
        this.classDistributionMap = classDistributionMap;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return
                "features=" + Arrays.toString(features) +
                ", radius=" + radius;
    }
}
