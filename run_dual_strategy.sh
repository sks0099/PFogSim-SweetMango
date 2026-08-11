#!/bin/bash
# run_dual_strategy.sh
#
# Added by sks0099@auburn.edu.
#
# Runs the full IMPROVED_SINGLE_LAYER_VORONOI simulation twice - once with
# preferredHostIndexAscending_for_slv=false (edge/lowest-MIPS-first) and once with =true
# (cloud/highest-MIPS-first) - using whatever device count/scenario settings are already set
# in default_config.properties, then compares the resulting task failure percentage and
# leaves default_config.properties set to whichever strategy produced the lower failure rate.
#
# Background: at 1000 devices, cloud-first cut the failure rate ~32x (0.33% vs 10.77%),
# because that scale is latency-bound and higher-MIPS hosts process fast enough to compensate
# for being farther away. At 6000 devices the result reverses (10.46% vs 1.66%) because the
# small high-MIPS tiers (e.g. University: 10 hosts, Ward: 50 hosts) run out of raw capacity.
# Neither strategy is a safe universal default, so this script re-decides per configuration
# instead of hardcoding one.
#
# Usage: ./run_dual_strategy.sh
# Only preferredHostIndexAscending_for_slv is varied between the two runs; every other setting
# (device count, iteration_number, etc.) is read as-is from the current config and left
# unchanged except for the winning ascending value.
#
# Cost: this runs the ENTIRE simulation twice (not just the assignment phase), so total time
# is roughly 2x a single run - at 6000 devices in this project that's been observed to take
# ~7-19 minutes per run depending on which strategy wins, so ~30 minutes combined.

set -e

CONFIG="scripts/sample_application/config/default_config.properties"
JAVA_HOME_BIN="/c/Program Files/Java/jdk1.8.0_202/bin"
CP="./bin;lib/cloudsim-4.0.jar;lib/colt.jar;lib/commons-collections4-4.4.jar;lib/commons-math3-3.6.1.jar;lib/json-simple-1.1.1.jar;lib/opencsv-5.12.0.jar"

if [ ! -f "$CONFIG" ]; then
    echo "Error: $CONFIG not found. Run this script from the project root." >&2
    exit 1
fi

ORIGINAL_CONFIG_BACKUP=$(mktemp)
cp "$CONFIG" "$ORIGINAL_CONFIG_BACKUP"

cleanup() {
    rm -f "$ORIGINAL_CONFIG_BACKUP"
}
trap cleanup EXIT

set_ascending() {
    local value=$1
    sed -i "s/^preferredHostIndexAscending_for_slv.*\$/preferredHostIndexAscending_for_slv = ${value}/" "$CONFIG"
}

run_and_capture() {
    local ascending=$1
    local logfile=$2
    set_ascending "$ascending"
    "$JAVA_HOME_BIN/java" -cp "$CP" edu.boun.edgecloudsim.sample_application.mainApp > "$logfile" 2>&1
}

extract_failed_pct() {
    grep "percentage of failed tasks" "$1" | tail -1 | grep -oE '[0-9]+\.[0-9]+'
}

# The "Output folder created in <path>" line only gives the shared base path (e.g.
# sim_results/ite10), not the unique per-run timestamped subfolder actually holding this run's
# SIMRESULT/CSV/JSON files - so find that subfolder directly by looking for whichever one under
# the base path was created most recently, immediately after each run finishes.
extract_output_folder() {
    local base
    base=$(grep "Output folder created in" "$1" | tail -1 | sed 's/.*Output folder created in //')
    find "$base" -maxdepth 1 -mindepth 1 -type d 2>/dev/null | sort | tail -1
}

LOG_FALSE=$(mktemp)
LOG_TRUE=$(mktemp)

echo "=== Run 1/2: preferredHostIndexAscending_for_slv = false (edge/lowest-MIPS-first) ==="
run_and_capture false "$LOG_FALSE"
FAILED_FALSE=$(extract_failed_pct "$LOG_FALSE")
FOLDER_FALSE=$(extract_output_folder "$LOG_FALSE")
echo "  Failed: ${FAILED_FALSE}%   Results: ${FOLDER_FALSE}"

echo ""
echo "=== Run 2/2: preferredHostIndexAscending_for_slv = true (cloud/highest-MIPS-first) ==="
run_and_capture true "$LOG_TRUE"
FAILED_TRUE=$(extract_failed_pct "$LOG_TRUE")
FOLDER_TRUE=$(extract_output_folder "$LOG_TRUE")
echo "  Failed: ${FAILED_TRUE}%   Results: ${FOLDER_TRUE}"

echo ""
echo "=== Decision ==="
echo "edge-first  (false): ${FAILED_FALSE}% failed"
echo "cloud-first (true):  ${FAILED_TRUE}% failed"

WINNER=$(awk -v a="$FAILED_FALSE" -v b="$FAILED_TRUE" 'BEGIN { print (b < a) ? "true" : "false" }')

if [ "$WINNER" = "true" ]; then
    echo "Winner: cloud-first (true) - ${FAILED_TRUE}% < ${FAILED_FALSE}%"
    WINNER_FOLDER="$FOLDER_TRUE"
else
    echo "Winner: edge-first (false) - ${FAILED_FALSE}% <= ${FAILED_TRUE}%"
    WINNER_FOLDER="$FOLDER_FALSE"
fi

cp "$ORIGINAL_CONFIG_BACKUP" "$CONFIG"
set_ascending "$WINNER"

echo ""
echo "default_config.properties updated: preferredHostIndexAscending_for_slv = ${WINNER}"
echo "Winning run's full results: ${WINNER_FOLDER}"
echo "(Losing run's results are also preserved at: $([ "$WINNER" = "true" ] && echo "$FOLDER_FALSE" || echo "$FOLDER_TRUE"))"

rm -f "$LOG_FALSE" "$LOG_TRUE"
