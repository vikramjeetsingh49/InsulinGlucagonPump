package scs.main;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import scs.gui.SimulatorController;
import scs.gui.MainController;

// Main class which starts the application.
public class Main extends Application {

	public static void main(String[] args)
	{
		launch(args);
	}

	private SimulatorController simulatorController;

	@Override
	public void start(Stage stage)
	{
		try
		{
			stage.setTitle("Insuline and Glucagon Simulator Pump");
			Pane mainPane = loadPane();
			Scene scene = new Scene(mainPane);
			stage.setScene(scene);
			stage.show();
			startClock();
		} catch (Exception e)
		{
			final String message = "Unexpected Exception in main function \n";
		}
	}

	private Pane loadPane()
	{
		Pane mainPane = null;
		try
		{
			FXMLLoader loader = new FXMLLoader();
			InputStream i = getClass().getClassLoader().getResourceAsStream("main.fxml");
			mainPane = (Pane) loader.load(i);
			simulatorController = (SimulatorController) loader.getController();
			MainController.setController(simulatorController);

		} catch (IOException e)
		{
			Logger.getLogger("IOException : "+e);
			e.printStackTrace();
		} catch (RuntimeException re)
		{
			Logger.getLogger("RuntimeException : "+re);
			re.printStackTrace();
		}
		return mainPane;
	}


	private void startClock()
	{
		new AnimationTimer() {
			@Override
			public void handle(long now)
			{
				simulatorController.updateGui();
			}
		}.start();
	}
}
