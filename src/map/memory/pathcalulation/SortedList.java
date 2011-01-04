package map.memory.pathcalulation;

import java.util.Iterator;
import java.util.PriorityQueue;

public class SortedList<E> extends PriorityQueue<E> {

	private static final long serialVersionUID = -44399310437985671L;
	
	public E getElementEqualTo(E other) {
		Iterator<E> it = this.iterator();
		while(it.hasNext()) {
			E element = it.next();
			if(element.equals(other)) {
				return element;
			}
		}
		return null;
	}

}
