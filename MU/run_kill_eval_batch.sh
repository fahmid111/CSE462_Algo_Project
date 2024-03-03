#!/bin/bash

for file in heur/*; do
    echo "-------------------"
    echo $file
    ./run_kill.sh "$file" $1 > solution.txt

    #sleep for 30 seconds to let the file be written
    sleep 30
    
    verifier/verifier "$file" solution.txt
    #print how many lines are in the file
    wc -l solution.txt
    #read out the first line
    # head -n 1 solution.txt
done
