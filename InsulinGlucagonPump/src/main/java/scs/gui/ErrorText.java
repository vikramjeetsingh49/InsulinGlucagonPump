package scs.gui;

public enum ErrorText {
	Battery_Low("Battery Low! Please Change Battery"), Insulin_Low(
			"Insulin Required! Refill the insulin."), Glucagon_low(
			"Glucagon Required! Refill the glucagon.");

	ErrorText(String text) {
		this.text = text;
	}

	private final String text;

	public String getText()
	{
		return text;
	}
}
