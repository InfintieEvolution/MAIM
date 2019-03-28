package Algorithm;

import AIS.Antigen;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.Instance;
import net.sf.javaml.distance.PearsonCorrelationCoefficient;
import net.sf.javaml.featureselection.subset.GreedyForwardSelection;
import net.sf.javaml.filter.normalize.NormalizeMidrange;
import net.sf.javaml.tools.data.FileHandler;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Stream;

import smile.data.AttributeDataset;
import smile.data.SparseDataset;
import smile.data.parser.ArffParser;
import smile.data.parser.LibsvmParser;
import smile.projection.PCA;


public class DataSet {


    ArrayList<Antigen> antigenList;
    public Antigen[] testSet;
    public Antigen[] trainingSet;
    public Antigen[] validationSet;
    public double trainingTestSplit;
    public double validationSplit;
    public HashMap<String,double[][]> featureMap;
    private Random random;
    public double[] featureSums;
    public double[][] totalFeatureMinMax;
    public ArrayList<String> labels;
    public HashMap<String, ArrayList<Antigen>> antigenMap;
    public HashMap<String, ArrayList<Antigen>> testAntigenMap;
    public HashMap<String, ArrayList<Antigen>> validationAntigenMap;
    public int labelColumn;
    public boolean pca;


    public DataSet(String path, double trainingTestSplit, double validationSplit, int labelColumn, boolean pca){
        this.trainingTestSplit = trainingTestSplit;
        this.validationSplit = validationSplit;
        this.random = new Random();
        this.featureMap = new HashMap<>();
        this.labels = new ArrayList<>();
        this.labelColumn = labelColumn;
        this.pca = pca;
        readFile(path);
    }

    private void readFile(String path) {
        antigenList = new ArrayList<>();
        Dataset data = null;
        try {
            data = FileHandler.loadDataset(new File(path), labelColumn, ",");
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        NormalizeMidrange nmr=new NormalizeMidrange(0.5,1);
        nmr.build(data);
        nmr.filter(data);

        for (Instance instance: data){
            processInstance(instance);
        }

        /*try (
                Stream<String> stream = Files.lines(Paths.get(path))) {
            stream.forEach(this::processLine);

            //iterate over all the lines in the dataset
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        //normalizeFeatures(antigenList);

        if(pca){
            double[][] data2 = new double[antigenList.size()][antigenList.get(0).getAttributes().length];
            int count =0;
            for(Antigen antigen:antigenList){
                for(int i=0; i<antigen.getAttributes().length;i++){
                    data2[count][i] = antigen.getAttributes()[i];
                }
                count ++;
            }
            PCA pca = new PCA(data2,true);
            pca.setProjection(2);
            double[][] X = pca.project(data2);

            for(int i=0; i<X.length;i++){
                antigenList.get(i).setAttributes(X[i]);
            }
        }
        createFeatureMap();

        this.testSet = new Antigen[(int)(antigenList.size()*trainingTestSplit)];
        this.validationSet = new Antigen[(int)((antigenList.size() - testSet.length)*validationSplit)];
        this.trainingSet = new Antigen[antigenList.size() - testSet.length - validationSet.length];


        for(int i=0; i< trainingSet.length;i++) {
            trainingSet[i] = antigenList.remove(random.nextInt(antigenList.size()));
        }
        for(int i=0; i< validationSet.length;i++){
            validationSet[i] = antigenList.remove(random.nextInt(antigenList.size()));
        }

        this.testSet = antigenList.toArray(testSet);

        this.antigenMap = Antigen.createAntigenMap(trainingSet);
        this.validationAntigenMap = Antigen.createAntigenMap(validationSet);
        this.testAntigenMap = Antigen.createAntigenMap(testSet);

        /*double[][] ts = new double[this.testSet.length][this.testSet[0].getAttributes().length];
        for(int i = 0; i < testSet.length; i++){
            ts[i] = testSet[i].getAttributes();
        }*/
    }

    private void processInstance(Instance instance){
        double[] attributes = new double[instance.values().size()];
        for(int i=0; i<attributes.length;i++){
            attributes[i] = instance.value(i);
        }
        Antigen antigen = new Antigen(attributes,instance.classValue().toString());

        if(this.featureSums == null){
            this.featureSums = new double[attributes.length];
        }
        if(this.totalFeatureMinMax == null){
            this.totalFeatureMinMax = new double[attributes.length][2];
            for(double[] featureBound:totalFeatureMinMax){
                featureBound[0] = Double.MAX_VALUE;
                featureBound[1] = Double.NEGATIVE_INFINITY;
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

        antigenList.add(antigen);

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

        antigenList.add(antigen);
    }

    public void createFeatureMap(){
        for(Antigen antigen: antigenList){
            if(!featureMap.containsKey(antigen.getLabel())){
                labels.add(antigen.getLabel());
                featureMap.put(antigen.getLabel(),new double[antigen.getAttributes().length][2]);
                for(double[] featureBound:featureMap.get(antigen.getLabel())){
                    featureBound[0] = Double.MAX_VALUE;
                    featureBound[1] = Double.NEGATIVE_INFINITY;
                }
            }

            for(int i=0; i<antigen.getAttributes().length;i++){
                //antigen.getAttributes()[i] = (antigen.getAttributes()[i] - totalFeatureMinMax[i][0])/(totalFeatureMinMax[i][1] - totalFeatureMinMax[i][0]);
                if(featureMap.get(antigen.getLabel())[i][0] > antigen.getAttributes()[i]){
                    featureMap.get(antigen.getLabel())[i][0] = antigen.getAttributes()[i];
                }
                else if(featureMap.get(antigen.getLabel())[i][1] < antigen.getAttributes()[i]){
                    featureMap.get(antigen.getLabel())[i][1] = antigen.getAttributes()[i];
                }
            }
        }
    }
    public void normalizeFeatures(ArrayList<Antigen> antigens){
        this.featureMap = new HashMap<>();
        for (Antigen antigen:antigens){

            if(!featureMap.containsKey(antigen.getLabel())){
                labels.add(antigen.getLabel());
                featureMap.put(antigen.getLabel(),new double[antigen.getAttributes().length][2]);
                for(double[] featureBound:featureMap.get(antigen.getLabel())){
                    featureBound[0] = Double.MAX_VALUE;
                    featureBound[1] = Double.MIN_VALUE;
                }
            }

            for(int i=0; i<antigen.getAttributes().length;i++){
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

    public static HashMap<String,ArrayList<Antigen>>[] splitDataSet(int split, HashMap<String,ArrayList<Antigen>> dataSet){
        ArrayList<Antigen> antigenList = new ArrayList<>();
        for(String label: dataSet.keySet()){
            antigenList.addAll(dataSet.get(label));
        }
        Random random = new Random();
        HashMap<String,ArrayList<Antigen>>[] splitAntigenLists = new HashMap[split];

        int numberOfAntigenPerList = antigenList.size()/split;

        //split the antigen into <split> number of equal sized lists of antigen
        for(int i= 0; i < split; i++){
            splitAntigenLists[i] = new HashMap<>();
            int antigenCount = 0;

            while (antigenCount < numberOfAntigenPerList){
                Antigen antigen = antigenList.remove(random.nextInt(antigenList.size()));

                if(splitAntigenLists[i].containsKey(antigen.getLabel())){
                    splitAntigenLists[i].get(antigen.getLabel()).add(antigen);
                }else splitAntigenLists[i].put(antigen.getLabel(), new ArrayList<>() {{
                    add(antigen);
                }});
                antigenCount++;
            }
        }
        while (!antigenList.isEmpty()){
            Antigen antigen =  antigenList.remove(0);
            int randomListIndex = random.nextInt(split);
            if(splitAntigenLists[randomListIndex].containsKey(antigen.getLabel()))
                splitAntigenLists[randomListIndex].get(antigen.getLabel()).add(antigen);
            else{
                splitAntigenLists[randomListIndex].put(antigen.getLabel(),new ArrayList<>(){{add(antigen);}});
            }
        }

        return splitAntigenLists;
    }

    public Antigen[] getTrainingSet() {
        return trainingSet;
    }

    public void setTrainingSet(Antigen[] trainingSet) {
        this.trainingSet = trainingSet;
    }

    public HashMap<String, ArrayList<Antigen>> getAntigenMap() {
        return antigenMap;
    }

    public void setAntigenMap(HashMap<String, ArrayList<Antigen>> antigenMap) {
        this.antigenMap = antigenMap;
    }

    public HashMap<String, ArrayList<Antigen>> getValidationAntigenMap() {
        return validationAntigenMap;
    }

    public void setValidationAntigenMap(HashMap<String, ArrayList<Antigen>> validationAntigenMap) {
        this.validationAntigenMap = validationAntigenMap;
    }
}
