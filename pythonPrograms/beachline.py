# Draw beachline by drawing overlapping parabolas
# using focii (x1, y1), (x2, y2)... and a directrix (ax+by+c=0).

import numpy as np
import matplotlib.pyplot as plt

epsilon = 8e-4
# Function to find equation of parabola.
def equation_parabola(x1, y1, a, b, c):
    t = a * a + b * b
    a1 = t - (a * a)
    b1 = t - (b * b)
    c1 = (-2 * t * x1) - (2 * c * a)
    d1 = (-2 * t * y1) - (2 * c * b)
    e1 = -2 * a * b
    f1 = (-c * c) + (t * x1 * x1) + (t * y1 * y1)
    print("equation of parabola is", a1, "x^2 +", b1,
          "y^2 +", c1, "x +", d1, "y + ", e1, "xy +", f1, "= 0.")
    return a1, b1, c1, d1, e1, f1

def plot_parabola(focii, eq_param_list, a, b, c): #coeff_x_sq, coeff_y_sq, coeff_x, coeff_y, coeff_xy, const):
    colors = ['red','green','blue']
    y_values_list = []
    x_values = np.linspace(-8, 8, 5000)
    sweepline_y_value = c/(-1.0*b)
    plt.figure(figsize=(8, 8))  # Optional: set figure size
    fig, ax = plt.subplots()
    intersection_points_x = []
    intersection_points_y = []
    lowest_intersection_points_x = []
    lowest_intersection_points_y = []
    lowest_parabola_index = []
    lowest_parabola_start_x = []
    lowest_parabola_end_x = []
    y_value0_prev = 0.0
    y_value1_prev = 0.0
    y_value2_prev = 0.0
    x_value_counter = 0
    # lowest_parabola_index_at_rightmost_point = 0
    for x_value in x_values:
        # print(x_value)
        coeff_x_sq0, coeff_y_sq0, coeff_x0, coeff_y0, coeff_xy0, const0 = eq_param_list[0][0], eq_param_list[0][1], \
                                                                          eq_param_list[0][2], eq_param_list[0][3], \
                                                                          eq_param_list[0][4], eq_param_list[0][5]
        coeff_x_sq1, coeff_y_sq1, coeff_x1, coeff_y1, coeff_xy1, const1 = eq_param_list[1][0], eq_param_list[1][1], \
                                                                          eq_param_list[1][2], eq_param_list[1][3], \
                                                                          eq_param_list[1][4], eq_param_list[1][5]
        coeff_x_sq2, coeff_y_sq2, coeff_x2, coeff_y2, coeff_xy2, const2 = eq_param_list[2][0], eq_param_list[2][1], \
                                                                          eq_param_list[2][2], eq_param_list[2][3], \
                                                                          eq_param_list[2][4], eq_param_list[2][5]
        if x_value_counter > 0:
            y_value0_prev = y_value0
            y_value1_prev = y_value1
            y_value2_prev = y_value2
        y_value0 = (coeff_x_sq0*pow(x_value,2)+coeff_x0*(x_value)+const0)/(-1.0*coeff_y0)
        y_value1 = (coeff_x_sq1 * pow(x_value, 2) + coeff_x1 * (x_value) + const1) / (-1.0 * coeff_y1)
        y_value2 = (coeff_x_sq2 * pow(x_value, 2) + coeff_x2 * (x_value) + const2) / (-1.0 * coeff_y2)
        if x_value_counter == 0:
            lowest_parabola_start_x.append(x_value)
            if y_value0 < y_value1 and y_value0 < y_value2 and y_value0 > sweepline_y_value:
                lowest_parabola_index.append(0)
            elif y_value1 < y_value0 and y_value1 < y_value2 and y_value1 > sweepline_y_value:
                lowest_parabola_index.append(1)
            elif y_value2 < y_value0 and y_value2 < y_value1 and y_value2 > sweepline_y_value:
                lowest_parabola_index.append(2)
            elif sweepline_y_value > y_value0 and sweepline_y_value > y_value1:
                lowest_parabola_index.append(2)
            elif sweepline_y_value > y_value1 and sweepline_y_value > y_value2:
                lowest_parabola_index.append(0)
            elif sweepline_y_value > y_value0 and sweepline_y_value > y_value2:
                lowest_parabola_index.append(1)

        if abs(y_value1 - y_value2) < epsilon and abs(y_value0 - y_value2) < epsilon \
                and y_value1 > sweepline_y_value:

            intersection_points_x.append(x_value)
            intersection_points_y.append(y_value1)

            lowest_intersection_points_x.append(x_value)
            lowest_intersection_points_y.append(y_value1)

            if y_value0_prev > y_value1_prev and y_value0_prev > y_value2_prev:
                lowest_parabola_index.append(0)
            elif y_value1_prev > y_value0_prev and y_value1_prev > y_value2_prev:
                lowest_parabola_index.append(1)
            elif y_value2_prev > y_value0_prev and y_value2_prev > y_value1_prev:
                lowest_parabola_index.append(2)

            lowest_parabola_end_x.append(x_value)
            lowest_parabola_start_x.append(x_value)

        elif abs(y_value0 - y_value1)<epsilon and y_value0 > sweepline_y_value:
            intersection_points_x.append(x_value)
            intersection_points_y.append(y_value0)
            if y_value2 > y_value0:
                lowest_intersection_points_x.append(x_value)
                lowest_intersection_points_y.append(y_value0)
                if lowest_parabola_index[len(lowest_parabola_index)-1] == 0:
                    lowest_parabola_index.append(1)
                else:
                    lowest_parabola_index.append(0)
                lowest_parabola_end_x.append(x_value)
                lowest_parabola_start_x.append(x_value)

        elif abs(y_value0 - y_value2)<epsilon and y_value0 > sweepline_y_value:
            intersection_points_x.append(x_value)
            intersection_points_y.append(y_value0)

            if y_value1 > y_value0:
                lowest_intersection_points_x.append(x_value)
                lowest_intersection_points_y.append(y_value0)
                if lowest_parabola_index[len(lowest_parabola_index)-1] == 0:
                    lowest_parabola_index.append(2)
                else:
                    lowest_parabola_index.append(0)
                lowest_parabola_end_x.append(x_value)
                lowest_parabola_start_x.append(x_value)

        elif abs(y_value1 - y_value2)<epsilon and y_value1 > sweepline_y_value:
            intersection_points_x.append(x_value)
            intersection_points_y.append(y_value1)
            # lowest_parabola_end_x.append(x_value)
            if y_value0 > y_value1:
                lowest_intersection_points_x.append(x_value)
                lowest_intersection_points_y.append(y_value1)
                if lowest_parabola_index[len(lowest_parabola_index)-1] == 1:
                    lowest_parabola_index.append(2)
                else:
                    lowest_parabola_index.append(1)
                lowest_parabola_end_x.append(x_value)
                lowest_parabola_start_x.append(x_value)

        if y_value0 < y_value1 and y_value0 < y_value2:
            lowest_parabola_index_at_rightmost_point = 0
        elif y_value1 < y_value0 and y_value1 < y_value2:
            lowest_parabola_index_at_rightmost_point = 1
        else:
            lowest_parabola_index_at_rightmost_point = 2
        x_value_counter += 1

    lowest_parabola_end_x.append(x_value)
    # lowest_parabola_index.append(lowest_parabola_index_at_rightmost_point)

    print(f'intersection_points_x:{intersection_points_x}')
    print(f'intersection_points_y:{intersection_points_y}')

    print(f'lowest_intersection_points_x:{lowest_intersection_points_x}')
    print(f'lowest_intersection_points_y:{lowest_intersection_points_y}')

    # exit(0)
    curve_index = 0
    for eq_params in eq_param_list:
        segment_index = 0
        print(eq_params)
        coeff_x_sq, coeff_y_sq, coeff_x, coeff_y, coeff_xy, const = eq_params[0],eq_params[1],eq_params[2],eq_params[3],eq_params[4],eq_params[5]
        print(f'equation of parabola is {coeff_x_sq}x^2 {("-" if coeff_y_sq < 0.0 else "+")}{abs(coeff_y_sq)}y^2 '
              f'{("-" if coeff_x < 0.0 else "+")}{abs(coeff_x)}x {("-" if coeff_y < 0.0 else "+")}{abs(coeff_y)}y '
              f'{("-" if coeff_xy < 0.0 else "+")}{abs(coeff_xy)}xy {("-" if const < 0.0 else "+")}{abs(const)} = 0')

        # 3. Calculate the corresponding y-values
        y_values = (coeff_x_sq*pow(x_values,2)+coeff_x*(x_values)+const)/(-1.0*coeff_y) #equation(x_values)
        # plt.plot(x_values, y_values, label='$y = x^2$', color='blue')
        for i in range(0, (len(x_values)-1)):
            # print(f'lowest_parabola_index:{lowest_parabola_index}')
            # print(f'lowest_parabola_start_x:{lowest_parabola_start_x}')
            # print(f'lowest_parabola_end_x:{lowest_parabola_end_x}')
            if x_values[i] > lowest_parabola_end_x[segment_index]:
                segment_index += 1
            if min(y_values) > sweepline_y_value:
                if x_values[i] >= lowest_parabola_start_x[segment_index] and x_values[i] <= lowest_parabola_end_x[segment_index] and \
                    lowest_parabola_index[segment_index] == curve_index:
                    plt.plot(x_values[i:i+2], y_values[i:i+2], color=colors[curve_index])#'blue')
                else:
                    plt.plot(x_values[i:i+2], y_values[i:i+2], color=colors[curve_index])#'gray')
        y_values_list.append(y_values)
        curve_index += 1


    points_array = np.array(focii)
    xpoints, ypoints = points_array.T
    # xpoints = np.array([1, 2, 6, 8])
    # ypoints = np.array([1, 2, 6, 8])
    focus_index = 0
    for focus in focii:
        plt.plot(focus[0], focus[1], 'o', color=colors[focus_index])
        focus_index += 1

    plt.plot(intersection_points_x, intersection_points_x, 'X', color='black')

    y_values = (a*x_values+c)/(-1.0*b)
    plt.plot(x_values, y_values, color='black')
    # 4. Plot the data



    plt.title("Plot of beachline")
    plt.xlabel("X-axis")
    plt.ylabel("Y-axis")

    # for xy in focii:
    #     ax.annotate('(%s, %s)' % xy, xy=xy, textcoords='data')
    focus_index=0
    subscript_list = ['i','j','k']
    for xy in focii:
        ax.annotate('$p_{}$'.format(subscript_list[focus_index]),
                    xytext = (0,8), xy=xy, textcoords='offset points') #xy='$p_{}$'.format(focus_index))#focus_index, xy=xy)
        focus_index += 1

    xy = (intersection_points_x[0],intersection_points_y[0])
    ax.annotate('$VV_{i,j,k}$',
                xytext=(0, 8), xy=xy,
                textcoords='offset points')  # xy='$p_{}$'.format(focus_index))#focus_index, xy=xy)

    xy = (0, y_values[0])
    ax.annotate('$sweep line$',
                xytext=(-16, 5), xy=xy,
                textcoords='offset points')

    #draw perpendicular bisectors
    bisector_len = 1
    for i in range(0, 3):
        x1, y1 = focii[i]
        x2, y2 = focii[(i+1) % 3]

        # 1. Calculate the midpoint
        m_x = (x1 + x2) / 2.0
        m_y = (y1 + y2) / 2.0

        # 2. Calculate the slope of the original line
        # Handle the case of a vertical line to avoid division by zero
        if x2 - x1 == 0:
            # Perpendicular bisector is a horizontal line
            slope_perp = 0
            # Define two points for the horizontal bisector
            x_values = [m_x - bisector_len, m_x + bisector_len]
            y_values = [m_y, m_y]
        else:
            slope = (y2 - y1) / (x2 - x1)
            # Handle the case of a horizontal line (slope = 0)
            if slope == 0:
                # Perpendicular bisector is a vertical line
                # Define two points for the vertical bisector
                x_values = [m_x, m_x]
                y_values = [m_y - bisector_len, m_y + bisector_len]
            else:
                # 3. Calculate the perpendicular slope (negative reciprocal)
                slope_perp = -1 / slope
                # 4. Define points for the bisector line using its equation: y = m_perp * (x - m_x) + m_y
                if i == 0:
                    x_values = np.array([-3, intersection_points_x[0]])
                    # x_values = np.array([m_x - bisector_len, m_x + bisector_len])
                    y_values = slope_perp * (x_values - m_x) + m_y
                elif i == 1:
                    x_values = np.array([intersection_points_x[0], 4])
                    # x_values = np.array([m_x - bisector_len, m_x + bisector_len])
                    y_values = slope_perp * (x_values - m_x) + m_y
                else:
                    x_values = np.array([intersection_points_x[0], 1])
                    # x_values = np.array([m_x - bisector_len, m_x + bisector_len])
                    y_values = slope_perp * (x_values - m_x) + m_y

        # Plot the original line segment
        # ax.plot([x1, x2], [y1, y2], '-', color='gray', linewidth=1)
        # Plot the perpendicular bisector
        ax.plot(x_values, y_values, '-.', color='gray', linewidth=1)

    plt.legend()
    # plt.grid(True)  # Optional: add a grid
    #plt.ax('equal')
    ax.set_aspect('equal', adjustable='box')
    # plt.axis.set_xlim(xmin=0, xmax=1000)
    # 5. Display the plot
    # plt.show()
    plt.savefig('voronoiNeighbor/'+'VoronoiPartitionVertexFormation.svg', format='svg')

# def annotate(ax, label, x, y, xytext):
#     ax.annotate(label, xy=(x,y),
#                 xytext=xytext, textcoords='offset points',
#                 fontsize=15,
#                 arrowprops={'arrowstyle': '-|>', 'color': 'black'})

# def annotate(ax, label, focus_index):
#     ax.annotate(label, xy = '$p_{}$'.format(focus_index),
#                 xytext = (5,5),
#                 textcoords='offset points',
#                 fontsize=15,
#                 arrowprops={'arrowstyle': '-|>', 'color': 'black'})

if __name__ == "__main__":
    # focii = [(0,2),(3,7),(5,4)]
    focii = [(-6, 2), (1, 7), (6, 4)]
    eq_param_list = []
    # a, b, c = 0, 1, -1.29
    a, b, c = 0, 1, 6.2428
    # equation_parabola(x1, y1, a, b, c)
    for focus in focii:
        print(focus)
        coeff_x_sq, coeff_y_sq, coeff_x, coeff_y, coeff_xy, const = equation_parabola(focus[0], focus[1], a, b, c)
        eq_param_list.append([coeff_x_sq, coeff_y_sq, coeff_x, coeff_y, coeff_xy, const])
    plot_parabola(focii, eq_param_list, a, b, c) #coeff_x_sq, coeff_y_sq, coeff_x, coeff_y, coeff_xy, const)
    # This code is contributed by Ryuga