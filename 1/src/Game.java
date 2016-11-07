import java.util.Arrays;

public class Game {
	private User[] playing = new User[5];
	private int playingCount = 0;
	private int id;
	
	public Game(int id){
		this.id = id;
	}
	
	public int getId(){
		return id;
	}
	
	public void joinUser(User user){
		user.setGame(this);
		playing[playingCount] = user;
		playingCount++;
	}
	
	public void kickUser(User user){
		boolean found = false;
		for (int i = 0; i < playingCount; i++) {
			if(playing[i].equals(user)){
				playing[i].setGame(null);
				found = true;
			}
			if(i==playingCount-1){
				playing[i] = null;
				playingCount--;
				break;
			}
			if(found){
				playing[i] = playing[i+1];
			}
		}
	}
	
	public int getPlayingCount(){
		return playingCount;
	}
	
	public User[] getPlayingUsers(){
		return playing;
	}
			
	public void notifyAboutJoin(User joined){
		for (int i = 0; i < playingCount; i++) {
			playing[i].getOwnThread().out.println("3~playerJoined~"+joined.getName());
		}
	}
}
