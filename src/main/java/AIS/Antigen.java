package AIS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class Antigen {

    private double[] attributes;
    private String label;
    private ArrayList<Antibody> connectedAntibodies;

    public Antigen(double[] attributes, String label){

        this.attributes = attributes;
        this.label = label;
        this.connectedAntibodies = new ArrayList<>();
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

    @Override
    public String toString() {
        return
                "attributes=" + Arrays.toString(attributes) +
                ", label='" + label + '\'';
    }
}


