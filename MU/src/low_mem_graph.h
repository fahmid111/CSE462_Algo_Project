// Copyright (c) 2021 Gabriel Bathie
// Copyright (c) 2021 Ulysse  Prieto
// This code is licensed under the terms of the MIT License (MIT).
#pragma once
#include "constants.h"

#include <vector>
#include <string>
#include <iostream>
#include <fstream>
#include <algorithm>

using namespace std;

/* Graph class with kernelization
 */
class LowMemGraph
{
private:
	vector<vector<int>> adjs; // Adj lists
	int64_t _n;
	int64_t _m;

	void add_edge(int u, int v);
	void sort_adjs();
public:
	LowMemGraph() {};
	LowMemGraph(int n, const vector<Edge> &edges);

	inline int64_t n() const { return _n; }
	inline int64_t m() const { return _m; }
	inline const vector<int> &neighbors(int u) const { return adjs[u]; }
	inline int64_t deg(int u) const { return adjs[u].size(); }
	inline bool adjacent(int u, int v) const { return binary_search(adjs[u].begin(), adjs[u].end(), v); }

	// Kernelization
	bool remove_excess_degree_one();
	bool disjoint_neighborhoods(int u, int v);
	bool remove_edge_disjoint_neighbors();
	bool remove_C4();
	int neighborhoods_intersection_size(int u, int v);
	void remove_edge(int u, int v);
	bool remove_deg3_triangles();
	bool isolate_small_complete(int s);
	bool isolate_small_complete2(int s);
	
	int64_t kernelize();

	vector<vector<int>> connected_components();
	
	// Does not handle comments
	static LowMemGraph from_istream(istream &is);
	static LowMemGraph from_cin();
	static LowMemGraph from_file(const string &fname);
};