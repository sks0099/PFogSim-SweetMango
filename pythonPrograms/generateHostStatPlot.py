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

def get_df_dict(plot_type_list):
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
    df_dict = {}
    outputDir = 'hostStatPlots'
    os.makedirs(outputDir, exist_ok=True)
    for plot_type in plot_type_list:
        if plot_type == 'HAFA':
            inputFolder = r"..\sim_results_1000_6000\HAFA_Voronoi\ite0rss\\"
            randomSeedSubfolderList = ['ite0rs123','ite0rs234','ite0rs345','ite0rs456','ite0rs567']
            fig_file = outputDir + '/hostStatPlot_HAFA' + '.svg'
            fig_title = 'Relative tasks assigned to different MIPS group by HAFA'
        elif plot_type == 'Voronoi':
            inputFolder = r"..\sim_results_1000_6000\HAFA_Voronoi\ite9rss\\"
            randomSeedSubfolderList = ['ite9rs123', 'ite9rs234', 'ite9rs345', 'ite9rs456', 'ite9rs567']
            fig_file = outputDir + '/hostStatPlot_Voronoi' + '.svg'
            fig_title = 'Relative tasks assigned to different MIPS group by Voronoi'


        # All files and directories ending with .txt and that don't begin with a dot:
        # print(glob.glob(inputFileFolder+'/'+'*.txt'))



        txtFilesWithFullPath = []

        consolerun_filename_first_part_ite_dict = {}
        for randomSeedSubfolder in randomSeedSubfolderList:
            inputConsolerunFolder = inputFolder + randomSeedSubfolder + '\\' + 'consoleruns'
            #txtFiles = []
            for file in glob.glob(inputConsolerunFolder+'/'+'*.txt'):
                txtFilesWithFullPath.append(file)
                # print(f'file:{txtFilesWithFullPath}')
                #splits = file.split('\\')
                #print(splits[len(splits)-1])
                #txtFiles.append(splits[len(splits)-1])
            # exit(0)

        # for f in txtFiles:
            # print(f)
        #     print(f'txtFiles[0]={txtFiles[0]}')
        #     consolerun_filename_first_part = txtFiles[0].split('_')[0]
        #     # print(f'consolerun_filename_first_part: {consolerun_filename_first_part}')
        #     file_name_list = find_filenames_with_pattern(inputFolder+randomSeedSubfolder+ '\\' + 'consoleruns', r"^"+consolerun_filename_first_part)
        #     for file_name in file_name_list:
        #         file_name_splits = file_name.split('\\')
        #         print(file_name+': '+file_name_splits[len(file_name_splits)-2])
        #         # if(file_name_splits[len(file_name_splits)-2] != 'consoleruns'):
        #         consolerun_filename_first_part_ite_dict[consolerun_filename_first_part] = #file_name_splits[len(file_name_splits)-2]
        # print(f'consolerun_filename_first_part_ite_dict:{consolerun_filename_first_part_ite_dict}')
        # exit(0)
        # inputConsoleRunFile = r"..\sim_results_100_600\consoleruns\1757380075499_console.txt"
        # print(f"input fail log file = {inputFile} \ninput consolerun file = {inputConsoleRunFile}")
        relevantLines = []
        relevantConsoleRunLines = []
        keep_phrases = ["Tasks executed per fog layer","#devices"]
        # df = pd.DataFrame(data=None, columns=['consolerunFile','iteNum','mobDevNum','fl1','fl2','fl3','fl4','fl5','fl6','fl7','totTask'])
        df = pd.DataFrame(data=None, columns=['consolerunFile','iteNum','mobDevNum','mips1','mips2','mips3','mips4','mips5','mips6','mips7','totTask'])
        # dfcr = pd.DataFrame(data=None, columns=['taskId','mdId','hostId','perceivedDelay'])
        for inputFile in txtFilesWithFullPath: #txtFiles:
            # print(inputFile)
            # print(inputFile.split('\\')[3].split('ite')[1].split('rss')[0])
            iteNum = int(inputFile.split('\\')[3].split('ite')[1].split('rss')[0])
            capture = False
            capture_line_count = 0
            mob_dev_num = 0
            fl=[]
            total_tasks = 0
            # with open(inputFolder+randomSeedSubfolder+'\\consoleruns\\'+inputFile) as f:
            with open(inputFile) as f:
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
                            # df.loc[len(df)] = [inputFile, consolerun_filename_first_part_ite_dict[inputFile.split('_')[0]],
                            #                    mob_dev_num, fl[0], fl[1], fl[2], fl[3], fl[4], fl[5], fl[6], total_tasks]
                            df.loc[len(df)] = [inputFile, iteNum,
                                               mob_dev_num, fl[0], fl[1], fl[2], fl[3], fl[4], fl[5], fl[6], total_tasks]
                            fl.clear()
                            total_tasks = 0
                    if keep_phrases[0] in line:
                        # print(line)
                        capture = True
                    if keep_phrases[1] in line:
                        # print(line, line.split(':')[3].strip())
                        mob_dev_num = int(line.split(':')[3].strip())

        # print(df)       # print(f)
        # print(df['mobDevNum'].unique())
        df.drop(columns=['consolerunFile'], inplace=True)
        df_sum = df.groupby(['mobDevNum'], as_index=False).sum()
        # print(df_sum)
        df_sum['mips1_p']=df_sum['mips1']/df_sum['totTask']
        df_sum['mips2_p']=df_sum['mips2']/df_sum['totTask']
        df_sum['mips3_p']=df_sum['mips3']/df_sum['totTask']
        df_sum['mips4_p']=df_sum['mips4']/df_sum['totTask']
        df_sum['mips5_p']=df_sum['mips5']/df_sum['totTask']
        df_sum['mips6_p']=df_sum['mips6']/df_sum['totTask']
        df_sum['mips7_p']=df_sum['mips7']/df_sum['totTask']
        # print(df_sum)
        df_only_percent = df_sum.drop(columns=['iteNum','mips1','mips2','mips3','mips4','mips5','mips6','mips7','totTask'])
        if plot_type == 'HAFA':
            df_only_percent.rename(columns={'mips1_p': 'MIPS1 HAFA', 'mips2_p': 'MIPS2 HAFA', 'mips3_p': 'MIPS3 HAFA', 'mips4_p': 'MIPS4 HAFA'
                                            , 'mips5_p': 'MIPS5 HAFA', 'mips6_p': 'MIPS6 HAFA', 'mips7_p': 'MIPS7 HAFA'}, inplace=True)
        elif plot_type == 'Voronoi':
            df_only_percent.rename(columns={'mips1_p': 'MIPS1 Voronoi', 'mips2_p': 'MIPS2 Voronoi', 'mips3_p': 'MIPS3 Voronoi',
                                            'mips4_p': 'MIPS4 Voronoi'
                , 'mips5_p': 'MIPS5 Voronoi', 'mips6_p': 'MIPS6 Voronoi', 'mips7_p': 'MIPS7 Voronoi'}, inplace=True)
        df['mobDevNum'] = df['mobDevNum'].astype(int)
        df_dict[plot_type] = df_only_percent
    return df_dict
    # print(df_only_percent)
    # for mobileDevNum in df['mobDevNum'].unique():
    #     filtered_df = df[df['mobDevNum'] == mobileDevNum]
    #     print(f'filtered_df:\{filtered_df}')

def plot_stackbars(plot_type_list, df_dict):
    outputDir = 'hostStatPlots'
    os.makedirs(outputDir, exist_ok=True)
    # fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(12, 5), sharex=True, sharey=True)
    fig, ax = plt.subplots(figsize=(8, 6))
    # fig, ax = plt.subplots()
    # bar_width = 0.35
    fig_file = outputDir + '/hostStatPlot_HAFA_Voronoi_separated' + '.svg'
    fig_title = 'Relative tasks assigned to different MIPS group'
    mobDevNum = df_dict['HAFA']['mobDevNum']
    bar_width = 1.0 #0.35  # Width of the grouped bars
    x_positions = np.arange(len(mobDevNum))  # The x-locations for the groups
    group_no = 1
    for key in df_dict:
        plot_type = key
        # for plot_type in plot_type_list:
        # if plot_type == 'HAFA':
            # inputFolder = r"..\sim_results_1000_6000\HAFA_Voronoi\ite0rss\\"
            # randomSeedSubfolderList = ['ite0rs123', 'ite0rs234', 'ite0rs345', 'ite0rs456', 'ite0rs567']
            # fig_file = outputDir + '/hostStatPlot_HAFA' + '.svg'
            # fig_title = 'Relative tasks assigned to different MIPS group by HAFA'
            # plot_ax = ax1
        # elif plot_type == 'Voronoi':
            # inputFolder = r"..\sim_results_1000_6000\HAFA_Voronoi\ite9rss\\"
            # randomSeedSubfolderList = ['ite9rs123', 'ite9rs234', 'ite9rs345', 'ite9rs456', 'ite9rs567']
            # fig_file = outputDir + '/hostStatPlot_Voronoi' + '.svg'
            # fig_title = 'Relative tasks assigned to different MIPS group by Voronoi'
            # plot_ax = ax2

        # df1.plot(x='Category', kind='bar', stacked=True, ax=ax1, title='DataFrame 1 Stacked Bar Chart')
        # ax = df_dict[key].plot(x='mobDevNum', kind='bar', stacked=True, title=fig_title, figsize=(10, 6))

        if group_no == 1:
            df_dict[key].plot(x='mobDevNum', kind="bar", stacked=True, width=0.25,
                              ax=ax, position=1)
        elif group_no == 2:
            df_dict[key].plot(x='mobDevNum', kind="bar", stacked=True, width=0.25,
                           ax=ax, position=-0.25, hatch='//')
        # First set (DF1)
        # if group_no == 1:
        #     ax.bar(x_positions - bar_width / 2, df_dict[key], bar_width, label='DF1 Stack 1', color='skyblue')
        #     ax.bar(x_positions - bar_width / 2, df_dict[key], bar_width, label='DF1 Stack 2', color='blue')
        #
        # # Second set (DF2)
        # elif group_no == 2:
        #     ax.bar(x_positions + bar_width / 2, df_dict[key], bar_width, label='DF2 Stack 1', color='salmon')
        #     ax.bar(x_positions + bar_width / 2, df_dict[key], bar_width, label='DF2 Stack 2',
        #            color='red')
        group_no += 1
    ax.set_ylabel('Percentage of tasks sent to MIPS groups')
    ax.set_xlabel('Number of mobile devices')

    ax.set_xlim(right=len(df_dict['HAFA']) - 0.3) #-0.5)
    # ax.legend(labelspacing=0.2, handlelength=1.0, handleheight=0.5, handletextpad=0.3, borderpad=0.5, loc='best')#,
             # bbox_transform=fig.transFigure)
    box = ax.get_position()
    width_factor = 0.8 #0.8
    ax.set_position([box.x0, box.y0, box.width * width_factor, box.height])
    bars, bar_labels = ax.get_legend_handles_labels()
    # ax2.legend(lines + bars, labels + bar_labels, loc='upper left')
    ax.legend(bars, bar_labels, labelspacing=0.3, handlelength=0.8, handleheight=0.6, handletextpad=0.3, borderpad=0.6,
               prop={'size': 9}, bbox_to_anchor=(1.3, 1))
    plt.title('Relative percentage of tasks sent to different\nMIPS capacity groups vs No. of Mobile devices')
    plt.savefig(fig_file, format="svg")
    # plt.show()

if __name__ == "__main__":
    plot_type_list = ['HAFA','Voronoi']
    df_dict = get_df_dict(plot_type_list)
    plot_stackbars(plot_type_list, df_dict)
    # plot_type = 'Voronoi'
    # main(plot_type)
