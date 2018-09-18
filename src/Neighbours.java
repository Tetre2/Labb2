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
 *  Program to simulate segregation.
 *  See : http://nifty.stanford.edu/2014/mccown-schelling-model-segregation/
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
		BLUE, RED, NONE, GREEN   // NONE used for empty locations
	}

	// Enumeration type for the state of an Actor
	enum State {
		UNSATISFIED,
		SATISFIED,
		NA     // Not applicable (NA), used for NONEs
	}

	// Below is the *only* accepted instance variable (i.e. variables outside any method)
	// This variable may *only* be used in methods init() and updateWorld()
	Actor[][] world;              // The world is a square matrix of Actors

	// This is the method called by the timer to update the world
	// (i.e move unsatisfied) approx each 1/60 sec.
	void updateWorld() {
		// % of surrounding neighbours that are like me
		final double threshold = 0.2;
		int worldLength = world.length * world.length;
		
		// Index 0 = Actor, Index 1 = State, Index 2 = Y, Index 3 = X
		Object[][] na = null;
		Object[][] unsatisfied = null;
		for (int y = 0; y < world.length; y++) {
			for (int x = 0; x < world.length; x++) {
				na = getNA(getAmountOfNeighbours(y, x), threshold);
				unsatisfied = getUnsatisfied(getAmountOfNeighbours(y, x), threshold);
//				if(!world[y][x].equals(Actor.NONE)){
//					State temp = calcState(y,x, getAmountOfNeighbours(y, x), threshold);
//				}else {
//					State temp = State.NA;
//				}
			}
		}


		shuffleMatrix(na);
		switchXAndY(na, unsatisfied);
		
		for (int i = 0; i < world.length; i++) {
			for (int j = 0; j < world.length; j++) {
				
				for (int k = 0; k < na.length; k++) {
					
					if( ( (int) na[k][2] == i) && ( (int) na[k][3] == j)) {
						world[i][j] = (Actor) na[k][0];
					
					}
				}
				for (int k = 0; k < unsatisfied.length; k++) {
					if( ( (int) unsatisfied[k][2] == i) && ( (int) unsatisfied[k][3] == j)) {
						world[i][j] = (Actor) unsatisfied[k][0];
					}
				}
			}
		}
		
		
		
		

		//----------------------------------------------

		//				Object[][] na = getNA();
		//
		//						for (int i = 0; i < world.length; i++) {
		//							for (int j = 0; j < world.length; j++) {
		//								if(isUnsatesfied(i, j)) {
		//								}
		//							}
		//						}



		//----------------------------------------------


		/*

		// Index 0 = Actor, Index 1 = State, Index 2 = Y, Index 3 = X
				Object[][] satisfiedActors = new Object[worldLength][4];

				for (int y = 0; y < world.length; y++) {
					for (int x = 0; x < world.length; x++) {
						if(!world[y][x].equals(Actor.NONE)){

							State temp = calcState(y,x, getAmountOfNeighbours(y, x), threshold);

							if(temp.equals(State.UNSATISFIED)){
								satisfiedActors[y*world.length + x][1] = temp;
							}
							satisfiedActors[y*world.length + x][0] = world[y][x];
						}
						else{
							satisfiedActors[y*world.length + x][1] = State.NA;
							satisfiedActors[y*world.length + x][0] = Actor.NONE;
						}
						satisfiedActors[y*world.length + x][2] = y;
						satisfiedActors[y*world.length + x][3] = x;

						//out.println(actorStates[y*world.length + x]);
					}
				}
				// TODO

				relocateUnsatisfied(satisfiedActors);*/
	}

	// This method initializes the world variable with a random distribution of Actors
	// Method automatically called by JavaFX runtime (before graphics appear)
	// Don't care about "@Override" and "public" (just accept for now)
	@Override
	public void init() {
		//test();    // <---------------- Uncomment to TEST!

		// %-distribution of RED, BLUE, GREEN and NONE
		double[] dist = {0.25, 0.25, 0.25, 0.25};
		// Number of locations (places) in world (square)
		int nLocations = 900;
		int arrDim = (int) Math.sqrt(nLocations);

		Actor[] a = new Actor[nLocations];
		int[] arrDist = new int[dist.length];

		for (int i = 0; i < dist.length; i++) {
			arrDist[i] = (int) Math.round(dist[i] * nLocations);
		}


		int index = 0;
		for (int i = 0; i < arrDist.length; i++) {
			for (int j = 0; j < arrDist[i]; j++) {
				a[index] = chooseColor(i);
				index++;
			}
		}

		shuffle(a);
		world = arrToMat(a, arrDim);


		// Should be last
		fixScreenSize(nLocations);
	}


	// ------- Methods ------------------

	// TODO write the methods here, implement/test bottom up

	void switchXAndY(Object[][] na, Object[][] unsatisfied){
		int tempY = 0;
		int tempX = 0;
		for (int i = 0; i < na.length; i++) {
			if((i < na.length) && (i < unsatisfied.length)) {
				tempY = (int) na[i][2];		//Y
				tempX = (int) na[i][3];		//X

				na[i][2] = unsatisfied[i][2];
				na[i][3] = unsatisfied[i][3];

				unsatisfied[i][2] = tempX;
				unsatisfied[i][3] = tempY;
			}

		}
		
	}


	Object[][] getNA(int neighbours, double threshold){
		int amountNA = 0;
		for (int i = 0; i < world.length; i++) {
			for (int j = 0; j < world.length; j++) {
				if(isNA(i, j, neighbours, threshold)) {
					amountNA++;
				}
			}
		}

		// Index 0 = Actor, Index 1 = State, Index 2 = Y, Index 3 = X
		Object[][] na = new Object[amountNA][4];
		int indexNA = 0;

		for (int i = 0; i < world.length; i++) {
			for (int j = 0; j < world.length; j++) {
				if(isNA(i, j, neighbours, threshold)) {
					na[indexNA][0] = Actor.NONE;
					na[indexNA][1] = State.NA;
					na[indexNA][2] = i;
					na[indexNA][3] = j;
					indexNA++;
				}
			}
		}

		return na;

	}



	boolean isNA(int y, int x, int neighbours, double threshold) {
		if(calcState(y, x, neighbours, threshold).equals(State.NA)) {
			return true;
		}else {		
			return false;
		}
	}



	Object[][] getUnsatisfied(int neighbours, double threshold){
		int amountUnsatisfied = 0;

		for (int i = 0; i < world.length; i++) {
			for (int j = 0; j < world.length; j++) {
				if(isUnsatesfied(i, j, neighbours, threshold)) {
					amountUnsatisfied++;
				}
			}
		}

		// Index 0 = Actor, Index 1 = State, Index 2 = Y, Index 3 = X
		Object[][] unsatisfied = new Object[amountUnsatisfied][4];
		int indexUnsatesfied = 0;

		for (int i = 0; i < world.length; i++) {
			for (int j = 0; j < world.length; j++) {
				if(isUnsatesfied(i, j, neighbours, threshold)) {
					unsatisfied[indexUnsatesfied][0] = world[i][j];
					unsatisfied[indexUnsatesfied][1] = State.UNSATISFIED;
					unsatisfied[indexUnsatesfied][2] = i;
					unsatisfied[indexUnsatesfied][3] = j;
					indexUnsatesfied++;
				}
			}
		}
		return unsatisfied;

	}
	
	
	
	boolean isUnsatesfied(int y, int x, int neighbours, double threshold) {
		if(calcState(y, x, neighbours, threshold).equals(State.UNSATISFIED)) {
			return true;
		}else {
			return false;
		}
	}

	
	
	
	void setstate(double threshold){
		for (int y = 0; y < world.length; y++) {
			for (int x = 0; x < world.length; x++) {
				if(!world[y][x].equals(Actor.NONE)){
					State temp = calcState(y,x, getAmountOfNeighbours(y, x), threshold);
				}else {
					State temp = State.NA;
				}
			}
		}
	}






	//RED, BLUE, GREEN
	State calcState(int y, int x, int neighbours, double threshold) {
		Actor thisColor = world[y][x];

		if(thisColor.equals(Actor.NONE)){
			return State.NA;
		}


		//out.println("Friends: " + getAmountOfFriends(y, x) + "\nNeighbours: " + neighbours );

		//Event: 0/0 kan förekomma och resulterar i värdet NaN.
		double percentOfSurroundingFriends = (double)getAmountOfFriends(y, x)/neighbours;

		//out.println("Percentage: " + percentOfSurroundingFriends);

		if(percentOfSurroundingFriends >= threshold){
			return State.SATISFIED;
		}
		else{
			return State.UNSATISFIED;
		}

	}

	int getAmountOfFriends(int y, int x){
		Actor a = world[y][x];

		int neighbours = 0;
		for (int i = -1; i < 2; i++) {
			for (int j = -1; j < 2; j++) {

				int indexX = x + j;
				int indexY = y + i;

				if (indexX < 0 || indexX >= world[0].length) {//Forutsatter att det ar en kvadratisk matris.

				} else if (indexY < 0 || indexY >= world.length) {

				} else if (world[indexY][indexX].equals(a)) {
					neighbours++;
				}
			}
		}

		//Tar -1 för att räkna bort sig själv.
		return neighbours -1;
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
		return neighbours -1;

	}


	void shuffleMatrix(Object[][] arr) {
		Random rand = new Random();
		for (int i = arr.length; i > 1; i--) {
			int j = rand.nextInt(i);
			Object[] temp = arr[j];
			arr[j] = arr[i - 1];
			arr[i - 1] = temp;
		}

	}

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
		case 4:                    //Can add more colors
			return Actor.RED;
		default:
			return Actor.NONE;
		}
	}

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
		double th = 0.5;   // Simple threshold used for testing
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
		//		Actor[] gfkhj = ArrTo1DArr(testWorld);
		//		System.out.println(Arrays.toString(gfkhj));
		//		shuffle(gfkhj);
		//		System.out.println(Arrays.toString(gfkhj));

		//		shuffle(testWorld);
		//		for (int i = 0; i < testWorld.length; i++) {
		//			System.out.println(Arrays.toString(testtst[i]));
		//		}


		// TODO test methods

		exit(0);
	}

	// Helper method for testing (NOTE: reference equality)
	<T> int count(T[] arr, T toFind) {
		int count = 0;
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] == toFind) {
				count++;
			}
		}
		return count;
	}


	// *****   NOTHING to do below this row, it's JavaFX stuff  ******

	double width = 400;   // Size for window
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
				if (elapsedNanos > interval) {
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

		timer.start();  // Start simulation
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
