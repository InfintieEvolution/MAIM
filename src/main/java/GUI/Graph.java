package GUI;

import AIS.Antibody;
import AIS.Antigen;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Graph extends Pane {

    private final Pane antigenPane, antibodyPane, routePane;
    private int minX, minY, maxX, maxY;
    private final double height, factorX, factorY;

    private final Color[] colors = {Color.GREEN, Color.RED, Color.ORANGE, Color.BLUE, Color.PURPLE, Color.PINK, Color.AQUA, Color.GOLDENROD, Color.LIME, Color.DARKRED, Color.DARKBLUE, Color.DARKOLIVEGREEN};

    private HashMap<String, ArrayList<Antigen>> antigenMap;
    private HashMap<String, ArrayList<Antibody>> antibodyMap;
//    private final HashMap<Antibody, Circle> antibodyCircleHashMap;
    private HashMap<String, double[][]> featureMap;
    private HashMap<String,String> colorMap;

    public Graph(double width, double height, HashMap<String, double[][]> featureMap, HashMap<String, ArrayList<Antibody>> antibodyMap) {
        super();
        antigenPane = new Pane();
        antibodyPane = new Pane();
        routePane = new Pane();
        super.setMinSize(width, height);
        super.setMaxSize(width, height);
        super.getChildren().addAll(routePane, antigenPane, antibodyPane);
        this.featureMap = featureMap;
        this.antibodyMap = antibodyMap;
        this.colorMap = new HashMap<>();

        setRandomColors(this.featureMap);
        setBounds(featureMap);

        this.height = height;
        this.factorX = width / Math.abs(this.maxX - this.minX);
        this.factorY = height / Math.abs(this.maxY - this.minY);

//        antibodyCircleHashMap = new HashMap<>();
    }

    public void setAntigens(HashMap<String, ArrayList<Antigen>> antigenMap) {
        this.antigenMap = antigenMap;
        antigenPane.getChildren().clear();
        for (String label : antigenMap.keySet()) {
            String color = this.colorMap.get(label);
            for (Antigen antigen : antigenMap.get(label)) {
                Rectangle antigenRectangle = new Rectangle(5, 5);
                antigenRectangle.setLayoutX(-5);
                antigenRectangle.setLayoutY(-5);
                antigenRectangle.setTranslateX(mapXToGraph(antigen.getAttributes()[0]));
                antigenRectangle.setTranslateY(mapYToGraph(antigen.getAttributes()[1]));
                antigenRectangle.setStyle("-fx-fill: " + color);
                antigenPane.getChildren().add(antigenRectangle);
            }
        }
    }

    public void setAntibodies(HashMap<String, ArrayList<Antibody>> antibodyMap) {
        this.antibodyMap = antibodyMap;
        antibodyPane.getChildren().clear();
        for (String label : antibodyMap.keySet()) {
            String color = this.colorMap.get(label);
            var al = antibodyMap.get(label);
            for (Antibody antibody : al){
                Circle antibodyCircle = new Circle(antibody.getRadius());
                antibodyCircle.setTranslateX(mapXToGraph(antibody.getFeatures()[0]));
                antibodyCircle.setTranslateY(mapYToGraph(antibody.getFeatures()[1]));
//                antibodyCircleHashMap.put(antibody, antibodyCircle);
                antibodyCircle.setFill(Paint.valueOf("transparent"));
                antibodyCircle.setStroke(Paint.valueOf(color));
                antibodyPane.getChildren().add(antibodyCircle);
            }
        }
    }

    public void setConnections(){
        routePane.getChildren().clear();
        for (String label : antibodyMap.keySet()) {
            String color = this.colorMap.get(label);
                for(Antibody antibody: antibodyMap.get(label)){
                    for(Antigen antigen: antibody.getConnectedAntigen().keySet()){
                        Line connection = new Line();
                        connection.setStroke(Paint.valueOf(color));
                        connection.setStartX(mapXToGraph(antibody.getFeatures()[0]));
                        connection.setStartY(mapYToGraph(antibody.getFeatures()[1]));
                        connection.setEndX(mapXToGraph(antigen.getAttributes()[0]));
                        connection.setEndY(mapYToGraph(antigen.getAttributes()[1]));
                        routePane.getChildren().add(connection);
                    }
                }
            }
        }

    private void setRandomColors(HashMap<String, double[][]> featureMap) {
        // create random object - reuse this as often as possible
        Random random = new Random();
        for(String label: featureMap.keySet()){
            // create a big random number - maximum is ffffff (hex) = 16777215 (dez)
            int nextInt = random.nextInt(0xffffff + 1);
            // format it as hexadecimal string (with hashtag and leading zeros)
            this.colorMap.put(label,String.format("#%06x", nextInt));
        }
    }

    private void setBounds(HashMap<String, double[][]> featureMap) {
        double lowestValuedFeatureX = Double.MAX_VALUE;
        double highestValuedFeatureX = Double.MIN_VALUE;

        double lowestValuedFeatureY = Double.MAX_VALUE;
        double highestValuedFeatureY = Double.MIN_VALUE;

        for (String label : featureMap.keySet()) {
            for (int i = 0; i < featureMap.get(label).length; i++) {
                double[][] f = featureMap.get(label);
                var xFeatLow = f[0][0];
                var xFeatHigh = f[0][1];
                var yFeatLow = f[1][0];
                var yFeatHigh = f[1][1];

                if (xFeatLow < lowestValuedFeatureX) {
                    lowestValuedFeatureX = xFeatLow;
                }
                if (xFeatHigh > highestValuedFeatureX) {
                    highestValuedFeatureX = xFeatHigh;
                }

                if (yFeatLow < lowestValuedFeatureY) {
                    lowestValuedFeatureY = yFeatLow;
                }
                if (yFeatHigh > highestValuedFeatureY) {
                    highestValuedFeatureY = yFeatHigh;
                }

            }
        }
        this.minX = (int)lowestValuedFeatureX;
        this.minY = (int)lowestValuedFeatureY;
        this.maxX = (int)highestValuedFeatureX;
        this.maxY = (int)highestValuedFeatureY;

    }

//    private void setLowerBound(HashMap<String, double[][]> featureMap){
//        double lowestValuedFeature = Double.MAX_VALUE;
//        double highestValuedFeature = Double.MIN_VALUE;
//
//        for(String label : featureMap.keySet()){
//            for (double[] feature : featureMap.get(label) ) {
//                if(feature[0] < lowestValuedFeature){
//                    lowestValuedFeature = feature[0];
//                }
//                if(feature[1] > highestValuedFeature){
//                    highestValuedFeature = feature[1];
//                }
//            }
//        }
//    }
//    void setRoutes(Antibody antibody) {
//        routePane.getChildren().clear();
//
//        for (Antibody antb: antibodies) {
//            int counter = 0;
//            for (Car car : depot.getCars()) {
//                Color color = colors[counter % colors.length];
//                counter ++;
//
//                int currentX = depot.getX();
//                int currentY = depot.getY();
//
//                ArrayList<Customer> customers = car.getCustomerSequence();
//                for (Customer customer : customers) {
//                    Circle customerCircle = antibodyCircleHashMap.get(customer);
//                    customerCircle.setFill(color);
//
//                    Line lineRoute = new Line();
//                    lineRoute.setStroke(color);
//                    lineRoute.setStartX(mapXToGraph(currentX));
//                    lineRoute.setStartY(mapYToGraph(currentY));
//                    lineRoute.setEndX(mapXToGraph(customer.getX()));
//                    lineRoute.setEndY(mapYToGraph(customer.getY()));
//                    currentX = customer.getX();
//                    currentY = customer.getY();
//                    routePane.getChildren().add(lineRoute);
//                }
//
//                Line lineRoute = new Line();
//                lineRoute.setStroke(color);
//                lineRoute.setStartX(mapXToGraph(currentX));
//                lineRoute.setStartY(mapYToGraph(currentY));
//                lineRoute.setEndX(mapXToGraph(depot.getX()));
//                lineRoute.setEndY(mapYToGraph(depot.getY()));
//                routePane.getChildren().add(lineRoute);
//            }
//        }
//    }

    private double mapXToGraph(double x) {
        return (x - minX) * factorX;
    }

    private double mapYToGraph(double y) {
        return height - ((y - minY) * factorY);
    }

}