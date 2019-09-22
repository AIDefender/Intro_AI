package controllers.Astar;

import java.awt.Graphics2D;
import java.lang.ProcessBuilder.Redirect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Stack;

import java.util.Random;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import java.util.PriorityQueue;
import tools.Vector2d;
import java.util.Queue;

/**
 * Created with IntelliJ IDEA. User: ssamot Date: 14/11/13 Time: 21:45 This is a
 * Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Agent1 extends AbstractPlayer {

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
    protected int KEY_COVERED_PUNISH = 1000000000;
    protected int block_size;
    protected int MAX_DEPTH = 15;




    protected Comparator<Node> comparator= new Comparator<Node>(){
        public int compare(Node n1, Node n2){
            if (n1.priority<n2.priority)
            {
                return -1;
            }
            if (n1.priority>n2.priority)
            {
                return 1;
            }
            return 0;
        }
    };
    // * unreached_nodes原来是Stack型,现在是优先队列,优先级就是A*函数值
    // protected Stack<Node> unreached_nodes = new Stack<Node>();
    protected Queue<Node> unreached_nodes;
    protected ArrayList<StateObservation> reached_states = new ArrayList<StateObservation>();
    protected Stack<Types.ACTIONS> choosed_actions = new Stack<Types.ACTIONS>();
    protected Vector2d goalpos;
    protected Vector2d keypos;
    protected ArrayList<Vector2d> boxpos;
    /**
     * Public constructor with state observation and time due.
     * 
     * @param so           state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public Agent1(StateObservation so, ElapsedCpuTimer elapsedTimer) {
        randomGenerator = new Random();
        grid = so.getObservationGrid();
        block_size = so.getBlockSize();
        ArrayList<Observation>[] fixedPositions = so.getImmovablePositions();
        ArrayList<Observation>[] movingPositions = so.getMovablePositions();
        goalpos = fixedPositions[1].get(0).position; //目标的坐标
        keypos = movingPositions[0].get(0).position;//钥匙的坐标
        System.out.println("LimitdepthInitialized!!");
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
    private double heur_function(StateObservation stateObs, Boolean key_flag)
    {
        // ! 深刻理解启发函数设计的原则!
        // TODO: 防止箱子被推到角上
        Vector2d state_vec = stateObs.getAvatarPosition();
        double goal_dist = state_vec.dist(goalpos)/50;
        if (key_flag)
        {
            return goal_dist; 
        }
        // 计算与盒子的距离
        double least_box_dist = 10000000;
        double box_key_dist = 0;
        try{
            for (Observation box_obj:stateObs.getMovablePositions()[1])
            {
                Vector2d box_pos = box_obj.position;

                if (box_pos.equals(keypos))
                {
                    //钥匙被覆盖
                    return 10000;
                }
                box_key_dist = box_key_dist + box_pos.dist(keypos);

                if (box_pos.equals(new Vector2d(50,200))||box_pos.equals(new Vector2d(550,200)))
                {
                    return 10000;
                }

                // System.out.println(box_pos);
                double box_dist = box_pos.dist(state_vec);
                if (box_dist < least_box_dist)
                {
                    least_box_dist = box_dist;
                }
            }
        } catch (Exception e){
            ;
        }
        // TODO:把50改成block_size
        // System.out.println(least_box_dist);
        return (state_vec.dist(keypos))/50+least_box_dist/20+box_key_dist/20;
    }
    Boolean has_rule = false;
    Boolean rule_flag = false;
    Boolean key_flag = false; 
    public Types.ACTIONS update_rules(StateObservation stateObs)
    {
        has_rule = false;
        if (rule_flag == true && stateObs.getAvatarPosition().equals(new Vector2d(300,100)) && !key_flag)
        {
            has_rule = true;
            choosed_actions = new Stack<Types.ACTIONS>();
            choosed_actions.push(Types.ACTIONS.ACTION_DOWN);
            choosed_actions.push(Types.ACTIONS.ACTION_DOWN);
            choosed_actions.push(Types.ACTIONS.ACTION_DOWN);
            choosed_actions.push(Types.ACTIONS.ACTION_DOWN);
            return Types.ACTIONS.ACTION_DOWN;
        }
        if (stateObs.getAvatarPosition().equals(new Vector2d(200,200)) && rule_flag==false)
        {
            rule_flag = false;
            has_rule = true;
            return Types.ACTIONS.ACTION_LEFT;
        }
        if (stateObs.getAvatarPosition().equals(new Vector2d(150,200)) && rule_flag==false)
        {
            rule_flag = true;
            has_rule = true;
            return Types.ACTIONS.ACTION_UP;
        }
        return Types.ACTIONS.ACTION_UP;
        
    }

    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        
        Boolean win_break = false;
        Types.ACTIONS pre_action = update_rules(stateObs);
        if (has_rule)
        {
            return pre_action;
        }
        if (choosed_actions.size()>1 || win_break)
        {
            return choosed_actions.pop();
        }
        // * Re-initiate stack and record upon each step begins 
        unreached_nodes = new PriorityQueue<Node>(1,comparator);
        reached_states = new ArrayList<StateObservation>();
        Boolean depth_break = false; 
        // System.out.println(stateObs.getAvatarPosition());
        // if(stateObs.getAvatarPosition().equals(new Vector2d(150,200)))
        // {
        //     System.out.println("here");
        //     ;
        // }
        // * Preparation

        StateObservation stCopy = stateObs.copy();
        StateObservation stCopy_of_Copy=stateObs.copy();
        Boolean should_push = true;
        Types.ACTIONS heur_action = null;

        // * initiate the root node 
        Node poped_node = new Node();
        poped_node.node_state = stateObs.copy();
        poped_node.node_action = null;
        poped_node.father_node = poped_node.clone();
        poped_node.depth = 1;

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
                    node.depth = node.father_node.depth+1;
                    unreached_nodes.add(node);
                }
            }
            stCopy_of_Copy = stCopy.copy();
            if (unreached_nodes.isEmpty())
            {
                System.out.println("Stack Empty!!!");
                break;
            }
            poped_node = unreached_nodes.poll();
            // System.out.println(poped_node.depth);
            // System.out.println(poped_node.node_state.getAvatarPosition());
            if (poped_node.node_state.getAvatarPosition().equals(keypos))
            {
                key_flag = true;
            }

            // * Judge whether max_depth is exceeded

            if (poped_node.depth > MAX_DEPTH)
            {   
                depth_break = true;
                double least_value = 100000000;
                Node best_node = null;
                // * 利用启发函数挑选出一步最优动作
                while (! unreached_nodes.isEmpty())
                {
                    Node curr_node = unreached_nodes.poll();
                    double curr_value = heur_function(curr_node.node_state, key_flag);
                    // System.out.println(curr_value);
                    if (curr_value < least_value)
                    {
                        least_value = curr_value;
                        best_node = curr_node.clone();
                    }
                }
                // * 这里拿到了最好的状态,已经可以break, 出去回溯了
                poped_node=best_node.clone();
                break;
            }

            action = poped_node.node_action;//!需要定义一个类,包含动作,状态,父节点等.直接pop一个对象即可
            stCopy = poped_node.node_state;
            save_state(stCopy);

            stCopy.advance(action);
            // System.out.println(stCopy.getAvatarPosition());

            if(stCopy.isGameOver())
            {
                if (stCopy.getGameWinner()==Types.WINNER.PLAYER_WINS)
                {
                    win_break = true;
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
        // * depth_break的区别就是poped_node不一样,还有不能改choosed_action
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
            if (current_node.father_node==null)
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
        if (depth_break)
        {
            Types.ACTIONS action_to_choose = choosed_actions.pop();
            // choosed_actions = new Stack<Types.ACTIONS>();
            return action_to_choose;
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
