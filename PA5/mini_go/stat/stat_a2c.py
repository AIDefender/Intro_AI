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


stat("../logs/log_5_a2c_cnn_vs_rnd_2_4_8_16_32**_32_64_14","mlp sizes 32, 64, 14")
stat("../logs/log_5_a2c_cnn_vs_rnd_2_4_8_16_32**_32_64_32","mlp sizes 32, 64, 32")
show("Different sizes of mlp layers")
stat("../logs/log_5_a2c_cnn_vs_rnd_2_4_8_16_32**_32_32","mlp sizes 32, 32")
stat("../logs/log_5_a2c_cnn_vs_rnd_2_4_8_16_32**_64_64","mlp sizes 64, 64")
show("Different sizes of mlp layers")

stat("../logs/log_5_a2c_cnn_vs_rnd_2_2_4_4_8_8_16_16_32_64**_32_64_32","cnn sizes 2,2,4,4,8,8,16,16,32,64")
stat("../logs/log_5_a2c_cnn_vs_rnd_2_2_4_4_8_8_16**_32_64_32","cnn sizes 2,2,4,4,8,8,16")
stat("../logs/log_5_a2c_cnn_vs_rnd_2_4_8_16_32**_32_64_32","cnn sizes 2,4,8,16,32")
show("Different sizes of cnn layers with mlp layers 32,64,32")

stat("../logs/log_5_a2c_cnn_vs_rnd_2_4_8_16_32**_32_64_14","cnn layers 2,4,8,16,32")
stat("../logs/log_5_a2c_cnn_vs_rnd_2_2_4_4_8_16**_32_64_14","cnn layers 2,2,4,4,8,8,16")
show("Different sizes of cnn layers with mlp layers 32,64,14")