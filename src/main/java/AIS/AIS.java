package AIS;

import java.util.*;

public class AIS {

    private Antigen[] antigens;
    private Antibody[] antibodies;
    private HashMap<String,ArrayList<Antigen>> antigenMap;
    private HashMap<String,ArrayList<Antigen>> antigenValidationMap;
    private HashMap<String,double[][]> featureMap;
    private HashMap<String, ArrayList<Antibody>> antibodyMap;
    private Random random = new Random();
    private int populationSize;
    private int numberOfTournaments;
    private double mutationRate;
    private final Comparator<Antibody> selectionComparator;
    private final Comparator<Antibody> accuracyComparator;
    private double bestAccuracy;
    private int bestIteration;
    private double bestAccuracyTestSet;
    private int bestIterationTestSet;
    private ArrayList<String> labels;
    private int iteration;
    private int maxIterations;
    private double averageFitness;
    private double radiusMultiplier;

//    public AIS(Antigen[] antigens, HashMap<String,double[][]> featureMap, ArrayList<String> labels, HashMap<String,ArrayList<Antigen>> antigenMap,HashMap<String,ArrayList<Antigen>> antigenValidationMap,int populationSize, double mutationRate, int numberOfTorunaments, int maxIterations,Set<Integer>[] featureSubsets, double radiusMultiplier){

    public AIS(Antigen[] antigens, HashMap<String,double[][]> featureMap, ArrayList<String> labels, HashMap<String,ArrayList<Antigen>> antigenMap,HashMap<String,ArrayList<Antigen>> antigenValidationMap,int populationSize, double mutationRate, int numberOfTorunaments, int maxIterations, double radiusMultiplier){
        this.antigens = antigens;
        this.antigenMap = antigenMap;
        this.featureMap = new HashMap<>();
        this.antibodyMap = new HashMap<>();
        this.populationSize = populationSize;
        this.antibodies = new Antibody[populationSize];
        this.numberOfTournaments = numberOfTorunaments;
        this.mutationRate = mutationRate;
        this.iteration = 0;
        this.bestAccuracy = 0.0;
        this.bestIteration = 0;
        this.bestAccuracyTestSet = 0.0;
        this.bestIterationTestSet = 0;
        this.featureMap = featureMap;
        this.labels = labels;
        this.maxIterations = maxIterations;
        this.antigenValidationMap = antigenValidationMap;
        this.radiusMultiplier = radiusMultiplier;

        selectionComparator = (o1, o2) -> {
            if (o1.getFitness() > o2.getFitness()) {
                return -1;
            }
            else if (o1.getFitness() < o2.getFitness()) {
                return 1;
            }
            return 0;
        };
        accuracyComparator = (o1, o2) -> {
            double comparisonValue1 = o1.getAccuracy();
            double comparisonValue2 = o2.getAccuracy();
            if (comparisonValue1 > comparisonValue2) {
                return -1;
            }
            else if (comparisonValue1 < comparisonValue2) {
                return 1;
            }
            return 0;
        };

        initialisePopulation(this.populationSize,false);
    }

    public void iterate(){
        this.iteration++;
        //clear the connected antibodies list for the next iteration
        for(Antigen antigen:antigens){
            antigen.setConnectedAntibodies(new ArrayList<>());
            antigen.setTotalInteraction(0.0);
            antigen.getInteractionMap().put(this,0.0);
        }

        ArrayList<Antibody> newAntibodiesOfLabel = new ArrayList<>();
        for(int i=0;i<populationSize;i++){
                String randomLabel = this.labels.get(random.nextInt(labels.size()));
                Antibody parent1;
                Antibody parent2;
                if(antibodyMap.get(randomLabel).size() > numberOfTournaments){
                    parent1 = tournamentSelection(antibodyMap.get(randomLabel), numberOfTournaments);
                    if(parent1.getTotalInteraction() == 0.0){ //if the antibody is not able to recognize anything correctly, do not allow it to reproduce
                        parent1 = createAntibody(randomLabel,true);
                    }
                    parent2 = tournamentSelection(antibodyMap.get(randomLabel), numberOfTournaments);
                    if(parent2.getTotalInteraction() == 0.0){
                        parent2 = createAntibody(randomLabel,true);
                    }
                }
                else{
                    parent1 = createAntibody(randomLabel,false);
                    parent2 = createAntibody(randomLabel,false);
                }

                Antibody child = crossover(parent1,parent2);
                //child.setConnectedAntigens();
                double p = Math.random();
                if(p <= this.mutationRate){
                    this.mutate(child);
                }
                //children[childrenCount ++] = child;
                newAntibodiesOfLabel.add(child);
            }
            for(Antibody antibody:newAntibodiesOfLabel){
                antibodyMap.get(antibody.getLabel()).add(antibody);
            }

        //set connections
        for(String label: antibodyMap.keySet()){
            for(Antibody antibody:antibodyMap.get(label)){
                antibody.setConnectedAntigens();
            }
        }
        //calculate fitness after connections has been set
        for(String label: antibodyMap.keySet()) {
            for(Antibody antibody:antibodyMap.get(label)) {
                antibody.calculateFitness();
            }
        }
        this.select();

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
        int randomIndex = random.nextInt(antibody.getFeatures().length);
        if(antibody.getFeatures()[randomIndex] == -1){
            if(rand > 0.5){
                antibody.setRadius(antibody.getRadius()*1.1);
            }else{
                antibody.setRadius(antibody.getRadius()*0.9);
            }
            return;
        }
        if(rand > 0.5){
            antibody.getFeatures()[randomIndex] *= 1.1;
        }else{
            antibody.getFeatures()[randomIndex] *= 0.9;
        }
    }

    /*p = Math.random();
    if(p < 0.1){
        antibody.getFeatures()[random.nextInt(antibody.getFeatures().length)] = -1.0;
    }else{
        antibody.getFeatures()[random.nextInt(antibody.getFeatures().length)] = p;
    }*/
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
        final ArrayList<Antibody> priorityQueue = new ArrayList<>();

        for (String label:antibodyMap.keySet()){
            priorityQueue.addAll(antibodyMap.get(label));
        }

        priorityQueue.sort(selectionComparator);

        final Antibody[] survivors = new Antibody[populationSize];

        int index = 0;

        //decaying new offspring size
        /*double offspringSize = this.populationSize*randomOffspringSize(this.populationSize,this.iteration,this.maxIterations);
        for(int i=0; i < this.populationSize - (int)offspringSize;i++){
            survivors[index ++] = priorityQueue.remove(i);
        }*/
        while (index < survivors.length) {

            double p = Math.random();
            //make survival selection more random as we go

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

                    //if the solution is the best solution we've seen, add as such
                    survivors[index ++] = priorityQueue.remove(listIndex);
                    break;
                }
                listIndex++;
                rank--;
            }
        }

        HashMap<String,ArrayList<Antibody>> newAntibodyHashmap = new HashMap<>();
        for (String label: labels){
            newAntibodyHashmap.put(label,new ArrayList<>());
        }
        for(Antibody antibody: survivors){
            newAntibodyHashmap.get(antibody.getLabel()).add(antibody);
        }
        this.antibodyMap = newAntibodyHashmap;
    }

    public void initialisePopulation(int populationSize, boolean shouldBeConnected){
        for(String label: labels){
            this.antibodyMap.put(label,new ArrayList<>());
        }
        int antibodyCount = 0;
        for(String label:antigenMap.keySet()){
            if(antibodyCount >= populationSize){
                break;
            }

            int labelCount = (int)(((double)this.antigenMap.get(label).size()/antigens.length)*populationSize);

            for (int i=0;i<labelCount;i++){
                Antibody antibody = createAntibody(label,shouldBeConnected);
                this.antibodyMap.get(label).add(antibody);
                antibodies[antibodyCount ++] = antibody;
            }
        }

        while (antibodyCount < populationSize){
            Antigen radnomAntigen = antigens[random.nextInt(antigens.length)];
            String label = radnomAntigen.getLabel();

            Antibody antibody = createAntibody(label,shouldBeConnected);
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

    public Antibody createAntibody(String label, boolean shouldBeConnected){

        while (true){
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
            double radius = (minAverage + (maxAverage - minAverage) * random.nextDouble()) + (featureMap.get(label).length * radiusMultiplier);


            Antibody antibody = new Antibody(attributes, radius, label, this.antigens,this);
            if(!shouldBeConnected){
                return antibody;
            }
            else{
                if(antibody.isConnected()){
                    return antibody;
                }
            }
        }
    }

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
                            double voteWeight = (1 / (distance)) * antibody.getAccuracy();
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

                //no classification was given, calculate nearest knn
                if(highestVoteLabel == null){
                    antigenClassification.put(antigen, knn(antigen,antibodyMap, 5));
                }else{
                    antigenClassification.put(antigen, highestVoteLabel);
                }
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

    private static String knn(Antigen antigen, HashMap<String,ArrayList<Antibody>> antibodyMap, int k){
        ArrayList<Antibody>  antibodyList = new ArrayList<Antibody>();

        // Comparator sorting smallest first
        final Comparator<DistanceTuple> distanceComparator = (o1, o2) -> {
            double distance1 = o1.getDistance();
            double distance2 = o2.getDistance();
            if (distance1 > distance2) {
                return 1;
            }
            else if (distance1 < distance2) {
                return -1;
            }
            return 0;
        };


        for (String label: antibodyMap.keySet()){
            antibodyList.addAll(antibodyMap.get(label));
        }
        PriorityQueue<DistanceTuple> distances = new PriorityQueue<>(k,distanceComparator);

        for (Antibody antibody : antibodyList){
            double distance = antibody.eucledeanDistance(antibody.getFeatures(), antigen.getAttributes());
            distances.add(new DistanceTuple(distance, antibody));
        }

        HashMap<String, Integer> counterNearest = new HashMap<>();

        for(int i = 0; i < k; i++){
            var distanceTuple = distances.remove();
            var label = distanceTuple.getAntibody().getLabel();

            if(counterNearest.containsKey(label)){
                counterNearest.put(label, counterNearest.get(label) + 1);
            }else{
                counterNearest.put(distanceTuple.getAntibody().getLabel(), 1);
            }
        }
        return Collections.max(counterNearest.entrySet(), Map.Entry.comparingByValue()).getKey();

    }


    public double randomOffspringSize(int populationSize,int iteration, int maxIterations){
        return 0.5 * Math.pow((double)2/populationSize,(double)iteration/maxIterations);
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

    public void setAntibodyMap(HashMap<String, ArrayList<Antibody>> antibodyMap) {
        this.antibodyMap = antibodyMap;
    }

    public double getBestAccuracy() {
        return bestAccuracy;
    }

    public void setBestAccuracy(double bestAccuracy) {
        this.bestAccuracy = bestAccuracy;
    }

    public int getBestIteration() {
        return bestIteration;
    }

    public void setBestIteration(int bestIteration) {
        this.bestIteration = bestIteration;
    }

    public double getBestAccuracyTestSet() {
        return bestAccuracyTestSet;
    }

    public void setBestAccuracyTestSet(double bestAccuracyTestSet) {
        this.bestAccuracyTestSet = bestAccuracyTestSet;
    }

    public int getBestIterationTestSet() {
        return bestIterationTestSet;
    }

    public void setBestIterationTestSet(int bestIterationTestSet) {
        this.bestIterationTestSet = bestIterationTestSet;
    }

    public int getIteration() {
        return iteration;
    }

    public void setIteration(int iteration) {
        this.iteration = iteration;
    }

    public int getMaxIterations() {
        return maxIterations;
    }

    public void setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    public int getPopulationSize() {
        return populationSize;
    }

    public ArrayList<String> getLabels() {
        return labels;
    }

    public void setLabels(ArrayList<String> labels) {
        this.labels = labels;
    }

    public HashMap<String, ArrayList<Antigen>> getAntigenValidationMap() {
        return antigenValidationMap;
    }

    public void setAntigenValidationMap(HashMap<String, ArrayList<Antigen>> antigenValidationMap) {
        this.antigenValidationMap = antigenValidationMap;
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


