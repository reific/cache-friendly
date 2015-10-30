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
package org.reific.binarysearch.biased;

/**
 * Alternate implementations of Arrays.binarySearch that are more cache-friendly. The initial midpoint is
 * calculated by masking off the low order bits of the size of the array with an evolved constant. The constant was evolved
 * to have the property that conflict misses will be less likely for popular TLB and CPU Cache subsystems. This is particularly
 * important when binary searching large arrays that are powers of two in size or multiples of large powers of two.
 */
public class BiasedBinarySearch {

	// Copied from Arrays.binarySearch
	private static void rangeCheck(int arrayLength, int fromIndex, int toIndex) {
		if (fromIndex > toIndex) {
			throw new IllegalArgumentException(
					"fromIndex(" + fromIndex + ") > toIndex(" + toIndex + ")");
		}
		if (fromIndex < 0) {
			throw new ArrayIndexOutOfBoundsException(fromIndex);
		}
		if (toIndex > arrayLength) {
			throw new ArrayIndexOutOfBoundsException(toIndex);
		}
	}

	public static int binarySearch(int[] a, int fromIndex, int toIndex,
			int key) {
		rangeCheck(a.length, fromIndex, toIndex);
		return binarySearch0(a, fromIndex, toIndex, key);
	}

	// A constant suitable for use on primitive 32 bit arrays (int, float)
	// Evolved using org.reific.binarysearch.biased.evolve.EvolveBinarySearchConstant
	private static final int EVOLVED_32_BIT_CONSTANT = 79643643;

	/**
	 * Our implementation, based on Arrays.binarySearch
	 */
	private static int binarySearch0(int[] a, int fromIndex, int toIndex,
			int key) {
		int low = fromIndex;
		int high = toIndex - 1;

		int mid = biasedMidpoint32(fromIndex, toIndex);

		while (low <= high) {
			int midVal = a[mid];

			if (midVal < key)
				low = mid + 1;
			else if (midVal > key)
				high = mid - 1;
			else
				return mid; // key found
			mid = (low + high) >>> 1;
		}
		return -(low + 1); // key not found.
	}

	private static int biasedMidpoint32(int fromIndex, int toIndex) {
		int size = toIndex - fromIndex;

		// this call will be replaced by single assembly op with a 
		// JVM intrinsic on modern hardware.
		int nlz = Integer.numberOfLeadingZeros(size);
		// number of extra high-order bits to preserve 
		// just a simple (fast) function of nlz that preserves 
		// a couple more original size bits for the largest
		// array sizes. 
		int keepBits = nlz + ((36 - nlz) >> 4);

		//zero-out the remaining bits
		int mid = size & (0x80000000 >> keepBits);

		//convert all the zeroed bits to ones by subtracting
		mid = mid - 1;

		//mask these one bits with our magic constant,
		// which guarantees a mid smaller than  (but close to) size
		int mask = EVOLVED_32_BIT_CONSTANT | (0x80000000 >> keepBits);
		mid = (mid & mask) + fromIndex;
		return mid;
	}


}
