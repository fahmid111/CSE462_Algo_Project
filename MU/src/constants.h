// Copyright (c) 2021 Gabriel Bathie
// Copyright (c) 2021 Ulysse  Prieto
// This code is licensed under the terms of the MIT License (MIT).
#pragma once

#include <vector>
#include <random>
#include "fast_rng.h"

using Node = int; 
using Edge = std::pair<int, int>;
using Cluster = std::vector<Node>;
using Labeling = std::vector<int>;
using ConnectedComponent = std::vector<Node>;

using RandomEngine = fast_rng::xor_shift;
