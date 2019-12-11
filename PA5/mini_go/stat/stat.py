import numpy as np, matplotlib.pyplot as plt, pandas as pd

def stat(path,tag):
    
    data = pd.read_csv(path,delimiter=",",header=None)

    data = np.array(data)

    data = data[data[:,0]<200000]

    plt.plot(data[:,0],data[:,1],label = tag)

def show(title):

    plt.xlabel("Timesteps")
    plt.ylabel("Rewards")
    plt.legend(loc = "lower right")
    plt.title(title)
    plt.show()

    
    
stat("../logs/log_5_1575965860.4041963","")
show("Results of mlp_dnn training")

stat("../logs/log_5_dqn_cnn_vs_rand_2_4_8_16_32_32_32","mlp layers:32, 32")
stat("../logs/log_5_dqn_cnn_vs_rand_2_4_8_16_32_64_64","mlp layers:64, 64")
stat("../logs/log_5_dqn_cnn_vs_rand_2_4_8_16_32_32_64_14","mlp layers:32, 64, 14")
show("Different sizes of mlp layers")
stat("../logs/log_5_dqn_cnn_vs_rand_2_4_8_16_32_128_128","mlp layers:128, 128")
stat("../logs/log_5_dqn_cnn_vs_rand_2_4_8_16_32_128","mlp layers:128")
show("Different sizes of mlp layers")

stat("../logs/log_5_dqn_cnn_vs_rand_2_2_4_4_8_16_32_64_14","mlp layers:32, 64, 14")
stat("../logs/log_5_dqn_cnn_vs_rand_2_2_4_4_8_16_64_64","mlp layers:64,64")
stat("../logs/log_5_dqn_cnn_vs_rand_2_2_4_4_8_16_128","mlp layers:128")
show("Different sizes of mlp layers")

stat("../logs/log_5_dqn_cnn_vs_rand_2_2_4_4_8_16_32_32","cnn layers:2,2,4,4,8,16")
stat("../logs/log_5_dqn_cnn_vs_rand_2_4_8_16_32_32_32","cnn layers:2,4,8,16,32")
show("Different sizes of cnn layers with mlp layer 32+32")

stat("../logs/log_5_dqn_cnn_vs_rand_2_2_4_4_8_16_64_64","cnn layers:2,2,4,4,8,16")
stat("../logs/log_5_dqn_cnn_vs_rand_2_4_8_16_32_64_64","cnn layers:2,4,8,16,32")
show("Different sizes of cnn layers with mlp layer 64+64")


stat("../logs/log_5_dqn_cnn_vs_rand_2_2_4_4_8_8_16_16_32_64_128","cnn layers:2,2,4,4,8,8,16,16,32")
stat("../logs/log_5_dqn_cnn_vs_rand_4_4_8_16_32_64_128","cnn layers:4,4,8,16,32")
show("Different sizes of cnn layers with mlp layer 64+128")

stat("../logs/log_5_dqn_cnn_vs_rand_2_2_4_4_8_16_128_128","cnn layers:2,2,4,4,8,16")
stat("../logs/log_5_dqn_cnn_vs_rand_2_4_8_16_32_128_128","cnn layers:2,4,8,16,32")
show("Different sizes of cnn layers with mlp layer 128+128")

