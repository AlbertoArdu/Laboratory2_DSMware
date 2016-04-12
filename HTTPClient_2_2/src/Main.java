import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;


public class Main {

	/**
	 * Main class
	 * @param args filename
	 */
	public static void main(String[] args) {
		if(args.length < 2){
			System.err.println("Usage: prog server port [filename]");
			System.exit(-1);
		}

		/*
		// --------------- Single thread ---------------
		HTTPClient httpC = new HTTPClient(args[0],Integer.valueOf(args[1]));	
		if(args.length >= 3){
			// Print on std output
			//httpC.doGET(args[2], System.out);
			
			// Print on file
			PrintStream fileWriter = null;
			try {
				fileWriter = new PrintStream(new FileOutputStream(args[2], false));
				
			} catch (FileNotFoundException e) {
				System.err.println("Unable to create the output file.");
				System.exit(-1);
			}
			httpC.doGET(args[2], fileWriter);
			fileWriter.close();
			
		}else{
			httpC.doGET(null, System.out);
		}
		httpC.close();
		*/
		
		// --------------- Multi thread ---------------
		HTTPClientMulti httpCM = new HTTPClientMulti(args[0],Integer.valueOf(args[1]));
		if(args.length >= 3){
			// Print on std output
			//httpCM.doGET(args[2], args[2]);
			
			// Print on file
			//httpCM.doGET(args[2], args[2]+".multi.txt");
			httpCM.doGETasync("file1.txt", "file1.multi.txt");
			httpCM.doGETasync("file2.txt", "file2.multi.txt");
			httpCM.doGETasync("file3.txt", "file3.multi.txt");
			
		}else{
			httpCM.doGETasync(null, null);
		}
		try {
			httpCM.close();
		} catch (InterruptedException e) {
			System.err.println("Unable to close all the requests.");
			System.exit(-1);
		}
		
	}

}
