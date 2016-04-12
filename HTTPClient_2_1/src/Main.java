import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;


public class Main {

	/**
	 * Main class
	 * @param args filename
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if(args.length < 2){
			System.err.println("Usage: prog server port [filename]");
			System.exit(-1);
		}
		
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
	}

}
