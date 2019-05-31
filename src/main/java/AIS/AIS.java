package AIS;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

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
    private double currentAccuracy;
    private int bestIteration;
    private double bestAccuracyTestSet;
    private int bestIterationTestSet;
    private ArrayList<String> labels;
    private int iteration;
    private int maxIterations;
    private double averageFitness;
    private int offspringSize;
    private int islandCount;
    public int islandIndex;
    public boolean globalSharingFactor;
    public AIS(Antigen[] antigens, HashMap<String,double[][]> featureMap, ArrayList<String> labels, HashMap<String,ArrayList<Antigen>> antigenMap,HashMap<String,ArrayList<Antigen>> antigenValidationMap,int populationSize, double mutationRate, int numberOfTorunaments, int maxIterations, int islandCount,boolean globalSharingFactor){
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
        this.currentAccuracy = 0.0;
        this.bestIteration = 0;
        this.bestAccuracyTestSet = 0.0;
        this.bestIterationTestSet = 0;
        this.featureMap = featureMap;
        this.labels = labels;
        this.maxIterations = maxIterations;
        this.antigenValidationMap = antigenValidationMap;
        this.offspringSize = populationSize;
        this.islandCount = islandCount;
        this.islandIndex = random.nextInt(2)+1;
        this.globalSharingFactor = globalSharingFactor;
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

        initialisePopulation(this.populationSize,true);
    }

    public void iterate(){
        this.iteration++;

        ArrayList<Antibody> newAntibodiesOfLabel = new ArrayList<>();

        //offspringSize = (int)(this.populationSize*randomOffspringSize(this.populationSize,this.iteration,this.maxIterations));
        //double offspringSize = populationSize;
        for(int i=0;i<offspringSize;i++){
                String randomLabel = antigens[random.nextInt(antigens.length)].getLabel();
                //String randomLabel2 = this.labels.get(random.nextInt(labels.size()));
                ArrayList<Antibody> antibodiesToExclude = new ArrayList<>();
                Antibody parent1;
                Antibody parent2 = null;
                //boolean crossover = true;
                double rand = 2.0;
                /*if(rand < (double)iteration / maxIterations){
                    crossover = false;
                }*/
                if(antibodyMap.get(randomLabel).size() > numberOfTournaments){
                    parent1 = tournamentSelection(antibodyMap.get(randomLabel), numberOfTournaments,antibodiesToExclude);
                    if(parent1.getTotalInteraction() == 0.0){ //if the antibody is not able to recognize anything correctly, do not allow it to reproduce
                        parent1 = createAntibody(randomLabel,true);
                    }
                    antibodiesToExclude.add(parent1);
                    //if(crossover){
                        parent2 = tournamentSelection(antibodyMap.get(randomLabel), numberOfTournaments,antibodiesToExclude);
                        if(parent2.getTotalInteraction() == 0.0){
                            parent2 = createAntibody(randomLabel,true);
                        }
                    //}
                }
                else{
                    parent1 = createAntibody(randomLabel,false);
                    //if(crossover){
                    parent2 = createAntibody(randomLabel,false);
                    //}

                }

                //crossover across classes
            /*if(antibodyMap.get(randomLabel2).size() > numberOfTournaments){
                parent2 = tournamentSelection(antibodyMap.get(randomLabel2), numberOfTournaments);
                if(parent2.getTotalInteraction() == 0.0){
                    parent2 = createAntibody(randomLabel2,true);
                }
            }
            else{
                parent2 = createAntibody(randomLabel2,false);
            }*/
            Antibody child;
                //if(!crossover){
                    //child = new Antibody(parent1.getFeatures(),parent1.getRadius(),parent1.getLabel(),parent1.getAntigens(),this,parent1.getFeaturesWeights());
                    //this.mutate(child);
                //}else{
                    child = crossover(parent1,parent2);
                    //child.setConnectedAntigens();
                    double p = Math.random();
                    if(p <= this.mutationRate){
                        this.mutate(child);
                    }
                //}
                //children[childrenCount ++] = child;
                newAntibodiesOfLabel.add(child);
            }
            for(Antibody antibody:newAntibodiesOfLabel){
                antibodyMap.get(antibody.getLabel()).add(antibody);
            }

        if(islandCount == 1 || !globalSharingFactor){   //only basic ais
            for(Antigen antigen:antigens){
                antigen.setConnectedAntibodies(new ArrayList<>());
                antigen.setTotalInteraction(0.0);
                antigen.getInteractionMap().put(this,0.0);
            }
        }

        //set connections
        for(String label: antibodyMap.keySet()){
            for(Antibody antibody:antibodyMap.get(label)){
                antibody.setConnectedAntigens();
                if(islandCount == 1 || !globalSharingFactor){   //only basic AIS so we calculate local sharing factor
                    antibody.setInteraction();
                }
            }
        }
        //calculate fitness after connections has been set
        for(String label: antibodyMap.keySet()) {
            for(Antibody antibody:antibodyMap.get(label)) {
                antibody.calculateFitness();
            }
        }
        this.antibodyMap = fitnessProportionateSelection(this.antibodyMap,this.populationSize,this.labels);
        //this.antibodyMap = extremeElitism(this.antibodyMap,this.populationSize,this.labels);

        //this.select();

    }


    private void mutate(Antibody antibody){
        double p = Math.random();
        double r;

        //if(p > 0.5){//mutate radius
            r = ThreadLocalRandom.current().nextDouble(0.8, 1.2);
            antibody.setRadius(antibody.getRadius()*r);
        //}else {
            double probability = (double)1/(1+antibody.getFeatures().length);
            for(int i=0;i<antibody.getFeatures().length;i++){
                double rand = Math.random();
                if(rand >= probability){
                    r = ThreadLocalRandom.current().nextDouble(0.1, 2);
                    antibody.getFeatures()[i] *= r;
                }
            }
            /*for(int i=0;i<antibody.getFeaturesWeights().length;i++){
            double rand = Math.random();
            if(rand >= probability){
                r = ThreadLocalRandom.current().nextDouble(0.8, 1.2);
                antibody.getFeaturesWeights()[i] *= r;
            }
        }*/
        //}

        //int randomIndex = random.nextInt(antibody.getFeatures().length);
        //antibody.getFeaturesWeights()[randomIndex] = !antibody.getFeaturesWeights()[randomIndex];
    }

    private Antibody crossover(Antibody parent1, Antibody parent2){

        Antibody bestParent;
        if(parent1.getFitness() > parent2.getFitness()){
            bestParent = parent1;
        }else{
            bestParent = parent2;
        }
        double[] features = new double[parent1.getFeatures().length];
        //double[] featureWeights = new double[parent1.getFeatures().length];

        for(int i=0;i<features.length;i++){
            double rand = Math.random();
            if(rand > 0.5){
                features[i] = parent1.getFeatures()[i];
                //featureWeights[i] = parent1.getFeaturesWeights()[i];
            }else{
                features[i] = parent2.getFeatures()[i];
                //featureWeights[i] = parent2.getFeaturesWeights()[i];
            }
        }
        Antibody antibody = new Antibody(features,this.calculateNewRadius(bestParent),parent1.getLabel(),this.antigens,this,null);
        /*double bestAffinity = antibody.calculateAffinity();
        for(int i=0;i<antibody.getFeatures().length;i++){
            Antibody newAntibody = new Antibody(features,this.calculateNewRadius(bestParent),parent1.getLabel(),this.antigens,this,bestParent.getFeaturesWeights());
            int randomIndex = random.nextInt(newAntibody.getFeatures().length);
            newAntibody.getFeaturesWeights()[randomIndex] = !newAntibody.getFeaturesWeights()[randomIndex];
            double affinity = newAntibody.calculateAffinity();

            if(affinity > bestAffinity){
                bestAffinity = affinity;
                antibody = newAntibody;
            }
        }*/


        return antibody;
    }

    private double calculateNewRadius(Antibody bestParent){
        double radius = bestParent.getRadius();

        //radius *= ThreadLocalRandom.current().nextDouble(0.1, 2);
        //double rand = Math.random();
        /*if(rand > 0.5){
            radius *= 1.1;
        }else{
            radius *= 0.9;
        }*/

        return radius;
    }

    private Antibody tournamentSelection(ArrayList<Antibody> antibodies, int numberOfTournaments, ArrayList<Antibody> antibodiesToExclude){
        Antibody winner = null;

        for (int i = 0; i < numberOfTournaments; i ++) {
            Antibody participant = antibodies.get(random.nextInt(antibodies.size()));

            while (antibodiesToExclude.contains(participant)){
                participant = antibodies.get(random.nextInt(antibodies.size()));
            }

            if (winner == null || participant.getFitness() > winner.getFitness()) {
                winner = participant;
            }
        }
        return winner;
    }

    private void rankSelection(){
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

    public static HashMap<String, ArrayList<Antibody>> fitnessProportionateSelection(HashMap<String, ArrayList<Antibody>> antibodyMap, int populationSize, ArrayList<String> labels){
        HashMap<String, ArrayList<Antibody>> newAntibdyMap = new HashMap<>();
        Random random = new Random();

        //use fitness selection comparator
        Comparator<Antibody> selectionComparator = (o1, o2) -> {
            if (o1.getFitness() > o2.getFitness()) {
                return -1;
            }
            else if (o1.getFitness() < o2.getFitness()) {
                return 1;
            }
            return 0;
        };

        //list for sorting
        final ArrayList<Antibody> priorityQueue = new ArrayList<>();

        for (String label:antibodyMap.keySet()){
            priorityQueue.addAll(antibodyMap.get(label));
        }

        priorityQueue.sort(selectionComparator);

        final Antibody[] survivors = new Antibody[populationSize];
        HashSet<Integer> hashSet = new HashSet<>();

        int index = 0;

        while (index < survivors.length) {
            double p = Math.random();
            //make survival selection more random as we go

            double fitnessSum = 0.0;
            for(int i=0;i<priorityQueue.size();i++){
                fitnessSum += priorityQueue.get(i).getFitness();
            }
            double cumulativeProbability = 0.0;
            int listIndex = 0;

            while (!priorityQueue.isEmpty()){
                if(listIndex == priorityQueue.size()){
                    survivors[index ++] = priorityQueue.remove(random.nextInt(priorityQueue.size()));
                    break;
                }
                Antibody currentAntibody = priorityQueue.get(listIndex);
                cumulativeProbability += currentAntibody.getFitness()/fitnessSum;

                if(p <= cumulativeProbability && !hashSet.contains(Arrays.hashCode(currentAntibody.getFeatures()))){

                    survivors[index ++] = priorityQueue.remove(listIndex);
                    hashSet.add(Arrays.hashCode(currentAntibody.getFeatures()));
                    break;
                }
                listIndex++;
            }
        }
        //HashSet<Integer> hashSet1 = new HashSet<>();

        for (String label: labels){
            newAntibdyMap.put(label,new ArrayList<>());
        }
        for(Antibody antibody: survivors){
            /*int hashCode = Arrays.hashCode(antibody.getFeatures());
            if(hashSet1.contains(hashCode)){
                System.out.println("hey");
            }else{
                hashSet1.add(hashCode);
            }*/
            newAntibdyMap.get(antibody.getLabel()).add(antibody);
        }

        return newAntibdyMap;
    }

    public static HashMap<String, ArrayList<Antibody>> extremeElitism(HashMap<String, ArrayList<Antibody>> antibodyMap, int populationSize, ArrayList<String> labels) {
        HashMap<String, ArrayList<Antibody>> newAntibdyMap = new HashMap<>();
        Random random = new Random();

        //use fitness selection comparator
        Comparator<Antibody> selectionComparator = (o1, o2) -> {
            if (o1.getFitness() > o2.getFitness()) {
                return -1;
            }
            else if (o1.getFitness() < o2.getFitness()) {
                return 1;
            }
            return 0;
        };

        //list for sorting
        final ArrayList<Antibody> priorityQueue = new ArrayList<>();

        for (String label:antibodyMap.keySet()){
            priorityQueue.addAll(antibodyMap.get(label));
        }

        priorityQueue.sort(selectionComparator);

        final Antibody[] survivors = new Antibody[populationSize];
        HashSet<Integer> hashSet = new HashSet<>();

        for(int i =0; i<survivors.length;i++){
            boolean antibodyNotFound = true;
            while (antibodyNotFound){
            Antibody antibody = priorityQueue.remove(0);

            if(!hashSet.contains(Arrays.hashCode(antibody.getFeatures()))){
                survivors[i] = antibody;
                hashSet.add(Arrays.hashCode(antibody.getFeatures()));
                antibodyNotFound = false;
            }
            }
        }

        for (String label: labels){
            newAntibdyMap.put(label,new ArrayList<>());
        }
        for(Antibody antibody: survivors){
            newAntibdyMap.get(antibody.getLabel()).add(antibody);
        }

        return newAntibdyMap;
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

            double overallMax = Double.NEGATIVE_INFINITY;
            double overallMin = Double.MAX_VALUE;

            for(int i=0; i<featureMap.get(label).length;i++){
                double[] featureBounds = featureMap.get(label)[i];
                double maxValue = featureBounds[1]*1.1;
                double minValue = featureBounds[0]*0.9;

                /*if(minValue < overallMin){
                    overallMin = minValue;
                }
                if(maxValue > overallMax){
                    overallMax  = maxValue;
                }*/

                maxAverage += maxValue;
                minAverage += minValue;

                attributes[i] = minValue + (maxValue - minValue)*random.nextDouble();
            }

            minAverage = minAverage/featureMap.get(label).length;
            maxAverage = maxAverage/featureMap.get(label).length;


            //TODO: Make initial radius better
            //double radius = ThreadLocalRandom.current().nextDouble(minAverage, maxAverage) + (featureMap.get(label).length * radiusMultiplier);
            //double radius = (minAverage + (maxAverage - minAverage) * random.nextDouble()) + (featureMap.get(label).length * radiusMultiplier);
            //double radius = (overallMin + (overallMax - overallMin) * random.nextDouble()) + (featureMap.get(label).length * radiusMultiplier);
            Antigen randomAntigen = antigenMap.get(label).get(random.nextInt(antigenMap.get(label).size()));
            double radius = AIS.eucledeanDistance(attributes,randomAntigen.getAttributes());
            Antibody antibody = new Antibody(attributes, radius, label, this.antigens,this,null);
            return antibody;
            /*if(!shouldBeConnected){
                return antibody;
            }
            else{
                if(antibody.isConnected()){
                    return antibody;
                }
            }*/
        }
    }

    public static double vote(HashMap<String,ArrayList<Antigen>> antigenMap,HashMap<String,ArrayList<Antibody>> antibodyMap, AIS ais) {
        HashMap<Antigen, String> antigenClassification = new HashMap<>();
        for(String antigenLabel: antigenMap.keySet()){
            for (Antigen antigen : antigenMap.get(antigenLabel)) {
                HashMap<String, Double> votingMap = new HashMap<>();
                for(String antibodyLabel: antibodyMap.keySet()){
                    for (Antibody antibody : antibodyMap.get(antibodyLabel)) {
                        double distance = antibody.eucledeanDistance(antibody.getFeatures(), antigen.getAttributes());
                        if (distance <= antibody.getRadius()) {
                            //antibody is inside recognition radius
                            double voteWeight = (1 / (distance)) * antibody.getWeightedAccuracy();
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
                    //if(ais!=null){
                    /*if(ais == null){
                        antigen.addDanger(1.0);
                    }*/
                    if(ais != null){
                        //antigen.getAntigenWeights().put(ais,antigen.getAntigenWeights().get(ais)+1);
                        antigen.addDanger(1.0);
                    }
                    //}
                }/*else{
                    if(ais != null){
                        antigen.addDanger(ais,1);
                    }
                }*/
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
            double distance = antibody.eucledeanDistance(antibody.getFeatures(), antigen.getAttributes()) - antibody.getRadius();
            if (distance < 0){
                distance = 0;
            }
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

    public double getCurrentAccuracy() {
        return currentAccuracy;
    }

    public void setCurrentAccuracy(double currentAccuracy) {
        this.currentAccuracy = currentAccuracy;
    }

    public int getIslandCount() {
        return islandCount;
    }

    public void setIslandCount(int islandCount) {
        this.islandCount = islandCount;
    }

    public static double eucledeanDistance(double[] featureSet1, double[] featureSet2){

        double eucledeanDistance = 0.0;
        for (int i=0;i<featureSet1.length;i++){
            eucledeanDistance +=(Math.pow(featureSet1[i] - featureSet2[i],2));
        }

        eucledeanDistance = Math.sqrt(eucledeanDistance);
        return eucledeanDistance;
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


