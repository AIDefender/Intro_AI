1. DQN加入模型加载,A2C加入模型加载与保存
2. 用小网络训练一个rollout policy 
3. 用卷积神经网络训练policy
- Log要增加当前参数信息
- 加一个 best-till-now模型保存(如果reward高于阀值0.1就保存)
- 加载模型后调整需要训练的次数和epsilon值.

- 更多的调参:学习率,epsilon,等

4. 加入MCTS
- 得搞一个很大的网络(refer the paper for exact sizes), 最好加上BN等.(4,4,8,8,16,16,32,32,64,64+mlp 64 128 32)
- dqn作为rollout policy 
- 训一个大的dqn,加BN,初始化

- MCTS:原来是一直学到底,这样慢.现在是学20步就用蒙特卡洛模拟.
    - 做inference的时候要现在脑海里面搜索.
    - 学习的时候要不要搜索呢?这时就应该改用A2C,这样搜索都是根据概率来的,比epsilon-greedy更有说服力.
        - 搜索几步?这个值可以逐渐增大.
- 现在发现了:学习的时候就强化学习的学;inference的时候改用Monte-Carlo搜索

5. 加入历史策略池(训练时从中随机采样)
6. 调参.
    - 初始化:正交初始化
    - epsilon 
    - lr

