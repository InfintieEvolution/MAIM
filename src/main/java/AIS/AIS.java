package AIS;

import java.util.*;

public class AIS {

    private Antigen[] antigens;
    private Antibody[] antibodies;
    private HashMap<String,ArrayList<Antigen>> antigenMap;
    private HashMap<String,double[][]> featureMap;
    private HashMap<String, ArrayList<Antibody>> antibodyMap;
    private Random random = new Random();
    private int populationSize;
    private int numberOfTournaments;
    private double mutationRate;
    private final Comparator<Antibody> selectionComparator;
    private int iteration;
    private double averageFitness;

    public AIS(Antigen[] antigens, HashMap<String,ArrayList<Antigen>> antigenMap,int populationSize, double mutationRate, int numberOfTorunaments){
        this.antigens = antigens;
        this.antigenMap = antigenMap;
        this.featureMap = new HashMap<>();
        this.antibodyMap = new HashMap<>();
        this.populationSize = populationSize;
        this.antibodies = new Antibody[populationSize];
        this.numberOfTournaments = numberOfTorunaments;
        this.mutationRate = mutationRate;
        this.iteration = 0;
        this.averageFitness = 0;

        selectionComparator = (o1, o2) -> {
            if (o1.getFitness() > o2.getFitness()) {
                return -1;
            }
            else if (o1.getFitness() < o2.getFitness()) {
                return 1;
            }
            return 0;
        };

        initialisePopulation(this.populationSize);
    }

    public void iterate(){
        this.iteration++;
        Antibody[] children = new Antibody[populationSize*antibodyMap.keySet().size()];
            int childrenCount = 0;
            for(String label:antibodyMap.keySet()){
                ArrayList<Antibody> newAntibodiesOfLabel = new ArrayList<>();
            for(int i=0;i<populationSize;i++){
                final Antibody parent1 = tournamentSelection(antibodyMap.get(label), numberOfTournaments);
                final Antibody parent2 = tournamentSelection(antibodyMap.get(label), numberOfTournaments);

                Antibody child = crossover(parent1,parent2);
                //child.setConnectedAntigens();
                double p = Math.random();
                if(p <= this.mutationRate){
                    this.mutate(child);
                }
                children[childrenCount ++] = child;
                newAntibodiesOfLabel.add(child);
                }
            for(Antibody antibody:newAntibodiesOfLabel){
                antibodyMap.get(label).add(antibody);
            }
        }

        for(String label: antibodyMap.keySet()){
            for(Antibody antibody:antibodyMap.get(label)){
                antibody.setConnectedAntigens();
                antibody.calculateFitness();
            }
        }

        this.select();

        //clear the connected antibodies list for the next iteration
        for(Antigen antigen:antigens){
            antigen.setConnectedAntibodies(new ArrayList<>());
            antigen.setTotalInteraction(0.0);
        }
    }

    private void mutate(Antibody antibody){
        double p = Math.random();
        if(p > 0.5){
            double rand = Math.random();
            if(rand > 0.5){
                antibody.setRadius(antibody.getRadius()*1.1);
            }else{
                antibody.setRadius(antibody.getRadius()*0.9);
            }
        }else {
            double rand = Math.random();
            if(rand > 0.5){
                antibody.getFeatures()[random.nextInt(antibody.getFeatures().length)] *= 1.1;
            }else{
                antibody.getFeatures()[random.nextInt(antibody.getFeatures().length)] *= 0.9;
            }
        }
    }

    private Antibody crossover(Antibody parent1, Antibody parent2){

        double[] features = new double[parent1.getFeatures().length];
        for(int i=0;i<features.length;i++){
            double rand = Math.random();
            if(rand > 0.5){
                features[i] = parent1.getFeatures()[i];
            }else{
                features[i] = parent2.getFeatures()[i];
            }
        }

        return new Antibody(features,this.calculateNewRadius(parent1,parent2),parent1.getLabel(),this.antigens,this);
    }

    private double calculateNewRadius(Antibody parent1, Antibody parent2){
        double radius;
        if(parent1.getFitness() > parent2.getFitness()){
            radius = parent1.getFitness();
        }else{
            radius = parent2.getFitness();
        }

        double rand = Math.random();
        if(rand > 0.5){
            radius *= 1.1;
        }else{
            radius *= 0.9;
        }

        return radius;
    }

    private Antibody tournamentSelection(ArrayList<Antibody> antibodies, int numberOfTournaments){
        Antibody winner = null;

        for (int i = 0; i < numberOfTournaments; i ++) {
            final Antibody participant = antibodies.get(random.nextInt(antibodies.size()));

            if (winner == null || participant.getFitness() > winner.getFitness()) {
                winner = participant;
            }
        }
        return winner;
    }

    private void select(){

        HashMap<String,Double> classDistributionMap = new HashMap<>();
        double totalWeightSum = 0.0;
        for(String label:antigenMap.keySet()){
            double labelWeightSum = 0.0;
            for(Antigen antigen:antigenMap.get(label)){
                labelWeightSum += antigen.getTotalInteraction();
               /*for(Antibody antibody:antigen.getConnectedAntibodies()){
                    labelWeightSum += antibody.getConnectedAntigen().get(antigen);
                }*/
            }
            totalWeightSum += labelWeightSum;
           classDistributionMap.put(label,labelWeightSum);
        }
        for(String label:classDistributionMap.keySet()){
            double numberOfIndividuals = (classDistributionMap.get(label)/totalWeightSum)*populationSize;
            //System.out.println(numberOfIndividuals);
            classDistributionMap.put(label,numberOfIndividuals);
        }

        HashMap<String,ArrayList<Antibody>> newAntibodyMap = new HashMap<>();
        double totalFitness = 0.0;
        int index = 0;
        int totalSelectedIndividuals = 0;
        int labelIndex = 0;
        for (String label: antigenMap.keySet()){
            labelIndex++;
            int numberOfIndividuals = (int)(double)(classDistributionMap.get(label));

            totalSelectedIndividuals += numberOfIndividuals;
            if(labelIndex == antibodyMap.keySet().size() && totalSelectedIndividuals < populationSize){
                numberOfIndividuals++;
            }
            int selectedIndividualsOfLabel = 0;

            if(antibodyMap.get(label) == null){
                continue;
            }
            antibodyMap.get(label).sort(selectionComparator);
            ArrayList<Antibody> priorityQueue = antibodyMap.get(label);

            while (index < populationSize && selectedIndividualsOfLabel <= numberOfIndividuals){
                double p = Math.random();

                int rank = priorityQueue.size();
                int rankSum = 0;
                for(int i=priorityQueue.size();i>0;i--){
                    rankSum += i;
                }
                double cumulativeProbability = 0.0;
                int listIndex = 0;

                while (!priorityQueue.isEmpty()){
                    cumulativeProbability += (double) rank/rankSum;

                    if(p <= cumulativeProbability){
                        Antibody survivor = priorityQueue.remove(listIndex);

                        this.antibodies[index ++] = survivor;
                        selectedIndividualsOfLabel++;
                        totalFitness += survivor.getFitness();
                        if(!newAntibodyMap.containsKey(survivor.getLabel())){
                            newAntibodyMap.put(survivor.getLabel(),new ArrayList<>(){{add(survivor);}});
                        }else{
                            newAntibodyMap.get(survivor.getLabel()).add(survivor);
                        }
                        break;
                    }
                    listIndex++;
                    rank--;
                }
            }
            totalSelectedIndividuals += selectedIndividualsOfLabel;
        }

        this.averageFitness = totalFitness / populationSize;
        this.antibodyMap = newAntibodyMap;
    }

    public void initialisePopulation(int populationSize){
        int antibodyCount = 0;
        for(String label:antigenMap.keySet()){
            if(antibodyCount >= populationSize){
                break;
            }
            createFeatureMap(antigenMap.get(label));

            int labelCount = (int)(((double)this.antigenMap.get(label).size()/antigens.length)*populationSize);

            for (int i=0;i<labelCount;i++){
                Antibody antibody = createAntibody(label);
                if(this.antibodyMap.containsKey(label)){
                    this.antibodyMap.get(label).add(antibody);

                }else{
                    this.antibodyMap.put(label, new ArrayList<>(){ { add(antibody);}});
                }
                antibodies[antibodyCount ++] = antibody;
            }
        }

        while (antibodyCount < populationSize){
            Antigen radnomAntigen = antigens[random.nextInt(antigens.length)];
            String label = radnomAntigen.getLabel();

            Antibody antibody = createAntibody(label);
            antibodyMap.get(label).add(antibody);
            antibodies[antibodyCount] = antibody;
            antibodyCount++;
        }

        for(Antibody antibody: antibodies){
            antibody.setConnectedAntigens();
        }


        for (Antibody antibody: antibodies){
            antibody.calculateFitness();
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
            }
        }
            featureMap.put(antigens.get(0).getLabel(),features);
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
        }

        minAverage = minAverage/featureMap.get(label).length;
        maxAverage = maxAverage/featureMap.get(label).length;

        //TODO: Make initial radius better
        double radius = (minAverage + (maxAverage - minAverage) * random.nextDouble());

        return new Antibody(attributes, radius, label, this.antigens,this);
    }
/*
    public static double vote(Antigen[] antigens, Antibody[] antibodies){
        HashMap<Antigen,String> antigenClassification = new HashMap<>();
        for(Antigen antigen: antigens){
            HashMap<String,Double> votingMap = new HashMap<>();
            for(Antibody antibody: antibodies){
                double distance = antibody.eucledeanDistance(antibody.getFeatures(),antigen.getAttributes());
                if(distance <= antibody.getRadius()){
                    //antibody is inside recognition radius
                    double voteWeight = 1/(1+distance)*antibody.getFitness();
                    if(!votingMap.containsKey(antibody.getLabel())){
                        votingMap.put(antibody.getLabel(),voteWeight);
                    }else{
                        double k = votingMap.get(antibody.getLabel());
                        votingMap.put(antibody.getLabel(),k + voteWeight);
                    }
                }
            }

            double highestVoteNumber = 0.0;
            String highestVoteLabel = null;

            for(String label: votingMap.keySet()){
                if(votingMap.get(label) > highestVoteNumber){
                    highestVoteNumber = votingMap.get(label);
                    highestVoteLabel = label;
                }
            }
            antigenClassification.put(antigen,highestVoteLabel);
        }

        int correctClassification = 0;
        for(Antigen antigen: antigens){
            if(antigen.getLabel().equals(antigenClassification.get(antigen))){
                correctClassification++;
            }
        }
        double accuracy = (double)correctClassification/antigens.length;

        return accuracy;
    }
*/
    public static double vote(HashMap<String,ArrayList<Antigen>> antigenMap,HashMap<String,ArrayList<Antibody>> antibodyMap) {
        HashMap<Antigen, String> antigenClassification = new HashMap<>();
        for(String antigenLabel: antigenMap.keySet()){
        for (Antigen antigen : antigenMap.get(antigenLabel)) {
            HashMap<String, Double> votingMap = new HashMap<>();
            for(String antibodyLabel: antibodyMap.keySet()){
            for (Antibody antibody : antibodyMap.get(antibodyLabel)) {
                double distance = antibody.eucledeanDistance(antibody.getFeatures(), antigen.getAttributes());
                if (distance <= antibody.getRadius()) {
                    //antibody is inside recognition radius
                    double voteWeight = 1 / (1 + distance) * antibody.getFitness();
                    if (!votingMap.containsKey(antibody.getLabel())) {
                        votingMap.put(antibody.getLabel(), voteWeight);
                    } else {
                        double k = votingMap.get(antibody.getLabel());
                        votingMap.put(antibody.getLabel(), k + voteWeight);
                    }
                }
            }
        }
            double highestVoteNumber = 0.0;
            String highestVoteLabel = null;

            for (String label : votingMap.keySet()) {
                if (votingMap.get(label) > highestVoteNumber) {
                    highestVoteNumber = votingMap.get(label);
                    highestVoteLabel = label;
                }
            }
            antigenClassification.put(antigen, highestVoteLabel);
        }
    }
        int correctClassification = 0;
        int antigenCount = 0;
        for(String antigenLabel: antigenMap.keySet()){
            for (Antigen antigen : antigenMap.get(antigenLabel)) {
                    if (antigen.getLabel().equals(antigenClassification.get(antigen))) {
                        correctClassification++;
                    }
                    antigenCount++;
                }
            }
        double accuracy = (double)correctClassification/antigenCount;

        return accuracy;
    }

    public Antigen[] getAntigens() {
        return antigens;
    }

    public void setAntigens(Antigen[] antigens) {
        this.antigens = antigens;
    }

    public Antibody[] getAntibodies() {
        return antibodies;
    }

    public void setAntibodies(Antibody[] antibodies) {
        this.antibodies = antibodies;
    }


    public HashMap<String, ArrayList<Antigen>> getAntigenMap() {
        return antigenMap;
    }

    public HashMap<String, double[][]> getFeatureMap() {
        return featureMap;
    }

    public HashMap<String, ArrayList<Antibody>> getAntibodyMap() {
        return this.antibodyMap;
    }

    public static HashMap<String, ArrayList<Antibody>> copy(HashMap<String, ArrayList<Antibody>> original)
    {
        HashMap<String, ArrayList<Antibody>> copy = new HashMap<>();
        for (Map.Entry<String, ArrayList<Antibody>> entry : original.entrySet())
        {
            copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return copy;
    }
}


