/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pascal.goap.AStar;

import java.util.ArrayList;
import java.util.UUID;

import pascal.goap.AStar.heuristics.*;

/**
 * A single node in the search graph
 * http://www.cokeandcode.com/pathfinding
 * @author Klinge
 */
public abstract class Node implements Comparable
{
        /** The path cost for this node */
        public float g;
        /** The parent of this node, how we reached it in the search */
        public Node parent;
        /** The search depth of this node */
        public int depth;
        // The heuristic cost of this node
        public float h;
        // The overall cost to travel to this node
        public float f;

        //TODO: das muss als funktionspointer implementiert werden, damit nicht immer ne neue instanz angelegt werdenmuss
        //oder die ClosestHeuristic.getCost methode wird einfach static gemacht
        protected AStarHeuristic heuristic;

        public Node(){}

        public float calculateHeuristics(IMap map, Node end)
        {
            return heuristic.getCost(map, this, end);
        }

        /**
         * @see Comparable#compareTo(Object)
         */
        public int compareTo(Object other) {
                Node o = (Node) other;

                if (f < o.f) {
                        return -1;
                } else if (f > o.f) {
                        return 1;
                } else {
                        return 0;
                }
        }

        /**
         * Set the parent of this node
         *
         * @param parent The parent node which lead us to this node
         * @return The depth we have now reached in searching
         */
        public int setParent(Node parent) {
                depth = parent.depth + 1;
                this.parent = parent;

                return depth;
        }

    public boolean shouldPause() {
        return false;
    }

    public abstract Node clone();

    public abstract boolean isFinished(IMover mover, Node other);
}
