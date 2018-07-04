package scs.main;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;

import scs.simulators.InsulinGlucagonSimulator;

/**
 * Bloodstream class offers a singleton object which is accessed by whole project, This
 * Object Contains the information about the Blood, like glucose level and Insulin amount available. 
 * This Object Showcases the Stream of data which is displayed in graph.
 *
 */
public class Glucose {

	private static Glucose instance;

	private static long simulationStartTime;

	public static double getBloodvalue()
	{
		return 5;
	}

	public static Glucose getInstance()
	{
		if (Glucose.instance == null)
		{
			Glucose.instance = new Glucose();
			Date buffer = new Date();
			simulationStartTime = buffer.getTime();
		}
		return Glucose.instance;
	}

	private ConcurrentLinkedQueue<Number> bglControl = new ConcurrentLinkedQueue<Number>();

	
	private ConcurrentLinkedQueue<Number> bglGui = new ConcurrentLinkedQueue<Number>();

	private ArrayList<ConcurrentLinkedQueue<Double>> injections = new ArrayList();

	private Glucose() {
		super();
	}

	public ConcurrentLinkedQueue<Number> getBglControl()
	{
		return bglControl;
	}


	public ConcurrentLinkedQueue<Number> getBglGui()
	{
		return bglGui;
	}

	public double getInjectValue()
	{
		double eff = 0;
		if (!injections.isEmpty())
		{
			eff = calInjectValue();
			while (clean())
				;
		}
		return eff;
	}

	public synchronized ArrayList<ConcurrentLinkedQueue<Double>> getInjections()
	{
		return injections;
	}

	private double calInjectValue()
	{
		double eff = 0;
		for (ConcurrentLinkedQueue<Double> injection : injections)
		{
			if (!injection.isEmpty())
			{
				eff = injection.remove();
			}
		}
		return eff;
	}

	private boolean clean()
	{
		int size = injections.size();
		boolean clean = false;
		for (int i = 0; i < size; i++)
		{
			ConcurrentLinkedQueue<Double> q = injections.get(i);
			if (q.isEmpty())
			{
				injections.remove(i);
				clean = true;
				return clean;
			}
		}
		return clean;
	}

}
