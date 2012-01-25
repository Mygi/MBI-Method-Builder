package nig.io;

import java.io.BufferedReader;
import java.io.InputStreamReader;




public	class EraserThread implements Runnable {
	private boolean stop;

	/**
	 *@param The prompt displayed to the user
	 */
	public EraserThread(String prompt) {
		System.out.print(prompt);
	}

	/**
	 * Begin masking...display asterisks (*)
	 */
	public void run () {
		stop = true;
		while (stop) {
			System.out.print("\010*");
			try {
				Thread.currentThread().sleep(1);
			} catch(InterruptedException ie) {
				ie.printStackTrace();
			}
		}
	}

	/**
	 * Instruct the thread to stop masking
	 */
	public void stopMasking() {
		this.stop = false;
	}
	
	/**
	 * Helper function for interactive prompting
	 * 
	 * @param prompt
	 * @return
	 * @throws Throwable
	 */
	public static String readString (String prompt) throws Throwable {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		if (prompt!=null) System.out.print(prompt);
		return in.readLine();
	}
}


