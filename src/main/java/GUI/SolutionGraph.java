package GUI;

import AIS.Antibody;
import AIS.Antigen;
import AIS.AIS;
import javafx.application.Platform;
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

    private final Pane antigenPane, antibodyPane, connectionPane;
    private double minX, minY, maxX, maxY;
    private double height, factorX, factorY;

    private final Color[] colors = {Color.GREEN, Color.RED, Color.ORANGE, Color.BLUE, Color.PURPLE, Color.PINK, Color.AQUA, Color.GOLDENROD, Color.LIME, Color.DARKRED, Color.DARKBLUE, Color.DARKOLIVEGREEN};

    private HashMap<String, ArrayList<Antigen>> antigenMap;
    private HashMap<String, ArrayList<Antibody>> antibodyMap;
    private HashMap<String, double[][]> featureMap;
    private HashMap<String,String> colorMap;
    private Text accuracyText = new Text();
    private Text bestAccuracyText = new Text();
    private double accuracy;
    private double width;
    public SolutionGraph(double width, double height, HashMap<String, double[][]> featureMap, HashMap<String, ArrayList<Antibody>> antibodyMap) {
        super();
        antigenPane = new Pane();
        antibodyPane = new Pane();
        connectionPane = new Pane();
        super.setMinSize(width, height);
        super.setMaxSize(width, height);
        super.getChildren().addAll(connectionPane, antibodyPane, antigenPane);
        this.featureMap = featureMap;
        this.colorMap = new HashMap<>();
        this.antibodyMap = antibodyMap;
        this.width = width;
        setRandomColors(this.featureMap);
        setBounds(featureMap, antibodyMap,false);
        this.accuracy = 0.0;
        this.height = height;

        accuracyText.setY(height+40);
        accuracyText.setX(((width/2)*0.5)+100);

        bestAccuracyText.setY(height+60);
        bestAccuracyText.setX((width/2)*0.5);

        super.getChildren().addAll(accuracyText, bestAccuracyText);
    }

    public void drawSolutionGraph(HashMap<String, ArrayList<Antigen>> antigenMap,HashMap<String, ArrayList<Antibody>> antibodyMap, double accuracy, boolean radiusPlot){
        setBounds(featureMap,antibodyMap,radiusPlot);
        this.factorX = width / Math.abs(this.maxX - this.minX);
        this.factorY = height / Math.abs(this.maxY - this.minY);
        this.accuracy = accuracy;
        //plot antigens
        //plot antibodies
        this.setAntibodies(antibodyMap,radiusPlot);
        this.setAntigens(antigenMap);

        if(!radiusPlot){
            this.setConnections();
        }

        if(accuracy > 0.0){
            setAccuracy(this.accuracy);
        }else{
            setAccuracy(AIS.vote(this.antigenMap,this.antibodyMap,null));
        }
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

    public void setAntibodies(HashMap<String, ArrayList<Antibody>> antibodyMap, boolean radiusPlot) {
        this.antibodyMap = antibodyMap;
        antibodyPane.getChildren().clear();
        for (String label : antibodyMap.keySet()) {
            String color = this.colorMap.get(label);
            var al = antibodyMap.get(label);
            for (Antibody antibody : al){
                if(radiusPlot){
                    Circle antibodyCircle = new Circle(antibody.getRadius()*factorX);
                    antibodyCircle.setTranslateX(mapXToGraph(antibody.getFeatures()[0]));
                    antibodyCircle.setTranslateY(mapYToGraph(antibody.getFeatures()[1]));
                    antibodyCircle.setFill(Paint.valueOf(color));
                    antibodyCircle.setOpacity(0.1);
                    antibodyCircle.setStroke(Color.BLACK);
                    antibodyPane.getChildren().add(antibodyCircle);

                }else{
                    Circle antibodyCircle = new Circle(5);
                    antibodyCircle.setTranslateX(mapXToGraph(antibody.getFeatures()[0]));
                    antibodyCircle.setTranslateY(mapYToGraph(antibody.getFeatures()[1]));
                    antibodyCircle.setFill(Paint.valueOf(color));
                    antibodyCircle.setStroke(Color.BLACK);
                    antibodyPane.getChildren().add(antibodyCircle);
                }
            }
        }
    }


    public void setConnections(){
        connectionPane.getChildren().clear();
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
                            connectionPane.getChildren().add(connection);
                        }
                    }
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

    private void setBounds(HashMap<String, double[][]> featureMap, HashMap<String, ArrayList<Antibody>> antibodyMap, boolean radiusPlot) {
        double lowestValuedFeatureX = Double.MAX_VALUE;
        double highestValuedFeatureX = Double.NEGATIVE_INFINITY;

        double lowestValuedFeatureY = Double.MAX_VALUE;
        double highestValuedFeatureY = Double.NEGATIVE_INFINITY;

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

        if(radiusPlot){
            for (String antibodyLabel : antibodyMap.keySet()){
                for(Antibody antibody : antibodyMap.get(antibodyLabel)){
                    var features = antibody.getFeatures();

                    if (features[0] -antibody.getRadius() < lowestValuedFeatureX) {
                        lowestValuedFeatureX = features[0] -antibody.getRadius();
                    }
                    if (features[0] +antibody.getRadius() > highestValuedFeatureX) {
                        highestValuedFeatureX = features[0] +antibody.getRadius();
                    }

                    if (features[1] -antibody.getRadius() < lowestValuedFeatureY) {
                        lowestValuedFeatureY = features[1] -antibody.getRadius();
                    }
                    if (features[1] + antibody.getRadius() > highestValuedFeatureY) {
                        highestValuedFeatureY = features[1] + antibody.getRadius();
                    }
                }
            }
        }else{
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
        }
        this.minX = lowestValuedFeatureX;
        this.minY = lowestValuedFeatureY;
        this.maxX = highestValuedFeatureX;
        this.maxY = highestValuedFeatureY;

        if(minX < minY){
            minY = minX;
        }else{
            minX = minY;
        }

        if(maxX > maxY){
            maxY = maxX;
        }else{
            maxX = maxY;
        }

        this.factorX = width / Math.abs(this.maxX - this.minX);
        this.factorY = height / Math.abs(this.maxY - this.minY);
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