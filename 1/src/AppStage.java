import java.util.HashMap;
import java.util.Map;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;


public class AppStage{


	private Stage stage;
	private Connection connection;
	private Map<String, Callback<String, Object>> callbacks = new HashMap<String, Callback<String, Object>>();
	private Label info;
	private TableView<TableViewCell> tables = new TableView<TableViewCell>();
	private boolean frozen = false;
	
	public AppStage(Stage stage){
		this.stage = stage;
		
	}
	
	public TableView<TableViewCell> getTableView(){
		return tables;
	}
	
	public void setInfo(String text, Color color){
		Platform.runLater(
				new Runnable() {
					@Override
					public void run() {
						info.setText(text);
						info.setTextFill(color);
					}
				}
		);
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
										String name = ar[3];
										connection.setNick(name);
										info.setTextFill(Color.GREEN);
										info.setText("Succesfully logged in");
										setTableStage();
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
										setTableStage();
										String name = ar[3];
										connection.setNick(name);
										info.setTextFill(Paint.valueOf("blue"));
										info.setText("New account registered");
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
						System.out.println("User is currently logged");
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
								setLoginStage();
								setLoginValues(connection.getServer(), connection.getPort(), connection.getNick(), connection.getPassword());
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
										if(!(stage.getScene().getRoot() instanceof Table)){
											setGameScene(stage);
										}
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
		SpinnerValueFactory<Integer> svf = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 45000);
		svf.setValue(1234);
		ruleta.setValueFactory(svf);
		ruleta.setEditable(true);
		ruleta.setPrefSize(80, 30);
		ruleta.focusedProperty().addListener((s, ov, nv) -> {
		    if (nv) return;
		    commitEditorText(ruleta);
		});
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
	
	private <T> void commitEditorText(Spinner<T> spinner) {
	    if (!spinner.isEditable()) return;
	    String text = spinner.getEditor().getText();
	    SpinnerValueFactory<T> valueFactory = spinner.getValueFactory();
	    if (valueFactory != null) {
	        StringConverter<T> converter = valueFactory.getConverter();
	        if (converter != null) {
	            T value = converter.fromString(text);
	            valueFactory.setValue(value);
	        }
	    }
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
		AppStage as = this;
		btloggin.setOnAction(new EventHandler<ActionEvent>(){
			@SuppressWarnings("unchecked")
			@Override
			public void handle(ActionEvent arg0) {
				
					String server = ((TextField)((HBox)vb.getChildren().get(0)).getChildren().get(1)).getText();
					int port = ((Spinner<Integer>)((HBox)vb.getChildren().get(0)).getChildren().get(2)).getValueFactory().getValue();
					String nick = ((TextField)((HBox)vb.getChildren().get(1)).getChildren().get(1)).getText();
					String pass = ((PasswordField)((HBox)vb.getChildren().get(2)).getChildren().get(1)).getText();
					if(server.isEmpty()){
						setInfo("Fill in server field", Color.RED);
					}else if(port > 65535 || port < 1){
						setInfo("Port must be between 1 and 65535", Color.RED);
					}else if(nick.isEmpty()){
						setInfo("Fill in name field", Color.RED);
					}else if(pass.isEmpty()){
						setInfo("Fill in password field", Color.RED);
					}else{
						connection = new Connection(as, callbacks, server, port, nick, pass);
						
					}
				
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

	public void setLoginStage(){
		stage.hide();
		Scene scene = new Scene(initControls());
		stage.setScene(scene);
		stage.setMinHeight(300);
		stage.setMinWidth(360);
		stage.setHeight(300);
		stage.setWidth(360);
		initCallbacks();
		stage.show();
	}
	
	public void setTableStage(){
		stage.hide();
		callbacks.clear();
		initCallbacks();
		Scene scene = new Scene(initTableScene());
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
	
	@SuppressWarnings("unchecked")
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
		
		tables.setRowFactory( tv -> {
		    TableRow<TableViewCell> row = new TableRow<>();
		    row.setOnMouseClicked(event -> {
		        if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
		        	TableViewCell rowData = row.getItem();
		            connection.join(rowData.getId());
		        }
		    });
		    return row ;
		});
		
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
				if(tables.getSelectionModel().getSelectedItem()!=null){
					connection.join(tables.getSelectionModel().getSelectedItem().getId());
				}
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
		stage.hide();
		
		Table table = new Table(this, connection, callbacks);
		Scene sc = new Scene(table);
		stage.setScene(sc);
		stage.setWidth(848);
		stage.setHeight(480);
		stage.show();
		
	}
	
	@SuppressWarnings("unchecked")
	public void setLoginValues(String server, int port, String nick, String pass){
		((TextField)
				((HBox)
						((VBox)
								((VBox)
										stage.getScene().getRoot()
								).getChildren().get(0)
						).getChildren().get(0)
				).getChildren().get(1)
			).setText(server);
			
		((Spinner<Integer>)
				((HBox)
						((VBox)
								((VBox)
										stage.getScene().getRoot()
								).getChildren().get(0)
						).getChildren().get(0)
				).getChildren().get(2)
			).getValueFactory().setValue(port);
		
		((TextField)
				((HBox)
						((VBox)
								((VBox)
										stage.getScene().getRoot()
								).getChildren().get(0)
						).getChildren().get(1)
				).getChildren().get(1)
			).setText(nick);
		
		((TextField)
				((HBox)
						((VBox)
								((VBox)
										stage.getScene().getRoot()
								).getChildren().get(0)
						).getChildren().get(2)
				).getChildren().get(1)
			).setText(pass);
	}
	
	public void disconnect(){
		Platform.runLater(
				new Runnable() {
					@Override
					public void run() {
						if(stage.getScene().getRoot() instanceof Table){
							((Table)stage.getScene().getRoot()).disconnect();
						}else{
							if(!info.getText().equals("Account is currently logged") && 
								!info.getText().equals("Account already exists, but wrong password")){
								info.setText("Disconnected from server");
								info.setTextFill(Color.RED);
								stage.getScene().addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
									@Override
									public void handle(MouseEvent event) {
										if(!(stage.getScene().getRoot() instanceof VBox)){
											stage.hide();
											setLoginStage();
											setLoginValues(connection.getServer(), connection.getPort(), connection.getNick(), connection.getPassword());
											stage.show();
										}
									}
								});
								
							}
						}
						
					}
				}
		);
		
		
	
	}
	
	public void freeze(){
		Platform.runLater(
				new Runnable() {
					@Override
					public void run() {
						if(stage.getScene().getRoot() instanceof Table){
							((Table)stage.getScene().getRoot()).freeze();
						}else{
							/*
							info.setText("Waiting for response from server");
							info.setTextFill(Color.RED);
							
								stage.getScene().getRoot().setOnMouseClicked(new EventHandler<MouseEvent>() {
									@Override
									public void handle(MouseEvent event) {
										if(!(stage.getScene().getRoot() instanceof VBox)){
											stage.hide();
											setLoginStage();
											setLoginValues(connection.server, connection.port, connection.nick, connection.password);
											stage.show();
										}
									}
								});
								*/
							}
							
						
						frozen = true;
					}
				}
		);
		
	}
	
	public void unfreeze(){
		
			Platform.runLater(
					new Runnable() {
						@Override
						public void run() {
							if(frozen == true){
								if(stage.getScene().getRoot() instanceof Table){
									((Table)stage.getScene().getRoot()).unfreeze();
								}else{
									/*
									info.setText("Connection established");
									info.setTextFill(Color.GREEN);
									stage.getScene().getRoot().setOnMouseClicked(null);
									*/
								}
								frozen = false;
							}
						}
					}
			);
			
		
	}
}
