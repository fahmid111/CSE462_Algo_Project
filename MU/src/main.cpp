// Copyright (c) 2021 Gabriel Bathie
// Copyright (c) 2021 Ulysse  Prieto
// This code is licensed under the terms of the MIT License (MIT).
#include "multi_cc_instance.h"

#include <csignal>
#include <cstdlib>
#include <cstring>
#include <iostream>

using namespace std;

// Signal handling
volatile sig_atomic_t tle = 0;

void term(int signum)
{
	(void) signum;
	tle = 1;
}

int main()
{
	// Signal handling
	struct sigaction action;
	memset(&action, 0, sizeof(struct sigaction));
	action.sa_handler = term;
	sigaction(SIGTERM, &action, NULL);

	// Fast IO
	cin.tie(0);
	ios::sync_with_stdio(false);

	vector<int> opt(50);
	iota(opt.begin(), opt.end(), 5);

	auto gs = KernelizedMultiCCInstance::from_cin();
	auto timeout = [&](int i){ (void) i; return tle == 0; };

	gs.greedy_bfs_fill(
		timeout,
		opt,
		10
	);

	gs.print_sol();

	return 0;
}
