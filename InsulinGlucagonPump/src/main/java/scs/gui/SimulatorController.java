package scs.gui;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.converter.NumberStringConverter;
import scs.main.Glucose;
import scs.simulators.InsulinGlucagonSimulator;
import scs.simulators.HarmonesSimulator;
import javafx.scene.image.ImageView;


/**
 * Main Controller, mapping of Data from model to gui. 
 *
 */
public class SimulatorController {
	

	private final static int NUM_OF_X_AXIS_SLICES = 300;

	@FXML
	private Label validCred;

	private Glucose glucose = Glucose.getInstance();

	@FXML
	private Button StartButton;

	private SimpleDateFormat dateFormatter;

	private InsulinGlucagonSimulator igSimulator = new InsulinGlucagonSimulator();

	@FXML
	private LineChart<Number, Number> lineChart;

	private HarmonesSimulator harmonesSimulator = new HarmonesSimulator();

	private Series<Number, Number> series;

	@FXML
	private NumberAxis xAxis;

	private double xSeriesData = 0;

	@FXML
	private NumberAxis yAxis;

	@FXML
	private GridPane doctorPane;

	@FXML
	private TextField user;

	@FXML
	private PasswordField password;

	@FXML
	private Button loginButton;

	@FXML
	private VBox Plane;

	@FXML
	private AnchorPane anchorPane;

    @FXML
    private AnchorPane anchorPane2;

    @FXML
	private ProgressBar insulinProgress;

	@FXML
	private ProgressBar batteryProgress;

	@FXML
	private ProgressBar glucagonProgress;

	@FXML
	ToggleGroup group = new ToggleGroup();

	@FXML
	RadioButton rb1 = new RadioButton("Automatic");

	@FXML
	RadioButton rb2 = new RadioButton("Manual");

	@FXML
	Button glucagonBtn = new Button("Glucagon");

	@FXML
	Button insulinBtn = new Button("Insulin");

	@FXML
	Button calculateBtn = new Button();

	@FXML
	TextField weightField;
	
	@FXML
	private Label errorMsg;

	@FXML
	private Label errorMsg2;

	@FXML
	private Label errorMsg3;

	@FXML
	private Label statusMsg;

	@FXML
	private Label statusMsg2;

	@FXML
	private Label statusMsg3;

	@FXML
	private Label timer;

	@FXML
	private Button stopPump;

	@FXML
	private Button startPump;

	@FXML
	private ImageView status1;

	@FXML
	private ImageView status2;


	ObservableList caloriesList= FXCollections.observableArrayList();

	@FXML
	ChoiceBox<String> caloriesCount;

	private static long startTime;

	Thread batteryThread;

	Thread insulinThread;

	Thread glucagonThread;
	
	public void batteryStatus(final String text)
	{
		Platform.runLater(new Runnable() {
			public void run()
			{
				errorMsg.setTextFill(Color.RED);
				errorMsg.setText(text);
				errorMsg.setVisible(true);
				status1.setVisible(false);
				status2.setVisible(true);
			}
		});
	}

	public void insulinStatus(final String text)
	{
		Platform.runLater(new Runnable() {
			public void run()
			{
				errorMsg2.setTextFill(Color.RED);
				errorMsg2.setText(text);
				errorMsg2.setVisible(true);
				status1.setVisible(false);
				status2.setVisible(true);
			}
		});
	}

	public void glucagonStatus(final String text)
	{
		Platform.runLater(new Runnable() {
			public void run()
			{
				errorMsg3.setTextFill(Color.RED);
				errorMsg3.setText(text);
				errorMsg3.setVisible(true);
				status1.setVisible(false);
				status2.setVisible(true);
			}
		});
	}

	public SimulatorController() {
		dateFormatter = new SimpleDateFormat("mm ss");
		dateFormatter.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
	}

	@FXML
	public void loginPump(ActionEvent e){

		if(user.getText().equals("admin")&&password.getText().equals("password")) {
			caloriesCount.setValue("calories count");
			doctorPane.setVisible(true);
			anchorPane.setVisible(false);
			anchorPane.setManaged(false);
			anchorPane2.setVisible(true);
			anchorPane2.setManaged(true);
			rb2.setVisible(true);
			rb2.setSelected(true);
			insulinBtn.setDisable(false);
			glucagonBtn.setDisable(false);
			calculateBtn.setDisable(false);
			weightField.setDisable(false);
			insulinBtn.setVisible(true);
			glucagonBtn.setVisible(true);
			calculateBtn.setVisible(true);
			weightField.setVisible(true);
			stopPump.setVisible(false);
			startPump.setVisible(true);
			batteryProgress.setProgress(1.0);
			glucagonProgress.setProgress(1.0);
			startClock();
			Date d = new Date();
			startTime = d.getTime();
			insulinThread.stop();
			insulinThread = new Thread(new insulinThread());
			insulinProgress.setProgress(1.0);
		}
		else{
			validCred.setTextFill(Color.RED);
			validCred.setText("Invalid Credentials.");
			validCred.setVisible(true);
		}
	}

	@FXML
	public void consume(ActionEvent e)
	{
		double calToCosume = 0;
			calToCosume = getCalories();

		if (calToCosume > 0)
			{
				igSimulator.consume(calToCosume);
				insulinThread = new Thread(new insulinThread());;
				insulinThread.start();
				statusMsg.setText("Insulin Injected");
				statusMsg.setTextFill(Color.GREEN);

			} else {
				igSimulator.consume(calToCosume);
				glucagonThread = new Thread(new glucagonThread());
				glucagonThread.start();
				insulinThread.stop();
				statusMsg.setText("Glucagon Injected");
				statusMsg.setTextFill(Color.GREEN);
			}
	}

		class progressThread implements Runnable {
			public void run() {
				for (int i = 100; i > 0; i--) {
					try {
						batteryProgress.setProgress(i / 100.0);
						Thread.sleep(1200);
					} catch (InterruptedException e) {
						Logger.getLogger("" + e);
					}
				}
				batteryStatus(ErrorText.Battery_Low.getText());
			}

		}

	class insulinThread implements Runnable {
		public void run(){
			for (int i = 100; i >0; i--) {
				try {
					insulinProgress.setProgress(i/100.0);
					Thread.sleep(1800);
				} catch (InterruptedException e) {
					Logger.getLogger(""+ e);
				}
			}
			insulinStatus(ErrorText.Insulin_Low.getText());

		}
	}

	class glucagonThread implements Runnable {
		public void run(){
			for (int i = 100; i >0; i--) {
				try {
					glucagonProgress.setProgress(i/100.0);
					Thread.sleep(1800);
				} catch (InterruptedException e) {
					Logger.getLogger(""+ e);

				}
			}
			glucagonStatus(ErrorText.Glucagon_low.getText());
		}
	}

	@FXML
	public void insulinLogin(ActionEvent e){
		anchorPane.setVisible(false);
		anchorPane.setManaged(false);
        anchorPane2.setVisible(true);
        anchorPane2.setManaged(true);
        igSimulator.start();
		harmonesSimulator.start();
		batteryThread= new Thread(new progressThread());
		batteryThread.start();
		startClock();
		Date d = new Date();
		startTime = d.getTime();
		statusMsg.setText("Simulator Started");
		statusMsg.setTextFill(Color.GREEN);
		stopPump.setVisible(true);
		startPump.setVisible(false);
		status1.setVisible(true);
		status2.setVisible(false);
	}

	@FXML
	public void recharge(ActionEvent e){
		batteryProgress.setProgress(1.0);
		batteryThread.stop();
		batteryThread= new Thread(new progressThread());
		batteryThread.start();
		errorMsg.setText("");
		if(errorMsg.getText()=="" && errorMsg2.getText()=="" && errorMsg3.getText()=="") {
			status1.setVisible(true);
			status2.setVisible(false);
		}
	}

	@FXML
	public void refillInsulin(ActionEvent e){
		insulinProgress.setProgress(1.0);
		insulinThread.stop();
		insulinThread= new Thread(new insulinThread());
		errorMsg2.setText("");
		if(errorMsg.getText()=="" && errorMsg2.getText()=="" && errorMsg3.getText()=="") {
			status1.setVisible(true);
			status2.setVisible(false);
		}

	}

	@FXML
	public void refillGlucagon(ActionEvent e){
		glucagonProgress.setProgress(1.0);
		glucagonThread.stop();
		glucagonThread= new Thread(new glucagonThread());
		errorMsg3.setText("");
		if(errorMsg.getText()=="" && errorMsg2.getText()=="" && errorMsg3.getText()=="") {
			status1.setVisible(true);
			status2.setVisible(false);
		}

	}

	public void startSimulator(ActionEvent e){
		System.out.println(caloriesCount.getValue());
		//igSimulator.start();
		harmonesSimulator.start();
		batteryThread= new Thread(new progressThread());
		batteryThread.start();
		startClock();
		Date d = new Date();
		startTime = d.getTime();
		statusMsg.setText("Simulator Started");
		statusMsg.setTextFill(Color.GREEN);
		stopPump.setVisible(true);
		startPump.setVisible(false);
	}

	public void stopSimulator(ActionEvent e){
		anchorPane.setVisible(true);
		anchorPane.setManaged(true);
		anchorPane2.setVisible(false);
		anchorPane2.setManaged(false);
		errorMsg.setText("");
		errorMsg2.setText("");
		errorMsg3.setText("");
		statusMsg.setText("");
		batteryThread.stop();
		System.out.println("caloriesCount: "+ caloriesCount.getValue());
		caloriesCount.setValue("");
		System.out.println("caloriesCount: "+ caloriesCount.getValue());

	}

	@SuppressWarnings("unchecked")
	public void initialize()
	{
		errorMsg.setVisible(false);
		validCred.setVisible(false);
		System.out.println(caloriesCount.getValue());
		series = new XYChart.Series<Number, Number>();
		lineChart.getData().addAll(series);
		series.getNode().setStyle("-fx-stroke: #989898; -fx-stroke-width: 2px; ");
		doctorPane.setVisible(false);
		anchorPane.setVisible(true);
        anchorPane2.setVisible(false);
		batteryProgress.setProgress(1.0);
		insulinProgress.setProgress(1.0);
		glucagonProgress.setProgress(1.0);
		rb1.setToggleGroup(group);
		rb1.setSelected(true);
		rb2.setToggleGroup(group);
		insulinBtn.setVisible(false);
		glucagonBtn.setVisible(false);
		calculateBtn.setVisible(false);
		weightField.setVisible(false);
		rb2.setVisible(false);
		loadCaloriesCount();
	}

	public void updateGui()
	{
		ConcurrentLinkedQueue<Number> bglQueue = Glucose.getInstance().getBglGui();

		int numOfPendingValues = bglQueue.size();
		for (int i = 0; i < numOfPendingValues; i++)
		{
			series.getData().add(new Data<Number, Number>(xSeriesData++, bglQueue.remove()));
		}


		if (series.getData().size() > 300)
		{
			series.getData().remove(0, series.getData().size()
					- 300);
		}
		xAxis.setLowerBound(xSeriesData - 300);
		xAxis.setUpperBound(xSeriesData - 1);

	}

	private double getCalories()
	{
		double calToConsume = 0;
		calToConsume = Double.parseDouble(caloriesCount.getValue());
		System.out.println(calToConsume);
		return calToConsume;
	}

	/**
	 * Updates the time stamp on the GUI
	 */
	private void updateTimeStamp()
	{
		//String rawTimeStamp = dateFormatter.format(bloodStream.getElapsedTime());
		String rawTimeStamp = dateFormatter.format(new Date(System.currentTimeMillis() - startTime));
		String[] timeStamp = rawTimeStamp.split(" ");
		String updateTime = timeStamp[0] + "   :   " + timeStamp[1] ;
		timer.setText(updateTime);
	}

	/**
	 * Battery Status
	 */

	public void radioButtonClicked (ActionEvent event)
	{
		if(rb1.isSelected()){
			insulinBtn.setDisable(true);
			glucagonBtn.setDisable(true);
			calculateBtn.setDisable(true);
			weightField.setDisable(true);
		} else {
			insulinBtn.setDisable(false);
			glucagonBtn.setDisable(false);
			calculateBtn.setDisable(false);
			weightField.setDisable(false);
		}
	}

	public void loadCaloriesCount (){
	caloriesList.removeAll();
	String c1 = "50";
	String c2= "100";
	String c3 = "200";
	String c4 = "300";
	String c5 = "-20";
	String c6 = "-50";
	String c7 = "-100";
	caloriesList.addAll(c1,c2,c3,c4,c5,c6,c7);
	caloriesCount.getItems().addAll(caloriesList);
	}


	private void startClock()
	{
		new AnimationTimer() {
			@Override
			public void handle(long now)
			{
				updateTimeStamp();
			}
		}.start();
	}

}
