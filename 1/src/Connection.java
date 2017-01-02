import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import javafx.scene.paint.Color;
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
	AppStage as;
	ArrayList<Checker> checkers = new ArrayList<Checker>();
	private TablePickerRefresher tpRefresher;
	
	public Connection(AppStage as, Map<String, Callback<String, Object>> callbacks, String server, int port, String nick, String password) {
		this.server = server;
		this.port = port;
		this.callbacks = callbacks;
		this.nick = nick;
		this.password = password;
		this.as = as;
		setDaemon(true);
		start();
	}
	
	public void run(){
		if(!connect(server, port)){
			System.out.println("Nelze se pripojit");
			as.setInfo("Unable to connect", Color.RED);
			return;
		}
		login(nick, password);
		tpRefresher = new TablePickerRefresher(as.getTableView(), this);
		tpRefresher.setDaemon(true);
		tpRefresher.start();
		String line = null;
		try {
			while((line = in.readLine()) != null){
				System.out.println("Client gets:	"+line+"	");
				recieve(line);
			}
		} catch (Exception e) {
			System.out.println("Disconnected");
			disconnect();
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
		removeChecker(type);
		as.unfreeze();
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
			as.disconnect();
			in.close();
			out.close();
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void removeChecker(String typ){
		for (int i = 0; i < checkers.size(); i++) {
			if(checkers.get(i).getResponse().equals(typ)){
				checkers.get(i).interrupt();
				checkers.remove(i);
			}
		}
	}
	
	public void login(String nick, String password){
		System.out.println("Client sends:	4~login~"+nick+"~"+password);
		out.println("4~login~"+nick+"~"+password);
		checkers.add(new Checker("4~login~"+nick+"~"+password, "login",this));
	}
	
	public void askTables(){
		System.out.println("Client sends:	3~tables~count");
		out.println("3~tables~count");
		checkers.add(new Checker("3~tables~count", "tables",this));
	}
	
	public void askTable(int id){
		System.out.println("Client sends:	3~table~"+id);
		out.println("3~table~"+id);
		checkers.add(new Checker("3~table~"+id, "table",this));
	}
	
	public void join(int id){
		System.out.println("Client sends:	3~join~"+id);
		out.println("3~join~"+id);
		checkers.add(new Checker("3~join~"+id, "join",this));
	}
	
	public void logout(){
		System.out.println("Client sends:	2~logout");
		out.println("2~logout");
		checkers.add(new Checker("2~logout", "logout",this));
	}
	
	public void drawCard(){
		System.out.println("Client sends:	2~draw");
		out.println("2~draw");
	}
	
	public void askPlayers(){
		System.out.println("Client sends:	3~players~count");
		out.println("3~players~count");
		checkers.add(new Checker("3~players~count", "players",this));
	}
	
	public void announceEnough(){
		System.out.println("Client sends:	2~enough");
		out.println("2~enough");
		checkers.add(new Checker("2~enough", "enough",this));
	}
	
	public void returnBack(){
		System.out.println("Client sends:	2~return");
		out.println("2~return");
		checkers.add(new Checker("2~return", "return",this));
	}
	
	public void checkCards(){
		System.out.println("Client sends:	2~checkCards");
		out.println("2~checkCards");
		checkers.add(new Checker("2~checkCards", "checkCards",this));
	}
	
	public void checkPlayers(){
		System.out.println("Client sends:	2~checkPlayers");
		out.println("2~checkPlayers");
		checkers.add(new Checker("2~checkPlayers", "checkPlayers",this));
	}
}