// Copyright (c) 2021 Gabriel Bathie
// Copyright (c) 2021 Ulysse  Prieto
// This code is licensed under the terms of the MIT License (MIT).
#pragma once
#include "instance.h"
#include "constants.h"

#include <vector>

bool bfs_destroy_repair_ls_one_it_order_norand(
	Instance &g, int n, int ndestroy, RandomEngine &rng,
	vector<int> &order, vector<bool> &seen,
	vector<int> &best_sol, int64_t &best_cost, bool same_zero_size = false);