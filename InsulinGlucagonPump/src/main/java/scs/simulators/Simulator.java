package scs.simulators;

import java.util.concurrent.ExecutorService;

import scs.main.Glucose;

public abstract class Simulator {


	protected Glucose glucose = Glucose.getInstance();

	protected ExecutorService executor;

	public abstract void start();

}
