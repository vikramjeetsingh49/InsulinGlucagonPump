package scs.simulators;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Logger;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import scs.main.Glucose;


public class HarmonesSimulator extends Simulator {

	private ConcurrentLinkedQueue<Number> bglValue = new ConcurrentLinkedQueue();

	private StringProperty maxValue = new SimpleStringProperty("6");

	private StringProperty minValue = new SimpleStringProperty("5");

	private final ConcurrentLinkedQueue<Double> glucoseLevelOffsetSeries = new ConcurrentLinkedQueue();

	private ArrayList<Double> data = new ArrayList<Double>();

	private StringProperty adjustedNegativeValue = new SimpleStringProperty("5.5");

	private StringProperty adjustedPositiveValue = new SimpleStringProperty("8");

	private HarmoneThread harmoneThread;

	private Double totalVal = 0d;

	public HarmonesSimulator() {
		super();
		executor = Executors.newCachedThreadPool(new ThreadFactory() {
			public Thread newThread(Runnable r)
			{
				Thread thread = new Thread(r);
				thread.setDaemon(true);
				return thread;
			}
		});
	}

	public StringProperty getMaxValue()
	{
		return maxValue;
	}

	public StringProperty getMinValue()
	{
		return minValue;
	}

	public StringProperty getAdjustedPositiveValue()
	{
		return adjustedNegativeValue;
	}

	public StringProperty getAdjustedNegativeValue()
	{
		return adjustedPositiveValue;
	}

	/**
	 * Starts the simulation
	 */
	public void start()
	{
		try
		{
			harmoneThread = new HarmoneThread();
			executor.execute(harmoneThread);
		} catch (Exception e)
		{
			Logger.getLogger("Exception in pancreas "+e);
			System.out.println("Exception in pancreas "+e);
		}
	}

	protected class HarmoneThread implements Runnable {
		
		double init = 0;
		Double insulinValue = 0d;
		int injectedTimes = 0;
		boolean isInjected = false;
		public void run()
		{
			try
			{
					bglValue = glucose.getBglControl();
					if (!bglValue.isEmpty())
						totalVal = (Double) bglValue.peek();
						bglValue.clear();
					if (!checkValue(totalVal))
						if (init == 0)
						{
							init = totalVal;
							data.add(init);
						} else
						{
							data.add(totalVal);
							evaluateData(totalVal);
						}
				try
				{
					Thread.sleep(500);
					executor.execute(this);
				} catch (InterruptedException ex)
				{
					Logger.getLogger("InterruptedException in pancreas "+ex);
					System.out.println("InterruptedException in pancreas "+ex);
				}
			} catch (RuntimeException e)
			{
				Logger.getLogger("RuntimeException in pancreas "+e);
				System.out.println("RuntimeException in pancreas "+e);
			}
		}

		protected void evaluateData(double val)
		{

			double increase = 0;
			double finalVal = 0;
			try
			{
				if (!isInjected)
				{
					finalVal = data.get(data.size() - 1);
					if (data.get(0) == 0.0)
						init = data.get(1);
					else
						init = data.get(0);
					System.out.println("init" + init);
					System.out.println("final" + finalVal);
					double diff = finalVal - init;
					double change = 0;
					if (diff > 0)
						change = diff * 30 * 10;
					else
						change = diff * 30;
				    
					increase = change;
					System.out.println("difference" + diff);
					if (diff > 0)
					{
						System.out.println("series rising make it donw");
						System.out.println("value for check"+ val);
						glucoseLevelOffsetSeries.addAll(seriesValue(increase, 1200d, "down"));
					} else
					{
						System.out.println("sugal level going down make it up");
						System.out.println(val);
						glucoseLevelOffsetSeries.addAll(seriesValue(10, 50d, "up"));
					}
					System.out.println("insulinValue :"+insulinValue);
					insulinValue = insulinValue + change;
					System.out.println("insulinValue2 : "+insulinValue);
					ArrayList<ConcurrentLinkedQueue<Double>> injections = Glucose.getInstance().getInjections();
					injections.add(glucoseLevelOffsetSeries);
					isInjected = true;
					System.out.println("total increase/decrease caluculated at a glance is"
							+ change);

				} else
				{
					double negativeValue = 8;
					double positiveValue = 5.5;
					try
					{
						negativeValue = Double.valueOf(adjustedNegativeValue.getValue());
						positiveValue = Double.valueOf(adjustedPositiveValue.getValue());
					} catch (NumberFormatException e)
					{
						Logger.getLogger("NumberFormatException in pancreas "+e);
						System.out.println("NumberFormatException in pancreas "+e);
					}
					if (val > negativeValue)
					{
						ConcurrentLinkedQueue<Double> posInjection = new ConcurrentLinkedQueue();
						if (val > 10)
							if (increase > 60)
								posInjection.addAll(seriesValue(increase / 3, 100d, "down"));
							else
								posInjection.addAll(seriesValue(increase / 5, 100d, "down"));
						else if (val > 8 && val < 10)
							posInjection.addAll(seriesValue(5, 50d, "down"));
						else
							posInjection.addAll(seriesValue(3, 50d, "down"));

						ArrayList<ConcurrentLinkedQueue<Double>> injections = Glucose.getInstance().getInjections();
						insulinValue = insulinValue
								+ (increase / (injectedTimes + 2));
						injections.add(posInjection);
						injectedTimes++;
					} else if (val < positiveValue)
					{
						ConcurrentLinkedQueue<Double> negInjection = new ConcurrentLinkedQueue();

						if (increase < 0)
						{
							negInjection.addAll(seriesValue(3, 50d, "up"));
						}

						else
						{
							negInjection.addAll(seriesValue(0.8, 50d, "up"));
						}

						ArrayList<ConcurrentLinkedQueue<Double>> injections = Glucose.getInstance().getInjections();
						insulinValue = insulinValue
								+ (increase / (injectedTimes + 2));
						injections.add(negInjection);
						injectedTimes++;
					}

				}
			} catch (Exception e)
			{
				Logger.getLogger("Exception in pancreas "+e);
				System.out.println("Exception in pancreas "+e);
			}

		}



		protected ArrayList<Double> seriesValue(
				double incBgl_mmol, double s, String m)
		{
			if (m == "up")
			{
				incBgl_mmol = Math.abs(incBgl_mmol);
			}
			ArrayList<Double> result = new ArrayList(1200);
			int itr = 0;
			double initAmt = incBgl_mmol * 0.2;
			int t1 = (int) (s * 0.2);
			result.addAll(itr, series(initAmt, t1, m));

			double initAmt2 = incBgl_mmol * 0.4;
			int t2 = (int) (1200 * 0.4);
			itr += t1;
			result.addAll(itr, series(initAmt2, t2, m));

			double initAmt3 = incBgl_mmol * 0.2;
			int t3 = (int) (1200 * 0.20);
			itr += t2;
			result.addAll(itr, series(initAmt3, t3, m));

			/*double initAmt4 = incBgl_mmol * 0.2;
			int t4 = (int) (1200 * 0.2);
			itr += t3;
			result.addAll(itr, getOffsetSerie(initAmt4, t4, m));*/

			return result;
		}

		protected ArrayList<Double> series(double amount,
				int s, String m)
		{
			double amt;
			if (m == "down")
				amt = -(amount / s);
			else
				amt = (amount / s);

			ArrayList<Double> result = new ArrayList(s);
			for (int i = 0; i < s; i++)
			{
				result.add(i, amt);
			}
			return result;
		}

		protected boolean checkValue(double totalVal)
		{
			double max = 6;
			double min = 5;
			try
			{
				max = Double.valueOf(getMaxValue().getValue());
				min = Double.valueOf(getMinValue().getValue());
			} catch (NumberFormatException e)
			{
				Logger.getLogger("NumberFormatException in pancreas "+e);
				System.out.println("NumberFormatException in pancreas "+e);
			}

			if ((totalVal < max) && (totalVal >= min))
			{
				return true;
			} else
			{
				return false;
			}

		}

	}

	

}
