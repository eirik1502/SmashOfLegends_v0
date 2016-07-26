package network;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedDeque;

import utils.LogWriter;

class ClientNetworkInput implements Runnable {

    
    private DatagramSocket socket; //UDP
    private DatagramPacket datagramPacket;
    
    private LogWriter log;
    
    
    private ConcurrentLinkedDeque<ClientObjectsState> objectsStateQueue = new ConcurrentLinkedDeque<>();
    private ClientObjectsState lastObjectsState = new ClientObjectsState();

    
    public ClientNetworkInput(DatagramSocket socket, LogWriter log) {
        this.socket = socket;
        this.log = log;
    }

    public void run(){

        try {

            while (true) {

            	byte[] bytes = new byte[Server.SEND_GAME_DATA_BYTE_SIZE_MAX];
    			datagramPacket = new DatagramPacket(bytes, bytes.length);
    			
    			socket.receive(datagramPacket); //wait for packet
    			//log.println("Recieved packet..");
    			
    			DataInputStream in = new DataInputStream( new ByteArrayInputStream(bytes) );
            	byte msgType = in.readByte();
            	
            	//Host newClient = new Host(datagramPacket);
            	switch(msgType) {
            		
            	case 1: //game data
            		//log.println("..game data");

            		float camX = in.readFloat();
            		float camY = in.readFloat();
            		float p1X = in.readFloat();
                	float p1Y = in.readFloat();
                	float p1Direction = in.readFloat();
                	float p1Speed = in.readFloat();
                	float p2X = in.readFloat();
                	float p2Y = in.readFloat();
                	float p2Direction = in.readFloat();
                	float p2Speed = in.readFloat();
                	byte bulletsCreatedCount = in.readByte();
                	NetCameraState cameraState = new NetCameraState(camX, camY);
                	CharacterState player1State = new CharacterState(p1X, p1Y, p1Direction, p1Speed);
                	CharacterState player2State = new CharacterState(p2X, p2Y, p2Direction, p2Speed);
                	
                	ArrayList<NetBulletState> bulletsState = new ArrayList<>();
                	for (int i = 0; i < bulletsCreatedCount; i++) {
                		byte bulletType = in.readByte();
                		float bX = in.readFloat();
                    	float bY = in.readFloat();
                    	float bDirection = in.readFloat();
                    	float bSpeed = in.readFloat();
                    	
                    	NetBulletState bulletState = new NetBulletState(bulletType, bX, bY, bDirection, bSpeed);
                    	bulletsState.add(bulletState);
                	}
                	
                	ClientObjectsState objectsState = new ClientObjectsState(cameraState, player1State, player2State, bulletsState);
                	System.out.println(objectsState);
                	
                	this.addObjectsState(objectsState);
                	
            		break;
            		
            	default:
            		break;
            	}
            	
            	
            	//log.flush();

            }
        }catch (IOException e){
        	e.printStackTrace();
        	System.out.println("Could not recieve data over network");
        }
    }
    
    private void addObjectsState(ClientObjectsState objectsState) {
    	this.objectsStateQueue.add(objectsState);
    }
    
    public ClientObjectsState getNextObjectsState() {
    	if (objectsStateQueue.isEmpty()) {
    		log.println("objectStateQueue is empty, returning last state");
    		lastObjectsState.clearBullets();
    		return this.lastObjectsState;
    	}
    	ClientObjectsState nextState = this.objectsStateQueue.poll();
    	lastObjectsState = nextState;
    	//log.println("Returning next objectsState: " + nextState);
    	return nextState;
    }
}