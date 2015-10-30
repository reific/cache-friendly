# cache-friendly
Cache-Friendly Algorithms and Data Structures for Java

## Biased Binary Search

**org.reific.binarysearch.biased.BiasedBinarySearch.binarySearch** is a drop-in replacement for Arrays.binarySearch that does not suffer for cache line aliasing effects for array sizes of large powers of two.

**org.reific.binarysearch.biased.evolve.EvolveBinarySearchConstant** (in the tst directory) runs a genetic algorithm to evolve the constants used in BiasedBinarySearch.

