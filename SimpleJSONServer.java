import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
// Based on: https://gist.github.com/ssaurel/2e8462d70b9e61c4dd6df2dc2cd725d7#file-javahttpserver-java

// Each Client Connection will be managed in a dedicated Thread
public class SimpleJSONServer implements Runnable { 
	
	static final int PORT = 8080;
	private Socket connect;	// Client Connection via Socket Class
	public SimpleJSONServer(Socket c) { connect = c; }
	
	public static void main(String[] args) {
		try {
			ServerSocket serverConnect = new ServerSocket(PORT);
			System.out.println("Server started.\nListening for connections on port : " + PORT + " ...\n");
			
			while (true) { // Listen until user halts server execution: Ctrl+C
				SimpleJSONServer myServer = new SimpleJSONServer(serverConnect.accept());
                System.out.println("Connecton opened. (" + new Date() + ")");
				Thread thread = new Thread(myServer); // Dedicated thread for client connection
				thread.start();
			}
			
		} catch (IOException e) {
			System.err.println("Server Connection error : " + e.getMessage());
		}
	}

	@Override
	public void run() {
		// we manage our particular client connection
		BufferedReader in = null; PrintWriter out = null; BufferedOutputStream dataOut = null;
		
		try {
			in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
			out = new PrintWriter(connect.getOutputStream());
			dataOut = new BufferedOutputStream(connect.getOutputStream());
			
            File file = new File(new File("."), "data.json");
            int fileLength = (int) file.length();
            byte[] fileData = readFileData(file, fileLength);
            
            // send HTTP Headers
            out.println("HTTP/1.1 200 OK");
            out.println("Server: Java JSON Server: 0.1");
            out.println("Date: " + new Date());
            out.println("Content-type: application/json");
            out.println("Content-length: " + fileLength);
            out.println(); 
            out.flush(); 
            dataOut.write(fileData, 0, fileLength);
            dataOut.flush();
			
		} catch ( Exception e) {

            System.err.println("Error");

		} finally {

			try {
				in.close();
				out.close();
				dataOut.close();
				connect.close(); // we close socket connection
			} catch (Exception e) {
				System.err.println("Error closing stream : " + e.getMessage());
			} 
            System.out.println("Connection closed.\n");

		}
		
	}
	
	private byte[] readFileData(File file, int fileLength) throws IOException {
		FileInputStream fileIn = null;
		byte[] fileData = new byte[fileLength];
		
		try {
			fileIn = new FileInputStream(file);
			fileIn.read(fileData);
		} finally {
			if (fileIn != null) 
				fileIn.close();
		}
		
		return fileData;
	}
}