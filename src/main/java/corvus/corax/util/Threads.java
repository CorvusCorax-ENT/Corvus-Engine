package corvus.corax.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Unsafe class, when using make sure you call shutdown after using any of the pools;
 * 
 * @author Vlad Ravenholm <br>
 */
public class Threads {

	private static ExecutorService fixed4;
	private static ExecutorService general;

	/**
	 * @param command
	 * @see java.util.concurrent.Executor#execute(java.lang.Runnable)
	 */
	public static void executeFixed(Runnable command) {
		fixed4().execute(command);
	}

	/**
	 * @param task
	 * @return
	 * @see java.util.concurrent.ExecutorService#submit(java.util.concurrent.Callable)
	 */
	public static <T> Future<T> submitFixed(Callable<T> task) {
		return fixed4().submit(task);
	}

	/**
	 * @param task
	 * @param result
	 * @return
	 * @see java.util.concurrent.ExecutorService#submit(java.lang.Runnable, java.lang.Object)
	 */
	public static <T> Future<T> submitFixed(Runnable task, T result) {
		return fixed4().submit(task, result);
	}

	/**
	 * @param task
	 * @return
	 * @see java.util.concurrent.ExecutorService#submit(java.lang.Runnable)
	 */
	public static Future<?> submitFixed(Runnable task) {
		return fixed4().submit(task);
	}

	/**
	 * @param command
	 * @see java.util.concurrent.Executor#execute(java.lang.Runnable)
	 */
	public static void executeGeneral(Runnable command) {
		general().execute(command);
	}

	/**
	 * @param task
	 * @return
	 * @see java.util.concurrent.ExecutorService#submit(java.util.concurrent.Callable)
	 */
	public static <T> Future<T> submitGeneral(Callable<T> task) {
		return general().submit(task);
	}

	/**
	 * @param task
	 * @param result
	 * @return
	 * @see java.util.concurrent.ExecutorService#submit(java.lang.Runnable, java.lang.Object)
	 */
	public static <T> Future<T> submitGeneral(Runnable task, T result) {
		return general().submit(task, result);
	}

	/**
	 * @param task
	 * @return
	 * @see java.util.concurrent.ExecutorService#submit(java.lang.Runnable)
	 */
	public static Future<?> submitGeneral(Runnable task) {
		return general().submit(task);
	}

	public static final void sleep(long milis) {
		try {
			Thread.sleep(milis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return the fixed4
	 */
	public static ExecutorService getFixed4() {
		return fixed4();
	}

	/**
	 * @return the general
	 */
	public static ExecutorService getGeneral() {
		return general();
	}
	
	public static void waitFor(Callable<Boolean> call) {
		try {
			while(!call.call()) {
				Threads.sleep(500);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static ExecutorService fixed4() {
		if(fixed4 == null)
			fixed4 = Executors.newFixedThreadPool(4);
			
		return fixed4;
	}

	public static ExecutorService general() {
		if(general == null)
			 general = Executors.newCachedThreadPool();
		
		return general;
	}
	
	public static void shutdown() {
		if(general != null)
			general.shutdown();
		
		if(fixed4 != null)
			fixed4.shutdown();
	}
}
