import csv
import random

import pandas as pd
import numpy as np
import seaborn as sns
import matplotlib.pyplot as plt

# file_path = "../sim_results_100_600/csvs/"
# file_name = "1761835027688_KASUS.csv"

file_path = "../sim_results/csvs/ite0/"
file_name = "1761912600405_5500_500_5500_node038.csv"
output_file_path = 'taskAnalysisPlots/ite0/'
# Open the CSV file in read mode ('r')
# with open(file_path+file_name, mode='r', newline='') as file:
#     # Create a csv.reader object
#     csv_reader = csv.reader(file)
#
#     # Iterate over each row in the CSV file
#     for row in csv_reader:
#         print(row)
df = pd.read_csv(file_path+file_name)
df['mobileDevId'] = df['mobileDevId'].astype(int)
df['taskSubmitTime'] = df['taskSubmitTime'].astype(float)
# print(df)

# mobileDevId_counts = df['mobileDevId'].value_counts()
# print("Frequency of values in 'mobileDevId' column:")
# print(mobileDevId_counts)
plt.figure(figsize=(25, 6))
g = sns.countplot(x='mobileDevId', data=df)
plt.title('Frequency of Mobile Device used for task creation')
plt.ylabel('task count')
plt.xlabel('mobile device id')
#g.set_xticks(range(10))

xtick_interval = (df['mobileDevId'].max()+1)/10
g.set_xticks(np.arange(1, df['mobileDevId'].max()+1, xtick_interval).tolist())
# set the labels
xtick_label_list = (np.arange(0, df['mobileDevId'].max()+1, xtick_interval)).tolist()
xtick_label_integer_list = [int(x) for x in xtick_label_list]
g.set_xticklabels(xtick_label_integer_list)
#plt.axis.set_xticks(self, ticks, minor=False)
#plt.show()
plt.savefig(output_file_path+'frequencyChartTaskCreation'+'_'+file_name.split(".")[0]+'.png')


# Declaring a figure "gnt"
# fig, gnt = plt.subplots()
subplot_y_size = 7 #int(10*df['mobileDevId'].max()/99)
subplot_x_size = 12 #subplot_y_size #int(subplot_y_size/2)
fig, gnt = plt.subplots(figsize=(subplot_x_size, subplot_y_size))
# Setting Y-axis limits
#gnt.set_ylim(0, 50)
gnt.set_ylim(0, df['mobileDevId'].max())

# Setting X-axis limits
#gnt.set_xlim(0, 160)
gnt.set_xlim(df['taskSubmitTime'].min()-10, df['taskSubmitTime'].max())

# Setting labels for x-axis and y-axis
gnt.set_xlabel('mseconds since start')
gnt.set_ylabel('Mobile Device Id')

# Setting ticks on y-axis
#gnt.set_yticks([15, 25, 35, 40, 45])
#list_ytick_label = np.arange(1, df['mobileDevId'].max()+1, 1).tolist()
# Labelling tickes of y-axis
#gnt.set_yticklabels(list_ytick_label)

# Setting graph attribute
gnt.grid(True)
delta_t = 0.5
bar_thickness = 0.5
for mobileDeviceIndex in range(0, df['mobileDevId'].max()+1):
    # print(f'mobileDeviceIndex: {mobileDeviceIndex}')
    filtered_df = df[df['mobileDevId'] == mobileDeviceIndex]
    #print(filtered_df)
    temp_list = []
    for index, row in filtered_df.iterrows():
        #print(row['mobileDevId'], row['taskSubmitTime'])
        temp_list.append((row['taskSubmitTime'],delta_t))
    # color_val_r = mobileDeviceIndex*1.0*(1.0/df['mobileDevId'].max())
    # color_val_g = mobileDeviceIndex*0.5 * (1.0 / df['mobileDevId'].max())
    # color_val_b = mobileDeviceIndex*0.25 * (1.0 / df['mobileDevId'].max())

    color_val_r = random.randint(0,255)/255.0
    color_val_g = random.randint(0,255)/255.0
    color_val_b = random.randint(0,255)/255.0
    #print(f'temp_list: {temp_list}')
    gnt.broken_barh(temp_list,(mobileDeviceIndex, bar_thickness), facecolors=(color_val_r, color_val_g, color_val_b, 0.5))
# # Declaring a bar in schedule
# gnt.broken_barh([(40, 50)], (30, 3), facecolors =('tab:orange'))
#
# # Declaring multiple bars in at same level and same width
# gnt.broken_barh([(110, 10), (150, 10)], (10, 9),
#                          facecolors ='tab:blue')
#
# gnt.broken_barh([(10, 50), (100, 20), (130, 10)], (20, 9),
#                                   facecolors =('tab:red'))

plt.savefig(output_file_path+'gantt_'+file_name.split(".")[0]+'.png')