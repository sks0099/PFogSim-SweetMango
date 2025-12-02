import json
import pandas as pd
import math
import matplotlib.pyplot as plt
import os

def convertLongLatToXY(longitude, latitude):
    R = 6378137.0; # in meter at equator
    scaling_factor = 1.0; # 0.000032;
    x = R * math.cos(math.pi * latitude / 180.0) * math.cos(math.pi * longitude / 180.0);
    y = R * math.cos(math.pi * latitude / 180.0) * math.sin(math.pi * longitude / 180.0);
    return x, y

def main():
    # Define the path to your JSON file
    # file_path = "../sim_results_100_600/jsons/ite0/"
    # fn_file_name = "1762348227403_KASUS_fogNode_loc.json"
    # md_file_name = "1762348227403_KASUS_mobile_loc.json"
    # fn_file_name = "1762570995475_KASUS_fogNode_loc.json"
    # md_file_name = "1762570995475_KASUS_mobile_loc.json"


    # file_path = "../sim_results/jsons/ite0/"
    # fn_file_name = "1762538911386_KASUS_fogNode_loc.json"
    # md_file_name = "1762538911386_KASUS_mobile_loc.json"
    # fn_file_name = "1762542313782_KASUS_fogNode_loc.json"
    # md_file_name = "1762542313782_KASUS_mobile_loc.json"

    file_path = "../sim_results_100_600_different_rand_seeds/rs567/jsons/ite9/"
    fn_file_name = "1762705174738_cse-gpu2_fogNode_loc.json"
    md_file_name = "1762705174738_cse-gpu2_mobile_loc.json"

    # outputDir = 'fogNodeMobilePlots'
    outputDir = 'fogNodeMobilePlots/rs567/ite9'
    os.makedirs(outputDir, exist_ok=True)

    num_fn_plus_md = 0
    sum_longitude = 0.0
    sum_latitude = 0.0
    x_max = -1e9
    x_min = 1e9
    y_max = -1e9
    y_min = 1e9

    try:
        # Open the JSON file in read mode ('r')
        with open(file_path+fn_file_name, 'r') as file:
            # Load the JSON data from the file into a Python dictionary
            fn_dict = json.load(file)
        #print("JSON data loaded successfully into a dictionary:")
        #print(fn_dict)
        #print(f"Type of data_dict: {type(fn_dict)}")

        # You can now access elements of the dictionary
        #print(f"Name: {fn_dict['name']}")
        #print(f"Age: {fn_dict['age']}")
        # Now you can work with the 'data' dictionary
        #print("JSON data loaded successfully:")
        #print(data_fn)
        #print(f"Type of loaded data: {type(data_fn)}")
        keylist = fn_dict.keys()  # this is of type `dict_key`, NOT a `list`
        sorted(keylist)
        for k in keylist:
            print(f'key: {k}')
            os.makedirs(outputDir + '/' + md_file_name.split('_')[0], exist_ok=True)
            fig_file = outputDir + '/' + md_file_name.split('_')[0]+'/'+md_file_name.split('_')[0] + '_' + md_file_name.split('_')[1] +'_'+str(k)+ '.svg'
            # Define column names and their desired data types
            columns = ['id', 'fogNodeId', 'fogNodeLongitude', 'fogNodeLatitude','fogNodeAltitude', 'fogNodeX', 'fogNodeY',
                       'fogNodeScaledX','fogNodeScaledY']
            dtypes = {'id': int, 'fogNodeId': int, 'fogNodeLongitude': float, 'fogNodeLatitude': float, 'fogNodeAltitude':float,
            'fogNodeX':float, 'fogNodeY':float, 'fogNodeScaledX':float, 'fogNodeScaledY':float}

            # Create an empty DataFrame with the specified schema
            fn_df = pd.DataFrame(columns=columns).astype(dtypes)


            #fn_df['fogNodeId'] = fn_df['fogNodeId'].astype('int64')

            #fn_df = pd.DataFrame
            # Example of accessing data:
            for key in fn_dict.get(k):
                fnId = (int)(fn_dict.get(k)[key]['fogNodeId'])
                fnLongitude = (float)(fn_dict.get(k)[key]['longitude'])
                fnLatitude = (float)(fn_dict.get(k)[key]['latitude'])
                fnAltitude = (float)(fn_dict.get(k)[key]['altitude'])
                fnX, fnY = convertLongLatToXY(fnLongitude, fnLatitude)

                fn_df.loc[len(fn_df)] = [int(key), fnId, fnLongitude,
                                         fnLatitude,fnAltitude,
                                         fnX,fnY,0.0,0.0]
            #if '0' in fn_dict:
                # print(f"fogNode: {fn_dict[key]}")
                # print(f"fogNodeId: {fn_dict[key][0]['fogNodeId']}")
                # print(f"fogNodeLongitude: {fn_dict[key][0]['longitude']}")
                # print(f"fogNodeLatitude: {fn_dict[key][0]['latitude']}")
                # print(f"fogNodeAltitude: {fn_dict[key][0]['altitude']}")

            num_nodes = len(fn_df)
            sum_longitude += fn_df['fogNodeLongitude'].sum()
            sum_latitude += fn_df['fogNodeLatitude'].sum()


            if fn_df['fogNodeX'].max() > x_max: x_max = fn_df['fogNodeX'].max()
            if fn_df['fogNodeX'].min() < x_min: x_min = fn_df['fogNodeX'].min()
            if fn_df['fogNodeY'].max() > y_max: y_max = fn_df['fogNodeY'].max()
            if fn_df['fogNodeY'].min() < y_min: y_min = fn_df['fogNodeY'].min()

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
                    mdId = (int)(md_dict.get(k)[key]['mobileDeviceId'])
                    mdLongitude = (float)(md_dict.get(k)[key]['longitude'])
                    mdLatitude = (float)(md_dict.get(k)[key]['latitude'])
                    mdAltitude = (float)(md_dict.get(k)[key]['altitude'])
                    mdX, mdY = convertLongLatToXY(mdLongitude, mdLatitude)

                    md_df.loc[len(md_df)] = [int(key), mdId, mdLongitude,
                                             mdLatitude, mdAltitude,
                                             mdX, mdY, 0.0, 0.0]
                    # print(f"md: {md_dict[key]}")
                    # print(f"mdId: {mdId}")
                    # print(f"mdLongitude: {mdLongitude}")
                    # print(f"mdLatitude: {mdLatitude}")
                    # print(f"mdAltitude: {mdAltitude}")

                num_nodes = len(md_df)
                sum_longitude += md_df['mdLongitude'].sum()
                sum_latitude += md_df['mdLatitude'].sum()
                # centroid_longitude = sum_longitude / num_nodes
                # centroid_latitude = sum_latitude / num_nodes
                # print(f'centroid_longitude = {centroid_longitude} centroid_latitude = {centroid_latitude}')
                # centroid_x, centroid_y = convertLongLatToXY(centroid_longitude, centroid_latitude)
                # print(f'centroid_x = {centroid_x} centroid_y = {centroid_y}')
                # x_max = md_df['mdX'].max()
                # x_min = md_df['mdX'].min()
                # y_max = md_df['mdY'].max()
                # y_min = md_df['mdY'].min()

                if md_df['mdX'].max() > x_max: x_max = md_df['mdX'].max()
                if md_df['mdX'].min() < x_min: x_min = md_df['mdX'].min()
                if md_df['mdY'].max() > y_max: y_max = md_df['mdY'].max()
                if md_df['mdY'].min() < y_min: y_min = md_df['mdY'].min()

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

            num_fn_plus_md = len(fn_df) + len(md_df)
            centroid_longitude = sum_longitude / num_fn_plus_md
            centroid_latitude = sum_latitude / num_fn_plus_md
            # print(f'centroid_longitude = {centroid_longitude} centroid_latitude = {centroid_latitude}')
            centroid_x, centroid_y = convertLongLatToXY(centroid_longitude, centroid_latitude)
            # print(f'centroid_x = {centroid_x} centroid_y = {centroid_y}')

            scaling_factor = 1024.0 / (3 * max((x_max - x_min), (y_max - y_min)))
            # print(f'scaling_factor = {scaling_factor}')
            fn_df['fogNodeScaledX'] = (fn_df['fogNodeX'] - centroid_x) * scaling_factor
            fn_df['fogNodeScaledY'] = (fn_df['fogNodeY'] - centroid_y) * scaling_factor
            md_df['mdScaledX'] = (md_df['mdX'] - centroid_x) * scaling_factor
            md_df['mdScaledY'] = (md_df['mdY'] - centroid_y) * scaling_factor

            plt.figure(figsize=(14, 14))
            plt.scatter(x=fn_df['fogNodeScaledX'], y=fn_df['fogNodeScaledY'], s=3)
            plt.scatter(x=md_df['mdScaledX'], y=md_df['mdScaledY'], s=1)
            plt.savefig(fig_file, format='svg')
            # plt.show()

        
        #print(fn_df)
    except FileNotFoundError:
        print(f"Error: The file '{file_path+fn_file_name}' was not found.")
    except json.JSONDecodeError:
        print(f"Error: Could not decode JSON from '{file_path+fn_file_name}'. Check if the file contains valid JSON.")
    except Exception as e:
        print(f"An unexpected error occurred: {e}")






if __name__ == "__main__":
        main()