package AIS;

import java.util.ArrayList;

public class Antigen {

    private double[] attributes;
    private String label;

    public Antigen(double[] attributes, String label){

        this.attributes = attributes;
        this.label = label;
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
}


