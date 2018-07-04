package scs.gui;

public class MainController {

	private static SimulatorController scontroller;

	public static SimulatorController getController() {
		return scontroller;
	}

	public static void setController(SimulatorController controller) {
		MainController.scontroller = controller;
	}
}
