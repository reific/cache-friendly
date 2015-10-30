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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;
import org.reific.HybridBinarySearch;

public class TestBiasedBinarySearch {


	@Test
	public void testKeepBits() throws Exception {
		assertEquals(9, keepBits(1));
		assertEquals(9, keepBits(2));
		assertEquals(9, keepBits(3));
		assertEquals(9, keepBits(4));
		assertEquals(9, keepBits(5));
		assertEquals(9, keepBits(6));
		assertEquals(9, keepBits(7));
		assertEquals(9, keepBits(8));
		assertEquals(10, keepBits(9));
		assertEquals(16, keepBits(15));
		assertEquals(21, keepBits(20));
		assertEquals(26, keepBits(25));
		assertEquals(32, keepBits(31));
		assertEquals(33, keepBits(32));
	}

	private int keepBits(int nlz) {
		return Math.max(nlz + 1, 9);
	}

	@Test
	public void testRange() throws Exception {
		int haystack[] = new int[1000];
		BiasedBinarySearch.binarySearch(haystack, 990, haystack.length, 0);
	}

	@Test
	/**
	 * Test a variety of array sizes for correct behaviour.
	 */
	public void testManySizes() throws Exception {
		int MAX_SIZE = 1024 * 1024;
		for (int size = 1; size <= MAX_SIZE; size = (int) (size * 1.10 + 1)) {
			int haystack[] = new int[size];
			for (int i = 0; i < haystack.length; i++) {
				haystack[i] = i * 2;
			}
			for (int i = 0; i < haystack.length; i++) {
				assertEquals(i, BiasedBinarySearch.binarySearch(haystack, 0, haystack.length, i * 2));
				assertTrue(BiasedBinarySearch.binarySearch(haystack, 0, haystack.length, i * 2 - 1) < 0);
			}
			assertTrue(BiasedBinarySearch.binarySearch(haystack, 0, haystack.length, -1) < 0);
			assertTrue(BiasedBinarySearch.binarySearch(haystack, 0, haystack.length, Integer.MAX_VALUE) < 0);
			assertTrue(BiasedBinarySearch.binarySearch(haystack, 0, haystack.length, Integer.MIN_VALUE) < 0);

		}
	}



	@Test
	public void testOneGig() throws Exception {

		//only run in ecl
		StackTraceElement[] stackTrace = new RuntimeException().getStackTrace();
		Assume.assumeTrue(stackTrace[stackTrace.length - 1].getClassName().startsWith("org.eclipse"));

		int haystack[] = new int[8 * 1024 * 1024];
		for (int i = 0; i < haystack.length; i++) {
			haystack[i] = i;
		}
		for (int i = 0; i < haystack.length; i++) {
			assertEquals(i, BiasedBinarySearch.binarySearch(haystack, 0, haystack.length, i));
		}
		assertTrue(BiasedBinarySearch.binarySearch(haystack, 0, haystack.length, -1) < 0);
		assertTrue(BiasedBinarySearch.binarySearch(haystack, 0, haystack.length, Integer.MAX_VALUE) < 0);
		assertTrue(BiasedBinarySearch.binarySearch(haystack, 0, haystack.length, Integer.MIN_VALUE) < 0);
	}

	@Test
	/**
	 */
	public void testHybrid() throws Exception {
		int MAX_SIZE = 1024 * 1024;
		for (int size = 1; size <= MAX_SIZE; size = (int) (size * 1.10 + 1)) {
			int haystack[] = new int[size];
			for (int i = 0; i < haystack.length; i++) {
				haystack[i] = i;
			}
			for (int i = 0; i < haystack.length; i++) {
				assertEquals(i, HybridBinarySearch.binarySearch(haystack, 0, haystack.length, i));
			}
			assertTrue(HybridBinarySearch.binarySearch(haystack, 0, haystack.length, -1) < 0);
			assertTrue(HybridBinarySearch.binarySearch(haystack, 0, haystack.length, Integer.MAX_VALUE) < 0);
			assertTrue(HybridBinarySearch.binarySearch(haystack, 0, haystack.length, Integer.MIN_VALUE) < 0);

		}
	}

	@Test
	@Ignore
	/**
	 */
	public void testHybridLargeSizes() throws Exception {
		int MAX_SIZE = 1024 * 1024 * 512;
		for (int size = 1024 * 1024 * 512; size <= MAX_SIZE; size *= 2) {
			System.out.println(size);
			int haystack[] = new int[size];
			for (int i = 0; i < haystack.length; i++) {
				haystack[i] = i;
			}
			System.out.println("." + size);
			for (int i = 0; i < haystack.length; i++) {
				assertEquals(i, HybridBinarySearch.binarySearch(haystack, 0, haystack.length, i));
			}
			assertTrue(HybridBinarySearch.binarySearch(haystack, 0, haystack.length, -1) < 0);
			assertTrue(HybridBinarySearch.binarySearch(haystack, 0, haystack.length, Integer.MAX_VALUE) < 0);
			assertTrue(HybridBinarySearch.binarySearch(haystack, 0, haystack.length, Integer.MIN_VALUE) < 0);

		}
	}

}
