import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

public class DummyServer extends Thread{
	
	private int threadId;
	private Socket socket;
	private ServerMaster sm;
	private User currentlyLogged = null;
	private Game currentGame = null;
	
	PrintWriter out = null;
	BufferedReader in = null;
	
	public DummyServer(ServerMaster sm, Socket socket, int threadId){
		this.threadId = threadId;
		this.socket = socket;
		this.sm = sm;
	}
	
	public void run(){
		
		try {
			System.out.println("Server prijal klienta "+threadId);
			System.out.println("server	"+socket.getPort()+"	"+
										socket.getLocalPort()+"	"+
										socket.getInetAddress()+"	"+
										socket.getLocalAddress());
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String line = null;
			while((line = in.readLine()) != null){
				System.out.println("Server gets:	"+line+"	");
				recieve(line);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			out.close();
			try {
				in.close();
				socket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	
		
	}
	
	
	public void recieve(String mess){
		String[] ar = mess.split("~");
		int size = Integer.parseInt(ar[0]);
		if(size!=ar.length){
			System.out.println("Server: Corrupted packet");
			return;
		}
		String type = ar[1];
		
		switch(type){
			case "login": 	out.println(login(ar[2],ar[3]));
				break;
			case "tables": 	out.println(getTables());
				break;
			case "table": 	out.println(getTablePlaying(Integer.parseInt(ar[2])));
				break;
			case "join": 	out.println(join(Integer.parseInt(ar[2])));
				break;
			case "logout": 	out.println(logout());
							Thread.currentThread().interrupt();
				break;
			case "players": getPlayers();
				break;
			case "draw": 	drawCard();
				break;
			case "enough": 	enough();
				break;
		}
	}
	
	public String login(String name, String password){
		try {wait(4000);}catch(Exception e){}
		
		for (int i = 0; i < sm.getUsers().size(); i++) {
			if(sm.getUsers().get(i).getName().equals(name)){
				if(sm.getUsers().get(i).isLogged()){
					return "3~login~alreadylogged";
				}
				if(sm.getUsers().get(i).getPassword().equals(password)){
					sm.getUsers().get(i).setLogged(true);
					currentlyLogged = sm.getUsers().get(i);
					return "3~login~success";
				}
				return "3~login~failpassword";
			}
		}
		User newUser = new User(name, password, this);
		newUser.setLogged(true);
		sm.getUsers().add(newUser);
		currentlyLogged = newUser;
		return "3~login~registered";
	}
	
	public String getTables(){
		return "3~tables~"+sm.getTables().size();
	}
	
	public String getTablePlaying(int id){
		return "4~table~"+id+"~"+sm.getTables().get(id).getPlayingCount();
	}
	
	public String join(int id){
		Game game = sm.getTables().get(id);
		if(game.getPlayingCount()==5){
			return "4~join~"+id+"~full";
		}
		if(currentlyLogged.getGame()!=null){
			return "5~join~"+id+"~alreadyplaying~"+currentlyLogged.getGame().getId();
		}
		this.currentGame = game;
		game.joinUser(currentlyLogged);
		game.notifyAboutJoin(currentlyLogged);
		return "4~join~"+id+"~success";
	}
	
	public String logout(){
		currentlyLogged.setLogged(false);
		currentGame = null;
		return "3~logout~success";
	}
	
	public void getPlayers(){
		for (int i = 0; i < currentGame.getPlayingCount(); i++) {
			if(!currentGame.getPlayingUsers()[i].getName().equals(currentlyLogged.getName())){
				out.println("3~playerJoined~"+currentGame.getPlayingUsers()[i].getName());
			}
		}
	}
	
	public void drawCard(){
		if(currentlyLogged.hasEnough()){
			out.println("3~draw~haveEnough");
		}else if(currentlyLogged.isOver()){
			out.println("3~draw~areOver");
		}else if(currentGame.getDeckCount()>0){
			out.println("4~draw~success~"+currentlyLogged.drawCard());
			currentGame.notifyAboutDraw(currentlyLogged);
		}
		if(currentlyLogged.isOver()){
			fold();
		}
		currentGame.tryToEndGame();
	}
	
	public void enough(){
		if(!currentlyLogged.isOver()){
			currentlyLogged.enough();
			out.println("3~enough~success");
			currentGame.notifyAboutEnough(currentlyLogged);
		}else{
			out.println("3~enough~areOver");
		}
		currentGame.tryToEndGame();
	}
	
	public void fold(){
		if(currentlyLogged.isOver()){
			currentGame.notifyAboutFold(currentlyLogged);
		}
	}
	
}
