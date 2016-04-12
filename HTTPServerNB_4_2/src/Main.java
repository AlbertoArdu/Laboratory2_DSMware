public class Main {

	public static void main(String[] args) {
		if(args.length < 1){
			System.err.println("Usage: prog port");
			System.exit(-1);
		}
		
		HTTPServerNB httpServer = new HTTPServerNB();
		httpServer.startWebServer(Integer.parseInt(args[0]));
	}

}
