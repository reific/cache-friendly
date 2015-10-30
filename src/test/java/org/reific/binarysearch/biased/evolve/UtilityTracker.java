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

import java.util.BitSet;

/**
 * Tracks simulated array accesses and calculates the utility for a specific Cache/TLB configuration
 */
class UtilityTracker {

	// TODO: Hard coded for now for 32 bit (int[]/float[]) arrays
	private static final int DATA_TYPE_SIZE_IN_BYTES = 4;

	private final BitSet sets1;
	private final BitSet sets2;
	private final BitSet sets3;
	private final BitSet cacheLines;
	private final int mask;
	private final int blockSizeInBytes;

	private float utility = 0;
	private boolean done = false;
	// The number of memory access before the first/second/third collision
	private int numberOfAccessesFirstFailure = 0;
	private int numberOfAccessesSecondFailure = 0;
	private int numberOfAccessesThirdFailure = 0;

	public UtilityTracker(int numberOfSets, int blockSizeInBytes) {
		this.blockSizeInBytes = blockSizeInBytes;
		sets1 = new BitSet(numberOfSets);
		sets2 = new BitSet();
		sets3 = new BitSet();
		cacheLines = new BitSet();
		mask = numberOfSets - 1;

	}

	public float utility() {
		return done ? utility : 14.0f;
	}

	/**
	 * simulate access to given array index. 
	 * @return true when the simulation should end because accesses are being made in duplicate cache lines/pages
	 */
	public boolean accessArrayLocation(int arrayLocation) {
		if (done) {
			return true;
		}
		// Which cache line (for caches simulation) or page (for TLB simulation) is being accessed? 
		int cacheLine = arrayLocation >>> (Util.log2(blockSizeInBytes) - Util.log2(DATA_TYPE_SIZE_IN_BYTES));

		// We are done if either we we access a duplicate cacheline/page (which means all future accesses are free)
		// or if we hit some arbitrary number of collisions (numberOfAccessesThirdFailure > 0)
		if (numberOfAccessesThirdFailure > 0 || cacheLines.get(cacheLine)) {
			//The actual utility calculation for a particular cache/page size
			float a = numberOfAccessesFirstFailure == 0 ? 15.0f : Util.log2(numberOfAccessesFirstFailure);
			float b = numberOfAccessesSecondFailure == 0 ? 15.0f : Util.log2(numberOfAccessesSecondFailure);
			float c = numberOfAccessesThirdFailure == 0 ? 15.0f : Util.log2(numberOfAccessesThirdFailure);
			utility = a;
			utility += b / 100.0;
			utility += c / 10000.0;

			done = true;
			return true;
		}

		// Keep track what cache lines or page is being accessed so we will know
		// when we've stated binary searching within the same line/page
		cacheLines.set(cacheLine);

		int set = cacheLine & mask;

		if (sets1.get(set)) {
			if (numberOfAccessesFirstFailure == 0) {
				numberOfAccessesFirstFailure = sets1.cardinality();
			}
			if (sets2.get(set)) {
				if (numberOfAccessesSecondFailure == 0) {
					numberOfAccessesSecondFailure = sets2.cardinality();
				}

				if (sets3.get(set)) {
					if (numberOfAccessesThirdFailure == 0) {
						numberOfAccessesThirdFailure = sets3.cardinality();
					}
				}
				else {
					sets3.set(set);
				}
			}
			else {
				sets2.set(set);
			}
		}
		else {
			sets1.set(set);
		}

		return false;

	}
}