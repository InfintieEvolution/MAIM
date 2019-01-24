package GUI;

import AIS.Antibody;
import AIS.Antigen;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.HashMap;

public class Graph extends Pane {

    private final Pane depotPane, customerPane, routePane;
    private final int minX, minY;
    private final double height, factorX, factorY;

    private final Color[] colors = {Color.GREEN, Color.RED, Color.ORANGE, Color.BLUE, Color.PURPLE, Color.PINK, Color.AQUA, Color.GOLDENROD, Color.LIME, Color.DARKRED, Color.DARKBLUE, Color.DARKOLIVEGREEN};

    private Antigen[] antigens;
    private Antibody[] antibodies;
    private final HashMap<Antibody, Circle> antibodyCircleHashMap;


    public Graph(double width, double height, int minX, int minY, int maxX, int maxY) {
        super();
        depotPane = new Pane();
        customerPane = new Pane();
        routePane = new Pane();
        super.setMinSize(width, height);
        super.setMaxSize(width, height);
        super.getChildren().addAll(routePane, depotPane, customerPane);

        this.minX = minX;
        this.minY = minY;
        this.height = height;
        this.factorX = width / Math.abs(maxX - minX);
        this.factorY = height / Math.abs(maxY - minY);

        antibodyCircleHashMap = new HashMap<>();
    }

    public void setAntigens(Antigen[] antigens) {
        this.antigens = antigens;
        depotPane.getChildren().clear();

        for (Antigen antigen: antigens) {
            Rectangle depotRectangle = new Rectangle(10,10);
            depotRectangle.setLayoutX(-5);
            depotRectangle.setLayoutY(-5);
            depotRectangle.setTranslateX(mapXToGraph(antigen.getAttributes()[0]));
            depotRectangle.setTranslateY(mapYToGraph(antigen.getAttributes()[1]));
            depotRectangle.setStyle("-fx-fill: #0ac5d4");
            depotPane.getChildren().add(depotRectangle);
        }
    }

    public void setAntibodies(Antibody[] antibodies) {
        this.antibodies = antibodies;
        customerPane.getChildren().clear();

        for (Antibody antibody : antibodies) {
            Circle customerCircle = new Circle(5);
            customerCircle.setTranslateX(mapXToGraph(antibody.getFeatures()[0]));
            customerCircle.setTranslateY(mapYToGraph(antibody.getFeatures()[1]));
            antibodyCircleHashMap.put(antibody, customerCircle);
            customerPane.getChildren().add(customerCircle);

        }
    }

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