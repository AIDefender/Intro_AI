import matplotlib.pyplot as plt 
with open("./timestep_4","r") as f:
    data = f.read()

a = [eval(i) for i in data.split()]
plt.plot(a,label = "feature with 4 timestep")
with open("./timestep_1","r") as f:
    data = f.read()

a = [eval(i) for i in data.split()]
plt.plot(a,label = "feature with 1 timestep")
plt.legend(loc='upper right')
plt.xlabel("Index of game")
plt.ylabel("Num of timesteps")
plt.show()
