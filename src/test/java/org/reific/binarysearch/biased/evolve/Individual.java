/*
    Copyright (C) 2015 James Scriven

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.reific.binarysearch.biased.evolve;

import java.util.List;

public class Individual implements Comparable<Individual> {

	// Only used locally in simulateBinarySearch(), but reuse to avoid reallocating memory
	private static ThreadLocal<long[]> theQueue = new ThreadLocal<long[]>() {
		protected long[] initialValue() {
			return new long[32 * 1024];
		}
	};

	private static final float[] memoizedStates = new float[0x10000000];
	public final float utility;
	public final int value;
	private SpecResult[] utilities;

	private List<Spec> specs;

	public Individual(int value, List<Spec> specs) {
		this.value = value;
		this.specs = specs;
		if (memoizedStates[value] != 0) {
			this.utility = memoizedStates[value];
		}
		else {
			utility = compute();
			memoizedStates[value] = utility;
		}
	}

	public SpecResult[] utilities() {
		if (utilities == null) {
			utilities = new SpecResult[specs.size()];
			int i = 0;
			for (Spec spec : specs) {
				utilities[i++] = new SpecResult(spec, worst(spec).utility());
			}
		}
		return utilities;
	}

	private float compute() {

		float total = 0;
		for (Spec spec : specs) {
			float u = worst(spec).utility();
			// take a fairly large root to bias improvements in smaller utilities over already large utilities
			total += Math.pow(u, (1.0 / 100));
		}
		return (total / (float) specs.size());

	}

	private UtilityTracker worst(final Spec spec) {

		UtilityTracker worst = null;
		//TODO hardcoded for 32 bit (int) data type
		int minArrayMagnitude = Util.log2(spec.minArraySizeInBytes / 4) - Util.log2(8);
		for (int magnitude = minArrayMagnitude; magnitude <= 27; magnitude += 1) {
			//System.out.println(magnitude);
			for (int significant = 8; significant <= 15; significant++) {
				int sizeOfArray = significant << magnitude;
				UtilityTracker utilityTracker1 = new UtilityTracker(spec.numberOfSets, spec.blockSizeInBytes);
				sim(value, utilityTracker1, 0, sizeOfArray);
				if (worst == null || utilityTracker1.utility() < worst.utility()) {
					worst = utilityTracker1;
				}
			}
		}
		return worst;
	}

	/**
	 * simulate binarySearch in org.reific.binarysearch.biased.BiasedBinarySearch, for the given candidate constant
	 */
	private static void sim(int candidate, UtilityTracker utilityTracker, int fromIndex, int toIndex) {

		int low = fromIndex;
		int size = toIndex - fromIndex;
		int nlz = Integer.numberOfLeadingZeros(size);
		int keepBits = nlz + ((36 - nlz) >> 4);

		//zero-out the remaining bits
		int mid = size & 0x80000000 >> keepBits;

		//convert all the zeroed bits to ones by subtracting
		mid = mid - 1;

		int mask = candidate | (0x80000000 >> keepBits);
		mid = (mid & mask) + low;

		utilityTracker.accessArrayLocation(mid);
		simulateBinarySearch(utilityTracker, fromIndex, mid);

	}

	private static void simulateBinarySearch(UtilityTracker utilityTracker, long low, long high) {

		if (low >= high) {
			return;
		}

		int insert = 0;
		int read = 0;

		theQueue.get()[insert++] = (low << 32) | high;

		while (insert < theQueue.get().length - 2) {

			long next = theQueue.get()[read++];
			long l = (int) (next >>> 32);
			long h = (int) (next & 0xFFFFFFFF);

			if (l > h) {
				return;
			}
			long mid = (l + h) >>> 1;

			boolean done = utilityTracker.accessArrayLocation((int) mid);
			if (done) {
				return;
			}

			// Simulate both the left half of the binary search...
			theQueue.get()[insert++] = (l << 32) | (mid - 1);
			// And the right half
			theQueue.get()[insert++] = ((mid + 1) << 32) | h;
		}
	}

	@Override
	public boolean equals(Object obj) {
		Individual other = (Individual) obj;
		return value == other.value;
	}

	@Override
	public int hashCode() {
		return Integer.hashCode(value);
	}

	@Override
	public int compareTo(Individual o) {
		return Float.compare(o.utility, utility);
	}
}
