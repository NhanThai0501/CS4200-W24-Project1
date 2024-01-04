/**
 * 
 */
package Eight_Puzzle;

import java.util.Scanner;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * @author NhanThai
 *
 */
public class Driver {

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {

		System.out.println("CS4200 Project 1: 8-Puzzle");

		Scanner input = new Scanner(System.in);
		int runType;

		while ((runType = selectInputMethod(input)) != 3) {
			switch (runType) {
			case 1:
				// Uses a random or manual input for the puzzle
				executeSinglePuzzleSolution(input);
				break;
			case 2:
				// Uses a file for input
				Map<Integer, List<AStar_algorithm>> infoTable = executeMultiplePuzzleRuns(input);
				analyzeAStarPerformance(infoTable);
				break;
			default:
				System.out.println("Unexpected error occurred.");
				break;
			}
		}

		System.out.println("Exiting program.");
		input.close();
	}

	public static int selectInputMethod(Scanner input) {
		int selection = 0;

		while (selection != 3) {
			// Display the question
			System.out.println("Select Input Method:");
			System.out.println("[1] Single Test Puzzle (Random or manually input)");
			System.out.println("[2] Multi-Test Puzzle (Input filename)");
			System.out.println("[3] Exit");

			// Get the answer
			String chosenRunType = input.nextLine();

			switch (chosenRunType) {
			case "1":
				return 1;
			case "2":
				return 2;
			case "3":
				selection = 3; // Assign 3 to exit the loop
				break;
			default:
				System.out.println("Please choose one of the options (1, 2, or 3).");
				break;
			}
		}

		return selection;
	}

	public static Map<Integer, List<AStar_algorithm>> executeMultiplePuzzleRuns(Scanner scanner) {
		Scanner puzzleFileScanner = retrievePuzzleFile(scanner);
		Map<Integer, List<AStar_algorithm>> statisticsMap = new HashMap<>();

		try {
			String line;
			String puzzleConfig;
			String heuristicTypeStr;
			int heuristicType;
			Puzzle puzzle;
			AStar_algorithm algorithm;

			while (puzzleFileScanner.hasNextLine()) {
				line = puzzleFileScanner.nextLine();

				if (line.startsWith("//") || line.isEmpty()) {
					continue;
				}

				if (line.length() != 11) {
					System.out.println("Skipping invalid line: " + line);
					continue;
				}

				puzzleConfig = line.substring(0, 9);
				heuristicTypeStr = line.substring(10, 11);

				try {
					puzzle = new Puzzle(puzzleConfig);

					heuristicType = Integer.parseInt(heuristicTypeStr);

					if (heuristicType != 1 && heuristicType != 2) {
						throw new IllegalArgumentException("Invalid heuristic type");
					}

					System.out.println("Puzzle:\n" + puzzle);
					algorithm = new AStar_algorithm(puzzle, heuristicType);
					statisticsMap.putIfAbsent(algorithm.totalSteps, new ArrayList<>());
					statisticsMap.get(algorithm.totalSteps).add(algorithm);

				} catch (Exception e) {
					System.out.println("Skipping invalid line: " + line);
				}
			}

			puzzleFileScanner.close();
			return statisticsMap;
		} catch (Exception e) {
			System.out.println("Error processing the puzzle file.");
		}

		puzzleFileScanner.close();
		return null;
	}

	public static Scanner retrievePuzzleFile(Scanner input) {

		System.out.println("Enter a filename (ex: testInput.txt): ");

		// Retrieve the answer (a Scanner for the requested file)
		String fileName = input.nextLine();
		FileInputStream fin = null;
		try {
			fin = new FileInputStream(fileName);
			return new Scanner(fin);
		} catch (FileNotFoundException e) {
			System.out.println("The provided input file (" + fileName + ") was not found.");
			return retrievePuzzleFile(input);
		}
	}

	public static String retrievePuzzleInput(Scanner input) {
		// Present the question
		System.out.println("Select Input Method:");
		System.out.println("[1] Random");
		System.out.println("[2] Input");

		// Retrieve the answer
		String chosenInputMethod = input.nextLine();
		switch (chosenInputMethod) {
		case "1":
			return generateShuffledPuzzle();
		case "2":
			return getUserDefinedPuzzle(input);
		default:
			// Repeatedly ask if the input is invalid
			System.out.println("Invalid input, should be 1 or 2.");
			return retrievePuzzleInput(input);
		}
	}

	public static void executeSinglePuzzleSolution(Scanner scanner) {
		// Function to handle single puzzle solution process

		String puzzleInput = retrievePuzzleInput(scanner);
		Puzzle currentPuzzle = new Puzzle(puzzleInput);
		int heuristicChoice = selectHeuristicFunction(scanner);

		System.out.println("Puzzle:\n" + currentPuzzle);

		new AStar_algorithm(currentPuzzle, heuristicChoice);
	}

	public static int selectHeuristicFunction(Scanner input) {
		// Display the question
		System.out.println("Select H Function:");
		System.out.println("[1] H1 (Number of Misplaced Tiles)");
		System.out.println("[2] H2 (Manhattan Distance)");

		// Get the answer
		String H_functionType = input.nextLine();
		switch (H_functionType) {
		case "1":
			return 1;
		case "2":
			return 2;
		default:

			System.out.println("Please choose 1 or 2.");
			return selectHeuristicFunction(input);
		}
	}

	public static String generateShuffledPuzzle() {
		List<Character> tileList = Arrays.asList(new Character[] { '0', '1', '2', '3', '4', '5', '6', '7', '8' });

		try {
			// Randomize the tile order
			Collections.shuffle(tileList);
			String shuffledPuzzle = "";
			for (Character tile : tileList) {
				shuffledPuzzle += Character.toString(tile);
			}
			new Puzzle(shuffledPuzzle);

			return shuffledPuzzle;
		} catch (Exception e) {
			// Try again if unsolvable puzzle appears
			return generateShuffledPuzzle();
		}
	}

	public static String getUserDefinedPuzzle(Scanner scanner) {

		System.out.println("Enter the puzzle configuration as a single line (ex: 012345678): ");

		String userPuzzleInput = scanner.nextLine();
		try {
			// Validate the input format and check if the puzzle is solvable
			if (userPuzzleInput.matches("[0-8]{9}")) {
				new Puzzle(userPuzzleInput);
				return userPuzzleInput;
			} else {
				throw new IllegalArgumentException("Invalid format.");
			}
		} catch (Exception e) {
			System.out
					.println("Invalid input. Ensure your input follows the specified format and is a solvable puzzle.");
			// Recursively call the function until valid input is received
			return getUserDefinedPuzzle(scanner);
		}
	}

	public static void analyzeAStarPerformance(Map<Integer, List<AStar_algorithm>> dataMap) {
		// Check if the data map is null, and if so, throw an exception
		if (dataMap == null) {
			throw new IllegalArgumentException("Cannot get the data.");
		}

		// Prepare a map to store aggregated data for each solution depth
		Map<Integer, double[]> aggregatedData = new HashMap<>();

		// Iterate over each solution depth in the data map
		for (Integer depth : dataMap.keySet()) {
			// Initialize variables to accumulate stats for the current depth
			double h1CostSum = 0, h1TimeSum = 0, h2CostSum = 0, h2TimeSum = 0, testCount = 0;

			// Sum up the cost and duration for both heuristic functions across all solvers
			// at the current depth
			for (AStar_algorithm eachSolver : dataMap.get(depth)) {
				h1CostSum += eachSolver.heuristic1Cost;
				h1TimeSum += eachSolver.heuristic1Duration;
				h2CostSum += eachSolver.heuristic2Cost;
				h2TimeSum += eachSolver.heuristic2Duration;
				testCount++;
			}

			// Calculate the averages for cost and duration, and store these along with the
			// test count
			double[] averages = { h1CostSum / testCount, h1TimeSum / testCount, h2CostSum / testCount,
					h2TimeSum / testCount, testCount };
			aggregatedData.put(depth, averages);
		}

		// Call a function to display the summarized statistics
		displaySummaryStats(aggregatedData);
	}

	public static void displaySummaryStats(Map<Integer, double[]> statsMap) {
		// Create a sorted list of depth levels
		List<Integer> depthLevels = new ArrayList<>(statsMap.keySet());
		Collections.sort(depthLevels);

		System.out.println("--------STATISTICS OF TEST CASES--------\n");
		for (Integer depthLevel : depthLevels) {
			double[] statsDetails = statsMap.get(depthLevel);
			Integer totalTests = (int) statsDetails[4]; // Convert double to Integer

			System.out.println("Solution Depth: " + depthLevel);
			System.out.println("Total Tests: " + totalTests);
			System.out.println("Average H1 Cost: " + statsDetails[0]);
			System.out.println("Average H1 Time: " + statsDetails[1]);
			System.out.println("Average H2 Cost: " + statsDetails[2]);
			System.out.println("Average H2 Time: " + statsDetails[3]);
			System.out.println();
		}

		System.out.println("---------------END REPORT----------------\n");
	}
}
