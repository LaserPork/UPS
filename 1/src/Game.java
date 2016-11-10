import java.util.Arrays;

public class Game {
	private User[] playing = new User[5];
	private int playingCount = 0;
	private int id;
	private int[] deck = new int[32];
	private int deckCount = 32;
	
	public Game(int id){
		this.id = id;
	}
	
	public int getId(){
		return id;
	}
	
	public void resetDeck(){
		deckCount = 32;
		for (int i = 0; i < deckCount; i++) {
			deck[i] = i;
		}
	}
	
	public int getDeckCount(){
		return deckCount;
	}
	
	public boolean isFirstTurn(){
		for (int i = 0; i < playingCount; i++) {
			if(playing[i].getHandCount()!=0){
				return false;
			}
		}
		return true;
	}
	
	public int drawCard(){
		if(isFirstTurn()){
			resetDeck();
		}
		if(deckCount>0){
			int card = (int)(Math.random()*deckCount);
			boolean found = false;
			int cardId = -1;
			for (int i = 0; i < deckCount; i++) {
				if(i == card){
					cardId = deck[i];
					deck[i] = -1;
					found = true;
				}
				if(i==deckCount-1){
					deck[i] = -1;
					deckCount--;
					break;
				}
				if(found){
					deck[i] = deck[i+1];
				}
			}
			return cardId;
		}else{
			return -1;
		}
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
	
	public void notifyAboutDraw(User user){
		for (int i = 0; i < playingCount; i++) {
			if(!playing[i].getName().equals(user.getName())){
				playing[i].getOwnThread().out.println("4~playerDraw~"+user.getName()+"~"+user.getHandCount());
			}
		}
	}
	
	public void notifyAboutEnough(User user){
		for (int i = 0; i < playingCount; i++) {
			if(!playing[i].getName().equals(user.getName())){
				playing[i].getOwnThread().out.println("3~playerEnough~"+user.getName());
			}
		}
	}
	
	public void notifyAboutFold(User user){
		for (int i = 0; i < playingCount; i++) {
			playing[i].getOwnThread().out.println("3~fold~"+user.getName());
		}
	}
	
	public void tryToEndGame(){
		boolean isEndable = true;
		for (int i = 0; i < playingCount; i++) {
			if(!playing[i].isOver() && !playing[i].hasEnough()){
				isEndable = false;
			}
		}
		if(isEndable){
			endGame();
		}
	}
	
	public void endGame(){
		User winner = null;
		for (int i = 0; i < playingCount; i++) {
			if(playing[i].hasEnough() && !playing[i].isOver()){
				if(winner == null){
					winner = playing[i];
				}else if(winner.getHandValue()<playing[i].getHandValue()){
					winner = playing[i];
				}
			}
		}
		notifyAboutWin(winner);
	}
	
	public void notifyAboutWin(User user){
		for (int i = 0; i < playingCount; i++) {
			playing[i].getOwnThread().out.println("3~end~"+user.getName());
		}
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		resetDeck();
		for (int i = 0; i < playingCount; i++) {
			playing[i].reset();
			playing[i].getOwnThread().out.println("2~reset");
		}
	}
}
