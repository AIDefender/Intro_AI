package controllers.depthfirst;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Stack;
import java.util.Random;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;

/**
 * Created with IntelliJ IDEA.
 * User: ssamot
 * Date: 14/11/13
 * Time: 21:45
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Agent extends AbstractPlayer {

    /**
     * Random generator for the agent.
     */
    protected Random randomGenerator;

    /**
     * Observation grid.
     */
    protected ArrayList<Observation> grid[][];

    /**
     * block size
     */
    protected int block_size;

    protected Stack<Types.ACTIONS> unreached = new Stack<Types.ACTIONS>();
    protected ArrayList<StateObservation> reached_states = new ArrayList<StateObservation>();

    /**
     * Public constructor with state observation and time due.
     * @param so state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer)
    {
        randomGenerator = new Random();
        grid = so.getObservationGrid();
        block_size = so.getBlockSize();
    }


    /**
     * Picks an action. This function is called every game step to request an
     * action from the player.
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {

        StateObservation stCopy_0 = stateObs.copy();
        reached_states.add(stCopy_0);
        System.out.println("For Debug:all contained positions:");
        for(StateObservation one_step_obs : reached_states)
        {

            System.out.println(one_step_obs.getAvatarPosition());
        }

        ArrayList<Observation>[] npcPositions = stateObs.getNPCPositions();
        // System.out.println(npcPositions);
        ArrayList<Observation>[] fixedPositions = stateObs.getImmovablePositions();
        // System.out.println(fixedPositions[0]);
        // for(int i = 0; i<fixedPositions.length; i++){
        //     System.out.println(fixedPositions[i]);
        // }
        ArrayList<Observation>[] movingPositions = stateObs.getMovablePositions();
        ArrayList<Observation>[] resourcesPositions = stateObs.getResourcesPositions();
        ArrayList<Observation>[] portalPositions = stateObs.getPortalsPositions();
        grid = stateObs.getObservationGrid();
        // System.out.println(grid[0][0]);

        /*printDebug(npcPositions,"npc");
        printDebug(fixedPositions,"fix");
        printDebug(movingPositions,"mov");
        printDebug(resourcesPositions,"res");
        printDebug(portalPositions,"por");
        System.out.println();               */

        Types.ACTIONS action = null;
        StateObservation stCopy = stateObs.copy();

        double avgTimeTaken = 0;
        double acumTimeTaken = 0;
        long remaining = elapsedTimer.remainingTimeMillis();
        int numIters = 0;

        int remainingLimit = 5;
        ArrayList<Types.ACTIONS> actions = stateObs.getAvailableActions();
        // System.out.println(actions);
        for(Types.ACTIONS possible_action : actions)
        {
            unreached.push(possible_action);
        }
        main:
        while(remaining > 2*avgTimeTaken && remaining > remainingLimit)
        {
            ElapsedCpuTimer elapsedTimerIteration = new ElapsedCpuTimer();
            // int index = randomGenerator.nextInt(actions.size());
            // action = actions.get(index);
            
            numIters++;
            acumTimeTaken += (elapsedTimerIteration.elapsedMillis()) ;
            //System.out.println(elapsedTimerIteration.elapsedMillis() + " --> " + acumTimeTaken + " (" + remaining + ")");
            avgTimeTaken  = acumTimeTaken/numIters;
            remaining = elapsedTimer.remainingTimeMillis();
            
            if (unreached.empty())
            {
                System.out.println("Stack Empty!!!");
                break;
            }
            action = unreached.pop();
            System.out.println(action);
            stCopy.advance(action);
            System.out.println("For Debug:After action:");
            System.out.println(stCopy.getAvatarPosition());
            if(stCopy.isGameOver())
            {
                stCopy = stateObs.copy();
                System.out.println("Game Over; rechoose action");
                continue;
            }
            else 
            {
                if(stateObs.equalPosition(stCopy))
                {
                    stCopy = stateObs.copy();
                    System.out.println("Same Position; rechoose action");
                    continue;
                }
                for(StateObservation one_state_obs : reached_states)
                {

                    if(one_state_obs.equalPosition(stCopy))
                    {
                        stCopy = stateObs.copy();
                        System.out.println("Same Position; rechoose action");
                        continue main;
                    }
                }
                
                break;
            }
            
        }
        System.out.println("Action finally taken:");
        System.out.println(action);
        return action;
    }

    /**
     * Prints the number of different types of sprites available in the "positions" array.
     * Between brackets, the number of observations of each type.
     * @param positions array with observations.
     * @param str identifier to print
     */
    private void printDebug(ArrayList<Observation>[] positions, String str)
    {
        if(positions != null){
            System.out.print(str + ":" + positions.length + "(");
            for (int i = 0; i < positions.length; i++) {
                System.out.print(positions[i].size() + ",");
            }
            System.out.print("); ");
        }else System.out.print(str + ": 0; ");
    }

    /**
     * Gets the player the control to draw something on the screen.
     * It can be used for debug purposes.
     * @param g Graphics device to draw to.
     */
    public void draw(Graphics2D g)
    {
        int half_block = (int) (block_size*0.5);
        for(int j = 0; j < grid[0].length; ++j)
        {
            for(int i = 0; i < grid.length; ++i)
            {
                if(grid[i][j].size() > 0)
                {
                    Observation firstObs = grid[i][j].get(0); //grid[i][j].size()-1
                    //Three interesting options:
                    int print = firstObs.category; //firstObs.itype; //firstObs.obsID;
                    g.drawString(print + "", i*block_size+half_block,j*block_size+half_block);
                }
            }
        }
    }
}
