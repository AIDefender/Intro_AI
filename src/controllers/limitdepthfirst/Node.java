package controllers.limitdepthfirst;
import core.game.StateObservation;
import ontology.Types;
// TODO:加上深度信息,作迭代深度受限搜索
public class Node implements Cloneable{
    public StateObservation node_state;
    public Types.ACTIONS node_action;
    public Node father_node;
    public int depth;
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
