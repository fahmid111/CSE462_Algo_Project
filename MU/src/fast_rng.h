// Copyright (c) 2021 Gabriel Bathie
// Copyright (c) 2021 Ulysse  Prieto
// This code is licensed under the terms of the MIT License (MIT).
#pragma once

#include <cstdint>
#include <limits>

namespace fast_rng
{
	using xor_shift_state = uint_fast64_t;
	class xor_shift
	{
	private:
		xor_shift_state x;
	public:
		using result_type = xor_shift_state;

		inline static constexpr xor_shift_state min() { return std::numeric_limits<xor_shift_state>::min(); };
		inline static constexpr xor_shift_state max() { return std::numeric_limits<xor_shift_state>::max(); };

		xor_shift(xor_shift_state s)
		{ 
			if (!s)
				++s;
			x = s;
		}

		inline xor_shift_state operator()()
		{
			x ^= x << 13;
			x ^= x >> 7;
			x ^= x << 17;
			return x;
		}

		inline xor_shift_state operator()(xor_shift_state n)
		{
			return operator()()%n;
		}
	};
}