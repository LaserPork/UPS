import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Map;

import javafx.scene.paint.Color;
import javafx.util.Callback;

class Connection extends Thread{

	private PrintWriter out;
	private BufferedReader in;
	private Socket socket;
	private Map<String, Callback<String, Object>> callbacks;
	private String server;
	private int port;
	private String nick;
	private String password;
	private AppStage as;
	private ArrayList<Checker> checkers = new ArrayList<Checker>();
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
			as.unfreeze();
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
			System.out.println("Read exception");
			disconnect();
		}
		System.out.println("Stream closed");
		disconnect();
	}
	
	public void recieve(String mess){
		String[] ar = mess.split("~");
		int size = Integer.parseInt(ar[0]);
		if(size!=ar.length){
			System.out.println("Corrupted packet");
			return;
		}
		String type = ar[1];
		if(type.equals("invalidState")){
			removeAllCheckers();
		}else{
			removeChecker(type);			
		}
		as.unfreeze();
		if(callbacks.get(type)!= null){
			callbacks.get(type).call(mess);
		}
		if(type.equals("end")){
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
			}
		}
	}

	
	public boolean connect(String host, int port){
		try {
			System.out.println("Klient se chce pripojit");
			as.freeze();
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
		if(socket != null){
			try {
				as.disconnect();
				tpRefresher.stopRefreshing();
				tpRefresher.interrupt();
				removeAllCheckers();
				in.close();
				out.close();
				socket.close();
				out = null;
				in = null;
				socket = null;
			} catch (Exception e) {
			}
			
		}
	}
	
	public void removeAllCheckers(){
		while(checkers.size()!=0){
			removeChecker(checkers.get(0).getResponse());
		}
	}
	
	public void removeChecker(String typ){
		ArrayList<Checker> newCheckers = new ArrayList<Checker>();
		for (int i = 0; i < checkers.size(); ++i) {
			if(checkers.get(i).getResponse().equals(typ)){
				checkers.get(i).interrupt();
			}else{
				newCheckers.add(checkers.get(i));
			}
		}
		checkers = newCheckers;
	}
	
	public void login(String nick, String password){
		if(socket != null && out != null){
			System.out.println("Client sends:	4~login~"+nick+"~"+password);
			checkers.add(new Checker("4~login~"+nick+"~"+password, "login",this));
			out.println("4~login~"+nick+"~"+password);
		}
	}
	
	public void askTables(){
		if(socket != null && out != null){
			System.out.println("Client sends:	3~tables~count");
			checkers.add(new Checker("3~tables~count", "tables",this));
			out.println("3~tables~count");
		}
	}
	
	public void askTable(int id){
		if(socket != null && out != null){
			System.out.println("Client sends:	3~table~"+id);
			checkers.add(new Checker("3~table~"+id, "table",this));
			out.println("3~table~"+id);
		}
	}
	
	public void join(int id){
		if(socket != null && out != null){
			System.out.println("Client sends:	3~join~"+id);
			checkers.add(new Checker("3~join~"+id, "join",this));
			out.println("3~join~"+id);
		}
	}
	
	public void logout(){
		if(socket != null && out != null){
			System.out.println("Client sends:	2~logout");
			checkers.add(new Checker("2~logout", "logout",this));
			out.println("2~logout");
		}
	}
	
	public void drawCard(){
		if(socket != null && out != null){
			System.out.println("Client sends:	2~draw");
			checkers.add(new Checker("2~draw", "draw",this));
			out.println("2~draw");
		}
	}
	
	public void askPlayers(){
		if(socket != null && out != null){
			System.out.println("Client sends:	3~players~count");
			checkers.add(new Checker("3~players~count", "players",this));
			out.println("3~players~count");
		}
	}
	
	public void announceEnough(){
		if(socket != null && out != null){
			System.out.println("Client sends:	2~enough");
			checkers.add(new Checker("2~enough", "enough",this));
			out.println("2~enough");
		}
	}
	
	public void returnBack(){
		if(socket != null && out != null){
			System.out.println("Client sends:	2~return");
			checkers.add(new Checker("2~return", "return",this));
			out.println("2~return");
		}
	}
	
	public void checkCards(){
		if(socket != null && out != null){
			System.out.println("Client sends:	2~checkCards");
			checkers.add(new Checker("2~checkCards", "checkCards",this));
			out.println("2~checkCards");
		}
	}
	
	public void checkPlayers(){
		if(socket != null && out != null){
			System.out.println("Client sends:	2~checkPlayers");
			checkers.add(new Checker("2~checkPlayers", "checkPlayers",this));
			out.println("2~checkPlayers");
			
		}
	}

	public PrintWriter getOut() {
		return out;
	}

	public void setOut(PrintWriter out) {
		this.out = out;
	}

	public BufferedReader getIn() {
		return in;
	}

	public void setIn(BufferedReader in) {
		this.in = in;
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	public Map<String, Callback<String, Object>> getCallbacks() {
		return callbacks;
	}

	public void setCallbacks(Map<String, Callback<String, Object>> callbacks) {
		this.callbacks = callbacks;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public AppStage getAs() {
		return as;
	}

	public void setAs(AppStage as) {
		this.as = as;
	}

	public ArrayList<Checker> getCheckers() {
		return checkers;
	}

	public void setCheckers(ArrayList<Checker> checkers) {
		this.checkers = checkers;
	}

	public TablePickerRefresher getTpRefresher() {
		return tpRefresher;
	}

	public void setTpRefresher(TablePickerRefresher tpRefresher) {
		this.tpRefresher = tpRefresher;
	}
	
	
}