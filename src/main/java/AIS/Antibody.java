package AIS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

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
    private double weightedAccuracy;
    private double[] featuresWeights;

    public Antibody(double[] features, double radius, String label, Antigen[] antigens, AIS ais, double[] featuresWeights){
        this.features = features;
        this.radius = radius;
        this.label = label;
        this.fitness = 0.0;
        this.antigens = antigens;
        this.connectedAntigen = new HashMap<>();
        this.boundAntigensCount = 0;
        this.correctClassificationCount = 0;
        this.connectedAntigensSet = false;
        this.ais = ais;
        this.accuracy = 0.0;
        this.correctInteraction = 0.0;
        this.connectedAntigenOfLabel = new HashMap<>();
        this.featuresWeights = new double[features.length];
        if(featuresWeights == null){
            initializeFeatureSet();
        }else{
            this.featuresWeights = featuresWeights;
        }
    }

    public void initializeFeatureSet(){
        for (int i = 0; i< featuresWeights.length; i++){
            double r = ThreadLocalRandom.current().nextDouble(0.5, 1.5);
            featuresWeights[i] = r;
            /*double p = Math.random();
            if(p<0.5){
                featuresWeights[i] = !featuresWeights[i];
            }*/
        }
    }
    public void setConnectedAntigens(){
        //Connected antigens has been calculated before, we only need to re-add the connected antigen
        if(!this.connectedAntigensSet){  //first time calculating fitness
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
                    if(antigen.getLabel().equals(this.label)){
                        correctClassificationCount +=1;
                        correctInteraction += weight;
                    }
                }
            }
            this.weightedAccuracy = (1 + correctInteraction) /(connectedAntigenOfLabel.keySet().size()+totalInteraction);
            this.accuracy = (double) correctClassificationCount/(boundAntigensCount);
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
            int connectedIndividuals = 0;
            double correctClassificationRate = 1.0;
            double dangerousIndividuals = 1;
            double classificationWeight = 1.0;
            double b =0.0;
            for(Antigen antigen: connectedAntigen.keySet()){
                /*if(antigen.getInteractionMap().containsKey(this.ais)){
                    b += Math.pow(antigen.getInteractionMap().get(this.ais),2)/antigen.getTotalInteraction();
                    //System.out.println(b);
                }*/
                /*if(antigen.getAntigenWeights().containsKey(ais) && antigen.getLabel().equals(this.getLabel())){
                    double w = antigen.getAntigenWeights().get(ais)/this.ais.getIteration();
                    if(w < classificationWeight){
                        classificationWeight = w;
                        //System.out.println(classificationWeight);
                    }
                }*/
                if(antigen.getCorrectlyClassified() > 0.0 && antigen.getLabel().equals(this.label)){
                    double w = antigen.getCorrectlyClassified() / ais.getIteration();
                    if(w < correctClassificationRate){
                        correctClassificationRate = w;
                        //System.out.println(w);
                    }
                }
                Random rd = new Random();
                double weight = connectedAntigen.get(antigen);
                //System.out.println(rd.nextInt(2)+1);
                sharingFactor += Math.pow(weight,2)/(antigen.getTotalInteraction()); //part of the antigen that belongs to the antibody
            }
            //System.out.println(b+" sharing factor "+sharingFactor);
            //System.out.println(b);

            //System.out.println(classificationWeight);
            this.fitness = ((sharingFactor*weightedAccuracy)/(totalInteraction));
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

    public double checkAccuracy(){
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
    }

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
    @Override
    public String toString() {
        return
                "features=" + Arrays.toString(features) +
                ", radius=" + radius +
                ", label='" + label + '\'';
    }
}
