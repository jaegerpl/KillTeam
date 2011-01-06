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

public class MapServer implements Runnable{
	ServerSocket sock;
	
	MemorizedMap map = null;
	
	public MapServer(MemorizedMap map){
		this.map = map;
		try {
			sock = new ServerSocket(6000);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
		
	public void run(){
		while(true){
			try {
				OutputStream outStream;
				InputStream inStream;
				ObjectOutputStream objOutStream;
				ObjectInputStream objInStream;
				
				
				Socket clientsocket = sock.accept();
				
				ArrayList<SimpleTile> simpleMap = convertMap(this.map.getUnderlyingMap().getMap());
				
				outStream = clientsocket.getOutputStream();
				objOutStream  = new ObjectOutputStream(outStream);
				inStream = clientsocket.getInputStream();
				objInStream = new ObjectInputStream(inStream);
				objOutStream.writeObject(simpleMap); //send information to client
				objOutStream.flush();
				objOutStream.close();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public ArrayList<SimpleTile> convertMap(Map<Point,LinkedTile> map){
		ArrayList<SimpleTile> simpleMap = new ArrayList<SimpleTile>();
		
		for(Point p: map.keySet()){
			LinkedTile t = map.get(p);
			simpleMap.add(new SimpleTile(p.x,p.y, t.isWater(), t.isPassable(), t.isExplored()));
		}
		
		return simpleMap;
	
	}

}
