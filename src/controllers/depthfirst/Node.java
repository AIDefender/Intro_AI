package controllers.depthfirst;
import core.game.StateObservation;
import ontology.Types.ACTIONS;
import ontology.Types;

public class Node {
    public StateObservation node_state;
    public Types.ACTIONS node_action;
    public Node father_node;
    public Object clone(){
        return super.clone();
    }
}
