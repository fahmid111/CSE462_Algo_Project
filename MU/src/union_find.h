// Copyright (c) 2021 Gabriel Bathie
// Copyright (c) 2021 Ulysse  Prieto
// This code is licensed under the terms of the MIT License (MIT).
#pragma once

#include <vector>

class union_find 
{
public:
	std::vector<int> parent, count;
	
	union_find(int n);

	int find(int a); 
	bool merge(int a, int b);	
};
