package GUI;

import AIS.Antibody;
import AIS.Antigen;
import AIS.AIS;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

public class SolutionGraph extends Pane {

    private final Pane antigenPane, antibodyPane, routePane;
    private int minX, minY, maxX, maxY;
    private double height, factorX, factorY;

    private final Color[] colors = {Color.GREEN, Color.RED, Color.ORANGE, Color.BLUE, Color.PURPLE, Color.PINK, Color.AQUA, Color.GOLDENROD, Color.LIME, Color.DARKRED, Color.DARKBLUE, Color.DARKOLIVEGREEN};

    private HashMap<String, ArrayList<Antigen>> antigenMap;
    private HashMap<String, ArrayList<Antibody>> antibodyMap;
    private HashMap<String, double[][]> featureMap;
    private HashMap<String,String> colorMap;
    private Text accuracyText = new Text();
    private Text bestAccuracyText = new Text();

    private double width;
    public SolutionGraph(double width, double height, HashMap<String, double[][]> featureMap, HashMap<String, ArrayList<Antibody>> antibodyMap) {
        super();
        antigenPane = new Pane();
        antibodyPane = new Pane();
        routePane = new Pane();
        super.setMinSize(width, height);
        super.setMaxSize(width, height);
        super.getChildren().addAll(routePane, antigenPane, antibodyPane);
        this.featureMap = featureMap;
        this.colorMap = new HashMap<>();
        this.antibodyMap = antibodyMap;
        this.width = width;
        setRandomColors(this.featureMap);
        setBounds(featureMap, antibodyMap);

        this.height = height;
        this.factorX = width / Math.abs(this.maxX - this.minX);
        this.factorY = height / Math.abs(this.maxY - this.minY);

        accuracyText.setY(height+50);
        //accuracyText.setX((width/2)*0.5);

        bestAccuracyText.setY(height+70);
        //bestAccuracyText.setX((width/2)*0.5);

        super.getChildren().addAll(accuracyText, bestAccuracyText);
    }

    public void drawSolutionGraph(HashMap<String, ArrayList<Antigen>> antigenMap,HashMap<String, ArrayList<Antibody>> antibodyMap){
        setBounds(featureMap,antibodyMap);
        this.factorX = width / Math.abs(this.maxX - this.minX);
        this.factorY = height / Math.abs(this.maxY - this.minY);
        //plot antigens
        this.setAntigens(antigenMap);
        //plot antibodies
        this.setAntibodies(antibodyMap);
        this.setConnections();
    }
    public void setAntigens(HashMap<String, ArrayList<Antigen>> antigenMap) {
        this.antigenMap = antigenMap;
        antigenPane.getChildren().clear();
        for (String label : antigenMap.keySet()) {
            String color = this.colorMap.get(label);
            for (Antigen antigen : antigenMap.get(label)) {
                Rectangle antigenRectangle = new Rectangle(10, 10);
                antigenRectangle.setLayoutX(-5);
                antigenRectangle.setLayoutY(-5);
                antigenRectangle.setTranslateX(mapXToGraph(antigen.getAttributes()[0]));
                antigenRectangle.setTranslateY(mapYToGraph(antigen.getAttributes()[1]));
                antigenRectangle.setStyle("-fx-fill: " + color);
                antigenRectangle.setStroke(Color.BLACK);
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
                Circle antibodyCircle = new Circle(5);
                antibodyCircle.setTranslateX(mapXToGraph(antibody.getFeatures()[0]));
                antibodyCircle.setTranslateY(mapYToGraph(antibody.getFeatures()[1]));
                antibodyCircle.setFill(Paint.valueOf(color));
                antibodyCircle.setStroke(Color.BLACK);
                antibodyPane.getChildren().add(antibodyCircle);
            }
        }
    }

    public void setConnections(){
        routePane.getChildren().clear();
        for (String antibodyLabel : antibodyMap.keySet()) {
            String color = this.colorMap.get(antibodyLabel);
            for(Antibody antibody: antibodyMap.get(antibodyLabel)){
                for (String antigenLabel : antigenMap.keySet()) {
                    for (Antigen antigen : antigenMap.get(antigenLabel)) {
                        double distance = antibody.eucledeanDistance(antibody.getFeatures(), antigen.getAttributes());
                        if (distance <= antibody.getRadius()) {
                            Line connection = new Line();
                            connection.setStroke(Paint.valueOf(color));
                            connection.setStartX(mapXToGraph(antibody.getFeatures()[0]));
                            connection.setStartY(mapYToGraph(antibody.getFeatures()[1]));
                            connection.setEndX(mapXToGraph(antigen.getAttributes()[0]));
                            connection.setEndY(mapYToGraph(antigen.getAttributes()[1]));
                            connection.setOpacity(0.1);
                            routePane.getChildren().add(connection);
                        }
                    }
                }
            }
        }
        setAccuracy(AIS.vote(this.antigenMap,this.antibodyMap));
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

    private void setBounds(HashMap<String, double[][]> featureMap, HashMap<String, ArrayList<Antibody>> antibodyMap) {
        double lowestValuedFeatureX = Double.MAX_VALUE;
        double highestValuedFeatureX = Double.MIN_VALUE;

        double lowestValuedFeatureY = Double.MAX_VALUE;
        double highestValuedFeatureY = Double.MIN_VALUE;


        for (String label : featureMap.keySet()) {
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
        /*
        for (String antigenLabel : antigenMap.keySet()){
            for(Antigen antigen : antigenMap.get(antigenLabel)){
                var features = antigen.getAttributes();

                if (features[0] < lowestValuedFeatureX) {
                    lowestValuedFeatureX = features[0];
                }
                if (features[0] > highestValuedFeatureX) {
                    highestValuedFeatureX = features[0];
                }

                if (features[1]< lowestValuedFeatureY) {
                    lowestValuedFeatureY = features[1];
                }
                if (features[1]> highestValuedFeatureY) {
                    highestValuedFeatureY = features[1];
                }
            }
        }*/

        for (String antibodyLabel : antibodyMap.keySet()){
            for(Antibody antibody : antibodyMap.get(antibodyLabel)){
                var features = antibody.getFeatures();

                if (features[0] < lowestValuedFeatureX) {
                    lowestValuedFeatureX = features[0];
                }
                if (features[0] > highestValuedFeatureX) {
                    highestValuedFeatureX = features[0];
                }

                if (features[1]< lowestValuedFeatureY) {
                    lowestValuedFeatureY = features[1];
                }
                if (features[1]> highestValuedFeatureY) {
                    highestValuedFeatureY = features[1];
                }
            }
        }

        this.minX = (int)lowestValuedFeatureX;
        this.minY = (int)lowestValuedFeatureY;
        this.maxX = (int)highestValuedFeatureX;
        this.maxY = (int)highestValuedFeatureY;

    }

    void setAccuracy(double accuracy) {
        Platform.runLater(() ->accuracyText.setText(String.format(Locale.US, "Accuracy of set shown: %.2f%%", accuracy*100)));
    }
    void setBestAccuracyIteration(double accuracy, int iteration){
        Platform.runLater(() ->bestAccuracyText.setText(String.format(Locale.US, "Highest accuracy achieved over training set: %.2f%%, at iteration %d", accuracy*100, iteration)));
    }

    private double mapXToGraph(double x) {
        return (x - minX) * factorX;
    }

    private double mapYToGraph(double y) {
        return height - ((y - minY) * factorY);
    }

}