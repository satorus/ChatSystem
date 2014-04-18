import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/*
 * 	A ChatServer implementation
 * 
 * 	start with java ChatServer [-debug] [port]
 *
 * 
 * 
 */


public class ChatServer extends Thread {
	
	final Lock lock = new ReentrantLock();	//lock to manage socket vector

	private Socket socket;				//the socket the thread belongs to
	private Vector<Socket> sockets;		//list of connected sockets (Vector for Thread-Safety)
	private boolean debug;

	public ChatServer(Socket socket, Vector<Socket> sockets,boolean debug){
		this.socket = socket;
		this.sockets = sockets;
		this.debug = debug;
	}
	
	public void run(){
		try{
		//-- get In/Out streams
		BufferedReader	in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream(), "US-ASCII");
		
		while(true){
			
		//-- read string from socket input
		String s = in.readLine();
		
		if(s == null){
			// remove socket from list because he issued "quit"; use of lock so that the socket can't be removed while iterating through the list
			lock.lock();
			sockets.remove(socket);
			lock.unlock();
			break;
		}
		
		if(debug == true){
			System.out.print(s);
		}
		
		
		//-- distribute message to all clients. Lock list so that socket can't be removed while iterating.
		try{
			lock.lock();
			distributeMessage(s);
			//lock.unlock();
		} catch(IOException e){
			e.printStackTrace();
		}
		finally {
			lock.unlock();
		}
		
		
		//System.out.println("message echoed!");
		
		}
		
		//System.out.println("thread stopped!");
			
		} catch(IOException e){}
	}
	
	
	/*
	 * Iterate through all sockets and send the given messasge to them
	 */
	
	public synchronized void distributeMessage(String s) throws IOException{
		try{			
			for(int i = 0;i < sockets.size();i++){
				if(sockets.get(i) != socket){
					OutputStreamWriter outTemp = new OutputStreamWriter(sockets.get(i).getOutputStream(), "US-ASCII");
					outTemp.write(s + "\n");
					outTemp.flush();
				}
			}
		} catch(SocketException e){}
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int port = 5678;
		boolean debug = false;
		Vector<Socket> sockets = new Vector<>();
		
		//-- parse the args[]
		
		if (args.length == 1){
			if(args[0].equals("-debug")){
				debug = true;
			}
			else {
			port = Integer.parseInt(args[0]);
			}
		}
		if (args.length == 2){
			if(args[0].equals("-debug")){
				debug = true;
				port = Integer.parseInt(args[1]);
			}
			
		}

		//System.out.println(port);
		
		try{
		//-- start new server
		ServerSocket server = new ServerSocket(port);
		//System.out.println("server created!");
		
		while(true){
			
		//-- accept connecting Clients and start new thread to handle them
		Socket socket = server.accept();
		sockets.add(socket);
		//System.out.println("connection!");
		new ChatServer(socket,sockets,debug).start();
		}

		} catch(IOException e) {/*System.out.println("IOException!"); */}
		
		
	}


}
