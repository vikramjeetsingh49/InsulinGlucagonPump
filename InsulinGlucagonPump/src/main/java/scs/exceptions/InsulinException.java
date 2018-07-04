package scs.exceptions;

import scs.gui.ErrorText;

public class InsulinException extends Exception {

	private static final long serialVersionUID = 1L;

	public InsulinException(ErrorText errorMsg) {

		super(errorMsg.getText());
	}

	public InsulinException(String message, Throwable cause) {
		super(message, cause);
	}

}
