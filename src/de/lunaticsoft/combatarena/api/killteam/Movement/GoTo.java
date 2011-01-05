package de.lunaticsoft.combatarena.api.killteam.Movement;

import goap.goap.WorldStateSymbol;
import goap.scenario.GoapActionSystem;

import java.util.ArrayList;

import de.lunaticsoft.combatarena.api.interfaces.IWorldInstance;
import de.lunaticsoft.combatarena.api.killteam.GlobalKI;
import map.fastmap.LinkedTile;


public class GoTo extends goap.goap.Action {
	private LinkedTile target;
	private IWorldInstance world;
	private GlobalKI globalKi;

	public GoTo(float cost, ArrayList<WorldStateSymbol> preCond,
			ArrayList<WorldStateSymbol> effect, GoapActionSystem gas,
			LinkedTile target) {
		super(cost, preCond, effect, gas);
		this.target = target;
		this.world = gas.getOwner().getWorld();
		this.globalKi = gas.getOwner().getGlobalKi();
	}

	@Override
	public boolean isValid() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isFinished() {
		if(globalKi.getWorldMap().getTileAtCoordinate(world.getMyPosition()).equals(target))
			return true;
		return false;
		
	}

	@Override
	public boolean contextPreconditionsMet() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void performAction() {
		// TODO Auto-generated method stub
		
	}



}
