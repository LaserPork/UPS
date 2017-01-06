import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.input.MouseEvent;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;

import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

import javafx.scene.shape.Rectangle;

import javafx.scene.text.Font;
import javafx.scene.text.Text;

import javafx.util.Callback;
import javafx.util.Duration;

public class Table extends StackPane{
	
	private Connection connection;
	private Map<String, Callback<String, Object>> callbacks;
	private Group hand = new Group();//(320, 540, 640, 180);
	private ArrayList<Group> otherHands = new ArrayList<Group>();
	private Map<Node,RotateTransition> animations = new HashMap<Node,RotateTransition>();
	private Group info = new Group();
	AppStage as;
	private boolean waiting = false;
	private boolean ending = false;
	
	public Table(AppStage as, Connection connection,Map<String, Callback<String, Object>> callbacks){
		super();
		this.connection = connection;
		this.callbacks = callbacks;
		callbacks.clear();
		initCallbacks();
		initTableView();
		this.as = as;
	}
	
	public void initCallbacks(){
		callbacks.put("playerJoined", new Callback<String, Object>() {
			public Object call(String mess) {
				Platform.runLater(
						new Runnable() {
							@Override
							public void run() {
								String[] ar = mess.split("~");
								String result = ar[2];
								joinPlayer(result);
							}
						}
				);
				
				return null;
			}
		});
		
		callbacks.put("draw", new Callback<String, Object>() {
			public Object call(String mess) {
				String[] ar = mess.split("~");
				String result = ar[2];
				switch (result) {
					case "success":
						Platform.runLater(
							new Runnable() {
								@Override
								public void run() {
									unwaiting();
									drawCard(Integer.parseInt(ar[3]));
								}
							}
					);
					break;
					case "haveEnough":
						Platform.runLater(
								new Runnable() {
									@Override
									public void run() {
										unwaiting();
										enough();
									}
								}
						);
					break;
					case "areOver":
						Platform.runLater(
								new Runnable() {
									@Override
									public void run() {
										unwaiting();
										fold();
									}
								}
						);
					break;
					case "waiting":
						Platform.runLater(
								new Runnable() {
									@Override
									public void run() {
										waiting();
									}
								}
						);
					break;
				}
				
				
				return null;
			}
		});
		
		callbacks.put("playerDraw", new Callback<String, Object>() {
			public Object call(String mess) {
				Platform.runLater(
						new Runnable() {
							@Override
							public void run() {
								String[] ar = mess.split("~");
								String nick = ar[2];
								int count = Integer.parseInt(ar[3]);
								playerDrawsCard(nick,count);
							}
						}
				);
				
				return null;
			}
		});
		
		callbacks.put("enough", new Callback<String, Object>() {
			public Object call(String mess) {
				String[] ar = mess.split("~");
				String result = ar[2];
				switch (result) {
					case "success":
						Platform.runLater(
							new Runnable() {
								@Override
								public void run() {
									if(mess.equals("success")){
										enough();
									}
								}
							}
						);
					case "areOver":
						Platform.runLater(
							new Runnable() {
								@Override
								public void run() {
									fold();
								}
							}
						);
				}
				return null;
			}
		});
		
		callbacks.put("playerEnough", new Callback<String, Object>() {
			public Object call(String mess) {
				Platform.runLater(
						new Runnable() {
							@Override
							public void run() {
								String[] ar = mess.split("~");
								String nick = ar[2];
								if(!nick.equals(connection.nick)){									
									playerEnough(nick);
								}else{
									fold();
								}
							}
						}
				);
				
				return null;
			}
		});
		
		callbacks.put("playerLeft", new Callback<String, Object>() {
			public Object call(String mess) {
				Platform.runLater(
						new Runnable() {
							@Override
							public void run() {
								String[] ar = mess.split("~");
								String nick = ar[2];
								if(!nick.equals(connection.nick)){
									playerFolds(nick);									
								}
							}
						}
				);
				
				return null;
			}
		});
		
		
		callbacks.put("end", new Callback<String, Object>() {
			public Object call(String mess) {
				Platform.runLater(
						new Runnable() {
							@Override
							public void run() {
								String[] ar = mess.split("~");
								ending = true;
								if(ar.length == 2){
									lose();
								}else if(ar.length == 3){
									if(ar[2].equals(connection.nick)){
										win();
									}else{
									//	playerWon(ar[2]);
										lose();
									}
								}else if(ar.length > 3){
									lose();
									for (int i = 2; i < ar.length; i++) {
										if(ar[i].equals(connection.nick)){
											draw();
										}else{
										//	playerDraw(ar[i]);									
										}
									}
								}
								
							}
						}
				);
				
				return null;
			}
		});
		
		callbacks.put("reset", new Callback<String, Object>() {
			public Object call(String mess) {
				Platform.runLater(
						new Runnable() {
							@Override
							public void run() {
								connection.removeAllCheckers();
								resetGame();
							}
						}
				);
				
				return null;
			}
		});
		
		callbacks.put("checkPlayers", new Callback<String, Object>() {
			public Object call(String mess) {
				Platform.runLater(
						new Runnable() {
							@Override
							public void run() {
								String[] ar = mess.split("~");
								String[] nicks = new String[ar.length-2];
								for (int i = 0; i < nicks.length; i++) {
									nicks[i] = ar[i+2];							
								}
								updatePlayers(nicks);
							}
						}
				);
				
				return null;
			}
		});
		
		callbacks.put("checkCards", new Callback<String, Object>() {
			public Object call(String mess) {
				Platform.runLater(
						new Runnable() {
							@Override
							public void run() {
								String[] ar = mess.split("~");
								String[] cards = new String[ar.length-2];
								for (int i = 0; i < cards.length; i++) {
									cards[i] = ar[i+2];							
								}
								updateCards(cards);
								if(cards.length == 0){
									connection.drawCard();
								}
							}
						}
				);
				
				return null;
			}
		});
		
		callbacks.put("return", new Callback<String, Object>() {
			public Object call(String mess) {
						Platform.runLater(
								new Runnable() {
									@Override
									public void run() {
										as.setTableStage();
									}
								}
						);
						
				return null;
			}
		});
		
		callbacks.put("playerCameBack", new Callback<String, Object>() {
			public Object call(String mess) {
				Platform.runLater(
						new Runnable() {
							@Override
							public void run() {
								String[] ar = mess.split("~");
								String nick = ar[2];
								resetPlayer(nick);
							}
						}
				);
				
				return null;
			}
		});
		
	}
	
	public void disconnect(){
		ColorAdjust ca = new ColorAdjust();
		ca.setSaturation(-1);
		setEffect(ca);
		Text disc = new Text("Disconnected from server");
		disc.setFont(new Font(70));
		disc.setFill(Color.RED);
		disc.setTranslateY(360);
		disc.setTranslateX(640-disc.getLayoutBounds().getWidth()/2);
		disc.setEffect(null);
		((Group)((Group)getChildren().get(0)).getChildren().get(0)).getChildren().add(disc);
		disc.setEffect(null);
		setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				as.setLoginStage();
				as.setLoginValues(connection.server, connection.port, connection.nick, connection.password);
			}
		});
	}
	
	public void initTableView(){
		setAlignment(Pos.CENTER);
		Group group = new Group();
		Group obal = new Group(group);
		getChildren().add(obal);
		group.setClip(new Rectangle(0, 0, 1280, 720));
		widthProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				Double scale = newValue.doubleValue()/1280.0;
				group.setScaleX(scale);
				
		}});
		heightProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				Double scale = newValue.doubleValue()/720.0;
				group.setScaleY(scale);
		}});
		
		Rectangle board = new Rectangle(0, 0, 1280, 720);
		RadialGradient gradient = new RadialGradient(0, 0, 640, 360, 800, 
				false, CycleMethod.NO_CYCLE, new Stop(0, Color.DARKGREEN), new Stop(1, Color.BLACK));
		board.setFill(gradient);
		group.getChildren().add(board);
		
		Group deck = createDrawDeck();
		
		Group enough = createEnough();
		
		Text back = createBackButton();
		
		group.getChildren().add(deck);
		group.getChildren().add(enough);
		group.getChildren().add(info);
		group.getChildren().add(back);
		
		hand.setTranslateX(320);
		hand.setTranslateY(540);
		hand.setClip(new Rectangle(0, 0, 640, 180));
		createRotationAnimation(hand);
		group.getChildren().add(hand);
		
		Group h1 = new Group();
		h1.setTranslateX(10);
		h1.setTranslateY(210);
		Group h2 = new Group();
		h2.setTranslateX(970);
		h2.setTranslateY(210);
		Group h3 = new Group();
		h3.setTranslateX(320);
		h3.setTranslateY(10);
		Group h4 = new Group();
		h4.setTranslateX(660);
		h4.setTranslateY(10);
		otherHands.add(h1);
		otherHands.add(h2);
		otherHands.add(h3);
		otherHands.add(h4);
		
		
		group.getChildren().addAll(h1,h2,h3,h4);
		connection.checkCards();
		connection.askPlayers();
	}
	
	public Text createBackButton(){
		Text back = new Text("Back");
		back.setFill(Color.RED);
		back.setFont(new Font(30));
		back.setTranslateY(715);
		back.setTranslateX(1275-back.getLayoutBounds().getWidth());
		back.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent arg0) {
				connection.returnBack();
			}
		});
		
		return back;
	}
	
	public Group createDrawDeck(){
		Group deck = createCardBack();
		
		deck.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				connection.drawCard();
			}
		});
		deck.setOnMouseEntered(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				getScene().setCursor(Cursor.HAND);
			}
		});deck.setOnMouseExited(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				getScene().setCursor(Cursor.DEFAULT);
			}
		});
		deck.setTranslateX(530);
		deck.setTranslateY(285);
		
		return deck;
	}

	public Group createEnough(){
		Group deck = createCardBack();
		
		deck.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				connection.announceEnough();
			}
		});
		deck.setOnMouseEntered(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				getScene().setCursor(Cursor.HAND);
			}
		});deck.setOnMouseExited(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				getScene().setCursor(Cursor.DEFAULT);
			}
		});
		deck.setTranslateX(650);
		deck.setTranslateY(285);
		
		return deck;
	}
	
	public void drawCard(int cardId){
		Group card = new Group();
		Rectangle c = new Rectangle(0, 0, 100, 150);
		c.setArcHeight(15);
		c.setArcWidth(15);
		c.setFill(Color.FLORALWHITE);
		c.setStroke(Color.BLACK);
		Text t = new Text(getCardValue(cardId)+getCardType(cardId));
		t.setFill(getCardColor(cardId));
		t.setX(10);
		t.setY(50);
		t.setFont(new Font(30));
		card.getChildren().addAll(c,t);
		hand.getChildren().add(card);
		repositionHand(hand);
		stopRotationAnimation(hand);
		createRotationAnimation(hand);
		
	}
	
	public void joinPlayer(String nick){
		if(getPlayer(nick) == null){
			ArrayList<Group> empty = new ArrayList<Group>();
			for (int i = 0; i < otherHands.size(); i++) {
				if(otherHands.get(i).getChildren().isEmpty()){
					empty.add(otherHands.get(i));
				}
			}
			if(empty.isEmpty()){
				return;
			}
			Group emptySpace = empty.get((int)(Math.random()*empty.size()));
			Group cards = createOtherHand(nick);
			cards.setTranslateX(100);
			cards.setTranslateY(75);
			emptySpace.getChildren().add(cards);
		}else{
			Group hand = getPlayer(nick);
			if(hand != null){
				hand.getChildren().get(0).setOpacity(1);
			}
		}
	}
	
	public void kickPlayer(String nick){
		for (int i = 0; i < otherHands.size(); i++) {
			if(!otherHands.get(i).getChildren().isEmpty()){
				Group hand = (Group)otherHands.get(i).getChildren().get(0);
				String name = ((Text)hand.getChildren().get(4)).getText();
				if(name.equals(nick)){
					otherHands.get(i).getChildren().clear();
				}
			}
		}
	}
	
	public Group createOtherHand(String nick){
		
		Group hand = new Group();
		Group c1 = createCardBack();
		int rnd = (int)(Math.random()*50-10);
		c1.setRotate(rnd);
		c1.setTranslateX(rnd);
	//	c1.setTranslateY(-20);
		Group c2 = createCardBack();
		rnd = (int)(Math.random()*50-10);
		c2.setRotate(-rnd);
		c2.setTranslateX(-rnd);
	//	c2.setTranslateY(20);
		Group c3 = createCardBack();
		hand.getChildren().addAll(c1,c2,c3);
		createRotationAnimation(hand);
		Text cards = new Text("0");
		cards.setTranslateX(30);
		cards.setTranslateY(100);
		cards.setFont(new Font(70));

		hand.getChildren().add(cards);
		
		Text name = new Text(nick);
		
		name.setTranslateY(-40);
		name.setFont(new Font(30));
		name.setFill(Color.GOLD);
		name.setTranslateX(50-name.getLayoutBounds().getWidth()/2);
		hand.getChildren().add(name);
		
		return hand;
	}
	
	public void repositionHand(Group hand){
		for (int i = 0; i < hand.getChildren().size(); i++) {
			Group card = (Group)hand.getChildren().get(i);
			Rectangle handSpace = (Rectangle)hand.getClip();
			double cardWidth = ((Rectangle)card.getChildren().get(0)).getWidth();
			
			double xPos = ((handSpace.getWidth()-100)/((hand.getChildren().size())*2))*(i*2+1);
			
			card.setTranslateX(50+xPos-cardWidth/2);
		}
	}
	
	public String getCardType(int cardId){
		int sign = cardId/8;
		switch (sign) {
		case 0: return "♦";
		case 1: return "♥";
		case 2: return "♠";
		case 3: return "♣";
		}
		return "ER";
	}
	
	public Color getCardColor(int cardId){
		int sign = cardId/8;
		switch (sign) {
		case 0:
		case 1: return Color.RED;
		case 2:
		case 3: return Color.BLACK;
	
		}
		return Color.GREEN;
	}
	
	public String getCardValue(int cardId){
		int sign = cardId%8;
		switch (sign) {
		case 0: return "7";
		case 1: return "8";
		case 2: return "9";
		case 3: return "10";
		case 4: return "J";
		case 5: return "Q";
		case 6: return "K";
		case 7: return "A";
		}
		return "ER";
	}
	
	public Group createCardBack(){
		Group deck = new Group();
		Rectangle card = new Rectangle(0, 0, 100, 150);
		card.setArcHeight(15);
		card.setArcWidth(15);
		card.setFill(Color.FLORALWHITE);
		
		Rectangle pattern = new Rectangle(5,5,90,140);
		pattern.setArcHeight(15);
		pattern.setArcWidth(15);
		pattern.setFill(Color.LIGHTGRAY);
		
		deck.getChildren().addAll(card, pattern);
		return deck;
	}
	
	public void playerDrawsCard(String nick, int cards){
		Group hand = getPlayer(nick);
		if(hand != null){
			if(hand.getChildren() != null){
		
				((Text)hand.getChildren().get(3)).setText(String.valueOf(cards));
			}
		}
		
	}
	
	public Group getPlayer(String nick){
		for (int i = 0; i < otherHands.size(); i++) {
			if(!otherHands.get(i).getChildren().isEmpty()){
				Group hand = (Group)otherHands.get(i).getChildren().get(0);
				if(((Text)hand.getChildren().get(4)).getText().equals(nick)){
					return hand;
				}
			}
		}
		connection.askPlayers();
		return null;
	}
	
	public void playerFolds(String nick){
		Group hand = getPlayer(nick);
		if(hand != null){
		hand.setOpacity(0.2);
		stopRotationAnimation(hand);
		}else{
			connection.askPlayers();
		}
	}
	
	public void fold(){
		hand.setOpacity(0.2);
		stopRotationAnimation(hand);
	}
	
	public void resetGame(){
		info.getChildren().clear();
		startAllRotationAnimations();
		for (int i = 0; i < otherHands.size(); i++) {
			if(!otherHands.get(i).getChildren().isEmpty()){
				otherHands.get(i).getChildren().get(0).setOpacity(1);
				((Text)((Group)otherHands.get(i).getChildren().get(0)).getChildren().get(3)).setText("0");
			}
		}
		this.hand.getChildren().clear();
		this.hand.setOpacity(1);
		connection.checkCards();
		ending = false;
	}
	
	public void resetPlayer(String nick){
		Group hand = getPlayer(nick);
		if(hand != null){
			createRotationAnimation(hand);
			hand.setOpacity(1);
		}else{
			connection.askPlayers();
		}
	}
	
	public void createRotationAnimation(Group hand){
		for (int i = 0; i < hand.getChildren().size(); i++) {
			if(animations.get(hand.getChildren().get(i))==null){
				RotateTransition rt = new RotateTransition(Duration.millis(100), hand.getChildren().get(i));
				rt.setFromAngle(hand.getChildren().get(i).getRotate()-5);
			    rt.setByAngle(10);
			    rt.setCycleCount(Timeline.INDEFINITE);
			    rt.setAutoReverse(true);
			    rt.play();
			    animations.put(hand.getChildren().get(i), rt);
			}else{
				animations.get(hand.getChildren().get(i)).playFromStart();
			}
		}
	}
	
	public void stopRotationAnimation(Group hand){
		if(!animations.isEmpty()){
			if(hand != null){
				if(!hand.getChildren().isEmpty()){
					for (int i = 0; i < hand.getChildren().size(); i++) {
						RotateTransition rt = animations.get(hand.getChildren().get(i));
						if(rt!=null){
							rt.stop();
							hand.getChildren().get(i).setRotate(0);
						}
					}
				}
			}
		}
	}
	
	public void startAllRotationAnimations(){
		for (Map.Entry<Node, RotateTransition> entry : animations.entrySet()) {
			entry.getValue().play();
		}
	}
	
	public void enough(){
		stopRotationAnimation(hand);
	}
	
	public void playerEnough(String nick){
		Group hand = getPlayer(nick);
		stopRotationAnimation(hand);
	}
	
	public void win(){
		if(waiting == false){
			info.getChildren().clear();
			Text t = new Text("Win");
			t.setFont(new Font(200));
			t.setFill(Color.GOLD);
			t.setStroke(Color.YELLOW);
			t.setStrokeWidth(10);
			t.setTranslateX(640-t.getLayoutBounds().getWidth()/2);
			t.setTranslateY(360+100/2);
			info.getChildren().add(t);
		//	label.setVisible(false);
			FadeTransition fd = new FadeTransition(Duration.millis(200), t);
		    fd.setFromValue(0);
		    fd.setToValue(1);
		    fd.setCycleCount(Timeline.INDEFINITE);
		    fd.setAutoReverse(true);
		/*    fd.setInterpolator(new Interpolator() {
				
				@Override
				protected double curve(double t) {
					
					return t<0.5?0:1;
				}
			});*/
		    fd.play();
		}
	}
	
	public void draw(){
		if(waiting == false){
			info.getChildren().clear();
			Text t = new Text("Draw");
			t.setFont(new Font(100));
			t.setFill(Color.GREEN);
			t.setStroke(Color.BLACK);
			t.setTranslateX(640-t.getLayoutBounds().getWidth()/2);
			t.setTranslateY(360+100/2);
			info.getChildren().add(t);
		//	label.setVisible(false);
			ScaleTransition st = new ScaleTransition(Duration.millis(5000), t);
		    st.setByX(1.05f);
		    
		    st.setByY(1.05f);
		    st.play();
		}
	}
	
	public void lose(){
		if(waiting == false){
			info.getChildren().clear();
			Text t = new Text("Lost");
			t.setFont(new Font(100));
			t.setFill(Color.RED);
			t.setStroke(Color.BLACK);
			t.setTranslateX(640-t.getLayoutBounds().getWidth()/2);
			t.setTranslateY(360+100/2);
			info.getChildren().add(t);
		//	label.setVisible(false);
			ScaleTransition st = new ScaleTransition(Duration.millis(5000), t);
		    st.setByX(1.05f);
		    
		    st.setByY(1.05f);
		    st.play();
		}
	}
	
	public void updatePlayers(String[] nicks){
		ArrayList<String> currNames = getCurrentPlayerNames();
		for (int i = 0; i < currNames.size(); i++) {
			boolean found = false;
			for (int j = 0; j < nicks.length; j++) {
				if(nicks[j].equals(currNames.get(i))){
					found = true;
					break;
				}
			}
			if(!found){
				kickPlayer(currNames.get(i));
			}
		}
	}

	public void updateCards(String[] cards){
		this.hand.getChildren().clear();
		for (int i = 0; i < cards.length; i++) {
			drawCard(Integer.parseInt(cards[i]));
		}
	}

	public ArrayList<String> getCurrentPlayerNames(){
		ArrayList<String> names = new ArrayList<String>();
		for (int i = 0; i < otherHands.size(); i++) {
			if(!otherHands.get(i).getChildren().isEmpty()){
				Group hand = (Group)otherHands.get(i).getChildren().get(0);
				names.add(((Text)hand.getChildren().get(4)).getText());
			}
		}
		return names;
	}
	
	public void freeze(){
		if(!ending){
			ColorAdjust ca = new ColorAdjust();
			ca.setSaturation(-1);
			setEffect(ca);
		}else{
			unfreeze();
		}
		/*
		setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				as.setLoginStage();
				as.setLoginValues(connection.server, connection.port, connection.nick, connection.password);
			}
		});
		*/
	}
	

	public void unfreeze(){
		setEffect(null);
		/*
		setOnMouseClicked(null);
		*/
	}
	
	public void waiting(){
		this.waiting = true;
		((Group)((Group)getChildren().get(0)).getChildren().get(0)).getChildren().get(1).setOpacity(0.2);
		((Group)((Group)getChildren().get(0)).getChildren().get(0)).getChildren().get(2).setOpacity(0.2);
	}
	
	public void unwaiting(){
		this.waiting = false;
		((Group)((Group)getChildren().get(0)).getChildren().get(0)).getChildren().get(1).setOpacity(1);
		((Group)((Group)getChildren().get(0)).getChildren().get(0)).getChildren().get(2).setOpacity(1);
	}
	
}
