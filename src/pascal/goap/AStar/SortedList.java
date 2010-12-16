/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pascal.goap.AStar;

import java.util.ArrayList;
import java.util.Collections;

/**
	 * A simple sorted list
	 *
	 * @author kevin
 * http://www.cokeandcode.com/pathfinding
	 */
	 public class SortedList extends ArrayList{

		/**
		 * Retrieve the first element from the list
		 *
		 * @return The first element from the list
		 */
		public Object first() {
			return super.get(0);
		}

		/**
		 * Empty the list
		 */
		public void clear() {
			super.clear();
		}

		/**
		 * Add an element to the list - causes sorting
		 *
		 * @param o The element to add
		 */
		public boolean add(Object o) {
			super.add(o);
			Collections.sort(this);
			return true;
		}

        public Object get(int i)
        {
            return super.get(i);
        }
        
        public Object get(Object o)
        {
        	if(super.contains(o))
        		return super.get(super.indexOf(o));
        	
        	return null;
        }

		/**
		 * Remove an element from the list
		 *
		 * @param o The element to remove
		 */
		public boolean remove(Object o) {
			return super.remove(o);
		}

		/**
		 * Get the number of elements in the list
		 *
		 * @return The number of element in the list
 		 */
		public int size() {
			return super.size();
		}

		/**
		 * Check if an element is in the list
		 *
		 * @param o The element to search for
		 * @return True if the element is in the list
		 */
		public boolean contains(Object o) {
			return super.contains(o);
		}
		
		public void sort()
		{
			Collections.sort(this);
		}
	}
