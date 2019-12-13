import numpy as np, matplotlib.pyplot as plt, pandas as pd

def stat(path,tag):
    
    data = pd.read_csv(path,delimiter=",",header=None)

    data = np.array(data)

    # data = data[data[:,0]<200000]

    plt.plot(data[:,0],data[:,1],label = tag)

def show(title):

    plt.xlabel("Timesteps")
    plt.ylabel("Rewards")
    # plt.legend(loc = "lower right")
    plt.title(title)
    plt.show()

stat("../logs/log_5_a2c_cnn_vs_rnd_2_4_8_16_32**_32_64_14","")
show("Training Result")