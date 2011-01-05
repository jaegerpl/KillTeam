/*
 * Copyright (C) 2009 Arne Klingenberg
 * E-Mail: klingenberg.a@googlemail.com
 * 
 * This software is free; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, see <http://www.gnu.org/licenses/>.
 */

package goap.scenario;
import static java.lang.Math.abs;
import static java.lang.Math.min;

import goap.agent.TankBlackboard;
import goap.astar.AStarMachine;
import goap.pathfinding.NavNode;
import goap.scenario.helper.Helper;

import java.util.ArrayList;
import java.util.Random;


import com.jme.intersection.BoundingPickResults;
import com.jme.intersection.PickResults;
import com.jme.math.Ray;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Controller;
import com.jme.scene.Line;
import com.jme.scene.shape.Box;
import com.jme.scene.state.MaterialState;
import com.jme.system.DisplaySystem;


/**
 * Is responsible for moving the agent through the world
 * @author Klinge
 *
 */
public class MovementContoller extends Controller{
	
	private Pedestrian p;
	private TankBlackboard blackboard;
	private float currentSpeed;
	private static AStarMachine<NavNode> pathfinder;
	private Vector3f direction = null;
	private Vector3f nextDestination = null;
	private static Random r = new Random();
	private ArrayList<Line> lines;
	private ColorRGBA clr[] = {ColorRGBA.white,ColorRGBA.white};
	private Line pickLine;
	
	public MovementContoller(Pedestrian p)
	{
		this.p = p;
		blackboard = p.getBlackboard();
		
		if(pathfinder == null)
			initPathfinding();
		
		//blackboard.path = pathfinder.findPath(null, Helper.getNavNode(navMap, p.getLocalTranslation()), new NavNode(7, 17));
		blackboard.currentNode = Helper.getNavNode(blackboard.navMap, p.getLocalTranslation());
//		blackboard.position = p.getLocalTranslation();
		//blackboard.destinationNode = blackboard.path.get(blackboard.path.size() - 1);
		
		lines = new ArrayList<Line>();
		
		Box box = new Box("abf", Vector3f.ZERO, 0.6f, 0.6f, 0.6f);
		MaterialState ms = DisplaySystem.getDisplaySystem().getRenderer().createMaterialState();
		// Give the MaterialState an emissive tint
		ms.setEmissive(ColorRGBA.red);

		box.setRenderState(ms);
		
		box.setLocalTranslation(Helper.navNodeToLocalCords(blackboard.navMap.nodes[7][17]));
		p.getParent().attachChild(box);
		
		
	}	
	
	@Override
	public void update(float time) {

		if(!blackboard.wait)
		{
			//if the pedestrian currently has a destination
			if(blackboard.destinationNode != null)
			{
				//but does not jet know how to reach it
				if((blackboard.path == null || blackboard.path.size() == 0))
				{
					//and is not at its desired location then calculate a new path
					if(blackboard.currentNode.x != blackboard.destinationNode.x || blackboard.currentNode.y != blackboard.destinationNode.y)
					{
						recalculatePath();
//						blackboard.path = pathfinder.findPath(null, blackboard.currentNode, blackboard.destinationNode);
					}
				}
			}
			
			move(time);
		}
	}
	
	public static int nextInt(int low, int high) {
		return min(low, high) + r.nextInt(abs(high - low));
		}
	

	
	private void initPathfinding() {		
		pathfinder = new AStarMachine<NavNode>(blackboard.navMap, 500);
	}
	
	private void move(float time){
		if(blackboard.path != null && blackboard.path.size() > 0){

			calcMovement();
					
			//calculate the new position
			Vector3f newPos = p.getLocalTranslation();
			newPos.addLocal(direction.mult(currentSpeed * time));
			//actually move the box
//			p.setLocalTranslation(newPos);
//			blackboard.position = p.getLocalTranslation().clone();
			blackboard.currentTranslation = p.getLocalTranslation();
			
			float distance = p.getLocalTranslation().distance(new Vector3f(nextDestination.x,p.getLocalTranslation().y,nextDestination.z));
			
			if(distance < 0.5 && blackboard.path != null)
			{
				blackboard.currentNode.blocked = false;
				blackboard.currentNode = Helper.getNavNode(blackboard.navMap, p.getLocalTranslation());
				blackboard.currentNode.blocked = true;
				
				if(blackboard.path.contains(blackboard.currentNode))
					blackboard.path.remove(blackboard.currentNode);
				
				if(lines != null && lines.size() > 0)
				{
					lines.get(0).removeFromParent();
					lines.remove(0);
				}
			}
		}
	}
	
	private void calcMovement()
	{   
		currentSpeed = blackboard.speed;
		
		//check if the nextNavNode of the previously calculated path has been blocked by
		//something in the meantime
		if(blackboard.navMap.nodes[blackboard.path.get(0).x][blackboard.path.get(0).y].blocked)
		{
			//if the next node is the last one in the path
			//wait for it to become unblocked (e.g let the pedestrian stop)
			if(blackboard.path.size() == 1)
			{
				currentSpeed = 0;
				return;
			}
			//otherwise just calculate a new path to the destination
			else 
				recalculatePath();
		}
	
		if(blackboard.path != null && blackboard.path.size() > 0)
		{
			nextDestination = Helper.navNodeToLocalCords(blackboard.path.get(0));			
			direction = p.getLocalTranslation().subtract(nextDestination.x,p.getLocalTranslation().y,nextDestination.z).normalize().negate();
		}
		
		 Vector3f psd= new Vector3f(p.getLocalTranslation().x + 2* direction.x ,p.getLocalTranslation().y,p.getLocalTranslation().z + 2* direction.z);
		 Ray ray = new Ray(psd, direction); 
         PickResults results = new BoundingPickResults();
         results.setCheckDistance(true);
         
         blackboard.direction = direction;
         
         p.getParent().findPick(ray,results);
         
         if(results.getNumber() > 0)
         {
		         float closest = results.getPickData(0).getDistance();	         
		         	         
		         if(closest < 0.5f)
		        	 recalculatePath();
		         else if(closest < 2)
		        	 currentSpeed = 1.0f;
		         else if(closest < 6)
		        	 currentSpeed = 5.0f;
		         else if(closest < 10)
		        	 currentSpeed = 8.0f;
		         
		         results.clear();
         }
         		     			
         if(pickLine!= null) {
        	 p.getParent().detachChild(pickLine);
         }
         	
         Vector3f endPoint = new Vector3f(psd.x + 10*direction.x,psd.y + 10* direction.y, psd.z+ 10*direction.z);
         pickLine = new Line("e",new Vector3f [] {psd, endPoint}, null, clr,null);
         p.getParent().attachChild(pickLine);
	}
	
	public void remove()
	{
		if(pickLine != null)
			pickLine.removeFromParent();
	}
	
	private void recalculatePath()
	{
		//save the current state of the destinationNode
		boolean blocked = blackboard.navMap.nodes[blackboard.destinationNode.x][blackboard.destinationNode.y].blocked;
		//unblock the destinationNode so that A* accepts it as a valid target
		blackboard.navMap.nodes[blackboard.destinationNode.x][blackboard.destinationNode.y].blocked = false;
		//calculate the new path
		blackboard.path = pathfinder.findPath(null, blackboard.currentNode, blackboard.destinationNode);	
		//reset the state of the destinationNode
		blackboard.navMap.nodes[blackboard.destinationNode.x][blackboard.destinationNode.y].blocked = blocked;
	}

}
