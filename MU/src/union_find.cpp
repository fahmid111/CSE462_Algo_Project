// Copyright (c) 2021 Gabriel Bathie
// Copyright (c) 2021 Ulysse  Prieto
// This code is licensed under the terms of the MIT License (MIT).
#include "union_find.h"

union_find::union_find(int n) : parent(n), count(n, 1) 
{
	for (int i = 0; i < n; ++i)
		parent[i] = i;
}

int union_find::find(int a) 
{
	int root = a, next;
	while (parent[root] != root)
		root = parent[root];

	while (parent[a] != root) 
	{
		next = parent[a];
		parent[a] = root;
		a = next;
	}

	return root;
}

bool union_find::merge(int a, int b) 
{
	a = find(a); b = find(b);
	if (a == b) return false;
	if (count[a] < count[b]) std::swap(a,b);
	parent[b] = a; 
	count[a] += count[b];
	return true; 
}	