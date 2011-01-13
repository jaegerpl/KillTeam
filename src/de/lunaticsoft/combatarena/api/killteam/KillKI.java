//package de.lunaticsoft.combatarena.api.killteam;
//
//import java.awt.Point;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.Queue;
//import java.util.TreeMap;
//import java.util.concurrent.LinkedBlockingQueue;
//
//import map.fastmap.FastRoutableWorldMap;
//import map.fastmap.LinkedTile;
//import memory.map.MemorizedMap;
//import memory.objectStorage.MemorizedWorldObject;
//import memory.objectStorage.ObjectStorage;
//import memory.pathcalulation.Path;
//import battle.Battle;
//import battle.ShootTarget;
//
//import com.jme.math.FastMath;
//import com.jme.math.Vector3f;
//
//import de.lunaticsoft.combatarena.api.killteam.globalKI.GlobalKI;
//import de.lunaticsoft.combatarena.api.killteam.globalKI.StatusType;
//
//public class KillKI implements IPlayer {
//  private IWorldInstance world;
//  private Vector3f spawnDirection; // tanks direction
//  private Vector3f moveDirection; // tanks direction
//  private Vector3f pos; // takns last updated position
//  private Vector3f goalPosition; // tanks goal its heading to
//  private Vector3f currentDirection; // direction of tank during update()
//  private final Queue<Vector3f> lastPositions;
//  private EColors color;
//  private final String name;
//  private LinkedTile moveTarget;
//  Path<LinkedTile> path = null;
//  private boolean pathReset = false;
//  private boolean stop = false;
//  private boolean calibrated = false;
//  int viewRangeRadius = 0;
//  int viewRangeOffset = 0;
//  // my variables
//  private Vector3f spawnPos;
//  private Vector3f flagPos;
//  private Vector3f flagPosPath; //flaPos f�r die der Pfad berechnet wurde
//  private Task lastTask;
//  private LinkedTile lastPathTarget;
//  private ArrayList<IWorldObject> perceivedObjects;
//  private int WOExistanceUpdate = 0;
//  // GOAP STUFF
//  private final GlobalKI globalKI;
//  private final MemorizedMap memoryMap;
//  private final ObjectStorage objectStorage;
//  protected TankBlackboard blackboard;
//  private boolean calcNewDefendPath = true;
//  //should be true if we play CTF
//  private final boolean CTFmode = false;
//  private final boolean iHaveTheFlag = false;
//  private final boolean flagCollectet = false;
//  private final java.util.Random rand;
//  private LinkedTile myPosTile;
//
//  public KillKI(final String name, final GlobalKI globalKI, final Task task) {
//    //System.out.println("KillKI "+name+" gestartet");
//    this.blackboard = new TankBlackboard();
//    this.name = name;
//    this.memoryMap = globalKI.getWorldMap();
//    this.globalKI = globalKI;
//    this.objectStorage = globalKI.getObjectStorage();
//    lastPositions = new LinkedBlockingQueue<Vector3f>(2);
//    this.blackboard.curTask = task;
//    rand = new java.util.Random();
//  }
//
//  @Override
//  public void attacked(final IWorldObject competitor) {
//    final Vector3f enemy = competitor.getPosition();
//    String out = "Attacked by position " + enemy;
//    final Vector3f direction = world.getMyPosition().clone().subtract(enemy.clone()).negate();
//    final float distance = world.getMyPosition().distance(enemy);
//    out += "\r\nDistance to enemy " + distance;
//    final float speed = getSpeed(30, distance);
//    out += "\r\nSpeed " + speed;
//    world.shoot(direction, speed, 30);
//    // GOAP STUFF
//    blackboard.hitsTaken++;
//  }
//
//  /**
//   * Pruefe den Sichtbereich um daraus den Mittelpunkt und die Position des
//   * Tanks zum Mittelpunkt zu berechnen
//   */
//  private void calibrate() {
//    final Vector3f myPosition = world.getMyPosition();
//    final Vector3f myDirection = world.getMyDirection();
//    final Vector3f incrementor = myDirection.normalize();
//    Vector3f currPos = myPosition;
//    int counterFront = 0;
//    do
//    {
//      currPos = currPos.add(incrementor);
//      counterFront++;
//    }
//    while (world.getTerrainNormal(currPos) != null);
//    counterFront--;
//    currPos = myPosition;
//    int counterBack = 0;
//    do
//    {
//      currPos = currPos.subtract(incrementor);
//      counterBack++;
//    }
//    while (world.getTerrainNormal(currPos) != null);
//    counterBack--;
//    this.viewRangeRadius = (counterFront + counterBack) / 2;
//    this.viewRangeOffset = this.viewRangeRadius - counterBack;
//    System.out.println("Radius: " + viewRangeRadius);
//    System.out.println("Offset: " + viewRangeOffset);
//    System.out.println();
//    System.out.println("Front: " + counterFront);
//    System.out.println("Back: " + counterBack);
//    this.calibrated = true;
//  }
//
//  /**
//   * Ueberprueft die perceivte Realitaet mit der map und loescht nichtmehr
//   * vorhandene Object aus der Map
//   */
//  private void checkWorldObjectExistance() {
//    // tiles die der tank sehen kann
//    final List<LinkedTile> viewTiles = memoryMap.getTilesPossiblyInViewRange(world.getMyPosition());
//    // entferne hangars, die ich nicht sehen kann
//    final List<MemorizedWorldObject> sureThing = objectStorage.getObjectsAtTiles(viewTiles);
//    // entferne alle hangars aus sureThing die perceived wurden
//    for (final IWorldObject obj : perceivedObjects)
//    {
//      if (obj.getType() == EObjectTypes.Hangar)
//      {
//        if (sureThing.contains(obj))
//        {
//          final LinkedTile tile = memoryMap.getTileAtCoordinate(obj.getPosition());
//          sureThing.remove(obj);
//        }
//      }
//    }
//    // die verbleibenden hangars in sureThing h�tten perceived werden sollen, wurden aber nicht
//    // l�sche sie in ObjectStorage
//    for (final MemorizedWorldObject obj : sureThing)
//    {
//      objectStorage.removeObject(obj);
//      globalKI.tankStatusChanged(this, obj, StatusType.HangarRemoved);
//    }
//  }
//
//  @Override
//  public void collected(final IWorldObject worldObject) {
//    switch (worldObject.getType())
//    {
//      case Item:
//        if (blackboard.spottedToolBox != null)
//        {
//          if (blackboard.spottedToolBox.getPosition() == worldObject.getPosition())
//          {
//            blackboard.spottedToolBox = null;
//            blackboard.toolBoxCollected = true;
//            // TODO update object storage
//          }
//        }
//        break;
//      default:
//        // DO NOTHING
//        break;
//    }
//  }
//
//  public void defend() {
//    if (world.getMyPosition().distance(spawnPos) > 25)
//    {
//      blackboard.inHangar = false;
//    }
//    if (!blackboard.inHangar)
//    {
//      if (calcNewDefendPath)
//      {
//        final float distance = world.getMyPosition().distance(spawnPos);
//        //float distance = 10;
//        double x = 0d; // real part
//        double z = 0d; // imaginary part
//        path = new Path<LinkedTile>();
//        path.setCircleCourse(true);
//        for (int angle = 0; angle < 360; angle += 15)
//        {
//          x = distance * Math.cos(angle);
//          z = distance * Math.sin(angle);
//          final Vector3f d = spawnPos.clone();
//          d.x += x;
//          d.z += z;
//          path.addWaypoint(memoryMap.getTileAtCoordinate(d));
//        }
//        calcNewDefendPath = false;
//      }
//      moveTarget = path.getNextWaypoint();
//    }
//  }
//
//  @Override
//  public void die() {
//    final WorldObject wO = new WorldObject(null, color, this.pos, EObjectTypes.Item);
//    this.objectStorage.storeObject(wO.getPosition(), new MemorizedWorldObject(wO));
//    // GOAP STUFF
//    globalKI.removeTank(this); // deregister tank in globalKI
//  }
//
//  private void evalNextTask() {
//    if (blackboard.curTask == Task.DEFEND)
//      return;
//    else if (iHaveTheFlag && flagCollectet)
//    {
//      blackboard.curTask = Task.GoToBase;
//    }
//    else if (blackboard.curTask == Task.EXPLORE)
//    {
//      if (CTFmode)
//      {
//        if (pathToFlagKnown())
//        {
//          blackboard.curTask = Task.CTF;
//        }
//        blackboard.curTask = Task.EXPLORE;
//      }
//      else if (!objectStorage.getEnemyHangars().isEmpty())
//      {
//        blackboard.curTask = Task.RAPEaHANGAR;
//      }
//    }
//  }
//
//  public void explore() {
//    Vector3f targetPos = this.pos.add(spawnDirection.normalize().mult(30));
//    //n�chsten Wegpunkt als Ziel setzen, wenn wir bereits eine Route haben
//    if ((null != path) && !path.isEmpty())
//    {
//      if (myPosTile.equals(moveTarget))
//      {
//        moveTarget = path.getNextWaypoint();
//      }
//    }
//    else
//    {
//      targetPos = this.pos.add(spawnDirection.normalize().mult(60));
//      final LinkedTile targetTile = memoryMap.getTileAtCoordinate(targetPos);
//      if (!targetTile.isExplored())
//      {
//        //wenn das Ziel nicht betretbar ist, die aktuelle richtung �ndern und in erneutem durchlauf weitersuchen
//        if (targetTile.isPassable())
//        {
//          path = memoryMap.calculatePath(myPosTile, targetTile);
//          if (path.isEmpty())
//          {
//            this.spawnDirection = rotateVector(this.spawnDirection, 10);
//            moveTarget = null;
//          }
//        }
//        else
//        {
//          //Rotieren und weitersuchen
//          this.spawnDirection = rotateVector(this.spawnDirection, 10);
//          moveTarget = null;
//        }
//        //von der map unexplored tile besorge, wenn tile bereits exploriert wurde
//      }
//      else
//      {
//        final TreeMap<Integer, LinkedTile> sortedTiles = memoryMap.getUnexploredTilesSortedByDistance(pos);
//        for (final LinkedTile tile : sortedTiles.values())
//        {
//          path = memoryMap.calculatePath(myPosTile, tile);
//          if (!path.isEmpty())
//          {
//            moveTarget = path.getNextWaypoint();
//            break;
//          }
//        }
//      }
//    }
//  }
//
//  public GlobalKI getGlobalKi() {
//    return globalKI;
//  }
//
//  public String getName() {
//    return name;
//  }
//
//  /**
//   * y = speed * time * sin(angle) - (gravity / 2) * time^2 y -> 0 <br>
//   * 0 = speed * time * sin(angle) - (gravity / 2) * time^2<br>
//   * <br>
//   * distance = speed * time * cos(angle)<br>
//   * speed = distance / (time * cos(angle))<br>
//   * <br>
//   * einsetzen: <br>
//   * 0 = (distance / (time * cos(angle))) * time * sin(angle) - (gravity / 2) *
//   * time^2 <br>
//   * umstellen und 'time' kürzen: <br>
//   * 0 = (distance * (sin(angle) / cos(angle))) - (gravity / 2) * time^2
//   * sin(angle) / cos(angle) <br>
//   * -> tan(angle) 0 = (distance * tan(angle)) - (gravity / 2)* time^2 |
//   * (gravity / 2) (gravity / 2) <br>
//   * -> 49.05f 0 = (distance * tan(angle)) - (gravity / 2) * time^2<br>
//   * <br>
//   * time^2 = (distance / 45.05f) * tan(angle) time = sqrt((distance / 45.05f) *
//   * tan(angle))<br>
//   * <br>
//   * distance = speed * time * cos(angle) <br>
//   * umstellen:<br>
//   * speed = distance / (cos(angle) * time)<br>
//   */
//  public float getSpeed(final float angleDeg, final float distance) {
//    // Bogenmaß
//    final float angle = angleDeg / FastMath.RAD_TO_DEG;
//    // gravity = 98.1f -> gravity/2 = 49.05f
//    final float time = FastMath.sqrt((distance / 49.05f) * FastMath.tan(angle));
//    final float speed = distance / (FastMath.cos(angle) * time);
//    return speed;
//  }
//
//  public IWorldInstance getWorld() {
//    return world;
//  }
//
//  /**
//   * Berechnet einen Weg zum Zielhangar, bewegt den Tank zum Zielhangar und
//   * aktuallisiert die Hangars in der Karte
//   */
//  public void goToHangar() {
//    final Vector3f goalPos = blackboard.spottedHangar.getPosition();
//    final Vector3f myPos = world.getMyPosition();
//    final LinkedTile targetTile = memoryMap.getTileAtCoordinate(goalPos);
//    final LinkedTile currentTile = memoryMap.getTileAtCoordinate(myPos);
//    // berechne Weg zum Hangar
//    if (path == null)
//    {
//      path = memoryMap.calculatePath(currentTile, targetTile);
//    }
//    // fahre den Pfad entlang
//    if ((null != path) && !path.isEmpty())
//    {
//      //System.out.println("Path ist nicht NULL!");
//      if (currentTile.equals(moveTarget))
//      {
//        //System.out.println("Zwischenziel erreicht");
//        moveTarget = path.getNextWaypoint();
//      }
//    }
//    // pruefe ob du am Hangar bist und ob er noch existiert
//    if (currentTile.equals(targetTile))
//    {
//      checkWorldObjectExistance();
//    }
//  }
//
//  private void goToTarget(final LinkedTile target) {
//    if (pathReset)
//    {
//      flagPosPath = flagPos;
//      path = memoryMap.calculatePath(memoryMap.getTileAtCoordinate(pos), target);
//      pathReset = false;
//    }
//    if (myPosTile.equals(moveTarget))
//    {
//      moveTarget = path.getNextWaypoint();
//    }
//    if (path.isEmpty())
//    {
//      pathReset = true;
//    }
//  }
//
//  private boolean pathToFlagKnown() {
//    if (memoryMap.calculatePath(memoryMap.getTileAtCoordinate(pos), memoryMap.getTileAtCoordinate(flagPos)).isEmpty())
//      return false;
//    return true;
//  }
//
//  @Override
//  public void perceive(final ArrayList<IWorldObject> worldObjects) {
//    perceivedObjects = worldObjects;
//    boolean hangarDiscovered = false;
//    // move WorldObjects into WorkingMemory
//    for (final IWorldObject wO : worldObjects)
//    {
//      switch (wO.getType())
//      {
//        case Competitor:
//          if (wO.getColor() != this.color)
//          {
//            this.objectStorage.storeObject(wO.getPosition(), new MemorizedWorldObject(wO));
//            //System.out.println("Panzer gefunden: " + wO.hashCode());
//            final ShootTarget target = Battle.getShootTarget(wO.getPosition(), this.pos);
//            world.shoot(target.direction, target.force, target.angle);
//            //System.out.println("Feind entdeckt");
//          }
//          break;
//        case Hangar:
//          if (wO.getColor() != this.color)
//          {
//            this.objectStorage.storeObject(wO.getPosition(), new MemorizedWorldObject(wO));
//            final ShootTarget target = Battle.getShootTarget(wO.getPosition(), this.pos);
//            world.shoot(target.direction, target.force, target.angle);
//            //System.out.println("feindlichen Hangar entdeckt");
//            globalKI.tankStatusChanged(this, wO, StatusType.HangarFound);
//            hangarDiscovered = true;
//          }
//          break;
//        case Item:
//          this.objectStorage.storeObject(wO.getPosition(), new MemorizedWorldObject(wO));
//          //System.out.println("Item entdeckt");
//          break;
//        /*
//         * case Flag: iHaveTheFlag = true; break;
//         */
//        default:
//          //System.out.println("Kein WO");
//      }
//    }
//    if (hangarDiscovered)
//    {
//      stop = true;
//    }
//    else
//    {
//      stop = false;
//    }
//    //ShootTarget target = Battle.getShootTarget(worldObject.getPosition(), world.getMyPosition());
//    //world.shoot(target.direction, target.force, target.angle);*/      
//  }
//
//  private void rapeHangar() {
//    final Map<Point, MemorizedWorldObject> enemyHangars = objectStorage.getEnemyHangars();
//    final MemorizedWorldObject[] hangars = new MemorizedWorldObject[enemyHangars.values().size()];
//    enemyHangars.values().toArray(hangars);
//    if (hangars[0] != null)
//    {
//      System.out.println("hangars:" + hangars[0]);
//      final LinkedTile target = memoryMap.getTileAtCoordinate(hangars[0].getPosition());
//      if (target != lastPathTarget)
//      {
//        pathReset = true;
//      }
//      System.out.println("gehe zu hangar");
//      goToTarget(target);
//    }
//    else
//    {
//      blackboard.curTask = Task.EXPLORE;
//    }
//  }
//
//  private Vector3f rotateVector(final Vector3f vec, final float phi) {
//    final Vector3f result = vec.clone();
//    result.x = vec.x * FastMath.cos(FastMath.DEG_TO_RAD * phi) - vec.z * FastMath.sin(FastMath.DEG_TO_RAD * phi);
//    result.z = vec.z * FastMath.cos(FastMath.DEG_TO_RAD * phi) + vec.x * FastMath.sin(FastMath.DEG_TO_RAD * phi);
//    return result;
//  }
//
//  private void scanTerrain() {
//    final List<LinkedTile> tiles = this.globalKI.getWorldMap().getTilesPossiblyInViewRange(this.pos);
//    for (final LinkedTile tile : tiles)
//    {
//      scanTile(tile);
//    }
//    //Direkt voraus gucken
//    final Vector3f voraus = pos.add(world.getMyDirection().normalize().mult(2));
//    if (null == voraus)
//    {
//      System.out.println("Voraus ist NULL!!!");
//    }
//    if ((voraus.x > 10000) || (voraus.z > 10000))
//    {
//      System.out.println("Alerm!");
//    }
//    if ((null != world.getTerrainNormal(voraus)) && !world.isPassable(voraus))
//    {
//      final LinkedTile tileVoraus = memoryMap.getTileAtCoordinate(voraus);
//      memoryMap.exploreTile(tileVoraus, tileVoraus.isWater(), false, tileVoraus.getNormalVector());
//    }
//    if (WOExistanceUpdate == 10)
//    {
//      checkWorldObjectExistance();
//      WOExistanceUpdate = 0;
//    }
//    else
//    {
//      WOExistanceUpdate++;
//    }
//  }
//
//  private void scanTile(final LinkedTile tile) {
//    if (!tile.isExplored())
//    {
//      if ((tile.mapIndex.x > 60) || (tile.mapIndex.y > 60) || (tile.mapIndex.x < 0) || (tile.mapIndex.y < 0))
//      {
//        System.out.println("Debug mich");
//      }
//      boolean isPassable = true;
//      boolean isWater = false;
//      final Vector3f tileCenter = tile.getTileCenterCoordinates();
//      final int increment = (int) FastMath.ceil(FastRoutableWorldMap.tilesize / 4.0f);
//      final List<Vector3f> scanPositions = new ArrayList<Vector3f>();
//      scanPositions.add(tileCenter);
//      scanPositions.add(tileCenter.add(new Vector3f(increment, 0, increment)));
//      scanPositions.add(tileCenter.add(new Vector3f(-increment, 0, increment)));
//      scanPositions.add(tileCenter.add(new Vector3f(increment, 0, -increment)));
//      scanPositions.add(tileCenter.add(new Vector3f(-increment, 0, -increment)));
//      for (final Vector3f scanPosition : scanPositions)
//      {
//        //if(memoryMap.positionIsInViewRange(pos, currentDirection, position) && world);
//        final Vector3f terrainNormal = world.getTerrainNormal(scanPosition);
//        if (null == terrainNormal)
//        {
//          if (memoryMap.positionIsInViewRange(pos, currentDirection, scanPosition))
//          {
//            //Ausserhalb der Map
//            memoryMap.markTileAsOutOfMap(tile);
//            return;
//          }
//          else
//            //nicht in Sichtweite
//            return;
//        }
//        if (world.isWater(scanPosition))
//        {
//          isWater = true;
//          isPassable = false;
//          memoryMap.exploreTile(tile, isWater, isPassable, tileCenter);
//          return;
//        }
//        if (!world.isPassable(scanPosition))
//        {
//          isPassable = false;
//        }
//      }
//      memoryMap.exploreTile(tile, isWater, isPassable, tileCenter);
//      return;
//    }
//  }
//
//  @Override
//  public void setColor(final EColors color) {
//    this.color = color;
//  }
//
//  @Override
//  public void setWorldInstance(final IWorldInstance world) {
//    this.world = world;
//    globalKI.setWorldInstance(world);
//  }
//
//  @Override
//  public void spawn() {
//    spawnPos = world.getMyPosition();
//    //System.out.println("StartPos: "+startPos);
//    spawnDirection = world.getMyDirection();
//    goalPosition = spawnPos.add(new Vector3f(1, 0, 1));
//    goalPosition.x = (int) goalPosition.x;
//    goalPosition.z = (int) goalPosition.z;
//    //      goalPosition = new Vector3f(300f,18f,300f);
//    //System.out.println("goalPosition: "+goalPosition);
//    // GOAP STUFF
//    globalKI.registerTank(this); // register tank in globalKI
//    blackboard.direction = spawnDirection;
//    blackboard.inHangar = true;
//  }
//
//  /**
//   * Prueft, ob sich der Panzer zu wenig bewegt hat.
//   * @return
//   */
//  private boolean stuck() {
//    boolean stuck = false;
//    if (lastPositions.size() > 1)
//    {
//      final Vector3f comparisonPosition = this.lastPositions.poll();
//      if ((comparisonPosition != null) && (Math.abs(comparisonPosition.distance(pos)) < 0.01f))
//      {
//        stuck = true;
//      }
//    }
//    this.lastPositions.add(pos);
//    return stuck;
//  }
//
//  @Override
//  public void update(final float interpolation) {
//    if (!calibrated)
//    {
//      calibrate();
//    }
//    evalNextTask(); //update Mission/Task of the tank       
//    // current position
//    pos = world.getMyPosition();
//    currentDirection = world.getMyDirection();
//    myPosTile = memoryMap.getTileAtCoordinate(pos);
//    //scan unknown terrain
//    scanTerrain();
//    //Pruefen ob durch neue Erkundung das Zwischenziel nicht mehr betretbar ist
//    if (((null != moveTarget) && (!moveTarget.isPassable() || !myPosTile.isPassable())) || (lastTask != blackboard.curTask))
//    {
//      world.stop();
//      if (!pathReset)
//      {
//        path = null;
//        moveTarget = null;
//        pathReset = true;
//      }
//    }
//    else
//    {
//      pathReset = false;
//    }
//    if (this.blackboard.curTask == Task.DEFEND)
//    {
//      defend();
//    }
//    else if (iHaveTheFlag && flagCollectet)
//    {
//      goToTarget(memoryMap.getTileAtCoordinate(spawnPos));
//    }
//    else if (this.blackboard.curTask == Task.EXPLORE)
//    {
//      explore();
//    }
//    else if (this.blackboard.curTask == Task.CTF)
//    {
//      if (flagPos != flagPosPath)
//      {
//        pathReset = true;
//      }
//      goToTarget(memoryMap.getTileAtCoordinate(flagPos));
//    }
//    else if (this.blackboard.curTask == Task.RAPEaHANGAR)
//    {
//      rapeHangar();
//    }
//    if (moveTarget != null)
//    {
//      final Vector3f newDirection = moveTarget.getTileCenterCoordinates().subtract(pos);
//      moveDirection = newDirection;
//    }
//    if (stuck())
//    {
//      System.out.println(name + " steckt fest. Sein Ziel ist " + moveTarget + "seine Aufgabe: " + blackboard.curTask + "und er befindet sich an Position " + memoryMap.getTileAtCoordinate(pos) + " seine aktuellen Wegpunkte: " + path);
//      final int offset = rand.nextInt(180);
//      final int alpha = 90 + offset;
//      final Vector3f unstuckDirection = rotateVector(world.getMyDirection(), alpha);
//      moveDirection = unstuckDirection;
//    }
//    world.move(moveDirection);
//    lastTask = blackboard.curTask;
//  }
//}
