package GUI;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

import java.util.Locale;

class StatisticGraph extends Pane {

    private final double width, height;
    private final int iterations;

    private int currentIteration = 0;
    private double previousX, previousY;

    private Text bestAccuracyText = new Text();

    StatisticGraph(double width, double height, int iterations) {
        super();
        super.setMouseTransparent(true);
        //super.setTranslateY(-height);
        super.setMinSize(width, height);
        super.setMaxSize(width, height);
        //height *= 2;

        this.width = width;
        this.height = height;
        this.iterations = iterations;

        final Line zeroLine = new Line();
        zeroLine.setStroke(Color.RED);
        zeroLine.setStartX(0);
        zeroLine.setStartY(0);
        zeroLine.setEndX(width);
        zeroLine.setEndY(0);

        final Line halfLine = new Line();
        halfLine.setStroke(Color.GRAY);
        halfLine.setStartX(0);
        halfLine.setStartY(height * 0.5);
        halfLine.setEndX(width);
        halfLine.setEndY(height * 0.5);

        final Line approvedLine = new Line();
        approvedLine.setStroke(Color.YELLOW);
        approvedLine.setStartX(0);
        approvedLine.setStartY(height * 0.90);
        approvedLine.setEndX(width);
        approvedLine.setEndY(height * 0.90);

        final Line optimalLine = new Line();
        optimalLine.setStroke(Color.GREEN);
        optimalLine.setStartX(0);
        optimalLine.setStartY(height);
        optimalLine.setEndX(width);
        optimalLine.setEndY(height);

        Text zeroText = new Text("0%");
        zeroText.setY(zeroLine.getEndY());
        zeroText.setX(-20);

        Text halfText = new Text("50%");
        halfText.setY(halfLine.getEndY());
        halfText.setX(-25);

        Text approvedText = new Text("90%");
        approvedText.setY(approvedLine.getEndY());
        approvedText.setX(-25);

        Text optimalText = new Text("100%");
        optimalText.setY(optimalLine.getEndY());
        optimalText.setX(-32);

        bestAccuracyText.setY(height+50);
        bestAccuracyText.setX((width/2)*0.7);
        BorderPane.setAlignment(bestAccuracyText, Pos.CENTER);
        super.getChildren().addAll(zeroLine, halfLine, approvedLine, optimalLine, zeroText, halfText, approvedText, optimalText, bestAccuracyText);
    }

    void addIteration(double fitness) {
        final Line line = new Line();
        line.setStroke(Color.BLUE);

        final double endX = ((double) currentIteration ++ / iterations) * width;
        final double endY = fitness * height;
        line.setEndX(endX);
        line.setEndY(endY);

        if (previousY == 0) {
            previousY = endY;
        }
        line.setStartX(previousX);
        line.setStartY(previousY);

        previousX = endX;
        previousY = endY;
        Platform.runLater(() -> super.getChildren().add(line));
    }

    void setBestAccuracy(double accuracy) {
        Platform.runLater(() -> bestAccuracyText.setText(String.format(Locale.US, "Highest achieved accuracy: %.2f%%", accuracy*100)));
    }
}
