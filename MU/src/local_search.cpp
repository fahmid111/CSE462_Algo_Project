// Copyright (c) 2021 Gabriel Bathie
// Copyright (c) 2021 Ulysse  Prieto
// This code is licensed under the terms of the MIT License (MIT).
#include "local_search.h"

#include <algorithm>

bool bfs_destroy_repair_ls_one_it_order_norand(
	Instance &g, int n, int ndestroy, RandomEngine &rng,
	vector<int> &order, vector<bool> &seen,
	vector<int> &best_sol, int64_t &best_cost, bool same_zero_size)
{
	vector<int> vs;
	vector<int> old_cluster_of_vs;

	fill(seen.begin(), seen.end(), false);
	shuffle(order.begin(), order.end(), rng);

	bool res = false;

	for (int i = 0; i < n; ++i)
	{
		if (seen[order[i]])
			continue;

		vs.clear();
		old_cluster_of_vs.clear();
		g.bfs_fill_vs(order[i], ndestroy, vs, old_cluster_of_vs, seen);

		if (vs.size() == 0)
			continue;

		shuffle(vs.begin(), vs.end(), rng);
		for (size_t j = 0; j < vs.size(); ++j)
			old_cluster_of_vs[j] = g.sol()[vs[j]];

		int64_t old_cost = g.cost();
		g.destroy_greedy_repair(vs, same_zero_size);

		if (g.cost() < best_cost)
		{
			res = true;
			best_cost = g.cost();
			best_sol = g.sol();
		}
		
		if (g.cost() > old_cost)
			g.revert_cluster_of_with_cost(vs, old_cluster_of_vs, old_cost);
	}
	return res;
}
