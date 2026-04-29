import configuration
import matplotlib.pyplot as plt
import numpy as np
import os
import re
import scipy.stats as stats
import generateHostStatPlot as ghsp

class plotGenericResult:

    def __init__(self, rowOfset, columnOfset, yLabel, appType, calculatePercentage, config, graphTitle='', yScale='', randomSeed=''):
        # Constructor method (initializer)
        # Initializes instance attributes unique to each object
        self.rowOfset = rowOfset
        self.columnOfset = columnOfset
        self.yLabel = yLabel
        self.appType = appType
        self.calculatePercentage = calculatePercentage
        self.config = config
        self.graphTitle = graphTitle
        self.yScale = yScale
        self.df_dict = ghsp.get_df_dict(['HAFA','Voronoi'])
        self.plotStackedBar = False
        self.randomSeed = randomSeed


    def get_filenames_with_pattern(self, root_dir, pattern):
        """
        Searches for files containing a specific pattern within all subfolders.

        Args:
            root_dir (str): The starting directory for the search.
        """
        regex = re.compile(pattern)  # Compile the regex for efficiency
        # print(pattern)
        ret_list = []
        for root, _, files in os.walk(root_dir):
            for file_name in files:
                # print(pattern)
                # print(f'file_name:{file_name}')
                file_path = os.path.join(root, file_name)
                if regex.match(file_name):
                    # print(f"Pattern found in: {file_path+file_name}")
                    # ret_list.append(file_path + file_name)
                    # print(f"Pattern found in: {file_path}")
                    ret_list.append(file_path)
        return ret_list

    def getPlot(self):
        #if nargin < 6
        #    config = configuration.autoConfig();
        #end
        if self.config == None:
            self.config = configuration.autoConfig()
        #if nargin < 7
        #    graphTitle = '';
        #end
        graphTitle = self.graphTitle
        #if nargin < 8
        #    yScale = 'linear';
        #end
        if(self.yScale == ''):
            self.yScale = 'linear'
        folderPath = self.config.FolderPath
        numOfSimulations = self.config.IterationCount
        stepOfxAxis = self.config.XAxisStep
        scenarioType = self.config.SimulationScenarioList
        startOfMobileDeviceLoop = self.config.MinimumMobileDevices
        stepOfMobileDeviceLoop = self.config.MobileDeviceStep
        # endOfMobileDeviceLoop = 500; % config.MaximumMobileDevices;
        endOfMobileDeviceLoop = self.config.MaximumMobileDevices
        numOfMobileDevices = (endOfMobileDeviceLoop - startOfMobileDeviceLoop) / stepOfMobileDeviceLoop + 1

        # all_results = zeros(size(scenarioType, 1), numOfMobileDevices, numOfSimulations)
        all_results = np.zeros((len(scenarioType), int(numOfMobileDevices), int(numOfSimulations)), dtype=float)
        # min_results = zeros(size(scenarioType, 1), numOfMobileDevices);
        min_results = np.zeros((len(scenarioType), int(numOfMobileDevices)), dtype=float)
        # max_results = zeros(size(scenarioType, 1), numOfMobileDevices);
        max_results = np.zeros((len(scenarioType), int(numOfMobileDevices)), dtype=float)

        # if ~exist('appType', 'var'):
        #     appType = 'ALL_APPS'
        #end

        # if 'appType' not in locals() and 'appType' not in globals():
            # Code to run if 'appType' does not exist
            # appType = 'ALL_APPS'
        if len(self.appType) == 0:
            self.appType = 'ALL_APPS'

        # Get contents of the target directory
        # dir_contents = dir(folderPath)
        # dir_contents = os.walk(folderPath)
        dir_contents = []
        for root, dirs, files in os.walk(folderPath):
            for d in dirs:
                full_dir_path = os.path.join(root, d)
                dir_contents.append(full_dir_path)

        # Initialize counter for matching folders
        count_matching_folders = 0

        search_string = 'ite'
        # Loop through each item in the directory contents
        for i in range(1, len(dir_contents)):
            item = dir_contents[i]

            # Check if the item is a directory and not '.' or '..'
            # if item.isdir and (item.name != '.') and (item.name != '..'):
                # Check if the folder name contains the search string
            if search_string in item:
                count_matching_folders = count_matching_folders + 1
                #end
            #end

        #end

        for s in range (1, int(numOfSimulations) + 1):
            # for i in range (1, size(scenarioType,1)):
            for i in range(1, len(scenarioType)+1):
                #for i=1:count_matching_folders
                for j in range (1, int(numOfMobileDevices)+1):
                    value = 0.0
                    mobileDeviceNumber = (int)(startOfMobileDeviceLoop + stepOfMobileDeviceLoop * (j-1))
                    #disp(scenarioType)
                    #disp(scenarioType(2))
                    #disp(strcat('**/*SIMRESULT_*',char(scenarioType(i)),'*_NEXT_FIT_*',int2str(mobileDeviceNumber),'*DEVICES_*',appType,'*_GENERIC.log'))
                    #fileName = strcat('**/*SIMRESULT_*',char(scenarioType(i)),'*_NEXT_FIT_*',int2str(mobileDeviceNumber),'*DEVICES_*',appType,'*_GENERIC.log');
                    # fileName_pattern = '**/*SIMRESULT_*' + scenarioType[i] + '*_NEXT_FIT_*' + str(mobileDeviceNumber) + '*DEVICES_*' + appType + '*_GENERIC.log'
                    # pattern_info = r"SIMRESULT_(?P<scenario>[\w_\s]+)_NEXT_FIT_(?P<devices>\d+)DEVICES_(?P<appType>[\w \s]+)_GENERIC"
                    fileName_pattern = r"SIMRESULT_*" + scenarioType[i-1] + "*_NEXT_FIT_*" + str(
                        mobileDeviceNumber) + "*DEVICES_*" + self.appType + "*_GENERIC.log"

                    # if exist(fileName, 'file')
                    #oldFolder = cd(folderPath)
                    # print(self.config.FolderPath)
                    allFiles = self.get_filenames_with_pattern(self.config.FolderPath, fileName_pattern)#dir(fileName)
                    # for f in allFiles:
                    #     print(f'{f}')
                    # Print the names of the files
                        #for k = 1:length(allFiles)
                            #disp(fullfile(allFiles(k).folder, allFiles(k).name)); # Prints full path
                            # disp(fileList(i).name); # For just the file name
                        #end
                    # cd(oldFolder)
                        #disp(['s: ',s])
                        #fprintf('s: %d, length(allFiles): %d\n', s, length(allFiles));
                    if s>len(allFiles):
                        #disp(['no. of iterations expected: ',numOfSimulations])
                        #disp(['Iterations found = ', length(allFiles)])
                        # error(strcat('Error: SIMRESULT files missing. Iterations expected: ', int2str(numOfSimulations), '. Iterations found: ', int2str(length(allFiles)), '.'))
                        print(f'Error: SIMRESULT files missing. Iterations expected: {str(numOfSimulations)}, Iterations found: {str(len(allFiles))}.')
                    #end
                    # filePath = strcat(allFiles(s).folder, '/', allFiles(s).name)
                    if len(allFiles) == 1:
                        # print(allFiles[0])
                        # file_path = 'my_text_file.txt'  # Replace with your file's path
                        file_to_process = allFiles[0]
                        try:
                            with open(file_to_process, 'r') as file:
                                line_count = 0
                                for line in file:
                                    # Each 'line' variable will contain one line from the file,
                                    # including the newline character '\n' at the end.
                                    # You can use .strip() to remove leading/trailing whitespace,
                                    # including the newline character.
                                    if line_count == self.rowOfset:
                                        extracted_line = line.strip()
                                        extracted_line_splits = extracted_line.split(';')
                                        # print(extracted_line)
                                        split_count = 0
                                        for split_index in range (0, len(extracted_line_splits)):
                                            # print(f'split_index={str(split_index)}: {extracted_line_splits[split_index]}')
                                            split_count += 1
                                        value = (float) (extracted_line_splits[self.columnOfset-1])
                                        # print(f'value={value}')
                                        if (self.calculatePercentage == 1):
                                            totalTask = (int)(extracted_line_splits[0]) + (int)(extracted_line_splits[self.columnOfset-1])
                                            value = (100 * value) / totalTask
                                        # print(f'value (%) ={value}')
                                        break
                                    line_count += 1

                        except FileNotFoundError:
                            print(f"Error: The file '{file_to_process}' was not found.")
                        except Exception as e:
                            print(f"An error occurred: {e}")
                        all_results[i - 1, j - 1, s - 1] = value
                        # print(f'all_results[{i - 1},{str(j - 1)},{str(s - 1)}]={all_results[i - 1, j - 1, s - 1]}')
                    else:
                        # print(len(allFiles))
                        # for logFile in allFiles:
                        #     print(f'logFile:{logFile}')
                        temp_all_results = np.zeros((len(allFiles)),
                                               dtype=float)
                        # min_results = zeros(size(scenarioType, 1), numOfMobileDevices);
                        temp_min_results = np.zeros((1, len(allFiles)), dtype=float)
                        # max_results = zeros(size(scenarioType, 1), numOfMobileDevices);
                        temp_max_results = np.zeros((1, int(numOfMobileDevices)), dtype=float)
                        for s_temp in range(1, len(allFiles) + 1):
                            # for i in range (1, size(scenarioType,1)):
                            i_temp = i
                            # for i_temp in range(1, len(scenarioType) + 1):
                                # for i=1:count_matching_folders
                            j_temp = j
                            # for j_temp in range(1, int(numOfMobileDevices) + 1):
                            value = 0.0
                            mobileDeviceNumber = (int)(
                                startOfMobileDeviceLoop + stepOfMobileDeviceLoop * (j_temp - 1))
                            # file_count = 0
                            # for f in allFiles:
                            f = allFiles[s_temp - 1]
                            # print(f)
                            file_to_process = f
                            try:
                                with open(file_to_process, 'r') as file:
                                    line_count = 0
                                    for line in file:
                                        # Each 'line' variable will contain one line from the file,
                                        # including the newline character '\n' at the end.
                                        # You can use .strip() to remove leading/trailing whitespace,
                                        # including the newline character.
                                        if line_count == self.rowOfset:
                                            extracted_line = line.strip()
                                            extracted_line_splits = extracted_line.split(';')
                                            # print(extracted_line)
                                            split_count = 0
                                            for split_index in range(0, len(extracted_line_splits)):
                                                # print(f'split_index={str(split_index)}: {extracted_line_splits[split_index]}')
                                                split_count += 1
                                            value = (float)(extracted_line_splits[self.columnOfset - 1])
                                            # print(f'value={value}')
                                            if (self.calculatePercentage == 1):
                                                totalTask = (int)(extracted_line_splits[0]) + (int)(
                                                    extracted_line_splits[self.columnOfset - 1])
                                                value = (100 * value) / totalTask
                                            # print(f'value (%) ={value}')
                                            break
                                        line_count += 1

                            except FileNotFoundError:
                                print(f"Error: The file '{file_to_process}' was not found.")
                            except Exception as e:
                                print(f"An error occurred: {e}")
                            temp_all_results[s_temp - 1] = value
                            # print(f'temp_all_results[{i_temp - 1},{j_temp - 1},{s_temp - 1}]'
                            #       f'={temp_all_results[i_temp - 1, j_temp - 1, s_temp - 1]}')
                        # all_results[i - 1, j - 1, s - 1] = value
                        all_results[(i-1),(j-1),(s-1)] = np.mean(temp_all_results)
                        # print(f'all_results[{i - 1},{j - 1},{s - 1}]={all_results[(i-1),(j-1),(s-1)]}')
                        # exit(0)
                        # fileData = readmatrix(filePath, 'Delimiter', ';','Range', self.rowOfset+1)
                        # value = fileData(1,columnOfset)
                        # if(self.calculatePercentage==1):
                        #     totalTask = fileData(1,1)+fileData(1,2)
                        #     value = (100 * value) / totalTask
                        #     #end
                        # all_results[i,j,s] = value
                #end
            #end
        #end
    #end
    
        if(numOfSimulations == 1):
            results = all_results
        else:
            # results = mean(all_results, 3); #still 3d matrix but 1xMxN format
            results = np.mean(all_results, axis=2);  # still 3d matrix but 1xMxN format
        #end

        # results = squeeze(results) #remove singleton dimensions
        results = np.squeeze(results)

        # for i in range (1, size(scenarioType,1)):
        if numOfSimulations > 1:
            for i in range(1, len(scenarioType)):
                for j in range (1, int(numOfMobileDevices) + 1):
                    # x=results[i,j,:]                    # Create Data
                    x = results[i, j]
                    # SEM = math.std(x)/math.sqrt(len(x))            # Standard Error
                    SEM = np.std(x) / np.sqrt(len(x))  # Standard Error
                    # ts = tinv([0.05, 0.95],len(x))   # T-Score
                    ts = stats.t.ppf([0.05, 0.95], len(x))  # T-Score
                    # CI = mean(x) + ts*SEM                   # Confidence Intervals
                    CI = np.mean(x) + ts * SEM  # Confidence Intervals

                    if(CI[1] < 0):
                        CI[1] = 0
                    #end

                    if(CI[2] < 0):
                        CI[2] = 0
                    #end

                    min_results[i,j] = results[i,j] - CI[1]
                    max_results[i,j] = CI[2] - results[i,j]
                #end
            #end


        # types = np.zeros((1,(int)(numOfMobileDevices)+1), dtype=int)
        # for i in range(0, (int)(numOfMobileDevices)):
        #     types[i]=(int)(startOfMobileDeviceLoop)+((i-1)*(int)(stepOfMobileDeviceLoop))

        types = np.zeros((int)(numOfMobileDevices), dtype=int)
        for i in range(0, (int)(numOfMobileDevices)):
            types[i] = (int)(startOfMobileDeviceLoop) + (i * (int)(stepOfMobileDeviceLoop))

        #end


        #hFig = figure;
        # hFig = self.figure('Visible','off')
        plt.ioff()
        # set(hFig, 'Position', self.config.PlotWindowCoordinates)
        plt.rcParams['font.family'] = 'Times New Roman'
        # set(0,'DefaultAxesFontName','Times New Roman')
        # set(0,'DefaultTextFontName','Times New Roman')
        # set(0,'DefaultAxesFontSize',12)
        # set(0,'DefaultTextFontSize',12)
        fig = plt.gcf()
        ax = plt.gca()
        if self.plotStackedBar:
            box = ax.get_position()
            width_factor = 0.8
            ax.set_position([box.x0, box.y0, box.width * width_factor, box.height])
            # plt.subplots_adjust(right=1.0)
            ax2 = ax.twinx()
            box2 = ax2.get_position()
            ax2.set_position([box2.x0, box2.y0, box2.width * width_factor, box2.height])
            bar_width = 100
        plt.rcParams['font.size'] = 9
        plt.rcParams['axes.labelsize'] = 12
        plt.rcParams['xtick.labelsize'] = 12
        plt.rcParams['ytick.labelsize'] = 12
        # plt.rcParams['figure.figuresize']=(7.5, 10)
        # plt.rc('figure', figsize=(10, 12))
        # plt.rcParams['figure.dpi'] = 600
        plot_list = []


        #set(0,'DefaultFigureVisible','off')
        #LineStyleOrder = {'-.','--','--','--','--','--','--','--','--','-'};
        if(self.config.ColorPlot == 1):
            # for i in range (stepOfxAxis, (int)(numOfMobileDevices), stepOfxAxis):
            #     xIndex=startOfMobileDeviceLoop+((i-1)*stepOfMobileDeviceLoop)
            #
            #     markers = self.config.LineStyleColor
                # for j in range (1, size(scenarioType,1)):
                # for j in range(1, len(scenarioType)):
                #     # if isempty(yScale):
                #     if len(self.yScale) == 0:
                #         self.yScale = 'linear'
                #     #end
                #     plt.grid()
                #
                #     y = max(1, results[j, i])
                #
                #
                #     #plt.title("Linear plot of y")
                #
                #     # plt.figure()
                #     #plt.grid()
                #
                #
                #     if self.yScale == 'log':
                #         #semilogy(xIndex, max(1, results[j,i]),char(markers[j]),'MarkerEdgeColor',config.LineColors(j,:),'color',config.LineColors(j,:))
                #         plt.title("Semilog plot of y")
                #         plt.semilogy(y)
                #
                #     elif self.yScale == 'linear':
                        #plot(xIndex, results(j,i),char(markers(j)),'MarkerFaceColor',config.LineColors(j,:),'color',config.LineColors(j,:));
                        #plot(xIndex, results(j,i),char(markers(j)),'MarkerFaceColor',config.LineColors(j,:),'color',config.LineColors(j,:))
                        #'LineStyleOrder',
                        #linestyles = ["-","-o","--d"];
                        #linestyleorder(LineStyleOrder);
                        # plt.title("Linear plot of y")
                        # plt.plot(y)
                    #end
                    #hold on
                #end
            #end
            yerr = np.linspace(0.05, 0.2, 10)
            # for j in range (len(scenarioType,1)+1):
            for j in range(len(scenarioType)):
                # plt.grid()
                if results.ndim == 1:
                    y = results
                else:
                    y = results[j] #results[j, :]

                # plt.title("Linear plot of y")

                # plt.figure()
                # if(self.config.IncludeErrorBars == 1):
                    #errorbar(types, results(j,:), min_results(j,:),max_results(j,:),':k','color',config.LineColors(j,:),'LineWidth',1.5)
                    # plt.errorbar(x, y, yerr=yerr, fmt='o', capsize=5, ecolor='red', elinewidth=2)
                # else:
                    #plot(types, results(j,:),':k','color',config.LineColors(j,:),'LineWidth',1.5)
                    # plt.grid()

                    # y = results[j, :]

                    # plt.title("Linear plot of y")

                    # plt.figure()
                    # if(j==6 or j==10):
                        #plot(types, results(j,:),'-k','color',config.LineColors(j,:),'LineWidth',1.5)
                        # plt.plot(y)
                    # else:
                        #plot(types, results(j,:),':k','color',config.LineColors(j,:),'LineWidth',1.5)
                        # plt.plot(y)
                    #end
                    #linestyleorder(LineStyleOrder);
                #end
                #hold on;
                x = types
                if self.plotStackedBar:
                    x_left = x - 25 - bar_width / 2
                    x_right = x + 25 + bar_width / 2
                # print(list(self.config.LineStyleMono)[j])
                ax.scatter(x, y, s=6, color=tuple(self.config.LineColors[j]), marker=self.config.markerStyle[j])#.LineStyleMono[j])#, label=self.config.ScenarioLabelsList[j])
                # print(appType, str(j), self.config.ScenarioLabelsList[j], self.config.lineStyle[j], self.config.lineWidth[j])
                # ls = self.config.lineStyle[j]
                # lw = float(self.config.lineWidth[j])
                # ax.plot(x, y, color=tuple(self.config.LineColors[j]), linestyle=self.config.lineStyle[j],
                #                          label=self.config.ScenarioLabelsList[j], linewidth=self.config.lineWidth[j])
                # plot_list.append(ax.plot(x,y, color=tuple(self.config.LineColors[j]), linestyle=self.config.lineStyle[j],
                #                          label=self.config.ScenarioLabelsList[j], linewidth=self.config.lineWidth[j]))
                # plt.scatter(x, y)
                # ax2 = ax.twinx()
                # group_no = 1
                # for key in self.df_dict:
                # if scenarioType[j] == key:
                #     plot_type = key
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
                if self.plotStackedBar:
                    bottom_3 = 0.0
                    bottom_4 = 0.0
                    bottom_5 = 0.0
                    bottom_6 = 0.0
                    bottom_7 = 0.0
                    key = scenarioType[j]
                    # if group_no == 1:
                    if j == 0:
                        key = list(self.df_dict.keys())[j]
                        col_index = 1
                        segment_1 = self.df_dict[key].iloc[:, col_index].values.flatten().tolist()#self.df_dict[key][1]#[10, 15, 7, 12, 9]
                        col_index += 1
                        segment_2 = self.df_dict[key].iloc[:, col_index].values.flatten().tolist()#[5, 8, 10, 6, 11]
                        col_index += 1
                        segment_3 = self.df_dict[key].iloc[:, col_index].values.flatten().tolist()#[3, 4, 6, 8, 5]
                        col_index += 1
                        segment_4 = self.df_dict[key].iloc[:, col_index].values.flatten().tolist()
                        col_index += 1
                        segment_5 = self.df_dict[key].iloc[:, col_index].values.flatten().tolist()
                        col_index += 1
                        segment_6 = self.df_dict[key].iloc[:, col_index].values.flatten().tolist()
                        col_index += 1
                        segment_7 = self.df_dict[key].iloc[:, col_index].values.flatten().tolist()
                        ax2.bar(x_left, segment_1, width=bar_width, label='MIPS1 HAFA', color=self.config.StackedBoxColors[0])

                        ax2.bar(x_left, segment_2, bottom=segment_1, width=bar_width, label='MIPS2 HAFA', color=self.config.StackedBoxColors[1])

                        bottom_3 = np.array(segment_1) + np.array(segment_2)
                        ax2.bar(x_left, segment_3, bottom=bottom_3, width=bar_width, label='MIPS3 HAFA', color=self.config.StackedBoxColors[2])

                        bottom_4 = bottom_3 + np.array(segment_3)
                        ax2.bar(x_left, segment_4, bottom=bottom_4, width=bar_width, label='MIPS4 HAFA', color=self.config.StackedBoxColors[3])

                        bottom_5 = bottom_4 + np.array(segment_4)
                        ax2.bar(x_left, segment_5, bottom=bottom_5, width=bar_width, label='MIPS5 HAFA', color=self.config.StackedBoxColors[4])

                        bottom_6 = bottom_5 + np.array(segment_5)
                        ax2.bar(x_left, segment_6, bottom=bottom_6, width=bar_width, label='MIPS6 HAFA', color=self.config.StackedBoxColors[5])

                        bottom_7 = bottom_6 + np.array(segment_6)
                        ax2.bar(x_left, segment_7, bottom=bottom_7, width=bar_width, label='MIPS7 HAFA', color=self.config.StackedBoxColors[6])

                        # self.df_dict[key].plot(x='mobDevNum', kind="bar", stacked=True, width=0.15,
                        #                   ax=ax2, position=1)
                    # elif group_no == 2:
                    elif j == 1:
                        key = list(self.df_dict.keys())[j]
                        col_index = 1
                        segment_1 = self.df_dict[key].iloc[:,
                                    col_index].values.flatten().tolist()  # self.df_dict[key][1]#[10, 15, 7, 12, 9]
                        col_index += 1
                        segment_2 = self.df_dict[key].iloc[:, col_index].values.flatten().tolist()  # [5, 8, 10, 6, 11]
                        col_index += 1
                        segment_3 = self.df_dict[key].iloc[:, col_index].values.flatten().tolist()  # [3, 4, 6, 8, 5]
                        col_index += 1
                        segment_4 = self.df_dict[key].iloc[:, col_index].values.flatten().tolist()
                        col_index += 1
                        segment_5 = self.df_dict[key].iloc[:, col_index].values.flatten().tolist()
                        col_index += 1
                        segment_6 = self.df_dict[key].iloc[:, col_index].values.flatten().tolist()
                        col_index += 1
                        segment_7 = self.df_dict[key].iloc[:, col_index].values.flatten().tolist()
                        ax2.bar(x_right, segment_1, width=bar_width, label='MIPS1 Voronoi', color=self.config.StackedBoxColors[0], hatch='//')

                        ax2.bar(x_right, segment_2, bottom=segment_1, width=bar_width, label='MIPS2 Voronoi', color=self.config.StackedBoxColors[1], hatch='\\')

                        bottom_3 = np.array(segment_1) + np.array(segment_2)
                        ax2.bar(x_right, segment_3, bottom=bottom_3, width=bar_width, label='MIPS3 Voronoi', color=self.config.StackedBoxColors[2], hatch='//')

                        bottom_4 = bottom_3 + np.array(segment_3)
                        ax2.bar(x_right, segment_4, bottom=bottom_4, width=bar_width, label='MIPS4 Voronoi', color=self.config.StackedBoxColors[3], hatch='\\')

                        bottom_5 = bottom_4 + np.array(segment_4)
                        ax2.bar(x_right, segment_5, bottom=bottom_5, width=bar_width, label='MIPS5 Voronoi', color=self.config.StackedBoxColors[4], hatch='//')

                        bottom_6 = bottom_5 + np.array(segment_5)
                        ax2.bar(x_right, segment_6, bottom=bottom_6, width=bar_width, label='MIPS6 Voronoi', color=self.config.StackedBoxColors[5], hatch='\\')

                        bottom_7 = bottom_6 + np.array(segment_6)
                        ax2.bar(x_right, segment_7, bottom=bottom_7, width=bar_width, label='MIPS7 Voronoi', color=self.config.StackedBoxColors[6], hatch='//')
                            # self.df_dict[key].plot(x='mobDevNum', kind="bar", stacked=True, width=0.15,
                            #                   ax=ax2, position=0, hatch='//')
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
                        # group_no += 1
                # print(f'j={j}: len(self.config.LineColors):{len(self.config.LineColors)}')
                # print(f'j={j}: len(self.config.lineStyle):{len(self.config.lineStyle)}')
                # print(f'j={j}: len(self.config.ScenarioLabelsList):{len(self.config.ScenarioLabelsList)}')
                # print(f'j={j}: len(self.config.lineWidth):{len(self.config.lineWidth)}')
                ax.plot(x, y, color=tuple(self.config.LineColors[j]), linestyle=self.config.lineStyle[j],
                        label=self.config.ScenarioLabelsList[j], linewidth=self.config.lineWidth[j])
            #end
        else:
            markers = self.config.LineStyleMono
            #linestyleorder(LineStyleOrder)
            yerr = np.linspace(0.05, 0.2, 10)
            for j in range(1, len(scenarioType,1)+1):
                if(self.config.IncludeErrorBars == 1 and self.config.IterationCount > 1):
                    #errorbar(types, results(j,:),min_results(j,:),max_results(j,:),char(markers(j)),'MarkerFaceColor','w','LineWidth',1.4)
                    plt.errorbar(x, y, yerr=yerr, fmt='o', capsize=5, ecolor='red', elinewidth=2)
                else:
                    plt.grid()

                    y = results[j, :]

                    # plt.title("Linear plot of y")

                    plt.figure()
                    if self.yScale == 'log':
                        #semilogy(types, max(1, results(j,:)), char(markers(j)), 'MarkerFaceColor', 'w', 'LineWidth', 1.4)
                        plt.semilogy(y)
                    else:
                        #plot(types, results(j,:),char(markers(j)),'MarkerFaceColor','w','LineWidth',1.4)
                        plt.plot(y)
                        #linestyleorder(LineStyleOrder)
                    #end
                #end
                #hold on;
            #end
        #end
        # lgnd = legend(self.config.ScenarioLabelsList,'Location','best')
        #plt.legend(self.config.ScenarioLabelsList, 'best')
        # if(self.config.ColorPlot == 1):
        #     set(lgnd,'color','none')
        #end

        #hold off;
        # axis square
        # xlabel(self.config.HorizontalAxisLabel)
        ax.set_xlabel(self.config.HorizontalAxisLabel)
        # plt.ylabel(self.yLabel)
        ax.set_ylabel(self.yLabel)
        if self.plotStackedBar:
            ax2.set_ylabel('Percentage of tasks sent to MIPS groups')

        # Define the start, end, and step for the x-axis ticks
        start_tick = stepOfxAxis*stepOfMobileDeviceLoop
        end_tick = endOfMobileDeviceLoop
        step_tick = stepOfxAxis*stepOfMobileDeviceLoop

        # Generate the tick locations using numpy.arange()
        tick_locations = np.arange(start_tick, end_tick + step_tick, step_tick)

        # Set the x-axis ticks
        ax.set_xticks(tick_locations)
        # ax2.set_xticks(tick_locations)

        # ax2.set_xlim(right=len(self.df_dict['HAFA']) - 0.5)
        if self.plotStackedBar:
            ax2.set_ylim(0.0, 10.0)

        ax.set_xlim(min(types)-200, max(types)+200)
        # ax2.set_xlim(min(types), max(types))
        ax.legend(labelspacing=0.2, handlelength=1.0,  handleheight=0.5, handletextpad=0.3, borderpad=0.5, bbox_transform=fig.transFigure)#markerscale=2.5, , color=self.config.LineColors)
        #ax.legend(bbox_transform=fig.transFigure)


        legend = ax.legend()
        legend_handles = legend.legend_handles
        # print(len(legend.legend_handles))
        for legend_i in range(len(legend_handles)):
            if len(self.randomSeed) > 0:
                legend_handles[legend_i].set_label(self.config.ScenarioLabelsList[legend_i]+'(Random seed: '+self.randomSeed+')')
            else:
                legend_handles[legend_i].set_label(self.config.ScenarioLabelsList[legend_i])  # Change label
            legend_handles[legend_i].set_color(self.config.LineColors[legend_i])  # Change color
            legend_handles[legend_i].set_marker(self.config.markerStyle[legend_i])  # Change marker

        if self.plotStackedBar:
            # lines, labels = ax.get_legend_handles_labels()
            bars, bar_labels = ax2.get_legend_handles_labels()
            # ax2.legend(lines + bars, labels + bar_labels, loc='upper left')
            ax2.legend(bars, bar_labels, labelspacing=0.2, handlelength=0.8,  handleheight=0.5, handletextpad=0.3, borderpad=0.5, prop={'size': 9}, bbox_to_anchor=(1.4, 1))
        # ax2.legend(prop={'size': 9})
            # legend_handles[legend_i].handleheight(0.3)
            # legend_handles[legend_i].set_linewidth(self.config.lineWidth[legend_i])
            # legend_handles[legend_i].set_linestyle(self.config.lineStyle[legend_i])
            # legend_handles[legend_i].text.set_fontsize(6)
        # Modify the first handle (corresponding to 'Original Sine')
        # legend_handles[0].set_label('Modified Sine')  # Change label
        # legend_handles[0].set_color('green')  # Change color
        # legend_handles[0].set_marker('s')  # Change marker
        #
        # # Modify the second handle (corresponding to 'Original Cosine')
        # legend_handles[1].set_label('Modified Cosine')  # Change label
        # legend_handles[1].set_color('purple')  # Change color
        # legend_handles[1].set_marker('^')  # Change marker
        #if len(graphTitle) == 0:
        graphTitle = self.yLabel + ' - ' + self.appType.replace('_', ' ')
            # end
        # title(graphTitle, 'FontSize', 12)
        plt.title(graphTitle, fontsize = 12)
        # plt.show()
        # plt.scatter
        # plt.tight_layout()
        # plt.show()
        # exit(0)
        return plt#.figure()
        #ax.set()
        #set(gca,'XTick', (stepOfxAxis*stepOfMobileDeviceLoop):(stepOfxAxis*stepOfMobileDeviceLoop):endOfMobileDeviceLoop)
        '''ylabel(yLabel)
        set(gca,'XLim',[startOfMobileDeviceLoop-5 endOfMobileDeviceLoop+5])
        
        set(get(gca,'Xlabel'),'FontSize',12)
        set(get(gca,'Ylabel'),'FontSize',12)
        set(lgnd,'FontSize',12)
        if isempty(graphTitle):
            graphTitle = yLabel + ' - ' + strrep(appType, '_', ' ')
        #end
        title(graphTitle, 'FontSize', 12)
        annotation('rectangle',[0, 0, 1, 1],'Color','w')
        plotOutput = hFig'''
        #set(0,'DefaultFigureVisible','on');
        #figure('Visible','on');
        #set(hFig, 'visible', 'on');
    #end