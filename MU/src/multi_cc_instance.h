// Copyright (c) 2021 Gabriel Bathie
// Copyright (c) 2021 Ulysse  Prieto
// This code is licensed under the terms of the MIT License (MIT).
#pragma once

#include "low_mem_graph.h"
#include "instance.h"
#include "constants.h"

#include <vector>
#include <functional>
#include <string>

using namespace std;

// Handle an instance divided in multiple CCs
// Output a solution in O(m) instead of O(n²)
class KernelizedMultiCCInstance
{
private:
	vector<Edge> initial_edges;
	vector<Instance> cc_instances;
	vector<ConnectedComponent> ccs; // ccs[i]: list of the vertices in the i-th connected component.
	vector<pair<int,int>> vertex_to_cc;
	vector<Labeling> solutions;		// labeling of each CC
	vector<int64_t> costs;
	int64_t total_cost;

public:
	KernelizedMultiCCInstance(int n, const vector<Edge> &edges);

	int64_t cost() 	const { return total_cost; }
	int64_t m() 	const { return initial_edges.size(); }
	int64_t _n_cc() const { return cc_instances.size(); }
	const vector<ConnectedComponent> &_ccs() const { return ccs; }
	vector<Instance> &i_ccs() { return cc_instances; }

	void greedy_bfs_fill(
		function<bool(int)> exit_condition,
		vector<int> &ndestroy_options,
		int default_weight);

	void print_sol();
	vector<Cluster> get_sol();
	int64_t count_sol();

	static KernelizedMultiCCInstance from_istream(istream &is);
	static KernelizedMultiCCInstance from_cin();
	static KernelizedMultiCCInstance from_file(const string &fname);
};
