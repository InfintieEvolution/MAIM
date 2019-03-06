package AIS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class Antigen {

    private double[] attributes;
    private String label;
    private ArrayList<Antibody> connectedAntibodies;
    private double totalInteraction;
    private HashMap<AIS,Double> interactionMap;
    public Antigen(double[] attributes, String label){

        this.attributes = attributes;
        this.label = label;
        this.connectedAntibodies = new ArrayList<>();
        this.totalInteraction = 0.0;
    }

    public double getTotalInteraction() {
        return totalInteraction;
    }

    public void setTotalInteraction(double totalInteraction) {
        this.totalInteraction = totalInteraction;
    }
    public double[] getAttributes() {
        return attributes;
    }

    public void setAttributes(double[] attributes) {
        this.attributes = attributes;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public ArrayList<Antibody> getConnectedAntibodies() {
        return connectedAntibodies;
    }

    public void setConnectedAntibodies(ArrayList<Antibody> connectedAntibodies) {
        this.connectedAntibodies = connectedAntibodies;
    }

    public HashMap<AIS, Double> getInteractionMap() {
        return interactionMap;
    }

    public void setInteractionMap(HashMap<AIS, Double> interactionMap) {
        this.interactionMap = interactionMap;
    }
    public void addInteraction(Antibody antibody){
        if(interactionMap.containsKey(antibody))
        antibody
    }
    public static HashMap<String,ArrayList<Antigen>> createAntigenMap(Antigen[] antigens){
        HashMap<String,ArrayList<Antigen>> antigenMap = new HashMap<>();

        for(Antigen antigen: antigens){
            if(!antigenMap.containsKey(antigen.getLabel())){
                antigenMap.put(antigen.getLabel(),new ArrayList<>(){{add(antigen);}});
            }else{
                antigenMap.get(antigen.getLabel()).add(antigen);
            }
        }
        return antigenMap;
    }

    @Override
    public String toString() {
        return
                "attributes=" + Arrays.toString(attributes) +
                ", label='" + label + '\'';
    }
}


