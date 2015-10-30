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

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Random;

public class CompareBiasedBinarySearch {

	private static final Random random = new Random(42);
	private static final long times[] = new long[8];

	private static final int MIN_ARRAY_SIZE = 1024 * 1;
	private static final int MAX_ARRAY_SIZE = 1024 * 1024 * 512;
	final static int[] needles = new int[10000];
	// 1/findRatio is the number of random lookups that will result in a match
	final static int FIND_RATIO = 2;
	final static int NUM_SUBINTERVALS = 128;
	final static int haystack[] = new int[MAX_ARRAY_SIZE * 2];
	final static int NUM_INNER_LOOPS = 256;
	final static int TOP_LEVEL_WARMUPS = 5;

	interface MeasureBinarySearch {
		int measureBinarySearch(int num_unique_needles, int size);
	}

	public static void main(String[] args) throws IOException {
		for (int j = 0; j < TOP_LEVEL_WARMUPS + 1; j++) {
			recordBinarySearchRuntime("fixedBinarySearch.csv", CompareBiasedBinarySearch::biasedBinarySearch, 100);
		}

		Runtime.getRuntime().gc();
		for (int j = 0; j < TOP_LEVEL_WARMUPS + 1; j++) {
			recordBinarySearchRuntime("binarySearch.csv", CompareBiasedBinarySearch::arraysBinarySearch, 100);
		}
	}

	private static void recordBinarySearchRuntime(String outputFilename, MeasureBinarySearch binarySearch,
			int num_unique_needles) {

		//prevent compiler optimizing away method calls
		int accumulate = 0;

		try (PrintWriter out = new PrintWriter(new FileWriter(outputFilename, false), false)) {

			out.println("arraysize,time");

			//Number of iterations. Results from each will be appended to the result times
			for (int iteration = 0; iteration < 5; iteration++) {

				for (int power = MIN_ARRAY_SIZE; power <= MAX_ARRAY_SIZE && power > 0; power *= 2) {
					System.err.println(outputFilename + " " + power + " " + num_unique_needles);
					for (int i = 0; i < power * 2; i++) {
						haystack[i] = i * FIND_RATIO;
					}
					for (int subrange = power / NUM_SUBINTERVALS; subrange <= power; subrange += power
							/ NUM_SUBINTERVALS) {
						int size = power + subrange;
						//test for overflow
						if (size > 0) {
							for (int i = 0; i < num_unique_needles; i++) {
								needles[i] = random.nextInt(size) * FIND_RATIO;
							}

							accumulate += binarySearch.measureBinarySearch(num_unique_needles, size);
							// output timing information to file
							for (int i = 1; i < times.length; i++) {
								out.println("" + size * 4L + "," + (times[i] - times[i - 1]) / num_unique_needles);
							}
							out.flush();
						}
					}
				}
			}

			//prevent compiler optimizing away method calls
			System.err.println(accumulate);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Define these two methods separately, so the timing code will be around a single monomorphic call site. 
	private static int arraysBinarySearch(int num_unique_needles, int size) {
		int accumulate = 0;
		for (int k = 0; k < NUM_INNER_LOOPS; k++) {
			for (int i = 0; i < num_unique_needles; i++) {
				accumulate += Arrays.binarySearch(haystack, 0, size, needles[i]);
			}
			long stop = System.nanoTime();
			//only keep that last 8 timings (the rest are warm-up iterations)
			times[k & 7] = stop;
		}
		return accumulate;
	}

	private static int biasedBinarySearch(int num_unique_needles, int size) {
		int accumulate = 0;
		for (int k = 0; k < NUM_INNER_LOOPS; k++) {
			for (int i = 0; i < num_unique_needles; i++) {
				accumulate += BiasedBinarySearch.binarySearch(haystack, 0, size, needles[i]);
			}
			long stop = System.nanoTime();
			//only keep that last 8 timings (the rest are warm-up iterations)
			times[k & 7] = stop;
		}
		return accumulate;
	}

}
