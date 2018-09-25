//HELLO WORLD

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.Random;

import com.sun.org.apache.bcel.internal.generic.AALOAD;
import org.omg.PortableInterceptor.ACTIVE;

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
// This variable may *only* be used in methods init() and updateWorld()
	Actor[][] world; // The world is a square matrix of Actors
	double speed = 0.1;
	// This is the method called by the timer to update the world
// (i.e move unsatisfied) approx each 1/60 sec.
	void updateWorld() {
// % of surrounding neighbours that are like me

		final double threshold = 0.6;

		int[][] naActors;
		Object[][] unsatActors;

		naActors = getNA();
		unsatActors = getUnsatisfied(threshold);
		shuffle(naActors);
		swapMatrixData(naActors, unsatActors);
		distributeActorsToWorld(naActors, unsatActors);

	}

	// This method initializes the world variable with a random distribution of Actors
// Method automatically called by JavaFX runtime (before graphics appear)
// Don't care about "@Override" and "public" (just accept for now)
	@Override
	public void init() {
//test(); // <---------------- Uncomment to TEST!

// %-distribution of RED, BLUE, GREEN and NONE
		double[] distribution = {0.15, 0.18, 0.17, 0.50};
// Number of locations (places) in world (square)
		int nLocations = 10000;
		int arrDim = (int) Math.sqrt(nLocations);

		Actor[] actors = new Actor[nLocations];
		int[] arrDistribution = new int[distribution.length];

		//Calculating amount of actors based on distribution and converting from double to int arr.
		for (int i = 0; i < distribution.length; i++) {
			arrDistribution[i] = (int) Math.round(distribution[i] * nLocations);
		}

		//Choose colors according to distribution and place in an actors array.
		int index = 0;
		for (int i = 0; i < arrDistribution.length; i++) {
			for (int j = 0; j < arrDistribution[i]; j++) {
				actors[index] = chooseColor(i);
				index++;
			}
		}
		//TODO vill vi ha två olika shuffle?
		shuffle(actors);
		world = arrToMat(actors, arrDim);


// Should be last
		fixScreenSize(nLocations);
	}


// ------- Methods ------------------


	//TODO does it distribute all actors or only NA and Unsat? Why?
	void distributeActorsToWorld(int[][] naActors, Object[][] unsatActors){
		for (int i = 0; i < world.length; i++) {
			for (int j = 0; j < world.length; j++) {

				for (int k = 0; k < naActors.length; k++) {

					if ((naActors[k][0] == i) && (naActors[k][1] == j)) {
						world[i][j] = Actor.NONE;

					}
				}
				for (int k = 0; k < unsatActors.length; k++) {
					if (((int) unsatActors[k][0] == i) && ((int) unsatActors[k][1] == j)) {
						world[i][j] = (Actor) unsatActors[k][2];
					}
				}
			}
		}

	}


	void swapMatrixData(int[][] naActors, Object[][] unsatActors) {
		int tempY = 0;
		int tempX = 0;
		for (int i = 0; i < naActors.length && i < unsatActors.length; i++) {
			//TODO återställ om det inte fungerar som tänkt, diskutera med Koftan. VI har tagit bort if statement och lagt det i for loopen istället.
				tempY = naActors[i][0]; //Y
				tempX = naActors[i][1]; //X

				naActors[i][0] = (int) unsatActors[i][0];
				naActors[i][1] = (int) unsatActors[i][1];

				unsatActors[i][0] = tempY;
				unsatActors[i][1] = tempX;

		}

	}

	//index 0 = Y, index 1 = X
	int[][] getNA() {

		//Checking how many NA there are in order to create an matrix.
		int amountNA = 0;
		for (int i = 0; i < world.length; i++) {
			for (int j = 0; j < world.length; j++) {
				if (world[i][j] == Actor.NONE) {
					amountNA++;
				}
			}
		}
		//Create matrix.
		int[][] naActors = new int[amountNA][2];

		int counter = 0;

		//Fill naActors with coordinates of NA within world.
		for (int i = 0; i < world.length; i++) {
			for (int j = 0; j < world.length; j++) {

				if (world[i][j] == Actor.NONE) {
					naActors[counter][0] = i;
					naActors[counter][1] = j;
					counter++;
				}

			}
		}
		return naActors;
	}


	Object[][] getUnsatisfied(double threshold) {
		int amountUnsatisfied = 0;

		//Checking how many unsatisfied actors there are in order to create an matrix.
		for (int i = 0; i < world.length; i++) {
			for (int j = 0; j < world.length; j++) {
				if (calcState(i, j, threshold) == State.UNSATISFIED) {
					amountUnsatisfied++;
				}
			}
		}

		// Create matrix. Index 0 = Y, Index 1 = X, Index 2 = Actor
		Object[][] unsatActors = new Object[amountUnsatisfied][3];
		int indexUnsatisfied = 0;

		//Fill unsatActors with coordinates of unsatisfied actors within world.
		for (int i = 0; i < world.length; i++) {
			for (int j = 0; j < world.length; j++) {
				if (calcState(i, j, threshold) == State.UNSATISFIED) {
					unsatActors[indexUnsatisfied][0] = i;
					unsatActors[indexUnsatisfied][1] = j;
					unsatActors[indexUnsatisfied][2] = world[i][j];
					indexUnsatisfied++;
				}
			}
		}
		return unsatActors;
	}


	//RED, BLUE, GREEN
	State calcState(int y, int x, double threshold) {
		Actor thisColor = world[y][x];

		if (thisColor.equals(Actor.NONE)) {
			return State.NA;
		}

		double percentOfSurroundingFriends;
		//TODO dubbelkika med koftan, problem löst. Event: 0/0 kan förekomma och resulterar i värdet NaN.
		if(getAmountOfNeighbours(y, x) == 0){
			percentOfSurroundingFriends = 0;
		}
		else {
			percentOfSurroundingFriends = (double) getAmountOfFriends(y, x) / getAmountOfNeighbours(y, x);
		}

		if (percentOfSurroundingFriends >= threshold) {
			return State.SATISFIED;
		} else {
			return State.UNSATISFIED;
		}

	}

	int getAmountOfFriends(int y, int x) {
		Actor a = world[y][x];

		int friends = 0;
		for (int i = -1; i < 2; i++) {
			for (int j = -1; j < 2; j++) {

				int indexX = x + j;
				int indexY = y + i;

				//TODO skriva en if-sats som kollar om i och j är 0. i så fall hoppa över hela steget.
				if (indexX < 0 || indexX >= world[0].length) {//Forutsatter att det ar en kvadratisk matris.
					//TODO fyll det med något för att undvika tomt block
				} else if (indexY < 0 || indexY >= world.length) {
					//TODO fyll det med något för att undvika tomt block
				} else if (world[indexY][indexX].equals(a)) {
					friends++;
				}
			}
		}

//Tar -1 för att räkna bort sig själv.
		return friends - 1;
	}

	//Kollar hur många grannar som finns genom att kolla hur många celler som inte är tomma.
	int getAmountOfNeighbours(int y, int x) {
		int neighbours = 0;
		for (int i = -1; i < 2; i++) {
			for (int j = -1; j < 2; j++) {

				int indexX = x + j;
				int indexY = y + i;

				if (indexX < 0 || indexX >= world[0].length) {//Forutsatter att det ar en kvadratisk matris.

				} else if (indexY < 0 || indexY >= world.length) {

				} else if (!world[indexY][indexX].equals(Actor.NONE)) {
					neighbours++;
				}
			}

		}
//Tar -1 för att räkna bort sig själv. OBS, inte anpassad om cellen är NONE.
		return neighbours - 1;

	}

	//TODO Varför behöver vi inte skriva andra delen av [][] ??? ?!?!?!?!
	void shuffle(int[][] matrix) {
		Random rand = new Random();
		for (int i = matrix.length -1; i >= 0; i--) {
			int j = rand.nextInt(i +1);
			int[] temp = matrix[j];
			matrix[j] = matrix[i];
			matrix[i] = temp;
		}

	}

	//TODO fixa till likadan som ovan efter konsultera med koftan.
	void shuffle(Actor[] arr) {
		Random rand = new Random();
		for (int i = arr.length; i > 1; i--) {
			int j = rand.nextInt(i);
			Actor temp = arr[j];
			arr[j] = arr[i - 1];
			arr[i - 1] = temp;

		}

	}

	Actor chooseColor(int index) {
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

	//FIXME Ta bort efter koncultering med Koftan.
	Actor[] matToArr(Actor[][] actor) {
		int arrDim = actor[0].length;
		Actor[] a = new Actor[arrDim * arrDim];
		for (int i = 0; i < actor.length; i++) {
			for (int j = 0; j < actor.length; j++) {
				a[i * actor.length + j] = actor[i][j];
			}
		}
		return a;
	}
	//TODO Använd heltalsdivision och modulus istället för denna krångliga loop.
	Actor[][] arrToMat(Actor[] arr, int arrDim) {
		Actor[][] a = new Actor[arrDim][arrDim];
		for (int i = 0; i < arrDim; i++) {
			for (int j = 0; j < arrDim; j++) {
				a[i][j] = arr[i * arrDim + j];
			}
		}
		return a;
	}


// ------- Testing -------------------------------------

	// Here you run your tests i.e. call your logic methods
// to see that they really work
	void test() {
// A small hard coded world for testing
		Actor[][] testWorld = new Actor[][]{
				{Actor.RED, Actor.RED, Actor.NONE},
				{Actor.NONE, Actor.BLUE, Actor.NONE},
				{Actor.RED, Actor.NONE, Actor.BLUE}
		};
		double th = 0.5; // Simple threshold used for testing
		int size = testWorld.length;

		Actor[] a = new Actor[]{
				Actor.RED, Actor.RED, Actor.NONE,
				Actor.NONE, Actor.NONE, Actor.NONE,
				Actor.RED, Actor.NONE, Actor.BLUE
		};


		Actor[][] testtst = arrToMat(a, 3);
		for (int i = 0; i < testtst.length; i++) {
			System.out.println(Arrays.toString(testtst[i]));
		}
		System.out.println(Actor.values().length);
// Actor[] gfkhj = ArrTo1DArr(testWorld);
// System.out.println(Arrays.toString(gfkhj));
// shuffle(gfkhj);
// System.out.println(Arrays.toString(gfkhj));

// shuffle(testWorld);
// for (int i = 0; i < testWorld.length; i++) {
// System.out.println(Arrays.toString(testtst[i]));
// }


// TODO test methods

		exit(0);
	}

/* // Helper method for testing (NOTE: reference equality)
int count(T[] arr, T toFind) {
int count = 0;
for (int i = 0; i < arr.length; i++) {
if (arr[i] == toFind) {
count++;
}
}
return count;
} */


// ***** NOTHING to do below this row, it's JavaFX stuff ******

	double width = 400; // Size for window
	double height = 400;
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