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
    private HashMap<AIS,Double> dangerMap;

    public Antigen(double[] attributes, String label){

        this.attributes = attributes;
        this.label = label;
        this.connectedAntibodies = new ArrayList<>();
        this.totalInteraction = 0.0;
        this.interactionMap = new HashMap<>();
        this.dangerMap = new HashMap<>();
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
    public void addInteraction(Antibody antibody, double weight){
        if(interactionMap.containsKey(antibody.getAis())){
            this.interactionMap.put(antibody.getAis(),this.interactionMap.get(antibody.getAis()) +weight);
        }else{
            this.interactionMap.put(antibody.getAis(),weight);
        }

    }
    public void addDanger(AIS ais, double danger){
        if(dangerMap.containsKey(ais)){
            this.dangerMap.put(ais,this.dangerMap.get(ais) +danger);
        }else{
            this.dangerMap.put(ais,danger);
        }
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

    public HashMap<AIS, Double> getDangerMap() {
        return dangerMap;
    }

    public void setDangerMap(HashMap<AIS, Double> dangerMap) {
        this.dangerMap = dangerMap;
    }

    @Override
    public String toString() {
        return
                "attributes=" + Arrays.toString(attributes) +
                ", label='" + label + '\'';
    }
}


