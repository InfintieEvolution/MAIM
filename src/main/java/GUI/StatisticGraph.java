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
    private Text averageAccuracyText = new Text();

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

        final Line line1 = new Line();
        line1.setStroke(Color.RED);
        line1.setStartX(0);
        line1.setStartY(0);
        line1.setEndX(width);
        line1.setEndY(0);

        final Line line2 = new Line();
        line2.setStroke(Color.GRAY);
        line2.setStartX(0);
        line2.setStartY(height * 0.5);
        line2.setEndX(width);
        line2.setEndY(height * 0.5);

        final Line line3 = new Line();
        line3.setStroke(Color.YELLOW);
        line3.setStartX(0);
        line3.setStartY(height * 0.90);
        line3.setEndX(width);
        line3.setEndY(height * 0.90);

        final Line line4 = new Line();
        line4.setStroke(Color.GREEN);
        line4.setStartX(0);
        line4.setStartY(height);
        line4.setEndX(width);
        line4.setEndY(height);

        Text line1Text = new Text("0%");
        line1Text.setY(line1.getEndY());
        line1Text.setX(-20);

        Text line2Text = new Text("50%");
        line2Text.setY(line2.getEndY());
        line2Text.setX(-25);

        Text line3Text = new Text("90%");
        line3Text.setY(line3.getEndY());
        line3Text.setX(-25);

        Text line4Text = new Text("100%");
        line4Text.setY(line4.getEndY());
        line4Text.setX(-32);

        bestAccuracyText.setY(height+50);
        bestAccuracyText.setX((width/2)*0.7);
        BorderPane.setAlignment(bestAccuracyText, Pos.CENTER);

        averageAccuracyText.setY(height+70);
        averageAccuracyText.setX((width/2)*0.7);
        BorderPane.setAlignment(averageAccuracyText, Pos.CENTER);

        super.getChildren().addAll(line1, line2, line3, line4, line1Text, line2Text, line3Text, line4Text, bestAccuracyText,averageAccuracyText);
    }

    void addIteration(double fitness, boolean migration) {
        final Line line = new Line();
        if(migration){
            line.setStroke(Color.RED);
        }else{
            line.setStroke(Color.BLUE);
        }
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

    void setAverageAccuracy(double accuracy) {
        Platform.runLater(() -> averageAccuracyText.setText(String.format(Locale.US, "Average achieved accuracy: %.2f%%", accuracy*100)));
    }
}
