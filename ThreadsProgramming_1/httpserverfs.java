// HttpServerFS.java

import java.net.*;
import java.io.*;

class HttpServerFS {
  BufferedReader inFromClient = null;
  DataOutputStream outToClient = null;
  PrintStream ps;

  String host; // name of client currently connected
  String address; // address of client currently connected

  int j;

  public void initWebServer(int port) {
    try {
      ServerSocket serverSocket = new ServerSocket(port, 5);
      byte[] buf = new byte[1000];
      int BUF_SIZE;
      InputStream is;
      BufferedReader bis;
      String page_requested = "/"; // page requested ("/" by default)

      System.err.println("Web Server started");
      while (true) {
	Socket socket = serverSocket.accept();
	System.err.println("-----------------------------------------------");

	System.err.println("Name and address of client:");
	System.err.println("---------------------------");
	host = socket.getInetAddress().getHostName();
	System.err.println(host);
	address = socket.getInetAddress().getHostAddress();
	System.err.println(address + "\n");

	inFromClient = new BufferedReader(
                         new InputStreamReader (
                           socket.getInputStream()));
	outToClient = new DataOutputStream(
                         socket.getOutputStream());
	ps = new PrintStream(outToClient);
	String line = null;

	System.err.println();
	System.err.println("REQUEST:");
	System.err.println("--------");
	while ((line=inFromClient.readLine()) != null && !line.equals("")) {
	  System.err.println("Server read: " + line);
	  j=4;
	  if (line.startsWith("GET")) {
	    while(j<line.length() && line.charAt(j) != ' ') j++;
	    page_requested = line.substring(4,j);
	  }
	}

	System.err.println();
	System.err.println("writing back page:" + page_requested);

	if (page_requested.equals("/"))
	    page_requested = "/index.html";
        String pageName = page_requested.replaceFirst("/","");
	try {
	    if (new File(pageName).isFile()){
		displayPage(200, pageName, true);
	    }
	    else {
		displayPage(404, "<H1>Error 404</H1>"+
                            "The Requested resource was not found ....<BR>" +
                            "Usage: http://server:"+port+"/file", false);
	    }
	}
	catch (Exception e) {System.out.println(e); }
	socket.close();
	System.err.println("server closed connection");
      }
    }
    catch(Exception e) {
      System.err.println(e);
      e.printStackTrace();
    }
  }

    public void displayPage (int statusCode, String responseString,
			     boolean isFile) throws Exception {

	String statusLine = null;
	String serverdetails = "Server: Java HTTPServer";
	String contentLengthLine = null;
	String fileName = null;
	String contentTypeLine = "Content-Type: text/html" + "\r\n";
	FileInputStream fin = null;

	if (statusCode == 200)
	 {
	     statusLine = "HTTP/1.1 200 OK" + "\r\n";
	     fileName = responseString;
	     System.out.println("sendResponse: " + fileName);
	     fin = new FileInputStream(fileName);
	     contentLengthLine = "Content-Length: "
		 + Integer.toString(fin.available()) + "\r\n";
	     if (!fileName.endsWith(".htm") && !fileName.endsWith(".html"))
		 contentTypeLine = "Content-Type: \r\n";
	}
	else {
	    statusLine = "HTTP/1.1 404 Not Found" + "\r\n";
	    responseString = myHTTPServer.HTML_START + responseString
		+ myHTTPServer.HTML_END;
	    contentLengthLine = "Content-Length: " + responseString.length()
		+ "\r\n";
	}

	// send header:
	outToClient.writeBytes(statusLine);
	outToClient.writeBytes(serverdetails);
	outToClient.writeBytes(contentTypeLine);
	outToClient.writeBytes(contentLengthLine);
	outToClient.writeBytes("Connection: close\r\n");
	outToClient.writeBytes("\r\n");
	
	// send page content:
	if (isFile) sendFile(fin, outToClient);
	else outToClient.writeBytes(responseString);
	
	// close connection:
	outToClient.close();
    }

    public void sendFile (FileInputStream fin, DataOutputStream out)
	throws Exception
    {
	byte[] buffer = new byte[1024] ;
	int bytesRead;
	
	while ((bytesRead = fin.read(buffer)) != -1 ) {
	    out.write(buffer, 0, bytesRead);
	}
	fin.close();
    }


}

