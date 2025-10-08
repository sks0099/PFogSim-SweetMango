import pandas as pd
import re
import numpy as np
#infile = r"D:\Documents and Settings\xxxx\Desktop\test_log.txt"
#inputFile = r"..\sim_results_100_600\ite9\2025-09-06_21-23-42\SIMRESULT_SINGLE_LAYER_VORONOI_NEXT_FIT_100DEVICES_FAIL.log"
#inputFile = r"..\sim_results_100_600\ite9\2025-09-06_22-43-21\SIMRESULT_SINGLE_LAYER_VORONOI_NEXT_FIT_100DEVICES_FAIL.log"
#inputFile = r"..\sim_results_100_600\ite9\2025-09-07_22-34-56\SIMRESULT_SINGLE_LAYER_VORONOI_NEXT_FIT_100DEVICES_FAIL.log"
#inputFile = r"..\sim_results_100_600\ite9\2025-09-07_22-56-36\SIMRESULT_SINGLE_LAYER_VORONOI_NEXT_FIT_100DEVICES_FAIL.log"
#inputFile = r"..\sim_results_100_600\ite9\2025-09-08_14-04-02\SIMRESULT_SINGLE_LAYER_VORONOI_NEXT_FIT_100DEVICES_FAIL.log"
#inputFile = r"..\sim_results_100_600\ite9\2025-09-08_14-25-29\SIMRESULT_SINGLE_LAYER_VORONOI_NEXT_FIT_100DEVICES_FAIL.log"
#inputFile = r"..\sim_results_100_600\ite9\2025-09-08_16-18-41\SIMRESULT_SINGLE_LAYER_VORONOI_NEXT_FIT_100DEVICES_FAIL.log"
#inputFile = r"..\sim_results_100_600\ite9\2025-09-08_16-18-41\SIMRESULT_SINGLE_LAYER_VORONOI_NEXT_FIT_100DEVICES_FAIL.log"
inputFile = r"..\sim_results_100_600\ite9\2025-09-08_20-07-55\SIMRESULT_SINGLE_LAYER_VORONOI_NEXT_FIT_100DEVICES_FAIL.log"
inputConsoleRunFile = r"..\sim_results_100_600\consoleruns\1757380075499_console.txt"
print(f"input fail log file = {inputFile} \ninput consolerun file = {inputConsoleRunFile}")
relevantLines = []
relevantConsoleRunLines = []
keep_phrases = ["task rejected due to unacceptable latency"]
df = pd.DataFrame(data=None, columns=['taskId','datacenterId','hostId','vmId','vmType','taskType','taskLength',
                                      'taskInputType','taskOutputSize','taskStartTime','taskEndTime','RejectionReasonId'])
dfcr = pd.DataFrame(data=None, columns=['taskId','mdId','hostId','perceivedDelay'])
with open(inputFile) as f:
    f = f.readlines()

for line in f:
    if(line[0]!='#'):
        #print(line)
        relevantLines.append(line)
        splits = re.split(r';', line)
        df.loc[len(df)] = [int(splits[0]), int(splits[1]),int(splits[2]),int(splits[3]),
                           splits[4],splits[5],splits[6],splits[7],
                           splits[8],float(splits[9]),float(splits[10]),int(splits[11])]
    '''for phrase in keep_phrases:
        if phrase in line:
            important.append(line)
            break'''

#with open(inputConsoleRunFile) as fcr:
#    fcr = fcr.readlines()
try:
    with open(inputConsoleRunFile, 'r', encoding='utf-8') as file:
        fcr = file.read()
    #print(file_content_string)
except FileNotFoundError:
    print(f"Error: The file '{inputConsoleRunFile}' was not found.")
except Exception as e:
    print(f"An error occurred: {e}")
crlines = fcr.split('\n')
for crline in crlines:
    #if(crline[0]!='#'):
    #print(line)
    #relevantConsoleRunLines.append(crline)
    #splits = re.split(r';', line)
    '''df.loc[len(df)] = [int(splits[0]), int(splits[1]),int(splits[2]),int(splits[3]),
                       splits[4],splits[5],splits[6],splits[7],
                       splits[8],float(splits[9]),float(splits[10]),int(splits[11])]'''
    for phrase in keep_phrases:
        if phrase in crline:
            relevantConsoleRunLines.append(crline)
            splits = re.split(r':',crline)
            #print(crline)
            #print((splits[2].strip()).split(' ')[0],(splits[3].strip()).split(' ')[0],
            #      (splits[4].strip()).split(' ')[0],(splits[5].strip()).split(' ')[0] )
            dfcr.loc[len(dfcr)] = [int((splits[2].strip()).split(' ')[0]), int((splits[3].strip()).split(' ')[0]),
                                   int((splits[4].strip()).split(' ')[0]),
                               float((splits[5].strip()).split(' ')[0])]
            #exit(0)
            #break
#print(relevantLines)
#print(relevantConsoleRunLines)
#for line in relevantConsoleRunLines:
#    print(line)
dfcr = dfcr.astype({'taskId': int, 'mdId':int, 'hostId':int, 'perceivedDelay':float})
dfcr = dfcr.drop_duplicates(subset='taskId')
print(dfcr)
#exit(0)
#print(df)
hostIds = df['hostId'].unique()
#print(hostIds)
sumOverlapCnt = 0
rowCnt = 0
for hostId in hostIds:
    #print(f'\nhostId = {hostId}')
    filtered_hostId = df[df['hostId'] == hostId].sort_values(by='taskStartTime')
    filtered_hostId['start'] = pd.to_timedelta(df.taskStartTime, unit='ms')
    filtered_hostId['end'] = pd.to_timedelta(df.taskEndTime, unit='ms')
    filtered_hostId['overlapCnt'] = filtered_hostId.apply(
        lambda row: len(filtered_hostId[(((row.start <= filtered_hostId.start) & (filtered_hostId.start <= row.end)) \
                                 | ((filtered_hostId.start <= row.start) & (row.start <= filtered_hostId.end)))
                                & (row.taskId != filtered_hostId.taskId) & (row.hostId == filtered_hostId.hostId)]), axis=1)
    sumOverlapCnt = sumOverlapCnt + filtered_hostId['overlapCnt'].sum();
    rowCnt = rowCnt + filtered_hostId['taskId'].count();
    if(hostId == 226):
        print(f'\nhostId = {hostId}')
        sorted_filtered_hostId = filtered_hostId.sort_values(by='taskId')
        print(sorted_filtered_hostId)#filtered_hostId[['taskId','taskLength','taskStartTime','taskEndTime', 'overlapCnt'].sort()])
print(f'total number of tasks = {rowCnt}')
print(f'Total overlap count={sumOverlapCnt} Percentage overlapped failed task = {sumOverlapCnt*100.0/rowCnt}%')

# Rows in df1 not in df2 based on 'key_column'
merged_df = pd.merge(df, dfcr, on='taskId', how='inner')
print(f'merged_df.shape={merged_df.shape}')
#mask = np.equal(df['taskId'].values, dfcr['taskId'].values).all(axis=1)
#non_matching_df1 = dfcr[~mask]#dfcr[~df.isin["taskId"]] # #merged_df[merged_df['_merge'] == 'left_only'].drop(columns=['_merge', 'df'])
non_matching_df1 = dfcr[~dfcr['taskId'].isin(df['taskId'])].sort_values(by='taskId')
print("Rows in dfcr not in df:")
print(non_matching_df1)