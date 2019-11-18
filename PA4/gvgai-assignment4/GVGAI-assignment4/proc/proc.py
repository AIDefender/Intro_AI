import matplotlib.pyplot as plt
a=eval("0.0")
with open("./out","r") as f:
    data = f.read()
nums = [i for i in data.split()]
avg_score = []
sum = 0
items = 0
for i in nums:
    try:
        if (eval(i)==0 and items != 0):
            avg_score.append(sum/items)
            sum=0
            items=0
        else:
            sum+=eval(i)
            items+=1
    except (NameError,SyntaxError):
        # print(i)
        pass 
avg_score = [i for i in avg_score if i != 0]
# print(avg_score)
plt.plot(avg_score)
plt.show()


