package petrinet;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Semaphore;
import java.util.LinkedList;
import java.util.Queue;


public class PetriNet<T> {

	private boolean fair; 
	private Semaphore mutex;
	private Map<T, Integer> tokens;
	private Collection<Semaphore> awaiting;

	/*
		Funkcje konstruktora
		0. Usuwa wejściowe wierzchołki z mapy tokenów, gdzie ich ilość jest 0
		1. Konstruktor

	*/

	private void deleteEmptyTokens () {
		Set<T> toRemove = new HashSet<T>();
		for(Map.Entry<T, Integer> tokens : this.tokens.entrySet()) {
			T where = tokens.getKey();
			Integer amount = tokens.getValue();
			if(amount == 0) {
				toRemove.add(where);
			}
		}
		for(T t : toRemove) {
			this.tokens.remove(t);
		}    
	}

    public PetriNet(Map<T, Integer> initial, boolean fair) {
    	this.mutex = new Semaphore(1, fair);
		this.tokens = new HashMap<T, Integer>(initial);
    	deleteEmptyTokens();
		if (fair) {
    		this.awaiting = new ArrayList<Semaphore>();
    	}
    	else {
    		this.awaiting = new HashSet<Semaphore>();
    	}
    }

    /*
		Rozwiązanie	(opisy metod z moodle)

		1. Reachable
		Metoda reachable(transitions) próbuje wyznaczyć zbiór wszystkich znakowań sieci, które są osiągalne (ang. reachable) z aktualnego jej stanu w rezultacie odpalenia, zero lub więcej razy, przejść z kolekcji transitions.
		Jeśli zbiór osiągalnych znakowań jest skończony, to jest on wynikiem metody. Mapa m, należąca do tego zbioru, reprezentuje znakowanie, w którym miejsce ma niezerową liczbę żetonów wtedy i tylko wtedy, gdy jest elementem m.keySet(), przy czym m.get(x) jest liczbą żetonów w miejscu x.
		Jeśli zbiór osiągalnych znakowań jest nieskończony, to wykonanie metody może się zapętlić lub zostać przerwane wyjątkiem

		2. Fire
		Metoda fire(transitions) dostaje jako argument niepustą kolekcję przejść. Wstrzymuje wątek, jeśli żadne przejście z tej kolekcji nie jest dozwolone. Gdy w kolekcji są przejścia dozwolone, metoda odpala jedno z nich, dowolnie wybrane. Wynikiem metody jest odpalone przez nią przejście.
		Jeżeli sieć Petriego została utworzona konstruktorem z argumentem fair równym true, to spośród tych wątków wstrzymanych metodą fire(transitions), które w danej chwili można wznowić, wybierany jest wątek czekający najdłużej.
		W przypadku przerwania wątku, metoda fire(transitions) zgłasza wyjątek InterruptedException.

    */

    public Set<Map<T, Integer>> reachable(Collection<Transition<T>> transitions) {
    	Set<Map<T, Integer>> solution = new HashSet<>();
    	Map<T, Integer> copyToken = new HashMap<T, Integer>();

    	try {
    		mutex.acquire();
    		copyToken = new HashMap<T, Integer>(tokens);
    		mutex.release();
    	} catch (InterruptedException e) {
    		
    	}

    	createAll(transitions, solution, copyToken);
    	return solution;
    }


    public Transition<T> fire(Collection<Transition<T>> transitions) throws InterruptedException {
		Semaphore stopIfNone = new Semaphore(0);
		while(true) {
			mutex.acquire();
			for (Transition<T> t : transitions) {
				if(createNextState(tokens, t)) {
					for(Semaphore s : awaiting) {
						s.release();
					}
					awaiting.clear();
					mutex.release();
					return t;
				}
			}
			awaiting.add(stopIfNone);
			mutex.release();
			stopIfNone.acquire();
		}        
    }

    /*
		Funkcje pomocnicze
    */

	private void createNewState (Transition<T> transition, Set<Map<T, Integer>> solution, Map<T, Integer> currState, Queue<Map<T, Integer>> q) {
		if(createNextState(currState, transition)) {
			if (!solution.contains(currState)) {
				solution.add(currState);
				q.add(currState);
			}
		}
	} 


    private void createAll (Collection<Transition<T>> transitions, Set<Map<T, Integer>> solution, Map<T, Integer> tokens) {
    	Queue<Map<T, Integer>> q = new LinkedList<>();
    	q.add(tokens);
    	solution.add(tokens);
    	while(q.size() > 0) {
    		Map<T, Integer> newStateBase = new HashMap<>(q.remove());

    		for(Transition<T> t : transitions) {
    			Map<T, Integer> newState = new HashMap<>(newStateBase);
    			createNewState(t, solution, newState, q);
    		}
    	
    	}
    }
    
    private boolean isEnabled (Map<T, Integer> tokens, Transition<T> transition) {
    	for (Map.Entry<T, Integer> input : transition.getInput().entrySet()) {
   
    		T where = input.getKey();
    		Integer weight = input.getValue();
    		if ( tokens.getOrDefault(where,0) < weight ) {
    			return false;
    		} 
    	}
    	for (T where : transition.getInhibitor() ) {
    		if (tokens.getOrDefault(where,0) > 0) {
    			return false;
    		}
    	}
    	return true;
    }


    private void moveInput (Map<T, Integer> tokens, Transition<T> transition) {
		for (Map.Entry<T, Integer> input : transition.getInput().entrySet()) {
    		T where = input.getKey();
    		Integer weight = input.getValue();
    		Integer howMuch = tokens.get(where);

    		tokens.replace(where, howMuch - weight);
    		tokens.remove(where, 0); // usuwa, jesli przesuwamy wszystkie żetony
    	}    	
    }

    private void moveOutput(Map<T, Integer> tokens, Transition<T> transition){
    	for(Map.Entry<T, Integer> output : transition.getOutput().entrySet()) {
    		T where = output.getKey();
    		Integer weight = output.getValue();
    		Integer howMuch = tokens.getOrDefault(where, 0);

    		if(tokens.containsKey(where)){
    			tokens.replace(where, howMuch + weight);
    		}
    		else {
    			tokens.put(where, weight);
    		}
    	}
    }

    private void setReset (Map<T, Integer> tokens, Transition<T> transition) {
    	for (T where : transition.getReset() ) {
    		if(tokens.containsKey(where)) {
    			tokens.remove(where);
    		}
    	}
    }
    // Zakładamy, że dane jest poprawne na wejściu
    private boolean createNextState(Map<T, Integer> tokens, Transition<T> transition) {
    	if(!isEnabled(tokens, transition)) {
    		return false;
    	}

    	moveInput(tokens, transition);
 		moveOutput(tokens, transition);
 		setReset(tokens, transition);

    	return true;
    }

}
