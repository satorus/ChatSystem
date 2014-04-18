import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;


/*
 * 		A ChatClient to use with the ChatServer
 * 		start with java ChatClient <nick> [host]:[port]
 * 
 * 
 */


public class ChatClient extends Thread {
	
	private Socket socket;
	private String nick;
	private static boolean shutdown = false;		//to check whether quit was called
	
	public ChatClient(Socket socket,String nick){
		this.socket = socket;
		this.nick = nick;
	}
	
	public void run(){
		try{
			//-- get In/Out streams
			OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream(), "US-ASCII");
			BufferedReader sysIn = new BufferedReader(new InputStreamReader(System.in));
			InputStream in =socket.getInputStream();
		
			while(true){
				//-- read line from console
				String s = sysIn.readLine();
				
				//-- close client if quit was given string
				if (s.equals("quit")){
					shutdown = true;
					in.close();
					out.close();
					socket.close();
					break;
				}
				
				//-- send given String
				out.write(nick + ":" + s +"\n");
				out.flush();
		}
		
		} catch(IOException e){}
	}
	
	


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		String nick ="";
		String host = "127.0.0.1";
		int port = 5678;
	
		
		//-- parse the args[]
		if(args.length == 0){
			// nickname missing
			System.out.println("Bitte Nicknamen als Argument uebergeben!");
			System.exit(0);
		}
		nick = args[0];
		if(args.length > 1){
			// split second string into host and port
			String[] argsSplit = args[1].split(":");
			if(!argsSplit[0].isEmpty()){
				host = argsSplit[0];
			}
			if(argsSplit.length > 1 && !argsSplit[1].isEmpty()){
				port = Integer.parseInt(argsSplit[1]);
			}
		}
		
		
		try{
		//-- establish new connection
		Socket socket = new Socket(host,port);
		
		//start new thread for writing purpose
		new ChatClient(socket,nick).start();
		
		InputStream in =socket.getInputStream();
		BufferedReader socketIn = new BufferedReader(new InputStreamReader(in));
		
		while(!shutdown){
			// get messages from server and print them
			String chatMessage = socketIn.readLine();
			System.out.println(chatMessage);
		}
		
		} catch(IOException e){}
	}
	
	/*
	 * Check whether given string is numeric
	 */
	
	public static boolean isNumeric(String str) {
		try {
			int d = Integer.parseInt(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

}
