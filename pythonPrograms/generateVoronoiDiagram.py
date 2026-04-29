import numpy as np
import matplotlib.pyplot as plt
from scipy.spatial import Voronoi, voronoi_plot_2d

def int_to_alphabet(num):
    """
    Convert a positive integer to its corresponding alphabetical representation.
    Example: 1 -> A, 26 -> Z, 27 -> AA
    """
    if not isinstance(num, int) or num <= 0:
        raise ValueError("Input must be a positive integer.")

    result = ""
    while num > 0:
        num -= 1  # Adjust for 1-based indexing
        result = chr(num % 26 + ord('A')) + result
        num //= 26
    return result

def draw_voronoi_with_labels(num_points, random_seed, label_offset):
    """
    Generates random points, draws a Voronoi diagram, and labels the sites.

    Args:
        num_points (int): The number of random points (sites) to generate.
        random_seed (int): The seed for the random number generator for reproducibility.
        label_offset (tuple): A (dx, dy) tuple for the label offset.
    """
    # Set the random seed for reproducibility
    np.random.seed(random_seed)

    # Generate random points in a 2D space (e.g., between 0 and 100)
    points = np.random.rand(num_points, 2) * 100

    # Compute the Voronoi diagram
    vor = Voronoi(points)

    # Plot the Voronoi diagram using scipy's helper function
    fig, ax = plt.subplots(figsize=(8, 8))
    voronoi_plot_2d(vor, ax=ax, show_points=False, show_vertices=False, line_colors='lightblue', line_width=0.85)

    # Plot the original sites as red dots
    ax.plot(points[:, 0], points[:, 1], 'o', color='red', markersize=3)
    mobile_x = 0.0
    mobile_y = 0.0
    # Add labels (A, B, C, ...) with an offset
    for i, point in enumerate(points):
        label = int_to_alphabet(i+1) #chr(ord('A') + i)
        modified_label_offset=label_offset
        if point[1]+label_offset[1] > 100.0:
            #print(label)
            modified_label_offset = (-2.0*label_offset[0], -2.0*label_offset[1])
            #exit(0)
        ax.annotate(label,
                    (point[0], point[1]),
                    textcoords="offset points",
                    xytext=modified_label_offset,
                    ha='center',
                    fontsize=8,
                    color='black')

        if i==22:
            mobile_x = point[0] - 4.0
            mobile_y = point[1] - 3.0
            ax.plot(mobile_x, mobile_y, 'x', color='green', markersize=3)
            ax.annotate('Mob',
                        (mobile_x, mobile_y),
                        textcoords="offset points",
                        xytext=label_offset,
                        ha='center',
                        fontsize=8,
                        color='purple')


    # Customize the plot
    ax.set_xlim(0, 100)
    ax.set_ylim(0, 100)
    ax.set_xlabel("X coordinate")
    ax.set_ylabel("Y coordinate")
    ax.set_title(f"Voronoi Diagram with {num_points} Labeled Sites")
    # ax.grid(True)

    # Display the plot
    # plt.show()
    plt.savefig('voronoiNeighbor/' + 'VoronoiDiagram.svg', format='svg')


# --- Example Usage ---
# Generate 10 random sites using seed 42, with a label offset of (5, 5) points
draw_voronoi_with_labels(num_points=45, random_seed=12, label_offset=(2.5, 2.5))
