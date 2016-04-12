/**
 * Class to record logs in a synchronized way
 */
import java.io.*;

/**
 * @author alberto.ardusso@gmail.com
 *
 */
public class Log {
	PrintStream out;
	
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
