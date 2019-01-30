package GUI;

import AIS.Antibody;
import javafx.application.Platform;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class StatGraph extends Pane {

    private double width, height;
    private double previousX, previousY;
    private String[] iterationsArray;
    private ArrayList<HashMap<String, ArrayList<Antibody>>> antibodyGenerations;
    private int currentIteration;
    private Text accuracy;
    private Text bestSolutionText;

    public StatGraph(double width, double height, String[] iterationsArray, ArrayList<HashMap<String,ArrayList<Antibody>>> antibodyGenerations) {
        super();
        this.width = width;
        this.height = height;
        this.iterationsArray = iterationsArray;
        this.antibodyGenerations = antibodyGenerations;

//        super.getChildren().add(bestSolutionText);
    }

    public void addIteration(double accuracy){
        final Line line = new Line();
        line.setStroke(Color.BLUE);

        final double endX = ((double) currentIteration ++ / iterationsArray.length) * width;
        final double endY = accuracy * height;

        line.setEndX(endX);
        line.setEndY(endY);
        if (previousY == 0){
            previousY = endY;
        }

        line.setStartX(previousX);
        line.setStartY(previousY);

        previousY = endY;
        previousX = endX;

        Platform.runLater(() -> super.getChildren().add(line));
    }

    public void setBestSolution(double accuracy, double percent){
        Platform.runLater(() -> bestSolutionText.setText(String.format(Locale.US, "Best accuracy: %.2f\nPercent: %.2f%%", accuracy, percent)));
    }






}
