# `µSolver`: Heuristic cluster editing solver 

[PACE Challenge 2021](https://pacechallenge.org/2021/) submission for the Heuristic Track, by Gabriel Bathie, Ulysse Prieto, Valentin Bartier, Nicolas Bousquet, Marc Heinrich, and Théo Pierron.

## Dependencies, compilation and usage

This program requires `c++17`, `g++ 9.0` or higher and a not too old version of `make`. 
The only library that is uses is the STL.

To compile `mu_solver`, simply type `make`. The resulting executable is named `main`.
You can then use it as follows:

```bash
./main < graph_file.gr
```

Here, `graph_file.gr` contains the description of a graph, given in the [DIMACS-like `.gr` format](https://pacechallenge.org/2021/tracks/#input-format).  
The program then runs until it receives a `SIGTERM` or `SIGKILL` (CTRL+C) signal. When it terminates, it prints within a few seconds on the standard output the best cluster editing that it found.

## Description of the algorithm

See `solver_description.pdf`. [![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.4947325.svg)](https://doi.org/10.5281/zenodo.4947325)

