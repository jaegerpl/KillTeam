package goap.agent;

import de.lunaticsoft.combatarena.api.enumn.EColors;
import de.lunaticsoft.combatarena.api.enumn.EObjectTypes;

/**
 * Describes the WorldObjects of TankArena in a way it can be used for GOAP
 * 
 * @author Pascal J�ger
 *
 */
public class MemoryObjectType {
	
	public EObjectTypes type;
	public EColors color;
	
	public MemoryObjectType(EObjectTypes objectType, EColors teamColor) {
		type = objectType;
		color = teamColor;
	}

}
