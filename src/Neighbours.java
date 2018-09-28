import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static java.lang.Math.round;
import static java.lang.Math.sqrt;
import static java.lang.System.*;

/*
Program Structure:
init();
updateWorld();
setNA();
setUnsatisfied();
calcState();
getAmountOfFriends();
getAmountOfNeighbours();
shuffleMatrix();
switchXandY();
disbributeActorstoWorld();
* Program to simulate segregation.
* See : http://nifty.stanford.edu/2014/mccown-schelling-model-segregation/
*
* NOTE:
* - JavaFX first calls method init() and then method start() far below.
* - To test uncomment call to test() first in init() method!
*
*/
// Extends Application because of JavaFX (just accept for now)
public class Neighbours extends Application {

    // Enumeration type for the Actors
    enum Actor {
        BLUE, RED, NONE, GREEN // NONE used for empty locations
    }

    // Enumeration type for the state of an Actor
    enum State {
        UNSATISFIED,
        SATISFIED,
        NA // Not applicable (NA), used for NONEs
    }

    // Below is the *only* accepted instance variable (i.e. variables outside any method)
// This variable may *only* be used in methods init() and updateWorld()//TODO LOOK HERE
    private Actor[][] world; // The world is a square matrix of Actors
    private double speed = 0.1;

    // This is the method called by the timer to update the world
// (i.e move unsatisfied) approx each 1/60 sec.
    private void updateWorld() {
// % of surrounding neighbours that are like me
        final double threshold = 0.5;

        ArrayList<int[]> naActors = getNA(world);
        Actor[][] unsatActors = getUnsatisfied(threshold, world);
        swapMatrixDataToWorld(naActors, unsatActors, world);
    }

    // This method initializes the world variable with a random distribution of Actors
    // Method automatically called by JavaFX runtime (before graphics appear)
    // Don't care about "@Override" and "public" (just accept for now)
    @Override
    public void init() {
        //test(); // <---------------- Uncomment to TEST!

        // %-distribution of RED, BLUE, GREEN and NONE
        double[] distribution = {0.3, 0.2, 0.3, 0.20};
        //double[] distribution = {0.15, 0.17, 0.18, 0.50};
        //double[] distribution = {0.19, 0.25, 0.55, 0.010};

        // Number of locations (places) in world (square)
        int nLocations = 90000;
        int[] arrDistribution = getDistrubution(distribution, nLocations);
        Actor[] actors = getActorArrInit(arrDistribution, nLocations);
        shuffle(actors);
        world = arrToMat(actors);


// Should be last
        fixScreenSize(nLocations);
    }


// ------- Methods ------------------


    private Actor[] getActorArrInit(int[] arrDistribution, int nLocations){
        Actor[] actors = new Actor[nLocations];
        //Choose colors according to distribution and place in an actors array.
        int index = 0;
        for (int i = 0; i < arrDistribution.length; i++) {
            for (int j = 0; j < arrDistribution[i]; j++) {
                actors[index] = chooseColor(i);
                index++;
            }
        }
        return actors;
    }

    private int[] getDistrubution(double[] distribution, int nLocations){
        int[] arrDistribution = new int[distribution.length];

        //Calculating amount of actors based on distribution and converting from double to int arr.
        for (int i = 0; i < distribution.length; i++) {
            arrDistribution[i] = (int) Math.round(distribution[i] * nLocations);
        }
        return arrDistribution;
    }

    private int getRandNaActor(Random r, List<int[]> na) {
        if (na.size() <= 0) {
            return -1;
        } else {
            return r.nextInt(na.size());
        }
    }

    private void swapMatrixDataToWorld(ArrayList<int[]> naActors, Actor[][] unsatActors, Actor[][] world) {
        //na index 0 = Y, index 1 = X
        Random rand = new Random();
        int size = unsatActors.length;

        for (int i = 0; i < unsatActors.length * unsatActors.length; i++) {
            if (unsatActors[i / size][i % size] != Actor.NONE && unsatActors[i / size][i % size] != null) {

                int randNaActor = getRandNaActor(rand, naActors);
                if (randNaActor != -1) {

                    world[i / size][i % size] = Actor.NONE;         //None to world
                    world[naActors.get(randNaActor)[0]][naActors.get(randNaActor)[1]] = unsatActors[i / size][i % size];    //unSat to world
                    naActors.remove(randNaActor);      //reuse None so they don't run out

                }
            }
        }
    }

    //index 0 = Y, index 1 = X
    private ArrayList<int[]> getNA(Actor[][] world) {
        ArrayList<int[]> naActors = new ArrayList<>();

        //Fill naActors with coordinates of NA within world.
        for (int i = 0; i < world.length; i++) {
            for (int j = 0; j < world.length; j++) {
                if (world[i][j] == Actor.NONE) {
                    naActors.add(new int[]{i, j});
                }
            }
        }

        return naActors;
    }


    private Actor[][] getUnsatisfied(double threshold, Actor[][] world) {

        // Create matrix. Index 0 = Y, Index 1 = X, Index 2 = Actor
        Actor[][] unsatActors = new Actor[world.length][world.length];

        //Fill unsatActors with coordinates of unsatisfied actors within world.
        for (int i = 0; i < world.length; i++) {
            for (int j = 0; j < world.length; j++) {
                if (calcState(i, j, threshold, world) == State.UNSATISFIED) {
                    unsatActors[i][j] = world[i][j];
                }
            }
        }
        return unsatActors;
    }


    //RED, BLUE, GREEN
    private State calcState(int y, int x, double threshold, Actor[][] world) {
        Actor thisColor = world[y][x];

        if (thisColor.equals(Actor.NONE)) {
            return State.NA;
        }

        double percentOfSurroundingFriends;

        if (getAmountOfNeighbours(y, x, world) == 0) {
            percentOfSurroundingFriends = 1; // ifall den är ensam kan vi säga att den är satisfied för att snabbare bygga upp nya "öar"
        } else {
            percentOfSurroundingFriends = (double) getAmountOfFriends(y, x, world) / getAmountOfNeighbours(y, x, world);
        }

        if (percentOfSurroundingFriends >= threshold) {
            return State.SATISFIED;
        } else {
            return State.UNSATISFIED;
        }

    }

    private int getAmountOfFriends(int y, int x, Actor[][] world) {
        Actor a = world[y][x];

        int friends = 0;
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {

                int indexX = x + j;
                int indexY = y + i;

                if (!(i == 0 && j == 0) && isValid(indexY, indexX, world)) {
                    if (world[indexY][indexX].equals(a)) {
                        friends++;
                    }
                }
            }

        }
        return friends;
    }

    private boolean isValid(int y, int x, Actor[][] world) {
        return !((y < 0 || y >= world.length) || (x < 0 || x >= world[0].length));
    }

    //Kollar hur många grannar som finns genom att kolla hur många celler som inte är tomma.
    private int getAmountOfNeighbours(int y, int x, Actor[][] world) {
        int neighbours = 0;
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {

                int indexX = x + j;
                int indexY = y + i;

                if (!(i == 0 && j == 0) && isValid(indexY, indexX, world)) {
                    if (!world[indexY][indexX].equals(Actor.NONE)) {
                        neighbours++;
                    }
                }
            }

        }
        return neighbours;

    }

    private void shuffle(Actor[] arr) {
        Random rand = new Random();
        for (int i = arr.length; i > 1; i--) {
            int j = rand.nextInt(i);
            Actor temp = arr[j];
            arr[j] = arr[i - 1];
            arr[i - 1] = temp;
        }
    }

    private Actor chooseColor(int index) {
        switch (Actor.values().length - index) {
            case 1:
                return Actor.NONE;
            case 2:
                return Actor.GREEN;
            case 3:
                return Actor.BLUE;
            case 4: //Can add more colors
                return Actor.RED;
            default:
                return Actor.NONE;
        }
    }


    private Actor[][] arrToMat(Actor[] arr) {
        int arrDim = (int) round(sqrt(arr.length));
        Actor[][] matrix = new Actor[arrDim][arrDim];
        for (int i = 0; i < arr.length; i++) {
            matrix[i / arrDim][i % arrDim] = arr[i];
        }
        return matrix;
    }


// ------- Testing -------------------------------------

    // Here you run your tests i.e. call your logic methods
// to see that they really work
    void test() {
// A small hard coded world for testing

        double th = 0.5; // Simple threshold used for testing


// TODO test methods

        exit(0);
    }

 // Helper method for testing (NOTE: reference equality)


// ***** NOTHING to do below this row, it's JavaFX stuff ******

    double width = 800; // Size for window
    double height = 800;
    long previousTime = nanoTime();
    final long interval = 450000000;
    double dotSize;
    final double margin = 50;

    void fixScreenSize(int nLocations) {
// Adjust screen window depending on nLocations
        dotSize = (width - 2 * margin) / sqrt(nLocations);
        if (dotSize < 1) {
            dotSize = 2;
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

// Build a scene graph
        Group root = new Group();
        Canvas canvas = new Canvas(width, height);
        root.getChildren().addAll(canvas);
        GraphicsContext gc = canvas.getGraphicsContext2D();

// Create a timer
        AnimationTimer timer = new AnimationTimer() {
            // This method called by FX, parameter is the current time
            public void handle(long currentNanoTime) {
                long elapsedNanos = currentNanoTime - previousTime;
                if (elapsedNanos > speed * interval) {
                    updateWorld();
                    renderWorld(gc, world);
                    previousTime = currentNanoTime;
                }
            }
        };

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Simulation");
        primaryStage.show();

        timer.start(); // Start simulation
    }


    // Render the state of the world to the screen
    public void renderWorld(GraphicsContext g, Actor[][] world) {
        g.clearRect(0, 0, width, height);
        int size = world.length;
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                double x = dotSize * col + margin;
                double y = dotSize * row + margin;

                if (world[row][col] == Actor.RED) {
                    g.setFill(Color.RED);
                } else if (world[row][col] == Actor.BLUE) {
                    g.setFill(Color.BLUE);
                } else if (world[row][col] == Actor.GREEN) {
                    g.setFill(Color.GREEN);
                } else {
                    g.setFill(Color.WHITE);
                }
                g.fillOval(x, y, dotSize, dotSize);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}