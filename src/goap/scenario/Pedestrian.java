package goap.scenario;

import goap.agent.Agent;
import goap.goap.Action;
import goap.goap.Goal;
import goap.goap.IGOAPListener;
import goap.pathfinding.NavigationMap;

import java.util.List;
import java.util.Random;


import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.shape.Box;
import com.jme.scene.shape.Sphere;
import com.jme.scene.state.MaterialState;

import de.lunaticsoft.combatarena.api.interfaces.IWorldInstance;
import de.lunaticsoft.combatarena.api.killteam.GlobalKI;

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

public class Pedestrian extends Agent implements IGOAPListener{

	private Box box;
	private Sphere visualRange;
	private static int number = 1;
//	private MovementContoller mc;

	private GoapController gc;
	private static Random r = new Random();
	public String name;
	public double time, startTime;
	

	public Pedestrian(float x, float y, Node node, NavigationMap navMap) {		
		
		name = "player" + number++;
		blackboard.name = name; // just for debugging
		
		actionSystem = new GoapActionSystem(this, blackboard,memory);	
		
		gc = new GoapController((GoapActionSystem)actionSystem);
		box.addController(gc);
		((GoapActionSystem)actionSystem).addGOAPListener(this);
		
		generateActions();
		generateGoals();
		generateRandomDesires();
		
//		mc = new MovementContoller(this);
//		box.addController(mc);

//		private AppearanceController ac;
//		((GoapActionSystem)actionSystem).addGOAPListener(ac);
		
		createNavLocations();
		
//		// Stroll = NavLocations
//		for(MemoryObject m : ((GoapActionSystem)actionSystem).getMemoryFacts("Stroll"))
//		{
//			Box box = new Box("entrance", Vector3f.ZERO, 0.5f, 0.5f, 0.5f);
//			box.setLocalTranslation(m.position);
//			node.attachChild(box);
//			
//			// Get a MaterialState
//			MaterialState mcs = DisplaySystem.getDisplaySystem().getRenderer().createMaterialState();
//			// Give the MaterialState an emissive tint
//			mcs.setEmissive(ColorRGBA.orange.clone());				
//			box.setRenderState(mcs);
//		}
//		
//		startTime = System.currentTimeMillis();
    }
	
	private void generateRandomDesires()
	{
//		((GoapActionSystem)actionSystem).currentWorldState.add(new WorldStateSymbol<Float>(TankWorldProperty.Boredom, r.nextFloat() % 1.0f, PropertyType.Float));
//		((GoapActionSystem)actionSystem).currentWorldState.add(new WorldStateSymbol<Float>(TankWorldProperty.Hunger, r.nextFloat() % 1.0f, PropertyType.Float));
//		((GoapActionSystem)actionSystem).currentWorldState.add(new WorldStateSymbol<Float>(TankWorldProperty.Exhaustion, r.nextFloat() % 0.2f, PropertyType.Float));
	}
	
	
	private void generateGoals(){
//		((GoapActionSystem)actionSystem).addGoal(new Explore("Explore",0.6f, (GoapActionSystem) actionSystem));
	}
	
	private void generateActions(){			
//		((GoapActionSystem)actionSystem).addAction(new WatchEntertainment((GoapActionSystem) this.actionSystem,"WatchEntertianment",1.0f));
	}
	
    private void createNavLocations(){
    	
//		for(Vector3f exit : BaseGame.entrances)
//		{
//			MemoryObject o = new MemoryObject(1.0f,"Exit", exit);
//			o.persistend = true;
//			memory.addMemory(o);
//		}
//
//		MemoryObject i = new MemoryObject(1.0f,"Stroll", new Vector3f(10 * BaseGame.TILESIZE + BaseGame.TILESIZE / 2, 0, 12* BaseGame.TILESIZE + BaseGame.TILESIZE / 2));
//		MemoryObject k = new MemoryObject(1.0f,"Stroll", new Vector3f(30 * BaseGame.TILESIZE+ BaseGame.TILESIZE / 2, 0, 12* BaseGame.TILESIZE+ BaseGame.TILESIZE / 2));
//		//MemoryObject v = new MemoryObject(1.0f,"Stroll", new Vector3f(20 * BaseGame.TILESIZE+ BaseGame.TILESIZE / 2, 0, 35* BaseGame.TILESIZE+ BaseGame.TILESIZE / 2));
//		MemoryObject c = new MemoryObject(1.0f,"Stroll", new Vector3f(30 * BaseGame.TILESIZE+ BaseGame.TILESIZE / 2, 0, 28* BaseGame.TILESIZE+ BaseGame.TILESIZE / 2));
//		MemoryObject j = new MemoryObject(1.0f,"Stroll", new Vector3f(10 * BaseGame.TILESIZE+ BaseGame.TILESIZE / 2, 0, 28* BaseGame.TILESIZE+ BaseGame.TILESIZE / 2));
//		//MemoryObject n = new MemoryObject(1.0f,"Food",Helper.navNodeToLocalCords(BaseGame.foodStands.get(0).getFreeSpots().get(0)));
//		i.persistend = true;
//		k.persistend = true;
//		memory.addMemory(i);
//		memory.addMemory(k);	
//		c.persistend = true;
//		memory.addMemory(c);
//		j.persistend = true;
//		memory.addMemory(j);
    }
	
	public Spatial getModel(){
		return box;
	}
	
	public Node getParent()
	{
		return box.getParent();
	}
	
	public String getName()
	{
		return box.getName();
	}
	
	public Vector3f getLocalTranslation()
	{
		return box.getLocalTranslation();
	}
	
	public Vector3f getDirection()
	{
		return blackboard.direction;
	}
	
	public List<Goal> getGoals()
	{
		return ((GoapActionSystem)actionSystem).getGoals();
	}
	
	public void exit()
	{
		stop();
//		mc.remove();
		box.removeFromParent();
		visualRange.removeFromParent();
	}
	
	public void setMaterial(MaterialState ms)
	{
		box.setRenderState(ms);
		box.updateRenderState();
	}
	
	public GoapActionSystem getActionSystem()
	{
		return (GoapActionSystem)actionSystem;
	}
	
	public String toString()
	{
		String s = "Name: " + name + "\n"
		+ "Goal: " + ((GoapActionSystem)actionSystem).getCurrentGoal() + "\n"
		+ "Action: " + ((GoapActionSystem)actionSystem).getCurrentAction() + "\n";
		
		return s;
	}
	
	public void stop()
	{
		time += System.currentTimeMillis() - startTime;
		box.removeController(gc);
//		box.removeController(mc);
		//box.removeController(ac);
	}
	
	public void resume()
	{
		if(!box.getControllers().contains(gc))
			box.addController(gc);
//		if(!box.getControllers().contains(mc))
//			box.addController(mc);
//		if(!box.getControllers().contains(ac))
//			box.addController(ac);
		
		startTime = System.currentTimeMillis();
	}

	@Override
	public void actionChangedEvent(Object sender, Action oldAction,
			Action newAction) {
	}

	@Override
	public void goalChangedEvent(Object sender, Goal oldGoal, Goal newGoal) {
		
		if(oldGoal != null){
			// TODO
		}
	}

	@Override
	public GlobalKI getGlobalKi() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IWorldInstance getWorld() {
		// TODO Auto-generated method stub
		return null;
	}
}
