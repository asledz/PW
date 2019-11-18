package multiplicator;

import petrinet.PetriNet;
import petrinet.Transition;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;


public class Main {
	
    private static enum Place {
        inputA, inputB, result, singleton, right, down
    }	
	
    private static class Worker implements Runnable {
    	private Set<Transition<Place>> t;
    	private PetriNet<Place> p;
 		private int numOfTransition = 0;

    	public Worker (Set<Transition<Place>> t, PetriNet<Place> p) {
    		this.t = t;
    		this.p = p;
    	}

    	@Override
    	public void run () {
    		try {
    			while (true) {
    				p.fire(t);
    				numOfTransition++;
    			}
    		} catch (InterruptedException e) {
    			System.out.println(numOfTransition);
    		}
    	}

    }

	public static void main(String[] args) {
		int n=16, m=10;

		PetriNet<Place> p = new PetriNet<Place>(Map.of(Place.singleton, 1, Place.inputA, n, Place.inputB, m), true);

		Map<Place, Integer> input = new HashMap<>();
    	Map<Place, Integer> output = new HashMap<>();
		Set<Place> reset = new HashSet<>();
		Set<Place> inhibitor = new HashSet<>();

		Set<Transition<Place>> t = new HashSet<>();

		input.put(Place.inputA, 1);
		input.put(Place.right, 1);
		output.put(Place.result, 1);
		output.put(Place.right, 1);
		output.put(Place.down, 1);

		t.add(new Transition<Place>(input, reset, inhibitor, output));
		input.clear();
		output.clear();

		output.put(Place.right, 1);
		input.put(Place.singleton, 1);
		input.put(Place.inputB, 1);
		inhibitor.add(Place.down);

		t.add(new Transition<Place>(input, reset, inhibitor, output));
		input.clear();
		output.clear();
		inhibitor.clear();

		output.put(Place.inputA, 1);
		output.put(Place.singleton, 1);
		input.put(Place.singleton, 1);
		input.put(Place.down, 1);

		t.add(new Transition<Place>(input, reset, inhibitor, output));
		input.clear();
		output.clear();

		input.put(Place.right, 1);
		output.put(Place.singleton, 1);
		inhibitor.add(Place.inputA);
	
		t.add(new Transition<Place>(input, reset, inhibitor, output));
		input.clear();
		output.clear();
		inhibitor.clear();

		input.put(Place.singleton, 1);
		inhibitor.add(Place.inputB);

		Transition<Place> finish = new Transition<Place>(input, reset, inhibitor, output);
		input.clear();
		output.clear();
		inhibitor.clear();

		Thread threads[] = new Thread[4];
		for(int i = 0; i < 4; i++) {
			threads[i] = new Thread(new Worker(t, p));
			threads[i].start();
		}

		try {
			p.fire(Collections.singleton(finish));
		} catch (InterruptedException e) {
			System.out.println("Przerwany główny wątek");
		}

		Set<Map<Place, Integer>> myResult = p.reachable(t);
		for (Map<Place, Integer> res : myResult) {
			System.out.println("Wynik mnożenia "+ n + "*" + m + " = " + res.getOrDefault(Place.result, 0));
		}
		for (Thread x : threads) {
			x.interrupt();
		}

	}
}