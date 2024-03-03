// Copyright (c) 2021 Gabriel Bathie
// Copyright (c) 2021 Ulysse  Prieto
// This code is licensed under the terms of the MIT License (MIT).
#include "low_mem_graph.h"
#include "union_find.h"
#include <set>

LowMemGraph::LowMemGraph(int n, const vector<Edge> &edges) : adjs(n), _n(n), _m(0)
{
	for (auto &[u, v] : edges)
		add_edge(u, v);

	sort_adjs();
}

void LowMemGraph::add_edge(int u, int v)
{
	adjs[u].push_back(v);
	adjs[v].push_back(u);
	++_m;
}

void LowMemGraph::sort_adjs()
{
	for (int i = 0; i < _n; ++i)
		sort(adjs[i].begin(), adjs[i].end());
}

bool LowMemGraph::remove_excess_degree_one()
{
	bool deleted = false;
	bool delete_next;

	vector<bool> del(_n, false);
	for (int u = 0; u < _n; ++u)
	{
		if (deg(u) == 1)
			continue;

		// delete all but one neigbor of degree 1 of v
		delete_next = false;
		for (int v : adjs[u])
		{
			if (del[v])
				continue;
			if (deg(v) == 1)
			{
				if (delete_next)
				{
					del[v] = true;
					deleted = true;
				}
				else
					delete_next = true;
			}
		}
	}

	_m = 0;
	for (int u = 0; u < _n; ++u)
	{
		if (del[u])
			adjs[u].clear();
		else
			adjs[u].erase(remove_if(adjs[u].begin(), adjs[u].end(), [&](int x)
									{ return del[x]; }),
						  adjs[u].end());
		_m += adjs[u].size();
	}
	_m /= 2;

	return deleted;
}

// ajdacency lists are assumed to be sorted in increasing order
int LowMemGraph::neighborhoods_intersection_size(int u, int v)
{
	int r = 0;
	uint i = 0, j = 0;
	const vector<int> &nu = adjs[u];
	const vector<int> &nv = adjs[v];
	while (i < nu.size() && j < nv.size())
	{
		if (nu[i] == nv[j])
			++r;
		if (nu[i] < nv[j])
			++i;
		else
			++j;
	}
	return r;
}

void LowMemGraph::remove_edge(int u, int v)
{
	adjs[u].erase(remove(adjs[u].begin(), adjs[u].end(), v), adjs[u].end());
	adjs[v].erase(remove(adjs[v].begin(), adjs[v].end(), u), adjs[v].end());
	--_m;
}

// ajdacency lists are assumed to be sorted in increasing order
bool LowMemGraph::disjoint_neighborhoods(int u, int v)
{
	return neighborhoods_intersection_size(u, v) == 0;
}

bool LowMemGraph::remove_edge_disjoint_neighbors()
{
	bool deleted = false;
	vector<int> candidate(_n, -1);

	// Mark vertices that have a degree 1 adjacent or two degree 2 that are adjacent, adjacents
	for (int u = 0; u < _n; ++u)
	{
		if (deg(u) == 1)
		{
			int v = adjs[u][0];
			candidate[v] = u;
			continue;
		}
		if (deg(u) == 2)
		{
			int v = adjs[u][0], w = adjs[u][1];
			if (deg(v) != 2)
			{
				if (deg(w) == 2)
					swap(v, w);
				else
					continue;
			}
			// v has degree 2
			int x = adjs[v][0], y = adjs[v][1];
			if (y == u)
				swap(x, y);
			// x == u
			if (w == y)
				candidate[w] = u;
		}
	}

	vector<int> tmp;
	for (int u = 0; u < _n; ++u)
	{
		if (candidate[u] == -1)
			continue;
		tmp.clear();
		for (int v : adjs[u])
		{
			if (v != candidate[u] && disjoint_neighborhoods(u, v))
			{
				auto it = lower_bound(adjs[v].begin(), adjs[v].end(), u);
				adjs[v].erase(it);
			}
			else
			{
				tmp.push_back(v);
			}
		}
		adjs[u] = tmp;
	}

	_m = 0;
	for (int u = 0; u < _n; ++u)
		_m += deg(u);
	_m /= 2;

	return deleted;
}

// If 2 degree 2 vertices v,w are
// adjacent to u,x that are not adjacent,
// remove two non adjacent edges in this c4.
bool LowMemGraph::remove_C4()
{
	// vector<Edge> to_delete;
	bool deleted = false;
	int x, y;
	for (int u = 0; u < _n; ++u)
	{
		for (int v : adjs[u])
		{
			if (deg(v) != 2)
				continue;
			// x is the other neighbor of v
			if ((x = adjs[v][0]) == u)
				x = adjs[v][1];

			// u and x must be non-adjacent
			if (adjacent(u, x))
				continue;

			for (int w : adjs[u])
			{
				if (deg(w) != 2 || w == v)
					continue;

				if ((y = adjs[w][0]) == u)
					y = adjs[w][1];

				if (y != x)
					continue;

				remove_edge(u, v);
				remove_edge(w, x);
				deleted = true;
				break;
			}
		}
	}

	int64_t m = 0;
	for (int u = 0; u < _n; ++u)
		m += deg(u);
	m /= 2;

	return deleted;
}

// If 3 degree <= 3 vertices u,v,w form a triangle
// which is not in any diamond,
// isolate them.
bool LowMemGraph::remove_deg3_triangles()
{
	bool deleted = false;
	vector<Edge> to_delete;
	int a, b, c;
	int nb, min_d;
	for (int u = 0; u < _n; ++u)
	{
		if (deg(u) != 3)
			continue;

		to_delete.clear();

		a = adjs[u][0];
		b = adjs[u][1];
		c = adjs[u][2];

		min_d = min(deg(a), min(deg(b), deg(c)));
		if (min_d > 3) // we need at least a degree 2 or 3 in neighbors
			continue;

		nb = adjacent(a, b) + adjacent(b, c) + adjacent(c, a);
		if (nb == 3) // K_4
		{
			if (deg(b) == 3)
				swap(a, b);
			if (deg(c) == 3)
				swap(a, c);
			// a has degree 3

			if (deg(b) <= 5 && deg(c) <= 5)
			{
				for (int x : neighbors(b))
					if (x != a && x != c && x != u)
						to_delete.emplace_back(b, x);

				for (int x : neighbors(c))
					if (x != a && x != b && x != u)
						to_delete.emplace_back(c, x);
			}
		}
		else if (nb == 2) // Diamond
		{

			if (adjacent(a, b))
				swap(a, c);
			if (adjacent(a, b))
				swap(b, c);
			// a and b are not adjacent

			if (neighborhoods_intersection_size(a, b) == 2) // not a diamond
			{
				for (int x : neighbors(a))
					if (x != c && x != u)
						to_delete.emplace_back(a, x);

				for (int x : neighbors(b))
					if (x != c && x != u)
						to_delete.emplace_back(b, x);
			}
		}
		else if (nb == 1) // Triangle
		{
			if (adjacent(a, c))
				swap(b, c);
			if (adjacent(b, c))
				swap(a, c);
			// a and b are adjacent, the others are not

			if (deg(a) <= 3 && deg(b) <= 3 && neighborhoods_intersection_size(a, b) == 1) // not a diamond
			{
				to_delete.emplace_back(u, c);

				for (int x : neighbors(a))
					if (x != b && x != u)
						to_delete.emplace_back(a, x);

				for (int x : neighbors(b))
					if (x != a && x != u)
						to_delete.emplace_back(b, x);
			}
		}

		for (auto &[x, y] : to_delete)
		{
			remove_edge(x, y);
			deleted = true;
		}
	}

	return deleted;
}

// If 3 degree <= 3 vertices u,v,w form a triangle
// which is not in any diamond,
// isolate them.
bool LowMemGraph::isolate_small_complete(int s)
{
	bool deleted = false;
	vector<int> out_degs, outer, candidates;
	for (int u = 0; u < _n; ++u)
	{
		if (deg(u) != s - 1)
			continue;

		int a = 0;
		for (int v : adjs[u])
			for (int w : adjs[u])
				if (v != w && adjacent(v, w))
					++a;

		// If neighborhood is not a clique, continue
		if (a != (s - 1) * (s - 2))
			continue;

		out_degs.clear();

		// if ((deg(u) - s + 1)!= 0)
		// 	cout << "Error: " << deg(u) << " " << s << endl;
		out_degs.push_back(0);
		for (int v : adjs[u])
			out_degs.push_back(deg(v) - s + 1);

		sort(out_degs.begin(), out_degs.end());
		int64_t acc = 0, acc_deg = 0;
		bool valid = true;
		for (int i = 0; i < s; ++i)
		{
			acc += s - i - 1;
			acc_deg += out_degs[s - i - 1];
			if (acc_deg > acc) // outer connections are too big compared to this clique. cant isolate clique
			{
				valid = false;
				break;
			}
		}

		if (!valid)
			continue;

		// Check that all outer neighbors are disjoint
		outer.clear();
		for (auto v : adjs[u])
			for (auto w : adjs[v])
				if (w != u && !adjacent(w, u))
					outer.push_back(w); // N(K)

		sort(outer.begin(), outer.end());
		int ss = outer.size();
		for (int i = 0; i < ss - 1; ++i)
			if (outer[i] == outer[i + 1])
			{
				valid = false;
				break;
			}

		if (!valid)
			continue;

		for (auto v : adjs[u])
		{
			candidates = adjs[v];
			for (int x : candidates)
				if (x != u && !adjacent(x, u))
					deleted = true, remove_edge(x, v);
		}
	}

	return deleted;
}

bool LowMemGraph::isolate_small_complete2(int s)
{
	bool deleted = false;
	vector<int> out_degs, outer, candidates;

	set<int> checked;
	checked.clear();
	// define set
	set<int> N_K;

	for (int u = 0; u < _n; ++u)
	{

		if (deg(u) != s - 1)
			continue;

		if (checked.find(u) != checked.end())
			continue;

		
		int a = 0;
		for (int v : adjs[u])
			for (int w : adjs[u])
				if (v != w && adjacent(v, w))
					++a;

		// If neighborhood is not a clique, continue
		if (a != (s - 1) * (s - 2))
			continue;

		for (int v : adjs[u])
			if (deg(v) == s - 1){
				bool is_clique = true;
				for(int w: adjs[v]){
					if (w!=u && !adjacent(w,u)){
						is_clique = false;
						break;
					}
				}

				if(is_clique){
					checked.insert(v);
				}
			}

		// cout <<"clique found at u: "<<u<<" with size: "<<s<<endl;
		// clear N_K
		N_K.clear();

		for (int v : adjs[u])
			for (int w : adjs[v])
				if (w != u && !adjacent(w, u))
					N_K.insert(w); // N(K)

		bool NK_greater_than_K = false;
		bool K_and_NK_greater_than_editting_distance = false;

		if ((int)N_K.size() > s)
			NK_greater_than_K = true;

		// for each v in N_K, find editting_distance = edges missing with N(K) [need to add] + edges outside the clique and N(K) [need to remove]

		int editting_distance_sum = 0;
		for (auto v : N_K)
		{
			// find edges missing with N(K) [need to add]
			for (auto w : N_K)
				if (w != v && !adjacent(w, v))
					editting_distance_sum++;

			// find edges outside the clique and N(K) [need to remove]
			for (auto w : adjs[v])
				if (w != u && !adjacent(w, u) && !N_K.count(w))
					editting_distance_sum++;
		}

		if (s + (int)N_K.size() > editting_distance_sum)
			K_and_NK_greater_than_editting_distance = true;

		if (NK_greater_than_K && K_and_NK_greater_than_editting_distance)
		{

			for (auto v : N_K)
			{
				candidates = adjs[v];
				for (int x : candidates)
					if (x != u && !adjacent(x, u) && N_K.find(x) == N_K.end()) // not in N_K and not in clique
						deleted = true, remove_edge(x, v);
				// for (auto w : N_K)
				// {
				// 	if (v != w && !adjacent(v, w))
				// 		deleted = true, add_edge(v, w);
				// }
			}
		}
		else if (!NK_greater_than_K && K_and_NK_greater_than_editting_distance)
		{
			// find N2
			set<int> N2;
			N2.clear();
			for (auto w : N_K)
				for (auto x : adjs[w])
					if (x != u && !adjacent(x, u) && N_K.find(x) == N_K.end())
						N2.insert(x);

			bool Nw_intersect_NK_greater_than_K_NK_by_2 = false;
			set<int> vertices_that_fulfill;
			for (auto w : N2)
			{
				set<int> Nw_intersect_NK;
				Nw_intersect_NK.clear();
				for (auto x : adjs[w])
					if (N_K.find(x) != N_K.end())
						Nw_intersect_NK.insert(x);

				if ((int)Nw_intersect_NK.size() > (int)(N_K.size() + s) / 2)
				{
					Nw_intersect_NK_greater_than_K_NK_by_2 = true;
					vertices_that_fulfill.insert(w);
				}
			}

			if (!Nw_intersect_NK_greater_than_K_NK_by_2)
			{
				for (auto v : N_K)
				{
					candidates = adjs[v];
					for (int x : candidates)
						if (x != u && !adjacent(x, u) && N_K.find(x) == N_K.end()) // not in N_K and not in clique
							deleted = true, remove_edge(x, v);
					// for (auto w : N_K)
					// {
					// 	if (v != w && !adjacent(v, w))
					// 		deleted = true, add_edge(v, w);
					// }
				}
			}
			else{
				for (auto v : N_K)
				{
					candidates = adjs[v];
					for (int x : candidates)
						if (x != u && !adjacent(x, u) && N_K.find(x) == N_K.end() && vertices_that_fulfill.find(x)==vertices_that_fulfill.end()) // not in N_K and not in clique and not in vertices_that_fulfill
							deleted = true, remove_edge(x, v);
					// for (auto w : N_K)
					// {
					// 	if (v != w && !adjacent(v, w))
					// 		deleted = true, add_edge(v, w);
					// }
				}
			}
		}
	}


	return deleted;
}

int64_t LowMemGraph::kernelize()
{	

	int64_t m_removed = _m;
	int i = 0;
	bool cont;
	do
	{
		cont = false;
		while (remove_excess_degree_one())
			cont = true;

		while (remove_edge_disjoint_neighbors())
			cont = true;

		while (remove_C4())
			cont = true;

		while (remove_deg3_triangles())
			cont = true;

		for (int s = 3; s <= 10; ++s)
		{
			while (isolate_small_complete(s))
				cont = true;
		}
		// cout<<"Deleted "<<m_removed-_m<<" edges in iteration "<<i<<endl;
		for (int s = 3; s <= 10; ++s)
		{

			while (isolate_small_complete2(s)){
				// cout<<"DeletedX "<<m_removed-_m<<" edges in iteration "<<i<<endl;
				cont = true;
			}
		}
		// cout<<"Deleted "<<m_removed-_m<<" edges in iteration "<<i<<endl;
		++i;
		
	} while (cont);

	m_removed -= _m;
	cout << "Deleted " << m_removed << " edges" << endl;
	return m_removed;
}

vector<vector<int>> LowMemGraph::connected_components()
{
	// Use an union find to build all the connected components
	// from edges without building a graph
	union_find uf(_n);
	for (int u = 0; u < _n; ++u)
		for (int v : adjs[u])
			uf.merge(u, v);

	int cc_count = 0;
	vector<int> cc_id_aux(_n);
	for (int i = 0; i < _n; ++i)
		if (uf.parent[i] == i)
			cc_id_aux[i] = cc_count++;

	vector<vector<int>> ccs(cc_count);
	for (int i = 0; i < _n; ++i)
		ccs[cc_id_aux[uf.find(i)]].push_back(i);

	return ccs;
}

/* INPUT FUNCTIONS */
// Does not handle comments
LowMemGraph LowMemGraph::from_istream(istream &is)
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
		edges.emplace_back(u - 1, v - 1);
	}

	return LowMemGraph(n, edges);
}

LowMemGraph LowMemGraph::from_cin()
{
	return from_istream(cin);
}

LowMemGraph LowMemGraph::from_file(const string &fname)
{
	ifstream graph_file(fname);
	return from_istream(graph_file);
}