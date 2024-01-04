package Eight_Puzzle;

import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.PriorityQueue;
import java.util.ArrayList;
import java.util.HashSet;
import java.lang.reflect.Method;

/**
 * @author NhanThai
 *
 */

public class AStar_algorithm {
	Puzzle input;
	int selectedHeuristic;
	Puzzle solution;

	int heuristic1Cost;
	double heuristic1Duration;
	int heuristic2Cost;
	double heuristic2Duration;

	int totalSteps;

	public AStar_algorithm(Puzzle input, int selectedHeuristic) {
		this.input = input;
		this.selectedHeuristic = selectedHeuristic;
		this.solution = null;

		// Run both heuristics, only displaying the path of the chosen, but
		// the statistics of both
		compareHeuristics();

		// Print some statistics
		System.out.println("Total Steps: " + totalSteps);
		System.out.println("Heuristic 1 Cost: " + heuristic1Cost);
		System.out.println("Heuristic 1 Duration: " + heuristic1Duration + " ms");
		System.out.println("\nHeuristic 2 Cost: " + heuristic2Cost);
		System.out.println("Heuristic 2 Duration: " + heuristic2Duration + " ms");
		System.out.println();
	}

	public void compareHeuristics() {
		// Execute A* with the chosen heuristic
		long start = System.nanoTime();
		int cost = executeAStarSearch(selectedHeuristic);
		setCost(selectedHeuristic, cost);
		setTime(selectedHeuristic, start);
		displayPath();

		selectedHeuristic ^= 3;
		start = System.nanoTime();
		cost = executeAStarSearch(selectedHeuristic);
		setCost(selectedHeuristic, cost);
		setTime(selectedHeuristic, start);
	}

	public void displayPath() {
		List<Puzzle> path = generatePath();
		int numSteps = path.size(); // Start with the total number of steps

		// Iterate through the path in reverse order without altering the list
		for (int i = path.size() - 1; i >= 0; i--) {
			Puzzle currStep = path.get(i);
			System.out.println("Step: " + (numSteps - i));
			System.out.println(currStep);
		}
	}

	public List<Puzzle> generatePath() {
		// Ensure a solution has already been found (aStar was called)
		if (solution == null) {
			throw new IllegalArgumentException("No solution.");
		}

		List<Puzzle> solutionPath = new ArrayList<>();
		Puzzle currentPuzzle = this.solution;

		while (currentPuzzle.hasParent()) {
			solutionPath.add(currentPuzzle);
			currentPuzzle = currentPuzzle.getParent();
		}

		this.totalSteps = solutionPath.size();

		return solutionPath;
	}

	public int executeAStarSearch(int heuristicToUse) {
		// Functions that related to A* algorithm:
		// f(n) = g(n) + h(n) -> the total cost
		// g(n) = the true cost incurred so far
		// h(n) = estimated cost to solution
		input.setHeuristicValue(heuristicToUse);

		// Initialize a PriorityQueue for the search tree, sorting elements based on
		// their f(n) values.
		Queue<Puzzle> frontier = new PriorityQueue<>((p1, p2) -> Integer.compare(p1.calculateF(), p2.calculateF()));
		frontier.add(input);

		// To prevent the reassignment of previously solved puzzles, retain a cache of them.
		Set<Puzzle> explored = new HashSet<>();

		Puzzle currentPuzzle = null;
		Puzzle childPuzzle = null;
		int searchCost = 0;
		searchCost = puzzleModification(currentPuzzle, childPuzzle, searchCost, frontier, explored);
		return searchCost;
	}

	// This function will perform a graph search algorithm on a puzzle problem,
	// exploring different states, and keeping track of the solution and search cost
	private int puzzleModification(Puzzle currentPuzzle, Puzzle childPuzzle, int searchCost, Queue<Puzzle> frontier,
			Set<Puzzle> explored) {
		while (!frontier.isEmpty()) {
			currentPuzzle = frontier.poll();
			explored.add(currentPuzzle);

			// Check if goal is found
			if (currentPuzzle.isGoal()) {
				this.solution = currentPuzzle;
				break;
			}

			// Create a child node to keep track of previous movement
			for (Method possibleMethod : currentPuzzle.possibleMoves()) {
				try {
					// Create a child node that explore all the movements
					childPuzzle = (Puzzle) possibleMethod.invoke(currentPuzzle);

					// If there is a existed path for it, continue.
					if (explored.contains(childPuzzle)) {
						continue;
					}

					childPuzzle.setHeuristicValue(currentPuzzle.getHeuristicValue());
					childPuzzle.setG_value(currentPuzzle.getG_value() + 1); // This is the depth
					childPuzzle.setParent(currentPuzzle);

					// Add a new child to the frontier, thus the cost will increase
					frontier.add(childPuzzle);
					searchCost++;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return searchCost;

	}

	private void setCost(int heuristic, int cost) {
		// Set the cost based on the type of heuristic
		if (heuristic == 1) {
			heuristic1Cost = cost;
		} else {
			heuristic2Cost = cost;
		}
	}

	private void setTime(int heuristic, long start) {
		// Set the duration based on the type of heuristic
		long duration = (System.nanoTime() - start) / 1000000; // Convert to milliseconds
		if (heuristic == 1) {
			heuristic1Duration = duration;
		} else {
			heuristic2Duration = duration;
		}
	}
}
