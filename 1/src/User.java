
public class User {
	
	private String name;
	private String password;
	boolean logged = false;
	Game game = null;
	private DummyServer ownThread;
	
	public User(String name, String password, DummyServer ownThread){
		this.name = name;
		this.password = password;
		this.ownThread = ownThread;
	}
	
	public DummyServer getOwnThread() {
		return ownThread;
	}

	public void setOwnThread(DummyServer ownThread) {
		this.ownThread = ownThread;
	}

	public String getName(){
		return name;
	}
	
	public String getPassword(){
		return password;
	}
	
	public boolean isLogged(){
		return logged;
	}
	
	public void setLogged(boolean logged){
		this.logged = logged;
	}
	
	public void setGame(Game game){
		this.game = game;
	}
	
	public Game getGame(){
		return game;
	}
}
