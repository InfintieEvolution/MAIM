package AIS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class AIS {

    private ArrayList<Antigen> antigens;
    private Antibody[] antibodies;
    private HashMap<String,ArrayList<Antigen>> antigenMap;
    HashMap<String,double[][]> featureMap;
    Random random;

    public AIS(ArrayList<Antigen> antigens, HashMap<String,ArrayList<Antigen>> antigenMap){
        //this.antibodies = antibodies;
        this.antigens = antigens;
        this.antigenMap = antigenMap;
        this.featureMap = new HashMap<>();
        this.random = new Random();
        initialisePopulation(100,antigens);
    }

    public void initialisePopulation(int populationSize, ArrayList<Antigen> antigens){
        Antibody[] antibodies = new Antibody[populationSize];

        //createAntibody(antigenMap.get())
        for(String label:antigenMap.keySet()){
            createFeatureMap(antigenMap.get(label));
        }
        for(int i=0; i<populationSize;i++){

            //Antibody antibody = new Antibody();
        }
        createAntibody(antigens.get(0).getLabel());
        this.antibodies = antibodies;
    }

    public void createFeatureMap(ArrayList<Antigen> antigens){
        double[][] features = new double[antigens.get(0).getAttributes().length][2];

        for(double[] featureBound:features){
            featureBound[0] = Double.MAX_VALUE;
            featureBound[1] = Double.MIN_VALUE;
        }

        for (int j=0;j<antigens.size();j++){
            for(int i=0;i<antigens.get(j).getAttributes().length;i++){

                double featureValue = antigens.get(j).getAttributes()[i];

                if(featureValue < features[i][0]){
                    features[i][0] = featureValue;
                }else if(featureValue > features[i][1]){
                    features[i][1] = featureValue;
                }
                //features[i][j] = antigens.get(j).getAttributes()[i];
            }
        }

        //System.out.println(features[0][0]);
        //if(!featureMap.containsKey(antigens.get(0).getLabel())){
            featureMap.put(antigens.get(0).getLabel(),features);
        /*}else{
            featureMap.put(antigens.get(0).getLabel(),features);
        }*/
    }
    public Antibody createAntibody(String label){
        double[][] featureList = featureMap.get(label);
        double[] attributes = new double[featureList.length];

        for(int i=0; i<featureMap.get(label).length;i++){
            double[] featureBounds = featureMap.get(label)[i];
            double maxValue = featureBounds[1]*1.1;
            double minValue = featureBounds[0]*0.9;
            attributes[i] = minValue + (maxValue - minValue)*random.nextDouble();
            
            //System.out.println("maxvalue "+ maxValue+" "+"minvalue "+minValue+"random in range "+attributes[i]);
        }

        //Antibody antibody = new Antibody(attributes,);
        return null;
    }
    public ArrayList<Antigen> getAntigens() {
        return antigens;
    }

    public void setAntigens(ArrayList<Antigen> antigens) {
        this.antigens = antigens;
    }

    public Antibody[] getAntibodies() {
        return antibodies;
    }

    public void setAntibodies(Antibody[] antibodies) {
        this.antibodies = antibodies;
    }


}


