# AISIGA
An evolutionary classification algorithm based on a combination two bio-inspired techinques: Artificial immune systems (AIS) and island genetic algorithms (IGA).

The algorithm evolves cluester or "antibodes" that attempt to classify the "antigens" or dataset cases. An antibody that connects to an antigen will vote for the antigen to belong to its class.

Code for the master project of Andreas Norstein and Eirik Baug

Written in Java 11

# Example runs
Antibodies are the circles, while the antigen (training cases) are the squares. Different classes are depicted with different colours.

## Artificialy generated spirals dataset.
This dataset is artificiall generated as 3 classes forming a spiral in two dimensions.

### Radius plot
Here the antibodies are plotted as their recognition regions being colored circles. The antibody will classify any antigen within its recognition region as its own class. Antigen are plotted as squares.

#### Iteration 0 (initial generation)
![Alt text](https://raw.githubusercontent.com/InfintieEvolution/AISIGA/master/Images/Spirals/circles%20-%20iteration%200.PNG)

#### Iteration 3
![Alt text](https://raw.githubusercontent.com/InfintieEvolution/AISIGA/master/Images/Spirals/circles%20-%20iteration%203.PNG)

#### Iteration 4
![Alt text](https://raw.githubusercontent.com/InfintieEvolution/AISIGA/master/Images/Spirals/circles%20-%20iteration%204.PNG)

#### Iteration 6
![Alt text](https://raw.githubusercontent.com/InfintieEvolution/AISIGA/master/Images/Spirals/circles%20-%20iteration%206.PNG)

#### Iteration 529
![Alt text](https://raw.githubusercontent.com/InfintieEvolution/AISIGA/master/Images/Spirals/circles%20-%20iteration%20529.PNG)

### Connection plot
Here the antibodies are plotted as small colored circles while while antigens are still plotted as squares. The colored lines are connections from the antibodies to all the antigens they are connected to.

#### Iteration 0 (Initial generation)
![Alt text](https://raw.githubusercontent.com/InfintieEvolution/AISIGA/master/Images/Spirals/lines%20-%20iteration%200.PNG)

#### Iteration 3
![Alt text](https://raw.githubusercontent.com/InfintieEvolution/AISIGA/master/Images/Spirals/lines%20-%20iteration%203.PNG)

#### Iteration 4
![Alt text](https://raw.githubusercontent.com/InfintieEvolution/AISIGA/master/Images/Spirals/lines%20-%20iteration%204.PNG)

#### Iteration 238
![Alt text](https://raw.githubusercontent.com/InfintieEvolution/AISIGA/master/Images/Spirals/lines%20-%20iteration%20238.PNG)
