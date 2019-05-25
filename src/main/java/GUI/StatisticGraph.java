package GUI;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

import java.util.Locale;

class StatisticGraph extends Pane {

    private final double width, height;
    private final int iterations;
    private final int index;
    private int currentIteration = 0;
    private double previousX = 0, previousY = 0;
    private final boolean slave;
    private Text bestAccuracyText = new Text();
    private Text averageAccuracyText = new Text();
    private Text iterationText = new Text();

    private Text labelText = new Text();

    StatisticGraph(double width, double height, int iterations,boolean kfold, boolean slave, int index) {
        super();
        super.setMouseTransparent(true);
        //super.setTranslateY(-height);
        super.setMinSize(width, height);
        super.setMaxSize(width, height);
        //height *= 2;

        this.width = width;
        this.height = height;
        this.iterations = iterations;
        this.index = index;
        this.slave = slave;

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
        line1Text.setY(5);
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

        if(!kfold){
            labelText.setY(-5);
            BorderPane.setAlignment(labelText, Pos.CENTER);
            labelText.setStyle("-fx-font-weight: bold");
            if(slave){
                labelText.setX((width/2)*0.9);
                labelText.setText("Slave "+(index+1));
            }else{
                labelText.setX((width/2)*0.85);
                labelText.setText("Master island");
            }
            bestAccuracyText.setX((width/2)*0.7);
        }else{
            bestAccuracyText.setX((width/2)*0.8);
        }

        this.setStyle("-fx-border-color: rgba(0,0,0,0.1)");
        //this.setStyle("-fx-border-opacity: 0.1");

        bestAccuracyText.setY(height+20);
        BorderPane.setAlignment(bestAccuracyText, Pos.CENTER);

        averageAccuracyText.setY(height+40);
        averageAccuracyText.setX((width/2)*0.8);
        BorderPane.setAlignment(averageAccuracyText, Pos.CENTER);

        iterationText.setY(height+60);
        iterationText.setX((width/2)*0.8);
        BorderPane.setAlignment(iterationText, Pos.CENTER);

        super.getChildren().addAll(line1,line2, line3, line4, line1Text,line2Text, line3Text, line4Text, bestAccuracyText,averageAccuracyText,labelText,iterationText);
    }

    public void addIteration(double fitness, boolean migration) {
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
    void setIteration(int iteration) {
        Platform.runLater(() -> iterationText.setText("Current iteration: "+ iteration));
    }
}
