import sys 
sys.path.append("../")
from algorithms.policy_gradient import PolicyGradient
from algorithms.dqn import DQN
from mcts import MCTS
from agent.agent import Agent
class MCTSAgent():

    def __init__(self,policy_module, rollout_module):

        self._rl_agent = policy_module
        self._rollout_agent = rollout_module

        # * policy_fn的准备要花时间.policy要求的legal_actions可以再env返回的
        # * time_step中得到.
        # * 那个(action, prob) tuple可以就是一个0~24的数组+prob


        self.mcts = MCTS(value_fn = self._rl_agent.value_fn,
                         policy_fn = self._rl_agent.policy_fn,
                         rollout_policy_fn = self._rollout_agent.policy_fn)

    def step(self,timestep,env):

        move = self.mcts.get_move(timestep,env)
        self.mcts.update_with_move(move)
        return move 

        