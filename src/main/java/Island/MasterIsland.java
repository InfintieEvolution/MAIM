package Island;

import AIS.AIS;
import AIS.Antibody;
import java.util.*;

public class MasterIsland {

    private AIS ais;
    private double migrationRate;
    private double migrationFrequency;
    private Comparator<Antibody> migrationSelectionComparator;
    private int numberOfMigrants;
    private ArrayList<Island> allIslands;
    Random random = new Random();

    public MasterIsland(AIS ais, double migrationRate, double migrationFrequency, ArrayList<Island> allIslands) {
        this.ais = ais;
        this.migrationRate = migrationRate;
        this.migrationFrequency = migrationFrequency;
        this.numberOfMigrants = (int) (migrationRate * ais.getPopulationSize());
        this.allIslands = allIslands;

        this.migrationSelectionComparator = (o1, o2) -> {
            if (o1.getFitness() > o2.getFitness()) {
                return -1;
            }
            else if (o1.getFitness() < o2.getFitness()) {
                return 1;
            }
            return 0;
        };
    }

    public void removeRandomAntibodies() {
        Set<String> labels = this.ais.getAntibodyMap().keySet();
        ArrayList<Antibody>allAntibodies = new ArrayList<>();
        for (String label : labels) {
            allAntibodies.addAll(this.ais.getAntibodyMap().get(label));
        }

        for (int i=0; i < numberOfMigrants; i++) {
            int randomIndex = random.nextInt((allAntibodies.size()-1)+1);
            var antibody = allAntibodies.get(randomIndex);

            this.ais.getAntibodyMap().get(antibody.getLabel()).remove(antibody);
        }
    }

    public void receive(Island fromIsland) {
//        this.removeRandomAntibodies();
        var receivingAntibodies = fromIsland.sendMigrants();
        for (Antibody antibody: receivingAntibodies) {
            this.ais.getAntibodyMap().get(antibody.getLabel()).add(antibody);
        }
    }


    public void removeWorstAntibodies() {
        Set<String> labels = this.ais.getAntibodyMap().keySet();
        ArrayList<Antibody> allAntibodies = new ArrayList<>();
        for (String label : labels) {
            allAntibodies.addAll(ais.getAntibodyMap().get(label));
        }
        allAntibodies.sort(migrationSelectionComparator); // Sort the antibodies
        ArrayList<Antibody> worstAntibodies = new ArrayList<>(allAntibodies.subList(allAntibodies.size()-this.numberOfMigrants, allAntibodies.size()));
        for (Antibody antibody : worstAntibodies) {
            this.ais.getAntibodyMap().get(antibody.getLabel()).remove(antibody);
        }
    }


    public AIS getAis() {
        return ais;
    }

    public double getMigrationRate() {
        return migrationRate;
    }

    public double getMigrationFrequency() {
        return migrationFrequency;
    }

    public int getNumberOfMigrants() {
        return numberOfMigrants;
    }
}
