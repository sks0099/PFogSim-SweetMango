import os.path
from pathlib import Path
import re
import numpy as np
import glob
# import wx #gtk, pygtk
import pyautogui


class Configuration:
    # Class attributes (shared by all instances)
    #class_attribute = "I am a class attribute"

    pwd = os.getcwd()#.par+""  # present working directory
    FolderPath = pwd  # must be a folder
    SimulationTime = (60 * 30)  # must be +ve
    IterationCount = 0  # Default is 0. It must be a non-negative integer.
    ScenarioIterationCounts = [0, 0]  # The value must be numeric. Values are per - scenario.
    SimulationScenarioList = ''  # It must be a text.
    ScenarioLabelsList = ''  # It must be a text.
    AppTypes = 'ALL_APPS'  # It must be a text.
    PlotWindowCoordinates = [350, 60, 720, 720]  # Values must be positive integers.
    HorizontalAxisLabel = 'Number of Mobile Devices'  # It must be a text.
    MinimumMobileDevices = 0  # It must be a non-negative integer.
    MobileDeviceStep = 1  # It must be a +ve integer.
    MaximumMobileDevices = 1  # It must be a +ve integer.
    IncludeErrorBars = -1  # It must be a +ve integer. Default to - 1 to differentiate defaults from manual settings.
    ColorPlot = -1  # It must be an integer. Default to - 1 to differentiate defaults from
    # manual settings.
    XAxisStep = 1  # # It must be a +ve integer.
    # LineColors {mustBeFloat} = [[0.8, 0, 0],[0, 0.15, 0.6],[0, 0.23, 0],[0.6, 0, 0.6], [0.08, 0.08, 0.08],
    # [0, 0.8, 0.8], [0.8, 0.4, 0], [0.8, 0.8, 0]]
    LineColors = [[0.8, 0, 0], [0, 0.15, 0.6], [0, 0.23, 0], [0.6, 0, 0.6], [0.08, 0.08, 0.08], [0.8, 0.4, 0],
                  [0.0, 0.2, 1.0], [0.8, 0.8, 0], [0.1, 0.5, 0.7], [1.0, 0, 0], [0.0, 1.0, 0], [0.0, 0.0, 1.0]]
    # LineStyleMono {mustBeText} = {'-k*', '-ko', '-ks', '-kv', '-kp', '-kd', '-kx', '-kh'}
    LineStyleMono = {'-k*', '-ko', '-ks', '-kv', '-kp', '-kd', '-kx', '-kh', '-k^', '-khexagram'}  # It must be a text.
    # LineStyleColor {mustBeText} = {':k*', ':ko', ':ks', ':kv', ':kp', ':kd', ':kx', ':kh'}
    LineStyleColor = {':k*', ':ko', ':ks', ':kv', ':kp', '-kd', ':kx', ':kh', ':k^', '-khexagram', '-.k'}  # It must be a text.
    markerStyle = ['o', 'v', '^', '<','>','s','p','*', 'h', '+','x', '.']
    # lineStyle = ['-',':',':',':',':',':','-','--','--','--','--','--']
    lineStyle = ['-', '-', '-', '-', '-', '-', '-', '-', '-', '-', '-', '-']
    # lineWidth = [1.0, 0.5, 0.5, 0.5, 0.5, 0.5, 1.0, 0.5, 0.5, 0.5, 0.5, 0.5]
    lineWidth = [1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0]
    # LineStyleOrder = {'-.', '--', '--', '--', '--', '--', '--', '--', '--', '-'}
    StackedBoxColors = ['#00ffff80', '#ff00ff80', '#7fff0080', '#ff000080', '#ff796c80', '#0000ff80', '#c0c0c080', '#aaff3280', '#c79fef80']

    #def __init__(self): #, param1, param2):
        # Constructor method (initializer)
        # Initializes instance attributes unique to each object
        #self.param1 = param1
        #self.param2 = param2

    def instance_method(self):
        # Instance method (operates on instance attributes)
        return f"This is an instance method. param1: {self.param1}"

    @classmethod
    def class_method(cls):
        # Class method (operates on class attributes, takes 'cls' as first arg)
        return f"This is a class method. Class attribute: {cls.class_attribute}"

    @staticmethod
    def static_method(arg):
        # Static method (does not take 'self' or 'cls', behaves like a regular function)
        return f"This is a static method with argument: {arg}"

    def find_filenames_with_pattern(root_dir, pattern):
        """
        Searches for files containing a specific pattern within all subfolders.

        Args:
            root_dir (str): The starting directory for the search.
            pattern (str): The regular expression pattern to search for within file content.
        """
        regex = re.compile(pattern)  # Compile the regex for efficiency
        print(pattern)
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

    def autoConfig():
        #AUTOCONFIG Automatically configure all settings based on presence of data files.
        #   Default FolderPath assumes simulation data 
        #   is stored in the sim_results folder.
        #   Default IterationCount is the number of copies
        #   of each output file in the sim_results folder,
        #   including all subfolders.
        #   Other data properties are likewise set according to 
        #   the file names present in the sim_results folder/subfolders.
        #   Plot details should default to something 
        #   appropriate to the data present.
        config = configuration
        config = Configuration.finishConfig(config)
            
    def finishConfig(oldConfig):
        #FINISHCONFIG As autoConfig(), but preserve non-defaults.
        #   For example, if oldConfig.FolderPath is set, then any
        #   oldConfig properties containing the default values will be
        #   reconfigured based on the files present in the
        #   oldConfig.FolderPath directory.
        newConfig = Configuration()
        # Most important property is FolderPath, so start there.
        scriptPath = newConfig.pwd
        #if os.path.samefile(newConfig.FolderPath, oldConfig.FolderPath):
        if newConfig.FolderPath == oldConfig.FolderPath:
            while 1:
                splitPath = pwd.split('\\')
                if 'PFogSim' in splitPath[len(splitPath)]:
                    break
                else:
                    pwd = '../'+pwd
            #resultsFolder = os.path('sim_results')


            resultFolderPath = Path("sim_results")  # Replace with your directory path

            if any(resultFolderPath.iterdir()):
                dataPath = Path(pwd+'/sim_results')
            #if ~isempty(resultsFolder):
            #    dataPath = strcat(pwd, '/sim_results')

            newConfig.FolderPath = dataPath
        else:
            newConfig.FolderPath = Path(oldConfig.FolderPath)

        #cd(newConfig.FolderPath)
        newConfigFolderPath = newConfig.FolderPath
        #allFiles = dir('**/*SIMRESULT_*_GENERIC*')
        # allFiles = Configuration.find_filenames_with_pattern(newConfigFolderPath, r'\w*\/\w*SIMRESULT_\w*_GENERIC\w*')
        # fileMatchingPattern = r'SIMRESULT_\w*_GENERIC\w*'
        fileMatchingPattern = r'SIMRESULT_(?!\w*LOC|EDGE_BY_DISTANCE)\w*_GENERIC\w*'
        allFiles = Configuration.find_filenames_with_pattern(newConfigFolderPath, fileMatchingPattern)
        #allFiles = [x for x in os.listdir(newConfigFolderPath) if re.match('\w*\/\w*SIMRESULT_\w*_GENERIC\w*')]
        # print(allFiles)
        # pattern_scenario = r'SIMRESULTS_'
        # pattern_info = r"Name: (?P<name>\w+), Age: (?P<age>\d+)"
        pattern_info = r"SIMRESULT_(?P<scenario>[\w_\s]+)_NEXT_FIT_(?P<devices>\d+)DEVICES_(?P<appType>[\w \s]+)_GENERIC"
        scenario_list = []
        scenario_unique_list = []
        device_count_unique_list = []
        appType_unique_list = []
        for f in allFiles:
            # print(f'{f}')
            match_info = re.search(pattern_info, f)
            if match_info:
                scenario = match_info.group("scenario")
                devices = match_info.group("devices")
                appType = match_info.group("appType")
                #print(f"scenario: {scenario}, devices: {devices}, appType: {appType}")
                scenario_list.append(scenario)
                if scenario not in scenario_unique_list:
                    scenario_unique_list.append(scenario)
                if int(devices) not in device_count_unique_list:
                    device_count_unique_list.append(int(devices))
                if appType not in appType_unique_list:
                    appType_unique_list.append(appType)
        print(f'scenario_unique_list: {scenario_unique_list}')
        print(f'device_count_unique_list: {device_count_unique_list}')
        print(f'appType_unique_list: {appType_unique_list}')
        #exit(0)
        # cd(scriptPath)
        # Code?? for previous line
        # Now, use the files in FolderPath to set remaining properties.}
        # allNames = [allFiles.name]
        #TODO: Find a way to replace NEXT_FIT in the regex to something
        #more dynamic in case "orchestrator_policies" is changed to
        #something else in the future.
        # regex = 'SIMRESULT_(?<scenario>[\w_\s]+)_NEXT_FIT_(?<devices>\d+)DEVICES_(?<appType>[\w \s]+)_GENERIC'
        # regex = r'SIMRESULT_(?<scenario>[\w_\s]+)_NEXT_FIT_(?<devices>\d+)DEVICES_(?<appType>[\w \s]+)_GENERIC'
        # combos = regexp(allNames, regex, 'names')
        #scenariosFullList = string({combos.scenario})
        # scenariosFullList = str({combos.scenario})

        #allScenarios = unique(scenariosFullList(:))
        # allScenarios = np.unique(scenariosFullList) #(:))
        allScenarios = scenario_unique_list
        # Update scenario list and series labels.
        if (newConfig.SimulationScenarioList.lower() == oldConfig.SimulationScenarioList.lower()):
            newConfig.SimulationScenarioList = allScenarios

        if (newConfig.ScenarioLabelsList.lower() == oldConfig.ScenarioLabelsList.lower()):
            newConfig.ScenarioLabelsList = [s.replace('_',' ') for s in allScenarios] #strrep(newConfig.SimulationScenarioList, '_', ' ')
        scenarioLabelListIndex = 0
        for ele in newConfig.ScenarioLabelsList:
            if ele == 'HAFA ORCHESTRATOR':
                newConfig.ScenarioLabelsList[scenarioLabelListIndex] = 'HAFA'
            elif ele == 'SINGLE LAYER VORONOI':
                newConfig.ScenarioLabelsList[scenarioLabelListIndex] = 'Voronoi'
            # print(ele, newConfig.ScenarioLabelsList[scenarioLabelListIndex])
            scenarioLabelListIndex += 1
        #devices = string({combos.devices})
        # devices = str({combos.devices})
        #allDeviceCounts = unique(devices(:))
        # allDeviceCounts = np.unique(devices) #(:))
        allDeviceCounts = device_count_unique_list
        #sort(allDeviceCounts)
        # Update mobile device min, max, and step.
        if newConfig.MinimumMobileDevices == oldConfig.MinimumMobileDevices:
            newConfig.MinimumMobileDevices = (float)(allDeviceCounts[0])

        if newConfig.MaximumMobileDevices == oldConfig.MaximumMobileDevices:
            newConfig.MaximumMobileDevices = (float)(allDeviceCounts[len(allDeviceCounts)-1])

        deviceStep = (newConfig.MaximumMobileDevices - newConfig.MinimumMobileDevices)/(len(allDeviceCounts)-1)
        if newConfig.MobileDeviceStep == oldConfig.MobileDeviceStep:
            newConfig.MobileDeviceStep = deviceStep

        #appTypes = string({combos.appType})
        # appTypes = str({combos.appType})
        #allAppTypes = unique(appTypes(:))
        # allAppTypes = np.unique(appTypes) #(:))
        allAppTypes = appType_unique_list
        # Update list of app types.
        if (newConfig.AppTypes.lower() == oldConfig.AppTypes.lower()):
            newConfig.AppTypes = allAppTypes

        # Set the IterationCount.
        if newConfig.IterationCount == oldConfig.IterationCount:
            # modIterations = mod(len(combos),len(allScenarios)*len(allDeviceCounts)*len(allAppTypes))
            modIterations = len(allScenarios) * len(allDeviceCounts) * len(allAppTypes) % len(scenario_list)
            if modIterations == 0:
                # newConfig.IterationCount = len(combos)/(len(allScenarios)*len(allDeviceCounts)*len(allAppTypes))
                newConfig.IterationCount = len(scenario_list) / (len(allScenarios) * len(allDeviceCounts) * len(allAppTypes))
            else:
                newConfig.IterationCount = 1

        #TODO: Restructure plotGenericResult() so that it uses the
        #ScenarioIterationCounts property instead of IterationCount. This will
        #likely require modifications to IncludeErrorBars behavior.
        #Then deprecate the IterationCount property.
        #
        # For each simulation scenario, find the number of instances
        # of ALL_APPS files for the minimum device count. This should
        # be the number of iterations run for that scenario.
        if newConfig.ScenarioIterationCounts == oldConfig.ScenarioIterationCounts:
            scenarioCount = len(newConfig.SimulationScenarioList)
            #countArray = zeros(scenarioCount, 1)
            countArray = np.zeros(scenarioCount, dtype=int)
            #filteredArray = combos(arrayfun(@(n) strcmp(n, 'ALL_APPS'), {combos.appType}))
            # filteredArray = combos(arrayfun( @ (n) strcmp(n, 'ALL_APPS'), {combos.appType}))
            # filteredArray = filteredArray(arrayfun(@(n) strcmp(n, string(newConfig.MinimumMobileDevices)), {filteredArray.devices}))
            # for i in range(1, scenarioCount):
            #     scenario = newConfig.SimulationScenarioList(i)
            #     countArray(i) = nnz(strcmp({filteredArray.scenario}, scenario))
            #end
            newConfig.ScenarioIterationCounts = countArray
        #end
        if newConfig.IncludeErrorBars == oldConfig.IncludeErrorBars:
            if newConfig.IterationCount > 1:
                newConfig.IncludeErrorBars = 1
            else:
                newConfig.IncludeErrorBars = 0

        if newConfig.ColorPlot == oldConfig.ColorPlot:
            if len(newConfig.SimulationScenarioList) > 3:
                newConfig.ColorPlot = 1
            else:
                newConfig.ColorPlot = 0

        if newConfig.PlotWindowCoordinates == oldConfig.PlotWindowCoordinates:
            screenSize = pyautogui.size()
            # print(screenSize)
            # print("width = " + str(screenSize.width) + ", height = " + str(screenSize.height))
            # screenSize = get(0, 'ScreenSize')
            # minDim = 0.9*min(screenSize(3), screenSize(4))
            minDim = 0.9 * min(screenSize.width, screenSize.height)
            # coord1 = (screenSize(3)-minDim)/2
            coord1 = (screenSize.width - minDim) / 2
            # coord2 = (screenSize(4)-minDim)/2
            coord2 = (screenSize.height - minDim) / 2
            newConfig.PlotWindowCoordinates = [coord1, coord2, minDim, minDim]
        return newConfig
 