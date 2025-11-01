import os.path
from pathlib import Path
import re
import numpy as np



class Configuration:
    # Class attributes (shared by all instances)
    #class_attribute = "I am a class attribute"

    pwd = os.getcwd().par+""  # present working directory
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
    LineColors = [[0.8, 0, 0], [0, 0.15, 0.6], [0, 0.23, 0], [0.6, 0, 0.6], [0.08, 0.08, 0.08], [0, 0.8, 0.8],
                  [0.8, 0.4, 0], [0.8, 0.8, 0], [0.1, 0.5, 0.7], [1.0, 0, 0]]
    # LineStyleMono {mustBeText} = {'-k*', '-ko', '-ks', '-kv', '-kp', '-kd', '-kx', '-kh'}
    LineStyleMono = {'-k*', '-ko', '-ks', '-kv', '-kp', '-kd', '-kx', '-kh', '-k^', '-khexagram'}  # It must be a text.
    # LineStyleColor {mustBeText} = {':k*', ':ko', ':ks', ':kv', ':kp', ':kd', ':kx', ':kh'}
    LineStyleColor = {':k*', ':ko', ':ks', ':kv', ':kp', '-kd', ':kx', ':kh', ':k^', '-khexagram'}  # It must be a text.

    # LineStyleOrder = {'-.', '--', '--', '--', '--', '--', '--', '--', '--', '-'}

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
        config = finishConfig(config)
            
    def finishConfig(oldConfig):
        #FINISHCONFIG As autoConfig(), but preserve non-defaults.
        #   For example, if oldConfig.FolderPath is set, then any
        #   oldConfig properties containing the default values will be
        #   reconfigured based on the files present in the
        #   oldConfig.FolderPath directory.
        newConfig = Configuration()
        # Most important property is FolderPath, so start there.
        scriptPath = newConfig.pwd
        if os.path.samefile(newConfig.FolderPath, oldConfig.FolderPath):
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
        allFiles = [x for x in os.listdir(newConfigFolderPath) if re.match('\w*\/\w*SIMRESULT_\w*_GENERIC\w*')]
        print(allFiles)
        exit(0)
        #cd(scriptPath)
        # Code?? for previous line
        # Now, use the files in FolderPath to set remaining properties.
        allNames = [allFiles.name]
        #TODO: Find a way to replace NEXT_FIT in the regex to something
        #more dynamic in case "orchestrator_policies" is changed to
        #something else in the future.
        regex = 'SIMRESULT_(?<scenario>[\w_\s]+)_NEXT_FIT_(?<devices>\d+)DEVICES_(?<appType>[\w \s]+)_GENERIC'
        combos = regexp(allNames, regex, 'names')
        #scenariosFullList = string({combos.scenario})
        scenariosFullList = str({combos.scenario})

        #allScenarios = unique(scenariosFullList(:))
        allScenarios = np.unique(scenariosFullList) #(:))
        # Update scenario list and series labels.
        if (newConfig.SimulationScenarioList.lower() == oldConfig.SimulationScenarioList.lower()):
            newConfig.SimulationScenarioList = allScenarios

        if (newConfig.ScenarioLabelsList.lower() == oldConfig.ScenarioLabelsList.lower()):
            newConfig.ScenarioLabelsList = strrep(newConfig.SimulationScenarioList, '_', ' ')

        #devices = string({combos.devices})
        devices = str({combos.devices})
        #allDeviceCounts = unique(devices(:))
        allDeviceCounts = np.unique(devices) #(:))
        #sort(allDeviceCounts)
        # Update mobile device min, max, and step.
        if newConfig.MinimumMobileDevices == oldConfig.MinimumMobileDevices:
            newConfig.MinimumMobileDevices = str2double(allDeviceCounts(1))

        if newConfig.MaximumMobileDevices == oldConfig.MaximumMobileDevices:
            newConfig.MaximumMobileDevices = str2double(allDeviceCounts(length(allDeviceCounts)))

        deviceStep = (newConfig.MaximumMobileDevices - newConfig.MinimumMobileDevices)/(length(allDeviceCounts)-1)
        if newConfig.MobileDeviceStep == oldConfig.MobileDeviceStep:
            newConfig.MobileDeviceStep = deviceStep

        #appTypes = string({combos.appType})
        appTypes = str({combos.appType})
        #allAppTypes = unique(appTypes(:))
        allAppTypes = np.unique(appTypes) #(:))
        # Update list of app types.
        if (newConfig.AppTypes.lower() == oldConfig.AppTypes.lower()):
            newConfig.AppTypes = allAppTypes

        # Set the IterationCount.
        if newConfig.IterationCount == oldConfig.IterationCount:
            modIterations = mod(len(combos),len(allScenarios)*len(allDeviceCounts)*len(allAppTypes))
            if modIterations == 0:
                newConfig.IterationCount = len(combos)/(len(allScenarios)*len(allDeviceCounts)*len(allAppTypes))
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
            countArray = np.zeros(scenarioCount, 1)
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
            screenSize = get(0, 'ScreenSize')
            minDim = 0.9*min(screenSize(3), screenSize(4))
            coord1 = (screenSize(3)-minDim)/2
            coord2 = (screenSize(4)-minDim)/2
            newConfig.PlotWindowCoordinates = [coord1, coord2, minDim, minDim]
        return newConfig
 