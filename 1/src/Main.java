
import javafx.application.Application;

import javafx.stage.Stage;

public class Main extends Application{

	
	public static void main(String[] args) {
		Application.launch(args);
		
	}

	@Override
	public void start(Stage stage) throws Exception {
	/*	ServerSocket ss = null;
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
		*/
		AppStage as = new AppStage(stage);
		as.setLoginStage();
		stage.show();
	}
	
	
}
