import pandas as pd
import os
import re
import numpy as np
import glob
import matplotlib.pyplot as plt
import seaborn as sns

def find_files_with_pattern(root_dir, pattern):
    """
    Searches for files containing a specific pattern within all subfolders.

    Args:
        root_dir (str): The starting directory for the search.
        pattern (str): The regular expression pattern to search for within file content.
    """
    regex = re.compile(pattern)  # Compile the regex for efficiency

    for root, _, files in os.walk(root_dir):
        for file_name in files:
            file_path = os.path.join(root, file_name)
            try:
                with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
                    content = f.read()
                    if regex.search(content):
                        print(f"Pattern found in: {file_path}")
            except Exception as e:
                print(f"Error reading {file_path}: {e}")

def find_filenames_with_pattern(root_dir, pattern):
    """
    Searches for files containing a specific pattern within all subfolders.

    Args:
        root_dir (str): The starting directory for the search.
        pattern (str): The regular expression pattern to search for within file content.
    """
    regex = re.compile(pattern)  # Compile the regex for efficiency
    ret_list = []
    for root, _, files in os.walk(root_dir):
        for file_name in files:
            file_path = os.path.join(root, file_name)
            if regex.match(file_name):
                # print(f"Pattern found in: {file_path+file_name}")
                ret_list.append(file_path+file_name)
    return ret_list

#infile = r"D:\Documents and Settings\xxxx\Desktop\test_log.txt"
#inputFile = r"..\sim_results_100_600\ite9\2025-09-06_21-23-42\SIMRESULT_SINGLE_LAYER_VORONOI_NEXT_FIT_100DEVICES_FAIL.log"
#inputFile = r"..\sim_results_100_600\ite9\2025-09-06_22-43-21\SIMRESULT_SINGLE_LAYER_VORONOI_NEXT_FIT_100DEVICES_FAIL.log"
#inputFile = r"..\sim_results_100_600\ite9\2025-09-07_22-34-56\SIMRESULT_SINGLE_LAYER_VORONOI_NEXT_FIT_100DEVICES_FAIL.log"
#inputFile = r"..\sim_results_100_600\ite9\2025-09-07_22-56-36\SIMRESULT_SINGLE_LAYER_VORONOI_NEXT_FIT_100DEVICES_FAIL.log"
#inputFile = r"..\sim_results_100_600\ite9\2025-09-08_14-04-02\SIMRESULT_SINGLE_LAYER_VORONOI_NEXT_FIT_100DEVICES_FAIL.log"
#inputFile = r"..\sim_results_100_600\ite9\2025-09-08_14-25-29\SIMRESULT_SINGLE_LAYER_VORONOI_NEXT_FIT_100DEVICES_FAIL.log"
#inputFile = r"..\sim_results_100_600\ite9\2025-09-08_16-18-41\SIMRESULT_SINGLE_LAYER_VORONOI_NEXT_FIT_100DEVICES_FAIL.log"
#inputFile = r"..\sim_results_100_600\ite9\2025-09-08_16-18-41\SIMRESULT_SINGLE_LAYER_VORONOI_NEXT_FIT_100DEVICES_FAIL.log"
# inputFile = r"..\sim_results_100_600\ite9\2025-09-08_20-07-55\SIMRESULT_SINGLE_LAYER_VORONOI_NEXT_FIT_100DEVICES_FAIL.log"
inputFolder = r"..\sim_results_100_600_different_rand_seeds\\"
randomSeedSubfolder = 'rs123'
inputConsolerunFolder = inputFolder+randomSeedSubfolder+'\\'+'consoleruns'
# All files and directories ending with .txt and that don't begin with a dot:
# print(glob.glob(inputFileFolder+'/'+'*.txt'))

outputDir = 'taskExecutionAnalysisPlots'
os.makedirs(outputDir, exist_ok=True)

txtFilesWithFullPath = []
txtFiles = []
for file in glob.glob(inputConsolerunFolder+'/'+'*.txt'):
    txtFilesWithFullPath.append(file)
    splits = file.split('\\')
    #print(splits[len(splits)-1])
    txtFiles.append(splits[len(splits)-1])
consolerun_filename_first_part_ite_dict = {}
for f in txtFiles:
    # print(f)
    consolerun_filename_first_part = f.split('_')[0]
    # print(f'consolerun_filename_first_part: {consolerun_filename_first_part}')
    file_name_list = find_filenames_with_pattern(inputFolder+randomSeedSubfolder, r"^"+consolerun_filename_first_part)
    for file_name in file_name_list:
        file_name_splits = file_name.split('\\')
        # print(file_name+': '+file_name_splits[len(file_name_splits)-2])
        if(file_name_splits[len(file_name_splits)-2] != 'consoleruns'):
            consolerun_filename_first_part_ite_dict[consolerun_filename_first_part] = file_name_splits[len(file_name_splits)-2]
print(consolerun_filename_first_part_ite_dict)
# exit(0)
# inputConsoleRunFile = r"..\sim_results_100_600\consoleruns\1757380075499_console.txt"
# print(f"input fail log file = {inputFile} \ninput consolerun file = {inputConsoleRunFile}")
relevantLines = []
relevantConsoleRunLines = []
keep_phrases = ["Tasks executed per fog layer","#devices"]
df = pd.DataFrame(data=None, columns=['consolerunFile','iteNum','mobDevNum','fl1','fl2','fl3','fl4','fl5','fl6','fl7','totTask'])
# dfcr = pd.DataFrame(data=None, columns=['taskId','mdId','hostId','perceivedDelay'])
for inputFile in txtFiles:
    print(inputFile)
    capture = False
    capture_line_count = 0
    mob_dev_num = 0
    fl=[]
    total_tasks = 0
    with open(inputFolder+randomSeedSubfolder+'\\consoleruns\\'+inputFile) as f:
        all_lines = f.readlines()
        for line in all_lines:
            # print(keep_phrases[0])
            # print(line)
            if capture:
                # print(line)
                line_splits = line.split(':')
                # print(f'fl{capture_line_count+1} :{(line_splits[1]).strip()}')
                fl.append(int((line_splits[1]).strip()))
                total_tasks += int((line_splits[1]).strip())
                capture_line_count += 1
                if capture_line_count == 7:
                    capture = False
                    capture_line_count = 0
                    df.loc[len(df)] = [inputFile, consolerun_filename_first_part_ite_dict[inputFile.split('_')[0]],
                                       mob_dev_num, fl[0], fl[1], fl[2], fl[3], fl[4], fl[5], fl[6], total_tasks]
                    fl.clear()
                    total_tasks = 0
            if keep_phrases[0] in line:
                # print(line)
                capture = True
            if keep_phrases[1] in line:
                # print(line, line.split(':')[3].strip())
                mob_dev_num = int(line.split(':')[3].strip())

print(df)       # print(f)
print(df['mobDevNum'].unique())
mob_dev_num_list = df['mobDevNum'].unique()
# df_normalized = pd.DataFrame()
fig, axs = plt.subplots(3, 2, figsize=(12, 12))
subplot_cnt = 0
# Create some sample data for the plots
x = np.linspace(0, 10, 100)
y1 = np.sin(x)
y2 = np.cos(x)
y3 = x
y4 = x ** 2
y5 = np.exp(-x)
y6 = np.log(x + 1)
colors = ['red', 'blue','green', 'yellow', 'magenta', 'purple', 'orange']
fig_file = outputDir + '/' + randomSeedSubfolder + '.svg'


# data1 = pd.DataFrame({'Category': ['A', 'B', 'C'], 'Value': [10, 20, 15]})
orchestratorList = ['HAFA','CENTRALIZED']#,'LOCAL_ONLY','CLOUD_ONLY','EDGE_BY_LATENCY','EDGE_BY_DISTANCE',\
  # 'FIXED_NODE','SELECTED_LEVELS','SELECTED_NODES','VORONOI']
data1 = pd.DataFrame({'Orchestrator': orchestratorList, 'Tasks executed': [[10, 20, 15, 2, 3, 67, 23],
                                                                           [1, 2, 25, 20, 30, 6, 3]]})
data2 = pd.DataFrame({'Category': ['X', 'Y', 'Z'], 'Value': [5, 12, 8]})



for mob_dev_num in mob_dev_num_list:
    print(mob_dev_num)
    df_filtered = df[df['mobDevNum'] == mob_dev_num]
    # print(df_filtered)
    df_normalized = df_filtered[['consolerunFile','iteNum','mobDevNum']].copy()
    # print(f'df_normalized:\n{df_normalized}')
    # df_normalized['fl1'] = 0.0
    df_normalized['fl1'] = df_filtered['fl1']/df_filtered['totTask']
    df_normalized['fl2'] = df_filtered['fl2'] / df_filtered['totTask']
    df_normalized['fl3'] = df_filtered['fl3'] / df_filtered['totTask']
    df_normalized['fl4'] = df_filtered['fl4'] / df_filtered['totTask']
    df_normalized['fl5'] = df_filtered['fl5'] / df_filtered['totTask']
    df_normalized['fl6'] = df_filtered['fl6'] / df_filtered['totTask']
    df_normalized['fl7'] = df_filtered['fl7'] / df_filtered['totTask']
    # print(df_normalized)


    # Create a figure and a 2x3 grid of subplots
    # The 'figsize' argument controls the overall size of the figure


    # Plot data on each subplot
    match subplot_cnt:
        case 0:
            # axs[0, 0].plot(x, y1)
            # axs[0, 0].hist([df_normalized['fl1'], df_normalized['fl2'], df_normalized['fl3'], df_normalized['fl4'],
            #                 df_normalized['fl5'], df_normalized['fl6'], df_normalized['fl7']], color=colors)
            # bar_plot(axs[0,0], data, total_width=.8, single_width=.9)
            # Plot on the first subplot
            sns.barplot(x='Orchestrator', y='Tasks executed', data=data1, ax=axs[0, 0])
            # axes[0].set_title('Plot 1')
            axs[0, 0].set_title('Mobile Device Number: '+str(mob_dev_num))
        case 1:
            axs[0, 1].hist([df_normalized['fl1'], df_normalized['fl2'], df_normalized['fl3'], df_normalized['fl4'],
                            df_normalized['fl5'], df_normalized['fl6'], df_normalized['fl7']], color=colors)
            axs[0, 1].set_title('Mobile Device Number: '+str(mob_dev_num))
        case 2:
            axs[1, 0].hist([df_normalized['fl1'], df_normalized['fl2'], df_normalized['fl3'], df_normalized['fl4'],
                            df_normalized['fl5'], df_normalized['fl6'], df_normalized['fl7']], color=colors)
            axs[1, 0].set_title('Mobile Device Number: '+str(mob_dev_num))
        case 3:
            axs[1, 1].hist([df_normalized['fl1'], df_normalized['fl2'], df_normalized['fl3'], df_normalized['fl4'],
                            df_normalized['fl5'], df_normalized['fl6'], df_normalized['fl7']], color=colors)
            axs[1, 1].set_title('Mobile Device Number: '+str(mob_dev_num))
        case 4:
            axs[2, 0].hist([df_normalized['fl1'], df_normalized['fl2'], df_normalized['fl3'], df_normalized['fl4'],
                            df_normalized['fl5'], df_normalized['fl6'], df_normalized['fl7']], color=colors)
            axs[2, 0].set_title('Mobile Device Number: '+str(mob_dev_num))
        case 5:
            axs[2, 1].hist([df_normalized['fl1'], df_normalized['fl2'], df_normalized['fl3'], df_normalized['fl4'],
                            df_normalized['fl5'], df_normalized['fl6'], df_normalized['fl7']], color=colors)
            axs[2, 1].set_title('Mobile Device Number: '+str(mob_dev_num))

    # Add a super title for the entire figure
    fig.suptitle('Subplots reg. tasks assigned to 7 fog layers', fontsize=16)

    # Adjust layout to prevent titles and labels from overlapping
    plt.tight_layout(rect=[0, 0.03, 1, 0.95])  # Adjust rect to make space for suptitle

    subplot_cnt += 1

# Display the plot
#plt.show()
plt.savefig(fig_file, format="svg")
