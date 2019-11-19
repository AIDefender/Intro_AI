package controllers.learningmodel;

import controllers.Heuristics.SimpleStateHeuristic;
import controllers.Heuristics.StateHeuristic;
import controllers.Heuristics.WinScoreHeuristic;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeoutException;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Utils;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

public class Agent extends AbstractPlayer {

    protected Classifier m_model;
    protected Random m_rnd;
    private static int SIMULATION_DEPTH = 100;
    private final HashMap<Integer, Types.ACTIONS> action_mapping;
    protected QPolicy m_policy;
    protected int N_ACTIONS;
    protected static Instances m_dataset;
    protected int m_maxPoolSize = 1000;
    protected double m_gamma = 0.99;
    protected int nr = 0;

    public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        m_rnd = new Random();

        // convert numbers to actions
        action_mapping = new HashMap<Integer, Types.ACTIONS>();
        int i = 0;
        for (Types.ACTIONS action : stateObs.getAvailableActions()) {
            action_mapping.put(i, action);
            i++;
        }

        N_ACTIONS = stateObs.getAvailableActions().size();
        m_policy = new QPolicy(N_ACTIONS);
        m_dataset = new Instances(RLDataExtractor.s_datasetHeader);
    }

    /**
     *
     * Learning based agent.
     *
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {

        //m_timer = elapsedTimer;
        learnPolicy(stateObs, SIMULATION_DEPTH, new WinScoreHeuristic(stateObs));

        Types.ACTIONS bestAction = null;
        try {
            double[] features = RLDataExtractor.featureExtract(stateObs);
            int action_num = m_policy.getActionNoExplore(features); // no exploration
            bestAction = action_mapping.get(action_num);
        } catch (Exception exc) {
            exc.printStackTrace();
        }

        // System.out.println("====================");
        return bestAction;
    }

    private Instances simulate(StateObservation stateObs, StateHeuristic heuristic, QPolicy policy) {

        Instances data = new Instances(RLDataExtractor.datasetHeader(), 0);
        stateObs = stateObs.copy();

        Instance sequence[] = new Instance[SIMULATION_DEPTH];
        int depth = 0;
        double factor = 1;
        for (; depth < SIMULATION_DEPTH; depth++) {
            try {
                double[] features = RLDataExtractor.featureExtract(stateObs);

                int action_num = policy.getAction(features);

                double score_before = heuristic.evaluateState(stateObs);

                // simulate
                Types.ACTIONS action = action_mapping.get(action_num);
                stateObs.advance(action);

                double score_after = heuristic.evaluateState(stateObs);

                double delta_score = factor * (score_after - score_before);
                // System.out.println(delta_score);
                factor = factor * m_gamma;
                // collect data
                sequence[depth] = RLDataExtractor.makeInstance(features, action_num, delta_score);
                // System.out.println(sequence[depth].classValue());

            } catch (Exception exc) {
                exc.printStackTrace();
                break;
            }
            if (stateObs.isGameOver()) {
                depth++;
                break;
            }
        }

        // get the predicted Q from the last state(最后一个state,而不是上一个state)
        double accQ = 0;
        if (!stateObs.isGameOver()) {
            try {
                accQ = factor*policy.getMaxQ(RLDataExtractor.featureExtract(stateObs));
            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }

        // calculate the acumulated Q
        for (depth = depth - 1; depth >= 0; depth--) {
            accQ += sequence[depth].classValue();
            sequence[depth].setClassValue(accQ);// 重新标定类别.
            // System.out.println(accQ);
            data.add(sequence[depth]);
        }
        nr++;
        if (nr>100)
        {
            System.out.println(accQ);
            nr=0;
        }
        return data;
    }

    private void learnPolicy(StateObservation stateObs, int maxdepth, StateHeuristic heuristic) {

        // assume we need SIMULATION_DEPTH*10 milliseconds for one iteration
        int iter = 0;
        while (iter++ <= 10 //truem_timer.remainingTimeMillis() > SIMULATION_DEPTH*10
                ) {

            // get training data of the MC sampling
            Instances dataset = simulate(stateObs, heuristic, m_policy);

            // update dataset
            m_dataset.randomize(m_rnd);
            // System.out.println("num of instances");
            // System.out.println(dataset.numInstances());
            for (int i = 0; i < dataset.numInstances(); i++) {
                m_dataset.add(dataset.instance(i)); // add to the last
            }
            while (m_dataset.numInstances() > m_maxPoolSize) {
                m_dataset.delete(0);
            }
        }
        // train policy
        try {
            m_policy.fitQ(m_dataset);
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }
}
