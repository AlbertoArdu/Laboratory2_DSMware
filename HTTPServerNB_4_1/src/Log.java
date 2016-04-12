/**
 * Class to record logs in a synchronized way
 */
import java.io.*;

public class Log {
	private PrintStream out;
	
	public Log(){
		out = System.err;
	}
	public Log(String filename)throws FileNotFoundException{
		out = new PrintStream(new FileOutputStream(filename));
	}
	synchronized public void log(String s){
		out.println(s);
	}
}
