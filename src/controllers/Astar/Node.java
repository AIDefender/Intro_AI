package controllers.Astar;
import core.game.StateObservation;
import ontology.Types;

public class Node implements Cloneable{
    public StateObservation node_state;
    public Types.ACTIONS node_action;
    public Node father_node;
    public double depth;
    public double priority;//值越小优先级越高
    @Override
    public Node clone(){
        Node node = null;
        try{
            node = (Node)super.clone();
        }catch(CloneNotSupportedException e){
            e.printStackTrace();
        }
        return node;
    }
}
