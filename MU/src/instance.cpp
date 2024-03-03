// Copyright (c) 2021 Gabriel Bathie
// Copyright (c) 2021 Ulysse  Prieto
// This code is licensed under the terms of the MIT License (MIT).
#include "instance.h"

#include <queue>
#include <algorithm>
#include <random>

Instance::Instance(int64_t _n, const vector<Edge> &edges):
	g(_n, edges), 
	cluster_of(g.n(), 0), cluster_size(g.n(), 0), candidate_clusters_adj(g.n(), 0), n(g.n())
{
	cluster_size[0] = n;
	for (int i = 1; i < n; ++i)
		zero_size_cluster.push_back(i);
	_cost = ((int64_t)n * (n - 1)) / 2 - g.m();
}

void Instance::reinit_all_zero()
{
	fill(cluster_size.begin(), cluster_size.end(), 0);
	fill(cluster_of.begin(), cluster_of.end(), 0);
	cluster_size[0] = n;

	zero_size_cluster.clear();
	for (int i = 1; i < n; ++i)
		zero_size_cluster.push_back(i);
	_cost = ((n * (n - 1)) / 2) - g.m();
}


void Instance::reinit_state(const vector<int> &v, int64_t cost)
{
	for (int i = 0; i < n; ++i)
		cluster_size[i] = 0;
	for (int i = 0; i < n; ++i)
	{
		cluster_of[i] = v[i];
		cluster_size[v[i]]++;
	}
	_cost = cost;
	zero_size_cluster.clear();
	for (int i = 0; i < n; ++i)
	{
		if (cluster_size[i] == 0)
			zero_size_cluster.push_back(i);
	}
}

// amortized management of zero_size clusters
int Instance::get_zero_size()
{
	int res = zero_size_cluster.back();
	
	while (cluster_size[res] != 0 && !zero_size_cluster.empty())
	{
		zero_size_cluster.pop_back();
		res = zero_size_cluster.back();
	}
	return res;
}

// Move v and handle zero size clusters changes
void Instance::_move(int v, int c)
{
	int cv = cluster_of[v];
	if (cv == c)
		return;

	cluster_of[v] = c;
	cluster_size[cv]--;
	cluster_size[c]++;

	if (cluster_size[cv] == 0)
		zero_size_cluster.push_back(cv);

}

// Move v to the cluster that reduces the most the cost, if any.
// Returns True iff there was a move.
// O(d(v))
bool Instance::greedy_move(int v)
{
	int cu, cv = cluster_of[v];

	for (int u : g.neighbors(v))
	{
		cu = cluster_of[u];
		++candidate_clusters_adj[cu];
	}

	int64_t self_edges = candidate_clusters_adj[cv];
	int64_t best_cost = 0, cost;
	int best_cluster = -1;
	int64_t self_cost = -(cluster_size[cv] - 1 - 2 * self_edges);
	for (int u : g.neighbors(v))
	{
		cu = cluster_of[u];
		if (cu == cv)
			continue;
		cost = (cluster_size[cu] - 2 * candidate_clusters_adj[cu]) + self_cost;
		if (cost < best_cost)
		{
			best_cost = cost;
			best_cluster = cu;
		}
	}

	// Reset the content of candidate_clusters_adj
	for (int u : g.neighbors(v))
	{
		cu = cluster_of[u];
		candidate_clusters_adj[cu] = 0;
	}

	// Check whether it is better to put `v` in an empty cluster
	if (self_cost < best_cost && !zero_size_cluster.empty())
	{
		best_cost = self_cost;
		best_cluster = get_zero_size();
	}

	if (best_cluster == -1)
		return false;

	_move(v, best_cluster);
	_cost += best_cost;

	return true;
}

// Compute the cost of moving v to c
// O(d(v)), unless c is the cluster of v, in which case it is O(1)
int64_t Instance::delta_cost(int v, int c)
{
	int cv = cluster_of[v];
	if (cv == c)
		return 0;
	
	int64_t self_edges = 0, to_edges = 0;
	for (int u : g.neighbors(v))
	{
		if (cluster_of[u] == cv)
			++self_edges;
		if (cluster_of[u] == c)
			++to_edges;
	}

	return (cluster_size[c] - 2 * to_edges) - (cluster_size[cv] - 1 - 2 * self_edges);
}

void Instance::move_with_delta(int v, int c, int64_t delta)
{
	_move(v, c);
	_cost += delta;
}

void Instance::move_to_zero_size(const vector<int> &vs)
{
	for (int v : vs)
	{
		if (cluster_size[cluster_of[v]] == 1)
			continue;
		int c = get_zero_size();
		move_with_delta(v, c, delta_cost(v, c));
	}
}

void Instance::move_to_same_zero_size(const vector<int> &vs)
{
	bool first_alone = cluster_size[cluster_of[vs[0]]] == 1;
	int c = first_alone ? cluster_of[vs[0]] : get_zero_size();
	for (size_t i = first_alone; i < vs.size(); ++i)
	{
		int v = vs[i];
		move_with_delta(v, c, delta_cost(v, c));
	}
}

void Instance::revert_cluster_of_with_cost(const vector<int> &vs, const vector<int> &old_cluster_of_vs, int64_t old_cost)
{
	for (size_t i = 0; i < vs.size(); ++i)
	{
		int v = vs[i], c = old_cluster_of_vs[i];
		_move(v, c);
	}
	_cost = old_cost;
}

void Instance::destroy_greedy_repair(const vector<int> &vs, bool same_zero_size)
{	
	same_zero_size ? move_to_same_zero_size(vs) : move_to_zero_size(vs);

	for (int v : vs)
		greedy_move(v);
}

void Instance::bfs_fill_vs(int v, unsigned int nv, vector<int> &vs, vector<int> &cluster_of_vs, vector<bool> &seen) const
{
	queue<int> q;
	q.push(v);
	while (!q.empty() && vs.size() < nv)
	{
		v = q.front(); q.pop();
		if (seen[v])
			continue;
		seen[v] = true;

		vs.push_back(v);
		cluster_of_vs.push_back(cluster_of[v]);
		for (int u : g.neighbors(v))
			q.push(u);
	}
}

Instance Instance::from_istream(istream &is)
{
	int n, m, u, v;
	string s;
	is >> s >> s; // ignore header
	is >> n >> m;

	vector<Edge> edges;
	edges.reserve(m);
	for (int i = 0; i < m; ++i)
	{
		is >> u >> v;
		// In our data structure, vertices are 0-indexed
		edges.emplace_back(u - 1, v - 1); 
	}

	return Instance(n, edges);
}

Instance Instance::from_cin()
{
	return from_istream(cin);
}

Instance Instance::from_file(const string &fname)
{

	ifstream graph_file(fname);
	return from_istream(graph_file);
}
