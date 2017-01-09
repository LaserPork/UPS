

public class User {
	
	private String name;
	private String password;
	boolean logged = false;
	private Game game = null;
	private int[] hand = new int[32];
	private int handCount = 0;
	private DummyServer ownThread;
	private boolean hasEnough = false;
	
	public User(String name, String password, DummyServer ownThread){
		this.name = name;
		this.password = password;
		this.ownThread = ownThread;
	}
	
	public void reset(){
		dropHand();
		hasEnough = false;
	}
	
	public int getHandCount(){
		return handCount;
	}
	
	public boolean hasEnough(){
		return hasEnough;
	}
	
	public void enough(){
		this.hasEnough = true;
	}
	
	public boolean isOver(){
		return getHandValue()>21;
	}
	
	public void dropHand(){
		for (int i = 0; i < 32; i++) {
			hand[i] = -1;
		}
		handCount = 0;
	}
	
	public int drawCard(){
		int card = game.drawCard();
		hand[handCount] = card;
		handCount++;
		return card;
	}
	
	public int getHandValue(){
		int value = 0;
		for (int i = 0; i < handCount; i++) {
			value += getCardValue(hand[i]);
		}
		return value;
	}
	
	public int getCardValue(int cardId){
		int sign = cardId%8;
		switch (sign) {
		case 0: return 7;
		case 1: return 8;
		case 2: return 9;
		case 3: return 10;
		case 4: return 1;
		case 5: return 1;
		case 6: return 2;
		case 7: return 11;
		}
		return -Integer.MAX_VALUE;
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
