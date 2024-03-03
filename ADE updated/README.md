### PACE 2021 - Exact Track
This repository contains the source code for the cluster editing solver for the exact track
of the [PACE 2021](https://pacechallenge.org/2021/) competition.

#### External Dependencies:
* no external dependencies are required

#### Build instructions (Linux)
* a jdk version >= 1.8 is required to compile the source code
* running `make` will create a jar archive `ade-ce-solver.jar`
* it is recommended to increase the jvm stack size when invoking the solver to prevent StackOverflowExceptions `java -Xss8m -jar ade-ce-solver-jar < <path to graph>`

#### Solver description
* [Solver description](https://doi.org/10.5281/zenodo.4960094)

#### Authors
* Alexander Bille (Araxon)
* Dominik Brandenstein (dobrand)
* Emanuel Herrendorf (EmanuelHerrendorf)
