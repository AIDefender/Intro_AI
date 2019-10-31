package gamePlayer.algorithms;

import gamePlayer.Action;
import gamePlayer.Decider;
import gamePlayer.InvalidActionException;
import gamePlayer.State;
import gamePlayer.State.Status;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents an AI Decider that uses a MiniMax algorithm.
 * We use alpha-beta pruning, but besides that we're pretty vanilla.
 * @author Ashoat Tevosyan
 * @author Peter Brook 
 * @since Mon April 28 2011
 * @version CSE 473
 */
public class MiniMaxDecider implements Decider {
	
	// Are we maximizing or minimizing?
	private boolean maximize;
	// The depth to which we should analyze the search space
	private int depth;
	// HashMap to avoid recalculating States
	private Map<State, Float> computedStates;
	// Used to generate a graph of the search space for each turn in SVG format
	private static final boolean DEBUG = true;
	
	/**
	 * Initialize this MiniMaxDecider. 
	 * @param maximize Are we maximizing or minimizing on this turn? True if the former.
	 * @param depth    The depth to which we should analyze the search space.
	 */
	public MiniMaxDecider(boolean maximize, int depth) {
		this.maximize = maximize;
		this.depth = depth;
		computedStates = new HashMap<State, Float>();
	}
	
	/**
	 * Decide which state to go into.
	 * We manually MiniMax the first layer so we can figure out which heuristic is from which Action.
	 * Also, we want to be able to choose randomly between equally good options.
	 * "I'm the decider, and I decide what is best." - George W. Bush
	 * @param state The start State for our search.
	 * @return The Action we are deciding to take.
	 */
	int count_num = 0;
	double total_time = 0;
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Action decide(State state) {
		System.out.println(this.depth);
		double startTimeMillis = System.currentTimeMillis();

		// Choose randomly between equally good options
		float value = maximize ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY;
		List<Action> bestActions = new ArrayList<Action>();
		// Iterate!
		int flag = maximize ? 1 : -1;
		for (Action action : state.getActions()) {
			try {
				// Algorithm!
				State newState = action.applyTo(state);
				float newValue = this.miniMaxRecursor(newState, 1, !this.maximize, false, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
				float newValue_pruned = this.miniMaxRecursor(newState, 1, !this.maximize, true, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
				assert (newValue == newValue_pruned);
				// Better candidates?
				// ! Got it! 他没加alpha-beta剪枝!
				if (flag * newValue > flag * value) {
					value = newValue;
					bestActions.clear();
				}
				// Add it to the list of candidates?
				if (flag * newValue >= flag * value) bestActions.add(action);
			} catch (InvalidActionException e) {
				throw new RuntimeException("Invalid action!");
			}
		}
		// If there are more than one best actions, pick one of the best randomly
		Collections.shuffle(bestActions);
		System.out.println("Time passed:");
		double time_passed = System.currentTimeMillis()-startTimeMillis;
		System.out.println(time_passed);
		total_time+=time_passed;
		count_num+=1;
		if (count_num%5==4)
		{
			System.out.println("avg time till now:");
			System.out.println(total_time/count_num);
		}
		return bestActions.get(0);
	}
	
	/**
	 * The true implementation of the MiniMax algorithm!
	 * Thoroughly commented for your convenience.
	 * @param state    The State we are currently parsing.
	 * @param alpha    The alpha bound for alpha-beta pruning.
	 * @param beta     The beta bound for alpha-beta pruning.
	 * @param depth    The current depth we are at.
	 * @param maximize Are we maximizing? If not, we are minimizing.
	 * @return The best point count we can get on this branch of the state space to the specified depth.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public float miniMaxRecursor(State state, int depth, boolean maximize, boolean use_pruning, float alpha, float beta) {
		// Has this state already been computed?
		if (computedStates.containsKey(state)) 
                    // Return the stored result
                    return computedStates.get(state);
		// Is this state done?
		if (state.getStatus() != Status.Ongoing)
                    // Store and return
                    return finalize(state, state.heuristic());
		// Have we reached the end of the line?
		if (depth == this.depth)// * 注意this.的用法,和self.差不多
                    //Return the heuristic value
                    return state.heuristic();
                
		// If not, recurse further. Identify the best actions to take.
		float value = maximize ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY;
		int flag = maximize ? 1 : -1; //* 最大:1,最小:-1.其实把最大和最小放在一个函数中也体现了避免代码拷贝的原则
		double prune_condition = maximize ? beta : alpha;  // * beta在max中被利用,在min中维护
		double ab_to_change = maximize? alpha : beta; 
		List<Action> test = state.getActions();
		for (Action action : test) {
			// Check it. Is it better? If so, keep it.
			try {
				State childState = action.applyTo(state);
				// ! 大小交替,深度多1
				float newValue = this.miniMaxRecursor(childState, depth + 1, !maximize, use_pruning, alpha, beta);
				//Record the best value
				if (flag * newValue > flag * value) 
					value = newValue;
				if (use_pruning)
				{
					if (flag * value >= flag * prune_condition)
					{
						return finalize(state, value);
					}
					if (flag * value > flag * ab_to_change)
					{
						ab_to_change = value;
					}
				}
			} catch (InvalidActionException e) {
                                //Should not go here
				throw new RuntimeException("Invalid action!");
			}
		}
		// Store so we don't have to compute it again.
		return finalize(state, value);
	}
	
	/**
	 * Handy private function to stick into HashMap before returning.
	 * We don't always want to stick into our HashMap, so use carefully.
	 * @param state The State we are hashing.
	 * @param value The value that State has.
	 * @return The value we were passed.
	 */
	private float finalize(State state, float value) {
		// THIS IS BROKEN DO NOT USE
		//computedStates.put(state, value);
		return value;
	}
	
}