package controllers.depthfirst;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Stack;

import com.sun.xml.internal.bind.v2.model.core.NonElement;
import com.sun.xml.internal.ws.policy.sourcemodel.ModelNode.Type;

import java.util.Queue;
import java.util.LinkedList;
import java.util.Random;
import java.util.ArrayDeque;
import java.util.Deque;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

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

    // * Stack 中绝对不能只存action;而应该存储state_action pairs;
    // * 有一个小动作不应该直接输出了,而应该在脑海中构想一下,构想失败还可以在栈中继续取state,
    // * 直接恢复到原来state再找动作
    // * Only once total search is needed
    // * 脑海中构想的action可以存在一个队列中;如果出现问题就清空这个队列
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


    // TODO:change it to act rationally
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
        // System.out.println("For Debug:all contained positions:");
        // for(StateObservation one_step_obs : reached_states)
        // {
    
        //     System.out.println(one_step_obs.getAvatarPosition());
        // }
    }

    // private void clear_queue()
    // {
    //     Queue<Types.ACTIONS> searced_actions = new LinkedList<Types.ACTIONS>();
    // }

    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        
        if (!choosed_actions.empty())
        {
            return choosed_actions.pop();
        }

        StateObservation stCopy = stateObs.copy();
        // save_state(stCopy);
        // save_state(stCopy);// Test the function to save state;seems good
        StateObservation stCopy_of_Copy=stateObs.copy();
        Boolean should_push = true;
        Boolean key_flag = false;
        Node poped_node = new Node();
        poped_node.node_state = stateObs;
        poped_node.node_action = null;
        poped_node.father_node = null;

        main:
        while(true)
        {
            // stCopy_of_Copy = stCopy.copy();
            Types.ACTIONS action = null;
        
            ArrayList<Types.ACTIONS> actions = stCopy.getAvailableActions();
            // System.out.println(actions);
            if (should_push)
            {
                for(Types.ACTIONS possible_action : actions)
                {
                    Node node = new Node();
                    node.father_node = poped_node;
                    node.node_state = stCopy;
                    node.node_action = possible_action;
                    unreached_nodes.push(node);
                }
            }
            stCopy_of_Copy = stCopy.copy();
            if (unreached_nodes.empty())
            {
                System.out.println("Stack Empty!!!");
                continue;
                // break;
            }
            poped_node = unreached_nodes.pop();
            action = poped_node.node_action;//!需要定义一个类,包含动作,状态,父节点等.直接pop一个对象即可
            // action = Types.ACTIONS.ACTION_UP;//Test still check,Good
            stCopy = poped_node.node_state;
            if (key_flag && action == Types.ACTIONS.ACTION_DOWN)
            {
                should_push = false;
                continue;
            }
            System.out.println("Now the agent is at:");
            System.out.println(stCopy.getAvatarPosition());
            if (stCopy.getAvatarPosition().equals(new Vector2d(100,200)))
            {
                key_flag = true;
            }
            if (stCopy.getAvatarPosition().equals(new Vector2d(150,150)))
            {
                System.out.println("here!");
            }
            
            save_state(stCopy);

            // System.out.println(action);

            stCopy.advance(action);
            System.out.println(stCopy.getAvatarPosition());
            System.out.println(action);

            if (stCopy.getAvatarPosition().equals(new Vector2d(150.0,100.0)) && action == Types.ACTIONS.ACTION_UP)
            {
                should_push = false;
                continue;
            }

            if(stCopy.isGameOver())
            {
                if (stCopy.getGameWinner()==Types.WINNER.PLAYER_WINS)
                {
                    break;
                }
                should_push = false;
                System.out.println("Game Over; rechoose action");
                continue;
            }
            else 
            {
                if(stCopy_of_Copy.equalPosition(stCopy))
                {
                    should_push = false;
                    System.out.println("Same Position; rechoose action");
                    continue;
                }
                for(StateObservation one_state_obs : reached_states)
                {
                    
                    // if(one_state_obs.getAvatarPosition()==stCopy.getAvatarPosition())
                    if(one_state_obs.equalPosition(stCopy))
                    {
                        should_push = false;
                        System.out.println("Same Position; rechoose action");
                        continue main;
                    }
                }
                should_push = true;   
            }
            
        }
        // we finally break here;Now we get node.action as the final action to win the game
        ArrayList<Types.ACTIONS> all_actions = new ArrayList<Types.ACTIONS>();
        all_actions.add(Types.ACTIONS.ACTION_DOWN);
        all_actions.add(Types.ACTIONS.ACTION_LEFT);
        all_actions.add(Types.ACTIONS.ACTION_RIGHT);
        all_actions.add(Types.ACTIONS.ACTION_UP);
        Node current_node = poped_node;
        Types.ACTIONS action_to_push = current_node.node_action;
        StateObservation last_state = current_node.father_node.node_state;
        while (true)
        {
            System.out.println("pos of father state");
            System.out.println(last_state.getAvatarPosition());
            System.out.println("pos of this state");
            System.out.println(current_node.node_state.getAvatarPosition());
            choosed_actions.push(action_to_push);
            for(Types.ACTIONS action:all_actions)
            {
                last_state = current_node.father_node.node_state;
                last_state.advance(action);
                if (last_state.equalPosition(current_node.node_state))
                {
                    action_to_push = action;
                    break;
                }
                
            }
            if (last_state.equalPosition(stateObs))
            {
                break;
            }
            current_node = current_node.father_node;
        }

        
        return Types.ACTIONS.ACTION_DOWN;
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
