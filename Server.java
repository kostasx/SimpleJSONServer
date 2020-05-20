import java.net.*;
import java.io.*;
import java.io.PrintStream;
import java.util.Date;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.FileWriter;

class Post {

    int id;
    String title;
    String author;
    String content;


    public Post( int id, String title, String author, String content ){
        this.id = id;
        this.title = title;
        this.author = author;
        this.content = content;
    }

    @Override
    public String toString() { 
        return String.format("{ \"id\": " + id + ", \"title\": \"" + title + "\", \"author\" : \"" + author + "\", \"content\": \"" + content + "\" }"); 
    } 

}

public class Server {

    private static final int port = 8080;
    private static String json = "db.json";
    private static String path;
    private static String input = "";

    public void readRequest(Socket client) throws IOException {

        Reader raw = new InputStreamReader(client.getInputStream(), "utf8");
        BufferedReader reader = new BufferedReader(raw);

        while (true) {

            String line = reader.readLine().trim();
            input += line + "\r\n"; // Get user agent input

            // Get URL, HTTP method and URL Path
            if ( line.matches("(?i)^(GET|POST|PUT|DELETE|HEAD|TRACE|OPTIONS).*$") ){

                System.out.printf("\r\n >> HTTP REQUEST: %s \r\n", line);

                Pattern pattern = Pattern.compile("(?i)^(GET|POST|PUT|DELETE|HEAD|TRACE|OPTIONS)\\s+(\\S+)\\s+(HTTP)/");
                Matcher matcher = pattern.matcher(line);

                if ( matcher.find() ) {
                    path = matcher.group(2);
                    System.out.printf("Match: %s", path);
                }

            }

            if ( line.equals("") ){
                break;
            }

        }

    }

    public void sendHTML(Socket client) throws IOException {

        String lol = "<html><h1>Java Server</h1><p>Try the <a href='/json'><strong><code>/json</code></strong></a> path to get served a JSON file from the server.</p></html>";
        PrintStream writer = new PrintStream(client.getOutputStream());
        writer.println("HTTP/1.1 200 OK");
        writer.println("Content-Type: text/html");
        writer.println();
        writer.println(lol);
        writer.flush();

    }

    public void sendJSON(Socket client) throws IOException {

        PrintWriter writer = new PrintWriter(client.getOutputStream());
        BufferedOutputStream dataOut = new BufferedOutputStream(client.getOutputStream());

        File jsonFile = new File(
            Server.class.getProtectionDomain().getCodeSource().getLocation().getPath(), 
            json
        );

        // Create a dummy/sample data file, if not already exists:
        if ( !jsonFile.exists() ){ 

            jsonFile.createNewFile(); 
            FileWriter fileWriter = new FileWriter(jsonFile);
            Post post = new Post( 1, "Java Sockets", "Magnus", "Let's build a basic Java Server!" );
            String jsonData = "{\"posts\":[ " + post.toString() + " ]}";
            fileWriter.write( jsonData );
            fileWriter.close();
        }

        // Path currentRelativePath = Paths.get("");
        // String currRelPath = currentRelativePath.toAbsolutePath().toString(); // Current relative path

        int fileLength = (int) jsonFile.length();
        byte[] fileData = readFileData(jsonFile, fileLength);

        writer.println("HTTP/1.1 200 OK");
        writer.println("Server: Java JSON Server: 0.1");
        writer.println("Date: " + new Date());
        writer.println("Content-type: application/json");
        writer.println("Content-length: " + fileLength);
        writer.println("Access-Control-Allow-Origin: *");
        writer.println(); 
        writer.flush(); 
        dataOut.write(fileData, 0, fileLength);
        dataOut.flush();

    }

    private byte[] readFileData(File file, int fileLength) throws IOException {

        FileInputStream fileIn = null;
		byte[] fileData = new byte[fileLength];
		
		try {
			fileIn = new FileInputStream(file);
			fileIn.read(fileData);
		} finally {
			if (fileIn != null){
				fileIn.close();
            } 
		}
		
		return fileData;

    }
    
    public void Route(Socket client) throws IOException {

        if ( path.equals("/json") ){

            sendJSON( client );

        } else {

            sendHTML( client );

        }

    }

    public void serveClient(Socket client) throws IOException {
        // System.out.println("serveClient()");
        try {
            readRequest(client);
            // System.out.println(input);
            Route(client);
        } finally {
            client.close();
        }
    }

    public Server(int port) {

        try {
            ServerSocket socket = new ServerSocket(port);
            System.out.printf("Server started at port: %d %n", port); 
            System.out.printf("Visit the page at http://localhost:%d %n", port); 
            System.out.println("Waiting for a client ..."); 
 
            while (true) {
                Socket client = socket.accept();
                System.out.println("Client accepted"); 
                serveClient(client);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String args[]) {

        new Server(port);

    }
}