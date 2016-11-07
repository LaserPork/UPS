

import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;

public class Main extends Application{

	private Stage stage;
	private Connection connection;
	private Map<String, Callback<String, Object>> callbacks = new HashMap<String, Callback<String, Object>>();
	private Label info;
	private TableView<TableViewCell> tables = new TableView<TableViewCell>();
	private TablePickerRefresher tpRefresher;
	
	public static void main(String[] args) {
		Application.launch(args);
		
		
	}

	@Override
	public void start(Stage stage) throws Exception {
		ServerSocket ss = null;
		try{
			ss = new ServerSocket(1234);
		}catch(Exception e){
			
		}
		if(ss != null){
			ss.close();
			ServerMaster sm = new ServerMaster(1234, 6);
			sm.setDaemon(true);
			sm.start();
		}
		this.stage = stage;
		setLoginStage(stage);
		initCallbacks();
		stage.show();
	}
	
	public void initCallbacks(){
		callbacks.put("login", new Callback<String, Object>() {
			public Object call(String mess) {
				String[] ar = mess.split("~");
				String result = ar[2];
				switch(result){
					case "success":
						Platform.runLater(
								new Runnable() {
									@Override
									public void run() {
										info.setTextFill(Paint.valueOf("green"));
										info.setText("Succesfully logged in");
										setTableStage(stage);
									}
								}
						);
						System.out.println("Logged in");
						break;
					case "registered":
						Platform.runLater(
								new Runnable() {
									@Override
									public void run() {
										info.setTextFill(Paint.valueOf("blue"));
										info.setText("New account registered");
										setTableStage(stage);
									}
								}
						);
						System.out.println("Registered");
						break;
					case "failpassword":
						Platform.runLater(
								new Runnable() {
									@Override
									public void run() {
										info.setTextFill(Paint.valueOf("red"));
										info.setText("Account already exists, but wrong password");
									}
								}
						);
						System.out.println("Wrong password");
						break;
					case "alreadylogged":
						Platform.runLater(
								new Runnable() {
									@Override
									public void run() {
										info.setTextFill(Paint.valueOf("red"));
										info.setText("Account is currently logged");
									}
								}
						);
						System.out.println("Wrong password");
						break;
				}
				return null;
			}
		});
		
		callbacks.put("logout", new Callback<String, Object>() {
			public Object call(String mess) {
				Platform.runLater(
						new Runnable() {
							@Override
							public void run() {
								stage.hide();
								setLoginStage(stage);
								stage.show();
							}
						}
				);
				System.out.println("Logged out");
				return null;
			}
		});
		
		callbacks.put("tables", new Callback<String, Object>() {
			public Object call(String mess) {
				Platform.runLater(
						new Runnable() {
							@Override
							public void run() {
								String[] ar = mess.split("~");
								int result = Integer.parseInt(ar[2]);
								ObservableList<TableViewCell> data = FXCollections.observableArrayList();
								for (int i = 0; i < result; i++) {
									data.add(new TableViewCell(i,"Table "+(i+1), 0));
								}
								tables.setItems(data);
								tables.getSelectionModel().select(0);
								for (int i = 0; i < tables.getItems().size(); i++) {
									connection.askTable(i);
								}
							}
						}
				);
				System.out.println("Number of tables set");
				return null;
			}
		});
		
		callbacks.put("table", new Callback<String, Object>() {
			public Object call(String mess) {
				Platform.runLater(
						new Runnable() {
							@Override
							public void run() {
								String[] ar = mess.split("~");
								int id = Integer.parseInt(ar[2]);
								int result = Integer.parseInt(ar[3]);
							
								tables.getItems().get(id).setPlayers(new SimpleStringProperty(result+"/5"));
								tables.refresh();
								System.out.println("Number of players at Table "+id+" was set to "+result);
							}
						}
				);
				return null;
			}
		});
	
		callbacks.put("join", new Callback<String, Object>() {
			public Object call(String mess) {
				String[] ar = mess.split("~");
				int id = Integer.parseInt(ar[2]);
				String result = ar[3];
				switch(result){
					case "success":
						Platform.runLater(
								new Runnable() {
									@Override
									public void run() {
										info.setTextFill(Paint.valueOf("green"));
										info.setText("Succesfully joined Table "+(id+1));
										setGameScene(stage);
									}
								}
						);
						System.out.println("Joined table "+(id+1));
						break;
					case "full":
						Platform.runLater(
								new Runnable() {
									@Override
									public void run() {
										info.setTextFill(Paint.valueOf("red"));
										info.setText("Table "+(id+1)+" is already full");
									}
								}
						);
						System.out.println("Unable to join Table "+(id+1)+", it was full");
						break;
					case "alreadyplaying":
						Platform.runLater(
								new Runnable() {
									@Override
									public void run() {
										info.setTextFill(Paint.valueOf("red"));
										info.setText("You can't join Table "+(id+1)+
												", you are already playing at Table "
												+(Integer.parseInt(ar[4])+1));
									}
								}
						);
						System.out.println("Unable to join Table "+(id+1)+", already playing");
						break;
				}
				return null;
			}
		});
		
		
	}
	
	public Pane initGrid(){
		VBox vb = new VBox();
		
		Label st = new Label("Server:");
		st.setPrefSize(80, 30);
		TextField stf = new TextField("localhost");
		stf.setPrefSize(150, 30);
		Spinner<Integer> ruleta = new Spinner<Integer>();
		SpinnerValueFactory<Integer> svf = new SpinnerValueFactory.IntegerSpinnerValueFactory(1024, 45000);
		svf.setValue(1234);
		ruleta.setValueFactory(svf);
		ruleta.setEditable(true);
		ruleta.setPrefSize(80, 30);
		HBox server = new HBox(st,stf,ruleta);
		server.setAlignment(Pos.CENTER);
		
		Label nt = new Label("Nick:");
		nt.setPrefSize(80, 30);
		TextField ntf = new TextField("Pepa");
		ntf.setPrefSize(230, 30);
		HBox name = new HBox(nt,ntf);
		name.setAlignment(Pos.CENTER);
		
		Label pt = new Label("Password:");
		pt.setPrefSize(80, 30);
		PasswordField ptf = new PasswordField();
		ptf.setPrefSize(230, 30);
		HBox password = new HBox(pt,ptf);
		password.setAlignment(Pos.CENTER);
		
		vb.setPadding(new Insets(10));
		vb.setPrefWidth(400);
		vb.setAlignment(Pos.CENTER);
		vb.getChildren().addAll(server,name,password);
		return vb;
	}
	
	public Pane initControls(){
		Pane vb = initGrid();
		
		info = new Label();
		Button btloggin = new Button("Login");
		Button btexit = new Button("Exit");
		HBox btns = new HBox(btloggin,btexit);
		VBox control = new VBox(vb,info, btns);
		info.setAlignment(Pos.BOTTOM_CENTER);
		info.setWrapText(true);
		info.setPrefSize(180, 50);
		btns.setAlignment(Pos.CENTER);
		btns.setPadding(new Insets(10));
		btns.setSpacing(50);
		control.setAlignment(Pos.CENTER);
		control.setPadding(new Insets(10));
		
		btloggin.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent arg0) {
				String server = ((TextField)((HBox)vb.getChildren().get(0)).getChildren().get(1)).getText();
				int port = ((Spinner<Integer>)((HBox)vb.getChildren().get(0)).getChildren().get(2)).getValue();
				String nick = ((TextField)((HBox)vb.getChildren().get(1)).getChildren().get(1)).getText();
				String pass = ((PasswordField)((HBox)vb.getChildren().get(2)).getChildren().get(1)).getText();
				connection = new Connection(callbacks, server, port, nick, pass);
				connection.setDaemon(true);
				connection.start();
			}
		});
		btexit.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent arg0) {
				System.exit(0);
			}
		});
		return control;
	}

	public void setLoginStage(Stage stage){
		if(tpRefresher!=null){
			tpRefresher.interrupt();
		}
		stage.hide();
		Scene scene = new Scene(initControls());
		stage.setScene(scene);
		stage.setMinHeight(300);
		stage.setMinWidth(360);
		stage.setHeight(300);
		stage.setWidth(360);
	}
	
	public void setTableStage(Stage stage){
		stage.hide();
		Scene scene = new Scene(initTableScene());
		tpRefresher = new TablePickerRefresher(tables, connection);
		tpRefresher.setDaemon(true);
		tpRefresher.start();
		stage.setHeight(400);
		stage.setWidth(520);
		stage.setScene(scene);
		stage.show();
	}
	
	public Pane initTableScene(){
		BorderPane bp = new BorderPane();
		bp.setCenter(initTablePicker());
		bp.setRight(initTableControls());
		return bp;
	}
	
	public Node initTablePicker(){
		tables.getColumns().clear();
		TableColumn<TableViewCell, String> tableName = new TableColumn<TableViewCell, String> ("Table name");
		tableName.setCellValueFactory(
				new PropertyValueFactory<TableViewCell,String>("name"));
		tableName.setStyle("	-fx-alignment: CENTER-LEFT;"
							+ "	-fx-font-size: 12pt;");
		tableName.setSortable(false);
		tableName.setResizable(false);
		tableName.setPrefWidth(200);
		TableColumn<TableViewCell, String>  players = new TableColumn<TableViewCell, String> ("Players");
		players.setCellValueFactory(
				new PropertyValueFactory<TableViewCell,String>("players"));
		players.setStyle("	-fx-alignment: CENTER-RIGHT;"
						+ "	-fx-font-size: 12pt;");
		players.setSortable(false);
		players.setResizable(false);
		players.setPrefWidth(100);
		tables.getColumns().addAll(tableName, players);
		tables.setPrefSize(300, 400);
		
		connection.askTables();
		
		return tables;
	}
	
	public Node initTableControls(){
		HBox hb = new HBox();
		Button join = new Button("Join");
		Button logout = new Button("Log out");
		VBox btns = new VBox(info, hb);
		hb.getChildren().addAll(join,logout);
		hb.setAlignment(Pos.BOTTOM_CENTER);
		hb.setPadding(new Insets(10));
		hb.setSpacing(50);
		btns.setAlignment(Pos.BOTTOM_CENTER);
		btns.setPadding(new Insets(10));
		btns.setSpacing(50);
		
		join.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				connection.join(tables.getSelectionModel().getSelectedItem().getId());
			}
		});
		
		logout.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				connection.logout();
			}
		});
		
		return btns;
	}

	public void setGameScene(Stage stage){
		if(tpRefresher!=null){
			tpRefresher.interrupt();
		}
		stage.hide();
		
		StackPane platno =  new StackPane();
		Scene sc = new Scene(platno);
		stage.setScene(sc);
		Table table = new Table(platno, connection, callbacks);
		table.initTableView();
		stage.setWidth(848);
		stage.setHeight(480);
		stage.show();
		
	}
	
}
