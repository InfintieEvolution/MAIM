package Algorithm;

import AIS.Antibody;
import AIS.Antigen;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class DataSet {

    ArrayList<Antigen> antigenList;
    public Antigen[] testSet;
    public Antigen[] trainingSet;
    public double trainingTestSplit;
    public HashMap<String,double[][]> featureMap;
    private Random random;
    public double[] featureSums;
    public double[][] totalFeatureMinMax;
    public ArrayList<String> labels;
    public HashMap<String, ArrayList<Antigen>> antigenMap;
    public int labelColumn;
    public DataSet(String path, double trainingTestSplit, int labelColumn){
        this.trainingTestSplit = trainingTestSplit;
        this.random = new Random();
        this.featureMap = new HashMap<>();
        this.labels = new ArrayList<>();
        this.labelColumn = labelColumn;
        readFile(path);
    }

    private void readFile(String path) {
        antigenList = new ArrayList<>();

        try (
                Stream<String> stream = Files.lines(Paths.get(path))) {
            stream.forEach(this::processLine);

            //iterate over all the lines in the dataset
        } catch (IOException e) {
            e.printStackTrace();
        }
        //createFeatureMap(antigenList);

        normalizeFeatures(antigenList);
        /*for (String label: featureMap.keySet()){
            for(int i=0; i<featureMap.get(label).length;i++){
                System.out.println(label + ", featureNR: "+i+", minvalue: "+featureMap.get(label)[i][0]+", maxvalue: "+featureMap.get(label)[i][1]);

            }
        }*/
        this.testSet = new Antigen[(int)(antigenList.size()*trainingTestSplit)];
        this.trainingSet = new Antigen[antigenList.size() - testSet.length];
        for(int i=0; i< trainingSet.length;i++){
            trainingSet[i] = antigenList.remove(random.nextInt(antigenList.size()));
        }
        this.testSet = antigenList.toArray(testSet);

        this.antigenMap = Antigen.createAntigenMap(trainingSet);
    }

    private void processLine(String line){

        var list = Arrays.asList(line.split(","));
        if(list.size() == 1){
            return;
        }

        double[] attributes = new double[list.size()-1];
        var label = "";
        var attributeListCount = 0;
        for(int i=0;i<list.size();i++){
            if(i == this.labelColumn){
                label = list.get(i);
            }
            else{
                attributes[attributeListCount ++] = Double.parseDouble(list.get(i));
            }
        }
        Antigen antigen = new Antigen(attributes,label);

        if(this.featureSums == null){
            this.featureSums = new double[attributes.length];
        }
        if(this.totalFeatureMinMax == null){
            this.totalFeatureMinMax = new double[attributes.length][2];
            for(double[] featureBound:totalFeatureMinMax){
                featureBound[0] = Double.MAX_VALUE;
                featureBound[1] = Double.MIN_VALUE;
            }
        }

        for(int i=0; i< attributes.length;i++){
            featureSums[i] += attributes[i];
            if(totalFeatureMinMax[i][0] > attributes[i]){
                totalFeatureMinMax[i][0] = attributes[i];
            }
            else if(totalFeatureMinMax[i][1] < attributes[i]){
                totalFeatureMinMax[i][1] = attributes[i];
            }
        }
        /*if(!featureMap.containsKey(label)){
            featureMap.put(label,new double[attributes.length][2]);
            for(double[] featureBound:featureMap.get(label)){
                featureBound[0] = Double.MAX_VALUE;
                featureBound[1] = Double.MIN_VALUE;
            }
        }*/

        antigenList.add(antigen);
        /*for(int i=0; i< attributes.length;i++){
            if(featureMap.get(antigen.getLabel())[i][0] > attributes[i]){
                featureMap.get(antigen.getLabel())[i][0] = attributes[i];
            }
            else if(featureMap.get(antigen.getLabel())[i][1] < attributes[i]){
                featureMap.get(antigen.getLabel())[i][1] = attributes[i];
            }
        }*/
    }

    public void normalizeFeatures(ArrayList<Antigen> antigens){
        int numberOfAntigen = antigens.size();
        int labelCount = 0;
        for (Antigen antigen:antigens){
            //String label = antigen.getLabel();
            //double[] attributes = antigen.getAttributes();

            if(!featureMap.containsKey(antigen.getLabel())){
                labels.add(antigen.getLabel());
                featureMap.put(antigen.getLabel(),new double[antigen.getAttributes().length][2]);
                for(double[] featureBound:featureMap.get(antigen.getLabel())){
                    featureBound[0] = Double.MAX_VALUE;
                    featureBound[1] = Double.MIN_VALUE;
                }
            }

            for(int i=0; i<antigen.getAttributes().length;i++){
                //attributes[i] = (attributes[i] - (featureSums[i]/numberOfAntigen))/(totalFeatureMinMax[i][1] - totalFeatureMinMax[i][0]);
                antigen.getAttributes()[i] = (antigen.getAttributes()[i] - totalFeatureMinMax[i][0])/(totalFeatureMinMax[i][1] - totalFeatureMinMax[i][0]);
                if(featureMap.get(antigen.getLabel())[i][0] > antigen.getAttributes()[i]){
                    featureMap.get(antigen.getLabel())[i][0] = antigen.getAttributes()[i];
                }
                else if(featureMap.get(antigen.getLabel())[i][1] < antigen.getAttributes()[i]){
                    featureMap.get(antigen.getLabel())[i][1] = antigen.getAttributes()[i];
                }
            }

        }
    }
 }
