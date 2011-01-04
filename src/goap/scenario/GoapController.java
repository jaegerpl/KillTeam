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

import com.jme.scene.Controller;

/**
 * The GoapController is responsible for updating the GoapManager regularly.
 * @author Klinge
 *
 */
public class GoapController extends Controller{
	
	private GoapActionSystem actionSystem;
	private static final float UPDATETIME = 0.1f; //only update the plan every 100ms
	private float lastUpdate = 0;
	
	public GoapController(GoapActionSystem actionSystem)
	{
		this.actionSystem = actionSystem;
	}

	@Override
	public void update(float time) {
		
		lastUpdate += time;
		
		if(lastUpdate >= UPDATETIME)
		{
			actionSystem.selectAction();
			
			lastUpdate = 0;
		}
	}

}
