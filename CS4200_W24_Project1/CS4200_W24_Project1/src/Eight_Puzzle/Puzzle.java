package Eight_Puzzle;

import java.util.List;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.HashSet;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * @author NhanThai
 *
 */

public class Puzzle {
	// Class that will perform multiple tasks related to H1 and H2 function, and
	// algorithms to solve the 8-puzzle problems
	// Constants for the goal matrix
	private static final int[][] GOAL_MATRIX = { { 0, 1, 2 }, { 3, 4, 5 }, { 6, 7, 8 } };
	private static final int MATRIX_SIZE = 3;
	private static final Set<Integer> ALLOWED_VALUES = new HashSet<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8));

	// Instance variables
	private List<List<Integer>> puzzleMatrix;
	private int zeroXPosition;
	private int zeroYPosition;
	private int gValue;
	private int heuristicValue;
	private Puzzle parent;

	// Constructor using a List of Integer Lists
	public Puzzle(List<List<Integer>> matrix) {
		initializeMatrix(matrix);
	}

	// Constructor using a 2D Integer array
	public Puzzle(Integer[][] matrixArray) {
		List<List<Integer>> matrixList = convertArrayToList(matrixArray);
		initializeMatrix(matrixList);
	}

	// Constructor using a string representation
	public Puzzle(String flatMatrix) {
		List<List<Integer>> matrixList = convertStringToList(flatMatrix);
		initializeMatrix(matrixList);
	}

	// Helper method to convert 2D array to List of Lists
	private List<List<Integer>> convertArrayToList(Integer[][] matrixArray) {
		return Arrays.stream(matrixArray).map(Arrays::asList).collect(Collectors.toList());
	}

	// Helper method to convert string to List of Lists
	private List<List<Integer>> convertStringToList(String flatMatrix) {
		List<List<Integer>> matrixList = new ArrayList<>();
		List<Integer> row = new ArrayList<>();

		for (int i = 0; i < flatMatrix.length(); i++) {
			if (row.size() == 3) {
				matrixList.add(new ArrayList<>(row));
				row.clear();
			}
			row.add(Character.getNumericValue(flatMatrix.charAt(i)));
		}

		if (!row.isEmpty()) {
			matrixList.add(row);
		}

		return matrixList;
	}

	public void initializeMatrix(List<List<Integer>> matrix) {
		// A one-stop-hub for all our matrix needs

		// Ensure that matrix is valid
		if (!checkValidity(matrix))
			throw new IllegalArgumentException("Bad Matrix.");

		// Store some helpful information, useful for the A* algorithm
		this.puzzleMatrix = matrix;
		this.parent = null;
		this.gValue = 0;
		this.heuristicValue = 0;

		// Find the position of the empty tile
		findEmptyTilePosition(matrix);
	}

	// Function to find the position of the empty tile
	private void findEmptyTilePosition(List<List<Integer>> matrix) {
		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 3; x++) {
				if (matrix.get(y).get(x) == 0) {
					this.zeroXPosition = x;
					this.zeroYPosition = y;
					return; // Exit the loops once the empty tile is found
				}
			}
		}
	}

	// The initial heuristic involves counting the number of tiles that are not in
	// their correct positions
	// based on the predefined goal matrix.
	public int calculateMisplacedTiles() {
		int misplacedTiles = 0;

		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 3; col++) {
				if (puzzleMatrix.get(row).get(col) == 0) { // Skip the empty tile
					continue;
				}

				if (GOAL_MATRIX[row][col] != puzzleMatrix.get(row).get(col)) {
					misplacedTiles++; // Increment if tile is not in its goal position
				}
			}
		}

		return misplacedTiles;
	}

	// The second heuristic calculates the total distance that each tile must travel
	// to reach its intended position.
	public int calculateManhattanDistance() {
		int totalManhattanDistance = 0;

		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 3; col++) {
				if (puzzleMatrix.get(row).get(col) == 0) { // Skip the empty tile
					continue;
				}
				totalManhattanDistance += calculateTileDistance(row, col);
			}
		}

		return totalManhattanDistance;
	}

	// Calculate the Manhattan distance of a tile from its intended position
	public int calculateTileDistance(int currentRow, int currentCol) {
		int tileValue = puzzleMatrix.get(currentRow).get(currentCol);
		Point intendedPosition = findIntendedPosition(tileValue);

		// Calculate and return the total Manhattan distance
		return calculateManhattanDistance(intendedPosition, currentRow, currentCol);
	}

	// Find the intended position (trueX, trueY) of a tile in the GOAL_MATRIX
	private Point findIntendedPosition(int tileValue) {
		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 3; x++) {
				if (GOAL_MATRIX[y][x] == tileValue) {
					return new Point(x, y); // Found the intended position
				}
			}
		}
		throw new RuntimeException("Invalid tile value: No matching position found.");
	}

	// Calculate the Manhattan distance between two positions
	private int calculateManhattanDistance(Point intendedPosition, int currentRow, int currentCol) {
		int diffX = Math.abs(intendedPosition.x - currentCol);
		int diffY = Math.abs(intendedPosition.y - currentRow);
		return diffX + diffY;
	}

	// Point class to represent x and y coordinates
	private class Point {
		int x, y;

		Point(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}

	public boolean isGoal() {
		// If every tile is in the right spot
		return calculateMisplacedTiles() == 0;
	}

	// Simple getters, setters, and checks follow
	public void setzeroXPosition(int x) {
		this.zeroXPosition = x;
	}

	public void setzeroYPosition(int y) {
		this.zeroYPosition = y;
	}

	public int getG_value() {
		return this.gValue;
	}

	public void setG_value(int g) {
		this.gValue = g;
	}

	public int getHeuristicValue() {
		return this.heuristicValue;
	}

	public void setHeuristicValue(int h) {
		this.heuristicValue = h;
	}

	public boolean hasParent() {
		return parent != null;
	}

	public Puzzle getParent() {
		return this.parent;
	}

	public void setParent(Puzzle parent) {
		this.parent = parent;
	}

	public int calculateF() {
		// The function calculates the total cost f(n) for a puzzle state.
		// The cost consists of two components: g(n) and h(n).
		// g(n) represents the actual cost to reach this state, and it should already be
		// set.
		// h(n) is determined based on the selected heuristic.

		// In case there was an issue with selecting a heuristic,
		// an exception is thrown to handle this error condition.
		if (heuristicValue != 1 && heuristicValue != 2) {
			throw new RuntimeException("Illegal Heuristic value");
		}

		int h = (heuristicValue == 1) ? calculateMisplacedTiles() : calculateManhattanDistance();
		return gValue + h;
	}

	public Puzzle moveLeft() {
		// Moves the empty tile to the left
		return swap(zeroXPosition, zeroYPosition, zeroXPosition - 1, zeroYPosition);
	}

	public Puzzle moveRight() {
		// Moves the empty tile to the right
		return swap(zeroXPosition, zeroYPosition, zeroXPosition + 1, zeroYPosition);
	}

	public Puzzle moveUp() {
		// Moves the empty tile up
		return swap(zeroXPosition, zeroYPosition, zeroXPosition, zeroYPosition - 1);
	}

	public Puzzle moveDown() {
		// Moves the empty tile down
		return swap(zeroXPosition, zeroYPosition, zeroXPosition, zeroYPosition + 1);
	}

	// Retrieve a list of the possible moves
	public List<Method> possibleMoves() {
		List<Method> possibleMoves = new ArrayList<>();

		addMoveMethod(possibleMoves, "moveLeft", zeroXPosition > 0);
		addMoveMethod(possibleMoves, "moveRight", zeroXPosition < 2);
		addMoveMethod(possibleMoves, "moveUp", zeroYPosition > 0);
		addMoveMethod(possibleMoves, "moveDown", zeroYPosition < 2);

		return possibleMoves;
	}

	// Helper method to add a move method to the list if the condition is met
	private void addMoveMethod(List<Method> list, String methodName, boolean condition) {
		if (condition) {
			try {
				list.add(this.getClass().getDeclaredMethod(methodName));
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
	}

	private Puzzle swap(int sourceX, int sourceY, int targetX, int targetY) {
		// Validate tile positions
		ensurePositionsAreValid(sourceX, sourceY, targetX, targetY);

		// Create a new matrix based on the current puzzle state
		List<List<Integer>> newMatrix = createMatrixCopy();

		// Perform the swap
		int sourceValue = this.puzzleMatrix.get(sourceY).get(sourceX);
		int targetValue = this.puzzleMatrix.get(targetY).get(targetX);
		newMatrix.get(sourceY).set(sourceX, targetValue);
		newMatrix.get(targetY).set(targetX, sourceValue);

		// Generate a new puzzle state with the swapped matrix
		Puzzle newPuzzleState = new Puzzle(newMatrix);
		newPuzzleState.setHeuristicValue(this.heuristicValue);

		return newPuzzleState;
	}

	// Validate the positions of the tiles
	private void ensurePositionsAreValid(int sourceX, int sourceY, int targetX, int targetY) {
		final int MAX_BOUND = 3;
		if (isOutOfBounds(sourceX, MAX_BOUND) || isOutOfBounds(sourceY, MAX_BOUND) || isOutOfBounds(targetX, MAX_BOUND)
				|| isOutOfBounds(targetY, MAX_BOUND)) {
			throw new IllegalArgumentException("Invalid swap positions.");
		}
	}

	// Check if a position is out of bounds
	private boolean isOutOfBounds(int position, int maxBound) {
		return position < 0 || position > maxBound;
	}

	private List<List<Integer>> createMatrixCopy() {
		// Simply copies a matrix (a nested List)
		List<List<Integer>> matrixCopy = new ArrayList<>();
		for (List<Integer> sublist : this.puzzleMatrix) {
			matrixCopy.add(new ArrayList<>(sublist));
		}

		return matrixCopy;
	}

	public static boolean checkValidity(List<List<Integer>> matrix) {
		return isSizeValid(matrix) && areValuesValid(matrix) && isSolvable(matrix);
	}

	// checks if the matrix and its rows have the correct size
	private static boolean isSizeValid(List<List<Integer>> matrix) {
		if (matrix.size() != MATRIX_SIZE) {
			return false;
		}
		for (List<Integer> row : matrix) {
			if (row.size() != MATRIX_SIZE) {
				return false;
			}
		}
		return true;
	}

	// checks if all values are within the allowed range and are unique
	private static boolean areValuesValid(List<List<Integer>> matrix) {
		Set<Integer> valuesSeen = new HashSet<>();
		for (List<Integer> row : matrix) {
			for (int value : row) {
				if (!ALLOWED_VALUES.contains(value) || !valuesSeen.add(value)) {
					return false;
				}
			}
		}
		return true;
	}

	// checks if the puzzle is solvable using inversion count
	private static boolean isSolvable(List<List<Integer>> matrix) {
		return countInversions(matrix) % 2 == 0;
	}

	public static int countInversions(List<List<Integer>> matrix) {
		int inversions = 0;

		for (int i = 0; i < MATRIX_SIZE; i++) {
			for (int j = 0; j < MATRIX_SIZE; j++) {
				inversions += countInversionsForElement(matrix, i, j);
			}
		}

		return inversions;
	}

	// It calculates the inversions for a single element in the matrix
	private static int countInversionsForElement(List<List<Integer>> matrix, int row, int col) {
		int inversions = 0;
		int currentValue = matrix.get(row).get(col);

		// Skip the empty tile
		if (currentValue == 0) {
			return 0;
		}

		for (int i = row; i < MATRIX_SIZE; i++) {
			for (int j = (i == row ? col + 1 : 0); j < MATRIX_SIZE; j++) {
				int nextValue = matrix.get(i).get(j);

				// Skip the empty tile
				if (nextValue == 0) {
					continue;
				}

				if (currentValue > nextValue) {
					inversions++;
				}
			}
		}

		return inversions;
	}

	@Override
	public String toString() {
		// Print the matrix in a nicely formatted fashion
		StringBuilder output = new StringBuilder();
		for (List<Integer> row : this.puzzleMatrix) {
			output.append(row).append("\n");
		}
		return output.toString();
	}

	@Override
	public boolean equals(Object otherPuzzle) {
		if (this == otherPuzzle) {
			return true;
		}
		if (otherPuzzle == null || getClass() != otherPuzzle.getClass()) {
			return false;
		}
		return this.equals((Puzzle) otherPuzzle);
	}

	@Override
	public int hashCode() {
		// Calculate a custom hash code for the puzzle matrix
		int result = 0;
		for (List<Integer> row : puzzleMatrix) {
			result = 31 * result + row.hashCode();
		}
		return result;
	}

	public boolean equals(Puzzle otherPuzzle) {
		// Check if the puzzle matrices are equal
		return areMatricesEqual(this.puzzleMatrix, otherPuzzle.puzzleMatrix);
	}

	public boolean areMatricesEqual(List<List<Integer>> first, List<List<Integer>> second) {
		// Check if two matrices are equal
		if (first.size() != second.size() || first.isEmpty()) {
			return false;
		}

		for (int i = 0; i < first.size(); i++) {
			if (!first.get(i).equals(second.get(i))) {
				return false;
			}
		}

		return true;
	}
}
