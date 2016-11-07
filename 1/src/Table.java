import java.util.ArrayList;
import java.util.Map;

import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.RoundRectangle2D;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.jmx.MXNodeAlgorithm;
import com.sun.javafx.jmx.MXNodeAlgorithmContext;
import com.sun.javafx.sg.prism.NGNode;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.ColorInput;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Paint;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Callback;

public class Table {
	
	private StackPane platno;
	private Connection connection;
	private Map<String, Callback<String, Object>> callbacks;
	private Group hand = new Group();//(320, 540, 640, 180);
	private ArrayList<Group> otherHands = new ArrayList<Group>();
	
	public Table(StackPane platno, Connection connection,Map<String, Callback<String, Object>> callbacks){
		this.platno = platno;
		this.connection = connection;
		this.callbacks = callbacks;
		initCallbacks();
		platno.setOnKeyPressed(new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent event) {
				if(event.getCode() == KeyCode.R){
					resetGame();
				}
				if(event.getCode() == KeyCode.DIGIT1){
					playerFolds("Player 1");
				}
				if(event.getCode() == KeyCode.DIGIT2){
					playerFolds("Player 2");
				}
				if(event.getCode() == KeyCode.DIGIT3){
					playerFolds("Player 3");
				}
				if(event.getCode() == KeyCode.DIGIT4){
					playerFolds("Player 4");
				}
			}
		});
		connection.askPlayers();
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
		
	}
	
	public void initTableView(){
		platno.setAlignment(Pos.CENTER);
		Group group = new Group();
		Group obal = new Group(group);
		platno.getChildren().add(obal);
		group.setClip(new Rectangle(0, 0, 1280, 720));
		platno.widthProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				Double scale = newValue.doubleValue()/1280.0;
				group.setScaleX(scale);
				
		}});
		platno.heightProperty().addListener(new ChangeListener<Number>() {
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
		
		Group deck = createCardBack();
		
		deck.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				drawCard((int)(Math.random()*32));
			}
		});
		deck.setOnMouseEntered(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				platno.getScene().setCursor(Cursor.HAND);
			}
		});deck.setOnMouseExited(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				platno.getScene().setCursor(Cursor.DEFAULT);
			}
		});
		deck.setTranslateX(590);
		deck.setTranslateY(285);
		
		group.getChildren().add(deck);
		
		hand.setTranslateX(320);
		hand.setTranslateY(540);
		hand.setClip(new Rectangle(0, 0, 640, 180));
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
		
	}
	
	public void joinPlayer(String nick){
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
		Text cards = new Text("0");
		cards.setTranslateX(30);
		cards.setTranslateY(100);
		cards.setFont(new Font(70));

		hand.getChildren().add(cards);
		
		Text name = new Text(nick);
		
		name.setTranslateY(-40);
		name.setFont(new Font(20));
		name.setFill(Color.GOLD);
		name.setStroke(Color.BLACK);
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
	
	public void playerDrawsCard(String nick){
		Group hand = getPlayer(nick);
		String name = ((Text)hand.getChildren().get(4)).getText();
		if(name.equals(nick)){
			int value = Integer.parseInt(((Text)hand.getChildren().get(3)).getText());
			value++;
			((Text)hand.getChildren().get(3)).setText(String.valueOf(value));
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
		return null;
	}
	
	public void playerFolds(String nick){
		Group hand = getPlayer(nick);
		hand.setOpacity(0.2);
	}
	
	public void fold(){
		hand.setOpacity(0.2);
	}
	
	public void resetGame(){
		for (int i = 0; i < otherHands.size(); i++) {
			otherHands.get(i).getChildren().get(0).setOpacity(1);
			((Text)((Group)otherHands.get(i).getChildren().get(0)).getChildren().get(3)).setText("0");
			this.hand.getChildren().clear();
			this.hand.setOpacity(1);
		}
	}
}
