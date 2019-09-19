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
 * Created with IntelliJ IDEA. User: ssamot Date: 14/11/13 Time: 21:45 This is a
 * Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
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

    protected Stack<Node> unreached_nodes = new Stack<Node>();
    protected ArrayList<StateObservation> reached_states = new ArrayList<StateObservation>();
    protected Stack<Types.ACTIONS> choosed_actions = new Stack<Types.ACTIONS>();
    /**
     * Public constructor with state observation and time due.
     * 
     * @param so           state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer) {
        randomGenerator = new Random();
        grid = so.getObservationGrid();
        block_size = so.getBlockSize();
    }

    /**
     * Picks an action. This function is called every game step to request an action
     * from the player.
     * 
     * @param stateObs     Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */


    private void save_state(StateObservation para_stateObs)
    {
        Boolean should_add = true;
        for(StateObservation one_step_obs : reached_states)
        {
            if (para_stateObs.equalPosition(one_step_obs))
            {
                should_add = false;
                break;
            }
        }
        if (should_add)
        {
            reached_states.add(para_stateObs.copy());
        }
    }

    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        
        //TODO:修改为每一步进行一次深度搜索，但这时不需要一定搜索到通关 ，
        //TODO:而是搜索到一定的深度，再设计一个启发式函数判断局面好坏
        if (!choosed_actions.empty())
        {
            return choosed_actions.pop();
        }

        StateObservation stCopy = stateObs.copy();
        StateObservation stCopy_of_Copy=stateObs.copy();
        Boolean should_push = true;
        Node poped_node = new Node();
        poped_node.node_state = stateObs;
        poped_node.node_action = null;
        poped_node.father_node = null;

        main:
        while(true)
        {
            Types.ACTIONS action = null;
        
            ArrayList<Types.ACTIONS> actions = stCopy.getAvailableActions();
            if (should_push)
            {
                for(Types.ACTIONS possible_action : actions)
                {
                    Node node = new Node();
                    node.father_node = poped_node.clone();
                    node.node_state = stCopy.copy();//这个copy异常关键!
                    node.node_action = possible_action;
                    unreached_nodes.push(node);
                }
            }
            stCopy_of_Copy = stCopy.copy();
            if (unreached_nodes.empty())
            {
                System.out.println("Stack Empty!!!");
                continue;
            }
            poped_node = unreached_nodes.pop();
            action = poped_node.node_action;//!需要定义一个类,包含动作,状态,父节点等.直接pop一个对象即可
            stCopy = poped_node.node_state;
            save_state(stCopy);

            stCopy.advance(action);

            if(stCopy.isGameOver())
            {
                if (stCopy.getGameWinner()==Types.WINNER.PLAYER_WINS)
                {
                    break;
                }
                should_push = false;
                continue;
            }
            else 
            {
                if(stCopy_of_Copy.equalPosition(stCopy))
                {
                    should_push = false;
                    continue;
                }
                for(StateObservation one_state_obs : reached_states)
                {
                    
                    if(one_state_obs.equalPosition(stCopy))
                    {
                        should_push = false;
                        continue main;
                    }
                }
                should_push = true;   
            }
            
        }
        // 深搜成功.现根据父节点推断每步动作
        ArrayList<Types.ACTIONS> all_actions = new ArrayList<Types.ACTIONS>();
        all_actions.add(Types.ACTIONS.ACTION_DOWN);
        all_actions.add(Types.ACTIONS.ACTION_LEFT);
        all_actions.add(Types.ACTIONS.ACTION_RIGHT);
        all_actions.add(Types.ACTIONS.ACTION_UP);
        choosed_actions.push(poped_node.node_action);
        Node current_node = poped_node.father_node.clone();
        Types.ACTIONS action_to_push = current_node.node_action;
        StateObservation last_state = current_node.father_node.node_state;
        while (true)
        {
            if (current_node.father_node == null)
            {
                break;
            }
            for(Types.ACTIONS action:all_actions)
            {
                last_state = current_node.father_node.node_state.copy();
                last_state.advance(action);
                if (last_state.equalPosition(current_node.node_state))
                {
                    action_to_push = action;
                    break;
                }
                
            }
            choosed_actions.push(action_to_push);
            if (last_state.equalPosition(stateObs))
            {
                break;
            }
            current_node = current_node.father_node.clone();
        }

        
        return choosed_actions.pop();
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
