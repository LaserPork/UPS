import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerMaster extends Thread{

	private ArrayList<User> users = new ArrayList<User>();  
	private ArrayList<Game> tables = new ArrayList<Game>();
	
	public ServerMaster(int port, int numberOfTables){
		for (int i = 0; i < numberOfTables; i++) {
			tables.add(new Game(i));
		}
	}

	public void run(){
		try{
			ServerSocket server = new ServerSocket(1234);
			int threads = 0;
			
			
			while(true){
				System.out.println("Server ceka");
				Socket socket = server.accept();
				threads++;
				System.out.println("Klient pripojen");
				new DummyServer(this, socket, threads).start();
			}
		
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}

	public ArrayList<User> getUsers() {
		return users;
	}

	public void setUsers(ArrayList<User> users) {
		this.users = users;
	}

	public ArrayList<Game> getTables() {
		return tables;
	}

	public void setTables(ArrayList<Game> tables) {
		this.tables = tables;
	}
	
	
}
