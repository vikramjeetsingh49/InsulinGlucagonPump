package scs.simulators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import scs.main.Glucose;

public class InsulinGlucagonSimulator extends Simulator {

	private double bgl_mmol = 0;


	private double bgl = 0;

	public final ConcurrentLinkedQueue<Double> offsetValue = new ConcurrentLinkedQueue();

	private BooleanProperty isNormal = new SimpleBooleanProperty(true);

	private BooleanProperty checkNormal = new SimpleBooleanProperty(false);

	private BooleanProperty consume = new SimpleBooleanProperty();

	private GlucoseThread glucoseThread;

	private DoubleProperty bglTotal_mol = new SimpleDoubleProperty(0);

	public InsulinGlucagonSimulator() {
		super();
		consume.setValue(false);
			executor = Executors.newCachedThreadPool(new ThreadFactory() {
				public Thread newThread(Runnable r)
				{
					Thread thread = new Thread(r);
					thread.setDaemon(true);
					return thread;
				}
			});
	}

	public void start()
	{
		glucoseThread = new GlucoseThread();
		executor.execute(glucoseThread);
	}

	public Property<Boolean> getIsNormal()
	{
		return isNormal;
	}

	public Property<Boolean> getCheckNormal()
	{
		return checkNormal;
	}

	public Property<Boolean> getConsume()
	{
		return consume;
	}

	public DoubleProperty getBglTotal_mol()
	{
		return bglTotal_mol;
	}

	
	public double getRegularValue()
	{
		double value = 5.5;
		double offset = Math.random() / 5;
		return value + offset;
	}
	
	public void consume(double amtConsume)
	{
		consume.setValue(true);
		double amt = calBgl(amtConsume);
		bglTotal_mol.setValue(amt);
		if (amtConsume > 0)
		{
			offsetValue.addAll(positiveBglValues(amt));
		} else
		{
			offsetValue.addAll(negativeBglValues(amt));
		}
	}

	public double calBgl(Double kcal)
	{
		System.out.println("kcal :" + kcal);
		if (kcal > 0)
		{
			kcal /= 2;
		}
		double carbs_mg = (kcal / 4) * 1000;
		System.out.println("carbs in milligrams : " + carbs_mg);
		double bloodAmount = Glucose.getBloodvalue() * 10;

		double bgl_mg_l = carbs_mg / bloodAmount;
		System.out.println("blood glucose level in mg per blood level : " + bgl_mg_l);
		double bgl_mmol_L = bgl_mg_l / 18;

		System.out.println("blood glucose level in mmol per blood level :" + bgl_mmol_L);

		return bgl_mmol_L;

	}
	
	public ArrayList<Double> positiveBglValues(
			double increaseInBgl)
	{
		ArrayList<Double> result = new ArrayList(1200);



		int itr = 0;
		double initBglValue = increaseInBgl * 0.1;
		int t1 = (int) (1200 * 0.25);
		result.addAll(itr, bglSeries(initBglValue, t1));

		double bglValue1 = increaseInBgl * 0.45;
		int t2 = (int) (1200 * 0.125);
		itr = t1 - 1;
		result.addAll(itr, bglSeries(bglValue1, t2));
		
		double bglValue2 = increaseInBgl * 0.20;
		int t3 = (int) (1200 * 0.125);
		itr += t2;
		result.addAll(itr, bglSeries(bglValue2, t3));

		return result;
	}

	public Collection<? extends Double> negativeBglValues(
			double decreaseInBgl)
	{
		ArrayList<Double> result = new ArrayList(300);
		result.addAll(0, bglSeries(decreaseInBgl, 1200));
		return result;
	}

	public ArrayList<Double> bglSeries(double amt, int time)
	{
		double amount = amt / time;
		ArrayList<Double> result = new ArrayList(time);
		for (int i = 0; i < time; i++)
		{
			result.add(i, amount);
		}
		return result;
	}

	private class GlucoseThread implements Runnable {
		public void run()
		{
			try
			{
				ConcurrentLinkedQueue<Number> bglGui = glucose.getBglGui();
				ConcurrentLinkedQueue<Number> bglCtrl = glucose.getBglControl();
				
				double regularValue = getRegularValue();
				if (!offsetValue.isEmpty())
				{
					double nextValue = offsetValue.remove();
					bgl += nextValue;
					updateProgress(nextValue);
				} else
				{
					consume.setValue(false);
					bgl_mmol = 0;
				}

				if (isNormal.getValue())
				{
					final double injectValue = glucose.getInjectValue();
					bgl += injectValue;
					checkBalancingVal(injectValue);
				}
				double finalValue = regularValue + bgl;
				bglGui.add(finalValue);
				bglCtrl.add(finalValue);
				try
				{
					Thread.sleep(100);
					executor.execute(this);
				} catch (InterruptedException ex)
				{
					Logger.getLogger("InterruptedException in simulink "+ex);
					System.out.println("InterruptedException in simulink "+ex);
				}
			} catch (RuntimeException e)
			{
				Logger.getLogger("RuntimeException in simulink "+e);
				System.out.println("RuntimeException in simulink "+e);
			}
		}

		private void checkBalancingVal(final double injectValue)
		{
			Platform.runLater(new Runnable() {
				public void run()
				{
					if (injectValue == 0)
					{
						checkNormal.setValue(false);
					} else
					{
						checkNormal.setValue(true);
					}
				}
			});
		}

		private void updateProgress(double currentAmount)
		{
			bgl_mmol += currentAmount;
			double progress = bgl_mmol / bglTotal_mol.doubleValue();
		}

	}


}
