package debug;

import java.awt.Point;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Queue;

import map.fastmap.LinkedTile;
import memory.map.MemorizedMap;
import memory.objectStorage.MemorizedWorldObject;
import memory.objectStorage.ObjectStorage;

public class MapServer implements Runnable{
	ServerSocket sock;
	
	MemorizedMap map = null;
	ObjectStorage storage = null;
	
	public MapServer(MemorizedMap map, ObjectStorage storage){
		this.map = map;
		this.storage = storage;
		try {
			sock = new ServerSocket(6000);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
		
	public void run(){
		while(true){
			if(sock.isClosed()){
				try {
					sock = new ServerSocket(6000);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
			try {
				OutputStream outStream;
				InputStream inStream;
				ObjectOutputStream objOutStream;
				ObjectInputStream objInStream;
				
				
				Socket clientsocket = sock.accept();
				
				ArrayList<SimpleTile> simpleMap = convertMap(this.map.getUnderlyingMap().getMap());
				ArrayList<SimpleObject> simpleObjStorage = convertObjects(this.storage);
				Container c = new Container();
				c.map = simpleMap;
				c.objects = simpleObjStorage;
				
				outStream = clientsocket.getOutputStream();
				objOutStream  = new ObjectOutputStream(outStream);
				inStream = clientsocket.getInputStream();
				objInStream = new ObjectInputStream(inStream);
				objOutStream.writeObject(c); //send information to client
				objOutStream.flush();
				objOutStream.close();
				
			} catch (Exception e) {
				System.out.println("------------------------------------+++++++++++++++++");
				e.printStackTrace();
			}
		}
	}

	public ArrayList<SimpleTile> convertMap(Map<Point,LinkedTile> map){
		ArrayList<SimpleTile> simpleMap = new ArrayList<SimpleTile>();
		
		for(Point p: map.keySet()){
			LinkedTile t = map.get(p);
			if(t != null)
				simpleMap.add(new SimpleTile(p.x,p.y, t.isWater(), t.isPassable(), t.isExplored(), t.isOutOfMap()));
		}
		
		return simpleMap;
	
	}
	
	public ArrayList<SimpleObject> convertObjects(ObjectStorage storage){
		ArrayList<SimpleObject> objects = new ArrayList<SimpleObject>();
		
		Map<Point, MemorizedWorldObject> hangars = storage.getEnemyHangars();
		for(Point p : hangars.keySet()){
			MemorizedWorldObject o = hangars.get(p);
			if(o!=null)
				objects.add(new SimpleObject(1, o.getDurability() , p.x, p.y));
		}
		
		Map<Point, MemorizedWorldObject> tanks = storage.getEnemyTanks();
		for(Point p : tanks.keySet()){
			MemorizedWorldObject o = tanks.get(p);
			if(o!=null)
				objects.add(new SimpleObject(2, o.getDurability() , p.x, p.y));
		}
		
		Map<Point, MemorizedWorldObject> crates = storage.getRepairkits();
		for(Point p : crates.keySet()){
			MemorizedWorldObject o = crates.get(p);
			if(o!=null)
				objects.add(new SimpleObject(3, o.getDurability() , p.x, p.y));
		}
		
		return objects;
	}

}
