import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Map;

import javafx.util.Callback;

class Connection extends Thread{

	PrintWriter out;
	BufferedReader in;
	Socket socket;
	Map<String, Callback<String, Object>> callbacks;
	String server;
	int port;
	String nick;
	String password;
	
	
	public Connection(Map<String, Callback<String, Object>> callbacks, String server, int port, String nick, String password) {
		this.server = server;
		this.port = port;
		this.callbacks = callbacks;
		this.nick = nick;
		this.password = password;
	}
	
	public void run(){
		if(!connect(server, port)){
			System.out.println("Nelze se pripojit");
			return;
		}
		login(nick, password);
		String line = null;
		try {
			while((line = in.readLine()) != null){
				System.out.println("Client gets:	"+line+"	");
				recieve(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void recieve(String mess){
		String[] ar = mess.split("~");
		int size = Integer.parseInt(ar[0]);
		if(size!=ar.length){
			System.out.println("Corrupted packet");
			return;
		}
		String type = ar[1];
		if(callbacks.get(type)!= null){
			callbacks.get(type).call(mess);
		}
	}

	
	public boolean connect(String host, int port){
		try {
			System.out.println("Klient se chce pripojit");
			socket = new Socket(host, port);
			System.out.println("klient	"+socket.getPort()+"	"+
					socket.getLocalPort()+"	"+
					socket.getInetAddress()+"	"+
					socket.getLocalAddress());
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			return true;
		}
		catch(Exception e) {
			//e.printStackTrace();
			return false;
		}
	}
	
	public void disconnect(){
		try {
			in.close();
			out.close();
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void login(String nick, String password){
		out.println("4~login~"+nick+"~"+password);
	
	}
	
	public void askTables(){
		out.println("3~tables~count");
	}
	
	public void askTable(int id){
		out.println("3~table~"+id);
	}
	
	public void join(int id){
		out.println("3~join~"+id);
	}
	
	public void logout(){
		out.println("2~logout");
	}
	
	public void drawCard(){
		out.println("2~draw");
	}
	
	public void askPlayers(){
		out.println("3~players~count");
	}
}