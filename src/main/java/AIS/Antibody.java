package AIS;

import java.util.ArrayList;
import java.util.Arrays;

public class Antibody {

    private double[] features;
    private double radius;
    private String label;
    private double fitness;
    private Antigen[] antigens;
    private ArrayList<Antigen> connectedAntigen;

    public Antibody(double[] features, double radius, String label, Antigen[] antigens){
        this.features = features;
        this.radius = radius;
        this.label = label;
        this.fitness = 0.0;
        this.antigens = antigens;
        this.connectedAntigen = new ArrayList<>();
    }

    public void calculateFitness(){
        int wrongClassificationCount = 0;
        int correctClassificationCount = 0;
        double totalWeight = 0.0;
        int boundAntigensCount =0;
        for (Antigen antigen:antigens){
          double distance = eucledeanDistance(this.features,antigen.getAttributes());

          if (distance <= this.radius){
              antigen.getConnectedAntibodies().add(this);
              connectedAntigen.add(antigen);
              boundAntigensCount +=1;
              //Antibodies here is within the recognition zone

              totalWeight += distance;
              if(antigen.getLabel().equals(this.label)){
                  correctClassificationCount +=1;
              }else{
                  wrongClassificationCount +=1;
              }
          }
        }

        if(boundAntigensCount == 0){
            this.fitness = 0.0;
        }else{
            double accuracy = (double) correctClassificationCount/(correctClassificationCount+wrongClassificationCount);
            this.fitness = accuracy * (boundAntigensCount/totalWeight);
            /*System.out.println("accuracy "+accuracy);
            System.out.println("fitness "+this.fitness);
            System.out.println("-------------------------");*/
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

    @Override
    public String toString() {
        return
                "features=" + Arrays.toString(features) +
                ", radius=" + radius +
                ", label='" + label + '\'';
    }
}
