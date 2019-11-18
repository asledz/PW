package alternator;

import petrinet.PetriNet;
import petrinet.Transition;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.Collection;

public class Main {
	
	private static enum Place {
        mutex, criticalA, criticalB, criticalC, finalA, finalB, finalC
    }		

    private static class Worker implements Runnable {
    	private Collection<Transition<Place>> acquire;
    	private Collection<Transition<Place>> release;
    	private PetriNet<Place> p;

    	public Worker (Transition<Place> acquire, Transition<Place> release, PetriNet<Place> p) {
    		this.release = Collections.singleton(release);
    		this.acquire = Collections.singleton(acquire);
    		this.p = p;
    	}

    	@Override
    	public void run () {
    		String name = Thread.currentThread().getName();
    		try {
    			while (true) {
    				p.fire(acquire);
    				System.out.print(name);
    				System.out.print('.');
    				p.fire(release);
    			}
    		} catch (InterruptedException e) {
    			
    		}
    	}

    }

	public static void main(String[] args) {
		PetriNet<Place> p = new PetriNet<Place>(Map.of(Place.mutex, 1), true);

		Map<Place, Integer> input = new HashMap<>();
    	Map<Place, Integer> output = new HashMap<>();
		Set<Place> reset = new HashSet<>();
		Set<Place> inhibitor = new HashSet<>();

		// MUTEXA
		input.put(Place.mutex, 1);
		output.put(Place.criticalA, 1);
		inhibitor.add(Place.finalA);

		Transition<Place> mutexA = new Transition<Place>(input, reset, inhibitor, output);
		inhibitor.clear();
		output.clear();

		// MUTEXB
		output.put(Place.criticalB, 1);
		inhibitor.add(Place.finalB);
		
		Transition<Place> mutexB = new Transition<Place>(input, reset, inhibitor, output);		
		inhibitor.clear();
		output.clear();	

		//MUTEXC
		output.put(Place.criticalC, 1);
		inhibitor.add(Place.finalC);
		
		Transition<Place> mutexC = new Transition<Place>(input, reset, inhibitor, output);		
		inhibitor.clear();
		output.clear();	

		// RELEASE 
		input.clear();
		// RELEASE A
		output.put(Place.mutex, 1);
		output.put(Place.finalA, 1);
		input.put(Place.criticalA, 1);
		reset.add(Place.finalB);
		reset.add(Place.finalC);

		Transition<Place> releaseA = new Transition<Place>(input, reset, inhibitor, output);
		output.clear();
		input.clear();
		reset.clear();

		// RELEASE B
		output.put(Place.mutex, 1);
		output.put(Place.finalB, 1);
		input.put(Place.criticalB, 1);
		reset.add(Place.finalA);
		reset.add(Place.finalC);

		Transition<Place> releaseB = new Transition<Place>(input, reset, inhibitor, output);
		output.clear();
		input.clear();
		reset.clear();

		// RELEASE C
		output.put(Place.mutex, 1);
		output.put(Place.finalC, 1);
		input.put(Place.criticalC, 1);
		reset.add(Place.finalA);
		reset.add(Place.finalB);

		Transition<Place> releaseC = new Transition<Place>(input, reset, inhibitor, output);
		output.clear();
		input.clear();
		reset.clear();

		Thread a = new Thread(new Worker(mutexA, releaseA, p));
		Thread b = new Thread(new Worker(mutexB, releaseB, p));
		Thread c = new Thread(new Worker(mutexC, releaseC, p));

		a.setName("A");
		b.setName("B");
		c.setName("C");

		a.start();
		b.start();
		c.start();

		try {
			Thread.sleep(30000);
			a.interrupt();
			b.interrupt();
			c.interrupt();
		} catch (InterruptedException e) {

		}

	}
}