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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EvolveBinarySearchConstant {
	private final static int POPULATION_LIMIT = 1000;
	private static final Random random = new Random(5);

	/**
	 * pick a (biased) random individual from the population. The higher the eliteness parameter, the closer the individual
	 * will be to the currently most fit individual.
	 */
	private static Individual pickIndividual(List<Individual> population, int eliteness) {
		float nextFloat = random.nextFloat();
		int index = (int) (Math.pow(nextFloat, eliteness) * (Math.min(POPULATION_LIMIT, population.size())));
		return population.get(index);
	}

	public static void main(String[] args) throws InterruptedException {

		ArrayList<Spec> specs = new ArrayList<Spec>();

		specs.add(Spec
				.of("TLB - 4k Pages, 4-way, 64 entries (e.g. x86 i7-4790K Level 1)")
				.numberOfSets(16)
				.blockSizeInBytes(4 * 1024)
				.minArraySizeInBytes(512 * 1024)
				.build());
		specs.add(Spec
				.of("TLB - 64k Pages, 4-way, 512 entries (e.g Power7)")
				.numberOfSets(128)
				.blockSizeInBytes(64 * 1024)
				.minArraySizeInBytes(64 * 1024 * 1024)
				.build());
		specs.add(Spec
				.of("TLB - 2M Pages, 4-way, 32 entries (e.g. x86 i7-4790K Level 1)")
				.numberOfSets(8)
				.blockSizeInBytes(2 * 1024 * 1024)
				.minArraySizeInBytes(128 * 1024 * 1024)
				.build());
		specs.add(Spec
				.of("32k L1 Cache, 8-way, 64 byte cachelines (e.g. x86 i7-4790K)")
				.numberOfSets(64)
				.blockSizeInBytes(64)
				.minArraySizeInBytes(64 * 1024)
				.build());
		specs.add(Spec
				.of("16k L1 Cache, 4-way, 32 byte cachelines (e.g. Sparc T5,M5,M6)")
				.numberOfSets(128)
				.blockSizeInBytes(32)
				.minArraySizeInBytes(32 * 1024)
				.build());
		specs.add(Spec
				.of("32k L1 Cache, 8-way, 128 byte cachelines (e.g. Power7)")
				.numberOfSets(32)
				.blockSizeInBytes(128)
				.minArraySizeInBytes(64 * 1024)
				.build());
		specs.add(Spec
				.of("256k L2 Cache, 8-way, 64 byte cachelines (e.g. x86 i7-4790K)")
				.numberOfSets(512)
				.blockSizeInBytes(64)
				.minArraySizeInBytes(512 * 1024)
				.build());
		//		specs.add(Spec.of("8M L3 Cache, 16-way, 64 byte cachelines (e.g. x86 i7-4790K)")
		//				.numberOfSets(8192)
		//				.blockSizeInBytes(64)
		//				.minArraySizeInBytes(16 * 1024 * 1024)
		//				.build());

		ExecutorService crossoverExecutor = Executors.newFixedThreadPool(4);
		ScheduledExecutorService supplementaryService = Executors.newScheduledThreadPool(2);

		List<Individual> population = Collections
				.synchronizedList(new ArrayList<Individual>());

		Crossover[] crossovers = Crossover.values();
		Mutater[] mutaters = Mutater.values();

		for (int i = 0; i < 100; i++) {
			population.add(new Individual(random.nextInt(1 << 28), specs));
		}
		System.out.println("Initial population created");

		List<Runnable> crossoverTasks = new ArrayList<Runnable>();

		for (int i = 0; i < 5; i++) {

			Runnable crossoverTask = new Runnable() {
				@Override
				public void run() {

					while (true) {

						Individual a = pickIndividual(population, 10);
						Individual b = pickIndividual(population, 5);

						Crossover crossover = crossovers[random.nextInt(crossovers.length)];

						int offspringValue = crossover.cross(a.value, b.value);

						if (random.nextFloat() > 0.9) {
							Mutater mutater = mutaters[random.nextInt(mutaters.length)];
							offspringValue = mutater.mutate(offspringValue);
						}

						Individual offspring = new Individual(offspringValue, specs);
						if (!(population.contains(offspring))) {
							population.add(offspring);
						}
					}

				}

			};
			crossoverTasks.add(crossoverTask);
		}

		/**
		 * Monitor the population and print to STDOUT when a new most-fit individual is detected
		 */
		Runnable monitoringTask = new Runnable() {

			float best = 0;

			@Override
			public void run() {
				Individual peek = population.get(0);
				if (peek.utility > best) {
					best = peek.utility;
					print(peek);
				}
			}

		};

		/**
		 * Regularly sort and cull the population to POPULATION_LIMIT, add a handful of random new individuals and perform a greedy test
		 * of the most fit individuals to see if any mutations would improve them (hill climb phase)
		 */
		Runnable groomingTask = new Runnable() {

			@Override
			public void run() {

				Collections.sort(population);

				while (population.size() > POPULATION_LIMIT) {
					population.remove(POPULATION_LIMIT);
				}

				for (int i = 0; i < 10; i++) {
					population.add(new Individual(random.nextInt(1 << 28), specs));
				}

				// Hill Climb
				for (int i = 0; i < 20; i++) {
					Individual individual = population.get(i);
					for (Mutater mutater : mutaters) {
						int mutated = mutater.mutate(individual.value);
						Individual individual2 = new Individual(mutated, specs);
						if (individual2.utility > individual.utility) {
							if (!(population.contains(individual2))) {
								population.add(individual2);
							}
						}
					}

				}
			}
		};

		crossoverTasks.stream().forEach(r -> crossoverExecutor.execute(r));
		supplementaryService.scheduleWithFixedDelay(groomingTask, 1000, 1000, TimeUnit.MILLISECONDS);
		supplementaryService.scheduleWithFixedDelay(monitoringTask, 10, 10, TimeUnit.MILLISECONDS);

		// We never actually get here. Just kill the process when we're happy with the results
		crossoverExecutor.shutdown();
		crossoverExecutor.awaitTermination(1000, TimeUnit.DAYS);

	}

	/**
	 * format bits in an integer as a readable string of bits (with spaces every 8 bits)
	 */
	private static String formatBits(int x) {
		String bits = String.format("%32s", Integer.toBinaryString(x)).replace(' ', '0');
		return bits.substring(0, 8) + " " + bits.substring(8, 16) + " " + bits.substring(16, 24) + " "
				+ bits.substring(24, 32);
	}

	private static void print(Individual individual) {
		SpecResult[] utilities = individual.utilities();

		System.out.printf("Candidate: %12.12s (%s), Utility: %4.4f\n", individual.value, formatBits(individual.value),
				individual.utility);

		for (SpecResult specResult : utilities) {
			System.out.printf("  (%7.4f) %s\n", specResult.utility, specResult.spec.description);
		}
		System.out.println();
	}

}
