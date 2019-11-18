package petrinet;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;

public class Transition<T> {

	private final Map<T, Integer> input;
	private final Map<T, Integer> output;
	private final Collection<T> reset;
	private final Collection<T> inhibitor;
	
    public Transition(Map<T, Integer> input, Collection<T> reset, Collection<T> inhibitor, Map<T, Integer> output) {
        this.input = new HashMap<>(input);
        this.output = new HashMap<>(output);
        this.reset = new HashSet<>(reset);
        this.inhibitor = new HashSet<>(inhibitor); 
    }

    
    public Map<T, Integer> getInput() {
    	return this.input;
    }

    public Map<T, Integer> getOutput() {
    	return this.output;
    }

    public Collection<T> getReset() {
    	return this.reset;
    } 

    public Collection<T> getInhibitor() {
    	return this.inhibitor;
    }
}