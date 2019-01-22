package AIS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class AIS {

    private ArrayList<Antigen> antigens;
    private Antibody[] antibodies;
    private HashMap<String,ArrayList<Antigen>> antigenMap;
    private HashMap<String,double[][]> featureMap;
    private HashMap<String, ArrayList<Antibody>> antibodyMap;
    private Random random = new Random();
    private int populationSize;

    public AIS(ArrayList<Antigen> antigens, HashMap<String,ArrayList<Antigen>> antigenMap,int populationSize){
        //this.antibodies = antibodies;
        this.antigens = antigens;
        this.antigenMap = antigenMap;
        this.featureMap = new HashMap<>();
        this.antibodyMap = new HashMap<>();
        this.populationSize = populationSize;
        initialisePopulation(this.populationSize);
    }

    public void initialisePopulation(int populationSize){
        //Antibody[] antibodies = new Antibody[populationSize];

        //createAntibody(antigenMap.get())

        int antibodyCount = 0;
        for(String label:antigenMap.keySet()){
            if(antibodyCount >= populationSize){
                break;
            }
            createFeatureMap(antigenMap.get(label));

            int labelCount = (int)(((double)this.antigenMap.get(label).size()/antigens.size())*populationSize);

            for (int i=0;i<labelCount;i++){
                if(this.antibodyMap.containsKey(label)){
                    this.antibodyMap.get(label).add(createAntibody(label));

                }else{
                    this.antibodyMap.put(label, new ArrayList<>(){ { add(createAntibody(label));}});
                }
                antibodyCount++;
            }
        }

        if(antibodyCount < populationSize){
            Antigen radnomAntigen = antigens.get(random.nextInt(antigens.size()));
            String label = radnomAntigen.getLabel();

            antibodyMap.get(label).add(createAntibody(label));

        }
        for(String label:antibodyMap.keySet()){
            System.out.println(antibodyMap.get(label).size());
        }
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

        double maxAverage = 0;
        double minAverage = 0;

        for(int i=0; i<featureMap.get(label).length;i++){
            double[] featureBounds = featureMap.get(label)[i];
            double maxValue = featureBounds[1]*1.1;
            double minValue = featureBounds[0]*0.9;

            maxAverage += maxValue;
            minAverage += minValue;

            attributes[i] = minValue + (maxValue - minValue)*random.nextDouble();
            //System.out.println("maxvalue "+ maxValue+" "+"minvalue "+minValue+"random in range "+attributes[i]);
        }

        minAverage = minAverage/featureMap.get(label).length;
        maxAverage = maxAverage/featureMap.get(label).length;
        double radius = minAverage + (maxAverage - minAverage) * random.nextDouble();

        return new Antibody(attributes, radius, label);

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


