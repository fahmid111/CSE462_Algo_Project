#!/bin/bash
input=$1

java -Xss8m -jar ade-ce-solver_original.jar < exact/$input > original_graph.txt
java -Xss8m -jar ade-ce-solver.jar < exact/$input > modified_graph.txt
# Check if correct number of arguments are provided



# Copy contents excluding the first line
tail -n +2 exact/$input > main_graph.txt

./output