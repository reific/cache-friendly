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

import java.util.Arrays;

/**
 * Work in Progress
 */
public class HybridBinarySearch {

	private static int[] currentHaystack = null;
	private static int[] currentIndex = null;
	private static int from;
	private static int to;
	private static int currentLength;

	private static int FRACTION = 128;

	private static int[] createIndex(int[] haystack, int from, int to) {
		int length = to - from;
		//System.out.println(haystack.length);
		currentLength = length;
		int next = 0;
		int cacheSize = (flp2(length) / FRACTION) - 1;
		int[] cache = new int[cacheSize];

		for (int i = 2; i <= cacheSize + 1; i *= 2) {
			for (int j = 1; j < i; j += 2) {
				int index = length / i * j;
				int k = haystack[from + index];

				cache[next++] = k;
			}
		}
		return cache;
	}

	public static int binarySearch(int[] a, int fromIndex, int toIndex, int key) {
		if (a.length < FRACTION) {
			return Arrays.binarySearch(a, fromIndex, toIndex, key);
		}

		if (a != currentHaystack || fromIndex != from || toIndex != to) {
			currentHaystack = a;
			from = fromIndex;
			to = toIndex;
			currentIndex = createIndex(a, fromIndex, toIndex);
		}
		int i = 0;
		while (i < currentIndex.length) {
			if (key <= currentIndex[i]) {
				i = 2 * i + 1;
			}
			else {
				i = 2 * i + 2;
			}
		}

		int blah = currentIndex.length + 1;


		int numerator = i - blah + 1;
		int denominator = (currentIndex.length + 1);

		int low = numerator == 0 ? 0 : fromIndex + currentLength / denominator * numerator;
		int high = fromIndex + (currentLength + denominator - 1) / denominator * (numerator + 1) + 1;

		if (high > currentLength) {
			high = currentLength;
		}

		int binarySearch = Arrays.binarySearch(a, low, high, key);
		return binarySearch;

	}

	private static int flp2(int x)
	{
		x = x | (x >>> 1);
		x = x | (x >>> 2);
		x = x | (x >>> 4);
		x = x | (x >>> 8);
		x = x | (x >>> 16);
		return x - (x >>> 1);
	}

}
