import numpy as np
import matplotlib.pyplot as plt
import os
import pandas as pd
import json
import random
import math

def convertLongLatToXY(longitude, latitude):
    R = 6378137.0 # in meter at equator
    scaling_factor = 1.0 # 0.000032;
    x = R * math.cos(math.pi * latitude / 180.0) * math.cos(math.pi * longitude / 180.0)
    y = R * math.cos(math.pi * latitude / 180.0) * math.sin(math.pi * longitude / 180.0)
    return x, y


# Python 3 program to calculate Distance Between Two Points on Earth
from math import radians, cos, sin, asin, sqrt


def distanceTwoPoints(lat1, lat2, lon1, lon2):
    # The math module contains a function named
    # radians which converts from degrees to radians.
    lon1 = radians(lon1)
    lon2 = radians(lon2)
    lat1 = radians(lat1)
    lat2 = radians(lat2)

    # Haversine formula
    dlon = lon2 - lon1
    dlat = lat2 - lat1
    a = sin(dlat / 2) ** 2 + cos(lat1) * cos(lat2) * sin(dlon / 2) ** 2

    c = 2 * asin(sqrt(a))

    # Radius of earth in meters
    r = 6378137.0

    # calculate the result
    return (c * r)


# # driver code
# lat1 = 53.32055555555556
# lat2 = 53.31861111111111
# lon1 = -1.7297222222222221
# lon2 = -1.6997222222222223
# print(distance(lat1, lat2, lon1, lon2), "K.M")

def main():
    # Define the path to your JSON file
    # file_path = "../sim_results_100_600/jsons/ite0/"
    # fn_file_name = "1762348227403_KASUS_fogNode_loc.json"
    # md_file_name = "1762348227403_KASUS_mobile_loc.json"

    # fn_file_name = "1762487177423_KASUS_fogNode_loc.json"
    # md_file_name = "1762487177423_KASUS_mobile_loc.json"
    file_path = "../sim_results/jsons/ite0/"
    # fn_file_name = "1762538911386_KASUS_fogNode_loc.json"
    # md_file_name = "1762538911386_KASUS_mobile_loc.json"
    fn_file_name = "1762542313782_KASUS_fogNode_loc.json"
    md_file_name = "1762542313782_KASUS_mobile_loc.json"

    outputDir = 'fogNodeMobileDensityPlots'
    os.makedirs(outputDir, exist_ok=True)


    try:
        # Open the JSON file in read mode ('r')
        with open(file_path + fn_file_name, 'r') as file:
            # Load the JSON data from the file into a Python dictionary
            fn_dict = json.load(file)
        # print("JSON data loaded successfully into a dictionary:")
        # print(fn_dict)
        # print(f"Type of data_dict: {type(fn_dict)}")

        # You can now access elements of the dictionary
        # print(f"Name: {fn_dict['name']}")
        # print(f"Age: {fn_dict['age']}")
        # Now you can work with the 'data' dictionary
        # print("JSON data loaded successfully:")
        # print(data_fn)
        # print(f"Type of loaded data: {type(data_fn)}")

        keylist = fn_dict.keys()  # this is of type `dict_key`, NOT a `list`
        sorted(keylist)
        for k in keylist:
            fig_file = outputDir + '/' + md_file_name.split('_')[0] + '_' + md_file_name.split('_')[1]\
                       +'_'+str(k)+ '_density.svg'
            # Define column names and their desired data types
            columns = ['id', 'fogNodeId', 'fogNodeLongitude', 'fogNodeLatitude', 'fogNodeAltitude', 'fogNodeX', 'fogNodeY',
                       'fogNodeScaledX', 'fogNodeScaledY']
            dtypes = {'id': int, 'fogNodeId': int, 'fogNodeLongitude': float, 'fogNodeLatitude': float,
                      'fogNodeAltitude': float,
                      'fogNodeX': float, 'fogNodeY': float, 'fogNodeScaledX': float, 'fogNodeScaledY': float}

            # Create an empty DataFrame with the specified schema
            fn_df = pd.DataFrame(columns=columns).astype(dtypes)

            # fn_df['fogNodeId'] = fn_df['fogNodeId'].astype('int64')

            # fn_df = pd.DataFrame
            # Example of accessing data:
            for k in fn_dict:
                print(f'key: {k}')

                columns = ['id', 'fogNodeId', 'fogNodeLongitude', 'fogNodeLatitude', 'fogNodeAltitude', 'fogNodeX',
                           'fogNodeY', 'fogNodeScaledX', 'fogNodeScaledY']
                dtypes = {'id': int, 'fogNodeId': int, 'fogNodeLongitude': float, 'fogNodeLatitude': float,
                          'fogNodeAltitude': float,
                          'fogNodeX': float, 'fogNodeY': float, 'fogNodeScaledX': float, 'fogNodeScaledY': float}

                # Create an empty DataFrame with the specified schema
                fn_df = pd.DataFrame(columns=columns).astype(dtypes)

                # fn_df['fogNodeId'] = fn_df['fogNodeId'].astype('int64')

                # fn_df = pd.DataFrame
                # Example of accessing data:
                for key in fn_dict.get(k)[0]:
                    fnId = (int)(fn_dict.get(k)[0][key][0]['fogNodeId'])
                    fnLongitude = (float)(fn_dict.get(k)[0][key][0]['longitude'])
                    fnLatitude = (float)(fn_dict.get(k)[0][key][0]['latitude'])
                    fnAltitude = (float)(fn_dict.get(k)[0][key][0]['altitude'])
                    fnX, fnY = convertLongLatToXY(fnLongitude, fnLatitude)

                    fn_df.loc[len(fn_df)] = [int(key), fnId, fnLongitude,
                                             fnLatitude, fnAltitude,
                                             fnX, fnY, 0.0, 0.0]
            # fnId = (int)(fn_dict[key][0]['fogNodeId'])
            # fnLongitude = (float)(fn_dict[key][0]['longitude'])
            # fnLatitude = (float)(fn_dict[key][0]['latitude'])
            # fnAltitude = (float)(fn_dict[key][0]['altitude'])
            # fnX, fnY = convertLongLatToXY(fnLongitude, fnLatitude)
            #
            # fn_df.loc[len(fn_df)] = [int(key), fnId, fnLongitude,
            #                          fnLatitude, fnAltitude,
            #                          fnX, fnY, 0.0, 0.0]
        # if '0' in fn_dict:
        # print(f"fogNode: {fn_dict[key]}")
        # print(f"fogNodeId: {fn_dict[key][0]['fogNodeId']}")
        # print(f"fogNodeLongitude: {fn_dict[key][0]['longitude']}")
        # print(f"fogNodeLatitude: {fn_dict[key][0]['latitude']}")
        # print(f"fogNodeAltitude: {fn_dict[key][0]['altitude']}")
                try:
                    # Open the JSON file in read mode ('r')
                    with open(file_path + md_file_name, 'r') as file:
                        # Load the JSON data from the file into a Python dictionary
                        md_dict = json.load(file)
                    # print("JSON data loaded successfully into a dictionary:")
                    # print(fn_dict)
                    # print(f"Type of data_dict: {type(fn_dict)}")

                    # You can now access elements of the dictionary
                    # print(f"Name: {fn_dict['name']}")
                    # print(f"Age: {fn_dict['age']}")
                    # Now you can work with the 'data' dictionary
                    # print("JSON data loaded successfully:")
                    # print(data_fn)
                    # print(f"Type of loaded data: {type(data_fn)}")

                    # Define column names and their desired data types
                    columns = ['id', 'mdId', 'mdLongitude', 'mdLatitude', 'mdAltitude', 'mdX', 'mdY', 'mdScaledX',
                               'mdScaledY']
                    dtypes = {'id': int, 'mdId': int, 'mdLongitude': float, 'mdLatitude': float, 'mdAltitude': float,
                              'mdX': float, 'mdY': float, 'mdScaledX': float, 'mdScaledY': float}

                    # Create an empty DataFrame with the specified schema
                    md_df = pd.DataFrame(columns=columns).astype(dtypes)

                    # fn_df['fogNodeId'] = fn_df['fogNodeId'].astype('int64')

                    # fn_df = pd.DataFrame
                    # Example of accessing data:
                    for key in md_dict.get(k):
                        mdId = (int)(md_dict.get(k)[key][0]['mobileDeviceId'])
                        mdLongitude = (float)(md_dict.get(k)[key][0]['longitude'])
                        mdLatitude = (float)(md_dict.get(k)[key][0]['latitude'])
                        mdAltitude = (float)(md_dict.get(k)[key][0]['altitude'])
                        mdX, mdY = convertLongLatToXY(mdLongitude, mdLatitude)

                        md_df.loc[len(md_df)] = [int(key), mdId, mdLongitude,
                                                 mdLatitude, mdAltitude,
                                                 mdX, mdY, 0.0, 0.0]
                        # mdId = (int)(md_dict[key][0]['mobileDeviceId'])
                        # mdLongitude = (float)(md_dict[key][0]['longitude'])
                        # mdLatitude = (float)(md_dict[key][0]['latitude'])
                        # mdAltitude = (float)(md_dict[key][0]['altitude'])
                        # mdX, mdY = convertLongLatToXY(mdLongitude, mdLatitude)
                        #
                        # md_df.loc[len(md_df)] = [int(key), mdId, mdLongitude,
                        #                          mdLatitude, mdAltitude,
                        #                          mdX, mdY, 0.0, 0.0]

                    # scaling_factor = 1024.0 / (3 * max((x_max - x_min), (y_max - y_min)))
                    # print(f'scaling_factor = {scaling_factor}')
                    # md_df['mdScaledX'] = (md_df['mdX']-centroid_x)*scaling_factor
                    # md_df['mdScaledY'] = (md_df['mdY'] - centroid_y) * scaling_factor

                    # translate the points according to the centroid and add them to the list
                    # for (edu.boun.edgecloudsim.sample_voronoi_app.Point p: fogNodePoints) {
                    #                                                                       // System.out.println(p.x + ", " + p.y);
                    # p.x -= centroid_x;
                    # p.x *= scaling_factor;
                    # p.y -= centroid_y;
                    # p.y *= scaling_factor;
                    # }

                    # print(md_df)
                except FileNotFoundError:
                    print(f"Error: The file '{file_path + md_file_name}' was not found.")
                except json.JSONDecodeError:
                    print(
                        f"Error: Could not decode JSON from '{file_path + md_file_name}'. Check if the file contains valid JSON.")
                except Exception as e:
                    print(f"An unexpected error occurred: {e}")

                # for i in range(0,len(fn_df)):
                #     if(i<5):
                #         print(fn_df['fogNodeX'], fn_df['fogNodeY'])
                # data1 = np.array([])
                # data2 = np.array([])
                data_lists = []  # np.array([]) #np.empty(shape=(0,), dtype=object)
                for index, row in fn_df.iterrows():
                    # if index < 2:
                    # print(f"fogNodeX: {row['fogNodeX']}, fogNodeX: {row['fogNodeY']}")
                    temp_data = []  # np.array([], dtype=float)
                    for index_md, row_md in md_df.iterrows():
                        # if index_md < 5:
                        radial_dist_using_XY = math.sqrt(
                            (row['fogNodeX'] - row_md['mdX']) ** 2 + (row['fogNodeY'] - row_md['mdY']) ** 2)

                        radial_dist_using_longlat = distanceTwoPoints(row['fogNodeLatitude'], row_md['mdLatitude'],
                                                                      row['fogNodeLongitude'], row_md['mdLongitude'])
                        # temp_data = np.append(temp_data,radial_dist)
                        temp_data.append(radial_dist_using_longlat)

                        # print(f"mdX: {row_md['mdX']}, mdY: {row_md['mdY']}, radial dist: {radial_dist}")
                        # if(index %2 == 0):
                        #     data1 = np.append(data1, radial_dist)
                        # else:
                        #     data2 = np.append(data2, radial_dist)
                    data_lists.append(temp_data)
                data_arrays = np.asarray(data_lists)  # (data_arrays, [temp_data]),axis=1)
                # Generate some sample data
                # data1 = np.random.normal(0, 1, 1000)
                # print(data1)
                # data2 = np.random.normal(2, 0.5, 1500)

                # Define common bins for all histograms for easier comparison
                # bins = np.linspace(-4, 400000, 50)
                number_bins = 50
                bins = np.linspace(np.min(data_arrays), np.max(data_arrays), number_bins)
                bin_size = (np.max(data_arrays) - np.min(data_arrays))/number_bins

                histlist = []
                hist_norm_list = []
                for i in range(data_arrays.shape[0]):
                    histlist.append('hist' + str(i))
                    # hist_norm_list.append(histlist[i] / np.max(histlist[i]))
                    # hist_norm_list.append(histlist[i] / 1.0)

                # print(histlist)
                # Compute histogram data using numpy
                for i in range(data_arrays.shape[0]):
                    # print(i)
                    histlist[i], bin_edges = np.histogram(data_arrays[i], bins=bins)
                # hist1, bin_edges = np.histogram(data1, bins=bins)
                # hist2, _ = np.histogram(data2, bins=bins)

                # Calculate bin centers for positioning the bars
                bin_centers = (bin_edges[:-1] + bin_edges[1:]) / 2

                print(f'bin_size: {bin_size}')
                hist_norm_list = []
                for i in range(data_arrays.shape[0]):
                    hist_norm_list.append(bin_size * histlist[i] / np.max(histlist))
                    # hist_norm_list.append(histlist[i] / 1.0)

                # Normalize the histograms for better visualization if desired
                # hist1_norm = hist1 / np.max(hist1)
                # hist2_norm = hist2 / np.max(hist2)

                # hist_norm_list.append(histlist[0]/np.max(histlist[0]))
                # hist_norm_list.append(histlist[1] / np.max(histlist[1]))
                # hist_norm_list.append(histlist[1] / 1.0)
                # hist_norm[0] = histlist[0]/np.max(histlist[0])

                # Create the plot
                y_interval = 0.25
                fig, ax = plt.subplots(figsize=(10, 1.0 * y_interval * data_arrays.shape[0]))
                ytick_list = []
                ytick_label_list = []

                bar_height = y_interval * 0.9
                for i in range(0, data_arrays.shape[0]):
                    ytick_list.append(i * y_interval)
                    ytick_label_list.append(str(i))
                    color_val_r = random.randint(0, 255) / 255.0
                    color_val_g = random.randint(0, 255) / 255.0
                    color_val_b = random.randint(0, 255) / 255.0

                    ax.barh(y=i * y_interval, width=hist_norm_list[i], left=bin_centers, height=bar_height,
                            align='center',
                            color=(color_val_r, color_val_g, color_val_b, 0.5),
                            label='_Mobile Distribution - ' + str(i + 1))

                    # ax.barh(y=i * y_interval, width=hist_norm_list[i], left=bin_centers, height=bar_height,
                    #         align='center',
                    #         color=(color_val_r, color_val_g, color_val_b, 0.5),
                    #         edgecolor=(color_val_r, color_val_g, color_val_b, 0.5),
                    #         label='_Mobile Distribution - ' + str(i + 1))
                # i=1
                # ax.barh(y=i * y_interval, width=hist_norm_list[i], left=bin_centers, height=bar_height, align='center',
                #                      color='skyblue', label='Mobile Distribution - ' + str(i + 1))
                # if(i%2 == 0):
                #     # Plot the first histogram at y=0
                #     ax.barh(y=i*y_interval, width=hist1_norm, left=bin_centers, height=bar_height, align='center', color='skyblue', label='_Distribution '+str(i+1))
                # else:
                #     # Plot the second histogram at y=1
                #     ax.barh(y=i*y_interval, width=hist2_norm, left=bin_centers, height=bar_height, align='center', color='lightcoral', label='_Distribution '+str(i+1))

                # Set y-axis limits and labels
                ax.set_ylim(-y_interval, y_interval * data_arrays.shape[0])
                # ax.set_yticks([0, 1, 2, 3])
                ax.set_yticks(ytick_list)
                # ax.set_yticklabels(['Histogram at y=0', 'Histogram at y=1','Histogram at y=2', 'Histogram at y=3'])
                ax.set_yticklabels(ytick_label_list)
                ax.set_ylabel('Fog Node Id')
                # Set x-axis label and title
                ax.set_xlabel('Radial distance from fog node (m)')
                ax.set_title('Mobile device count histograms')

                # Add legend and grid
                ax.legend()
                # ax.grid(axis='x', linestyle='--', alpha=0.7)
                ax.grid(axis='x', linestyle='--', alpha=0.8)
                plt.savefig(fig_file, format="svg")
                # plt.show()

        # print(fn_df)
    except FileNotFoundError:
        print(f"Error: The file '{file_path + fn_file_name}' was not found.")
    except json.JSONDecodeError:
        print(f"Error: Could not decode JSON from '{file_path + fn_file_name}'. Check if the file contains valid JSON.")
    except Exception as e:
        print(f"An unexpected error occurred: {e}")

if __name__ == "__main__":
        main()