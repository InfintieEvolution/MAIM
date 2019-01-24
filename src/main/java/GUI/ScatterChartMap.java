//package GUI;
//
//
//import javafx.application.Application;
//import javafx.geometry.Insets;
//import javafx.scene.Group;
//import javafx.scene.Scene;
//import javafx.scene.chart.NumberAxis;
//import javafx.scene.chart.ScatterChart;
//import javafx.scene.chart.XYChart;
//import javafx.scene.control.Button;
//import javafx.scene.layout.HBox;
//import javafx.scene.layout.VBox;
//import javafx.stage.Stage;
//
//
//public class ScatterChartMap extends Application {
//
//    @Override
//    public void start(Stage stage) throws Exception {
//        stage.setTitle("Scatter Chart");
//
//        final NumberAxis xAxis = new NumberAxis(0, 10, 1);
//        final NumberAxis yAxis = new NumberAxis(-100, 500, 50);
//        final ScatterChart<Number, Number> sc = new ScatterChart<>(xAxis, yAxis);
//
//        xAxis.setLabel("Something along with x");
//        yAxis.setLabel("Something along with y");
//
//        XYChart.Series series1 = new XYChart.Series();
//        series1.setName("Option 1");
//
//        series1.getData().add(new XYChart.Data(4.2, 113));
//        series1.getData().add(new XYChart.Data(3.1, 22));
//        series1.getData().add(new XYChart.Data(7.2, 111));
//        series1.getData().add(new XYChart.Data(1.2, 143));
//        series1.getData().add(new XYChart.Data(2.2, 343));
//        series1.getData().add(new XYChart.Data(4.2, 293));
//
//        sc.setPrefSize(500, 400);
//        sc.getData().addAll(series1);
//        Scene scene = new Scene(new Group());
//        final VBox vBox = new VBox();
//        final HBox hBox = new HBox();
//
//        final Button addButton = new Button("Add series");
//        final Button removeButton = new Button("Remove series");
//
//        hBox.setSpacing(10);
//        hBox.getChildren().addAll(addButton, removeButton);
//
//        vBox.getChildren().addAll(sc, hBox);
//        hBox.setPadding(new Insets(10,10,10, 50));
//
//        ((Group) scene.getRoot()).getChildren().add(vBox);
//        stage.setScene(scene);
//        stage.show();
//
//    }
//}
