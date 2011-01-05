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

import goap.pathfinding.NavNode;
import goap.pathfinding.NavigationMap;
import goap.scenario.helper.Helper;

import java.util.ArrayList;
import java.util.Random;


import com.jme.app.SimpleGame;
import com.jme.input.KeyBindingManager;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.shape.Box;
import com.jme.scene.state.MaterialState;
import com.jme.system.DisplaySystem;

/**
 * This class acts as the starting point of the simulation.
 * Its responsible for initializing and updating everything in the simulation.
 * @author Klinge
 *
 */
public class BaseGame extends SimpleGame{

	public static final int BOARDWIDTH = 200;
	public static final int BOARDHEIGHT = 200;
	public static final int TILESIZE = 5;
	private static int NUMPEDESTRIANS = 40;
	private static int MAXPEDESTRIANS = 80;
	public static boolean emotionsEnabled;

	private Box floor;
	public static ArrayList<Pedestrian> pedestrians;
	private NavigationMap navMap;
	private Node geometryNode;
	private boolean overViewMode = true;
	private int watchPedestrianNum = 0;
	private Pedestrian watchedPedestrian = null;
	private Random r = new Random();
	private double nextSpawnTime;
	private boolean paused = true;
	
	public static ArrayList<Vector3f> entrances = new ArrayList<Vector3f>();
	public static ArrayList<MarketStand> foodStands = new ArrayList<MarketStand>();

	@Override
	protected void simpleInitGame() {

		floor = new Box("floor", new Vector3f(100, 0, 100), 100, -0.1f,
				100);

		navMap = new NavigationMap(BOARDWIDTH / TILESIZE, BOARDHEIGHT / TILESIZE);

		geometryNode = new Node();
		rootNode.attachChild(geometryNode);
		rootNode.attachChild(floor);
		// this.showBounds = true;
		
		Float newValue = new Float(0.2f) - 0.6f;
		
		newValue += 1.5f;

		createFoodStands();
		createDemoPedestrians();
		//createNavBoxes();
		nextSpawnTime = System.currentTimeMillis() + r.nextInt(10000);
	}

	private void createFoodStands() {
		
		createFoodStand(7,7);
		createFoodStand(33,7);
		createFoodStand(7,33);
		//createFoodStand(33,33);
		createFoodStand(20,29);
		createFoodStand(20,10);
		createFoodStand(30,20);
	}
	
	private void createFoodStand(int x, int y){
		Vector3f pos = new Vector3f(x * BaseGame.TILESIZE + BaseGame.TILESIZE
				/ 2, 0, y * BaseGame.TILESIZE + BaseGame.TILESIZE / 2);
		Vector3f size = new Vector3f(3 * TILESIZE, 6, 2 * TILESIZE);

		MarketStand stand = new MarketStand(pos.x, pos.z, geometryNode, navMap,
				size);
		
		foodStands.add(stand);
	}

	/**
	 * Creates some pedestrians which initially populate the market
	 */
	private void createPedestrians() {
		pedestrians = new ArrayList<Pedestrian>();


		ArrayList<NavNode> free = new ArrayList<NavNode>();
		
		for(int x = 0; x < navMap.height; x++)
			for(int y = 0; y < navMap.width; y++)
				if(!navMap.nodes[x][y].blocked)
					free.add(navMap.nodes[x][y]);
		
		
		for(int i = 0; i < NUMPEDESTRIANS; i++)
		{
			NavNode n  = free.get(r.nextInt(free.size()));
			
			Vector3f pos = Helper.PointToLocalCords(n.x, n.y);
			Pedestrian p;
			
			if(emotionsEnabled)
				p = new Pedestrian(pos.x, pos.z, geometryNode, navMap);
			else
				p = new Pedestrian(pos.x, pos.z,
						geometryNode, navMap);
			
			p.stop();
			pedestrians.add(p);
			free.remove(n);
		}
	}
	
	private void createDemoPedestrians()
	{
		pedestrians = new ArrayList<Pedestrian>();
		
		Vector3f pos = Helper.PointToLocalCords(18, 38);

		Pedestrian p1;

		
		if(emotionsEnabled)
			p1 = new Pedestrian(pos.x, pos.z, geometryNode, navMap);
		else
			p1 = new Pedestrian(pos.x, pos.z,
					geometryNode, navMap);
		
//		p1.setHunger(1.0f);
		
		p1.stop();
		pedestrians.add(p1);
	}

	@Override
	protected void simpleUpdate() {
		super.simpleUpdate();

		updateKeyboardInput();	
		if(!paused)
			spawnPedestrians();
		
		if(DisplaySystem.getDisplaySystem().isClosing())
			onShutdown();
	}

	private void updateKeyboardInput() {
		KeyBindingManager manager = KeyBindingManager.getKeyBindingManager();
		
		if ( KeyBindingManager.getKeyBindingManager().isValidCommand( "exit",
                false ) ) {
            onShutdown();
        }
		
		if ( KeyBindingManager.getKeyBindingManager().isValidCommand( "stats",
                false ) ) {
            onShutdown();
        }
		
		if (manager.isValidCommand("pause", false))
		{
			paused = !paused; 
			
			if(paused)
				for (Pedestrian p : pedestrians)
					p.stop();
			else
				for (Pedestrian p : pedestrians)
					p.resume();
		}
		
		if(!paused)
			for (Pedestrian p : pedestrians)
					p.update();

		if (!overViewMode) {

			if (manager.isValidCommand("plus", false)) {
				if (++watchPedestrianNum > pedestrians.size() - 1)
					watchPedestrianNum = 0;
			}

			if (manager.isValidCommand("minus", false)) {
				if (--watchPedestrianNum < 0)
					watchPedestrianNum = pedestrians.size() - 1;			
			}
			
			updateWatchedPedestrian(pedestrians.get(watchPedestrianNum));

			cam.setLocation(watchedPedestrian.getLocalTranslation().clone());
			cam.getLocation().addLocal(
					watchedPedestrian.getDirection().clone().negate()
							.normalize().mult(20));
			cam.getLocation().y += 20;
			/** Move our camera to a correct place and orientation. */
			cam.lookAt(watchedPedestrian.getLocalTranslation().clone(),
					Vector3f.UNIT_Y);

			cam.update();
		}
	}
	
	private void updateWatchedPedestrian(Pedestrian p)
	{
		// Get a MaterialState
		MaterialState ms = DisplaySystem.getDisplaySystem().getRenderer().createMaterialState();
		// Give the MaterialState an emissive tint
		ms.setEmissive(ColorRGBA.green.clone());
		
		//reset the color of the old pedestrian that was beeing watched
		if(watchedPedestrian != null)
			watchedPedestrian.setMaterial(ms);
		
		//set the new pedestrian to be watched
		watchedPedestrian = p;
		
		ms = DisplaySystem.getDisplaySystem().getRenderer().createMaterialState();
		// Give the MaterialState an emissive tint
		ms.setEmissive(ColorRGBA.magenta.clone());
		
		watchedPedestrian.setMaterial(ms);
	}

	private void spawnPedestrians() {
		if (System.currentTimeMillis() > nextSpawnTime && pedestrians.size() < MAXPEDESTRIANS) {
			
			Vector3f entrancePos = entrances.get(r.nextInt(entrances.size()));
			
			if(emotionsEnabled)
				pedestrians.add(new Pedestrian(entrancePos.x, entrancePos.z,
						geometryNode, navMap));
			else
				pedestrians.add(new Pedestrian(entrancePos.x, entrancePos.z,
						geometryNode, navMap));

			nextSpawnTime = System.currentTimeMillis() + r.nextInt(2500);
		}
	}
	
	private void spawnFrightened(){
		Vector3f entrancePos = entrances.get(r.nextInt(entrances.size()));
	}

	public void onShutdown() {
		
//		for(int i = 0; i < pedestrians.size(); i++)
//			pedestrians.get(i).exit();
		
		
		finish();
	}
	
	public static void main(String[] args) throws Exception {
		
		if(args.length > 0 && args[0].equals("emotions"))
			emotionsEnabled = true;
		else
			emotionsEnabled = false;
		
		try {
			if(args.length > 1)
				MAXPEDESTRIANS = Integer.parseInt(args[1]);
			else if(args.length > 0)
				MAXPEDESTRIANS = Integer.parseInt(args[0]);
			
			if(MAXPEDESTRIANS < NUMPEDESTRIANS)
				NUMPEDESTRIANS = MAXPEDESTRIANS;
			
			
		} catch (Exception e) {
			System.out.println("not a valid number!\n Max pedestrian count set to 80");
		}

		
		// Instantiate StandardGame
		BaseGame game = new BaseGame();
		game.setConfigShowMode(ConfigShowMode.AlwaysShow);
		// Show settings screen
		game.start();
	}

}
