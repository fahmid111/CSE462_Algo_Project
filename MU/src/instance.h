// Copyright (c) 2021 Gabriel Bathie
// Copyright (c) 2021 Ulysse  Prieto
// This code is licensed under the terms of the MIT License (MIT).
#pragma once

#include "low_mem_graph.h"
#include "constants.h"

#include <vector>
#include <functional>
#include <string>

using namespace std;

class Instance
{
private:	
	LowMemGraph g;
	vector<int> cluster_of;
	vector<int64_t> cluster_size;
	vector<int> zero_size_cluster;
	vector<int64_t> candidate_clusters_adj;
	int64_t _cost;

	void _move(int v, int c);
public:
	int64_t n;
	Instance() {};
	Instance(int64_t _n, const vector<Edge> &edges);

	inline int64_t m() { return g.m(); }
	inline const vector<int> &neighbors(int u) const { return g.neighbors(u); }
	inline int64_t cost() const { return _cost; }
	inline const vector<int> &sol() const { return cluster_of; }

	void reinit_all_zero();
	void reinit_state(const vector<int> &v, int64_t cost);

	
	int get_zero_size();

	// Move v to the cluster that reduces the most the cost, if any.
	// Returns True iff there was a move.
	// O(d(v))
	bool greedy_move(int v);

	// Cost of moving v to c
	// O(d(v)), unless c is the cluster of v, in which case it is O(1)
	int64_t delta_cost(int v, int c);

	void move_with_delta(int v, int c, int64_t delta);
	void move_to_zero_size(const vector<int> &vs);
	void move_to_same_zero_size(const vector<int> &vs);
	void revert_cluster_of_with_cost(const vector<int> &vs, const vector<int> &old_cluster_of_vs, int64_t old_cost);
	void destroy_greedy_repair(const vector<int> &vs, bool same_zero_size = false);

	// Fills vs and cluster_of_vs starting from vertex v and adding
	// vertices in a BFS manner. Stops when no more vertices can be
	// added or when nv are added.
	// vs and cluster_of_vs must be cleared prior to calling bfs_fill_vs
	// seen must be filled with false
	void bfs_fill_vs(int v, unsigned int nv, vector<int> &vs, vector<int> &cluster_of_vs, vector<bool> &seen) const;

	static Instance from_istream(istream &is);
	static Instance from_cin();
	static Instance from_file(const string &fname);

};
