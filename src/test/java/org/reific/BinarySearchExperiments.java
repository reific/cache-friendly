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
package org.reific;

public class BinarySearchExperiments {

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

	// A constant suitable for use on primitive 32 bit arrays (int, float)
	private static final int EVOLVED_32_BIT_CONSTANT = 79643643;

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
		int mid = size & 0x80000000 >> keepBits;

		//convert all the zeroed bits to ones by subtracting
		mid = mid - 1;

		//mask these one bits with our magic constant,
		// which guarantees a mid smaller than  (but close to) size
		int mask = EVOLVED_32_BIT_CONSTANT | (0x80000000 >> keepBits);
		mid = (mid & mask) + fromIndex;
		return mid;
	}

	public static int binarySearchBranchFree(int[] a, int fromIndex, int toIndex,
			int key) {
		rangeCheck(a.length, fromIndex, toIndex);
		return binarySearch0_BranchFree(a, fromIndex, toIndex, key);
	}

	public static int binarySearchBranchFreePrefetch(int[] a, int fromIndex, int toIndex,
			int key) {
		//rangeCheck(a.length, fromIndex, toIndex);
		return binarySearch0_BranchFreePrefetch(a, fromIndex, toIndex, key);
	}

	public static int binarySearchBranchFreeUnrolledStatic16(int[] a, int key) {
		return binarySearch0_BranchFreeUnrolledStatic16(a, key);
	}

	private static int binarySearch0_BranchFreeUnrolledStatic16(int[] a, int key) {
		//Assume size = 16
		int low = 8;
		int midVal;
		int highMask;

		midVal = a[low];
		low += 4;
		highMask = (int) (((long) key - midVal) >> 63);
		low -= 8 & highMask;

		midVal = a[low];
		low += 2;
		highMask = (int) (((long) key - midVal) >> 63);
		low -= 4 & highMask;

		midVal = a[low];
		low += 1;
		highMask = (int) (((long) key - midVal) >> 63);
		low -= 2 & highMask;

		midVal = a[low];
		highMask = (int) (((long) key - midVal) >> 63);
		low -= 1 & highMask;

		return a[low] == key ? low : -(low + 1);

	}

	private static int binarySearch0_BranchFree(int[] a, int fromIndex, int toIndex,
			int key) {
		int low = fromIndex;
		int high = toIndex - 1;

		int mid = biasedMidpoint32(fromIndex, toIndex);

		while (low <= high) {
			int midVal = a[mid];
			// possible new mid:
			// (low + (mid -1)) >>> 1
			// possible new mid:
			// (mid + 1 + high) >>> 1

			if (midVal == key) {
				return mid;
			}

			// propagate sign bit (so all 1 or all 0)
			int lowMask = (midVal - key) >> 31;

			int lowDelta = mid + 1 - low;
			int highDelta = high - (mid - 1);

			low += lowDelta & lowMask;
			high -= highDelta & ~lowMask;

			mid = (low + high) >>> 1;

		}
		return -(low + 1); // key not found.
	}

	private static int binarySearch0_BranchFreePrefetch(int[] a, int fromIndex, int toIndex,
			int key) {

		//assume size 2^n-1

		int sizeish = toIndex - fromIndex + 1;
		int i = sizeish / 4;
		int m = sizeish / 2 - 1;

		int lv;
		int hv;
		int v = a[m];

		for (; i > 0; i >>= 1) {

			lv = a[m - i];
			hv = a[m + i];

			//TODO: overflow
			int maskB = (int) (((long) key - v) >> 31);
			int maskA = (int) (((long) v - key) >> 31);

			m -= i & maskB;
			m += i & maskA;

			v = v & ~maskB & ~maskA;
			v += (lv & maskB) + (hv & maskA);
		}
		return v == key ? m : -(m + 1);
	}

	//	private static int binarySearch0(int[] a, int fromIndex, int toIndex,
	//			int key) {
	//		int low = fromIndex;
	//		int high = toIndex - 1;
	//
	//		int mid = biasedMidpoint(fromIndex, toIndex);
	//
	//		while (low <= high) {
	//			int midVal = a[mid];
	//
	//			if (midVal == key)
	//				return mid; // key found
	//
	//			int newLow = Math.max(-1, mid + 1);
	//			int newHigh = Math.max(-1, mid - 1);
	//
	//			low = newLow(midVal, key, low, newLow);
	//			high = newHigh(midVal, key, high, newHigh);
	//
	//			mid = (low + high) >>> 1;
	//		}
	//		return -(low + 1); // key not found.
	//	}

	private static int newLow(int midVal, int key, int low, int value) {
		if (midVal < key) {
			return value;
		}
		else {
			return low;
		}
	}

	private static int newHigh(int midVal, int key, int high, int value) {
		if (midVal > key) {
			return value;
		}
		else {
			return high;
		}
	}

	public static void main(String[] args) {
		long a = Long.MAX_VALUE;
		long b = -5;

		System.out.println(b - a);
		System.out.println(a - b);
	}



	/**
	 * A version of Arrays.binarySearch that is structured the same as our binary search, to provide a more fair
	 * comparison of run-times with and with the bias. This is required since HotSpot is generating sub-optimal code for 
	 * Arrays.binarySearch (for our use-case), which would not be a fair comparison. This is probably a CMOV vs branch issue. 
	 */
	public static int binarySearchX(int[] a, int fromIndex, int toIndex,
			int key) {
		rangeCheck(a.length, fromIndex, toIndex);
		return binarySearchX0(a, fromIndex, toIndex, key);
	}

	private static int binarySearchX0(int[] a, int fromIndex, int toIndex,
			int key) {
		int low = fromIndex;
		int high = toIndex - 1;

		int mid = (low + high) >>> 1;

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
}
