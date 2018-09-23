/***********************************************************************

 SimpleWebServer.java

 Curtis P. Hohl
 9/22/2018

 This toy web server is used to illustrate security vulnerabilities.
 This web server only supports extremely simple HTTP GET requests.

 ***********************************************************************/

import java.io.*;
import java.net.*;
import java.util.*;

public class WebServer {

// First, declare class variables

    /* Run the HTTP server on this TCP port. */
    /* The socket used to process incoming connections
       from web clients */
    private static ServerSocket dServerSocket; //Class variable, from java.net


// Second, declare class methods

    public WebServer () throws Exception { //Constructor for the WebServer
        /* Instantiate a socket and connect it to the port */
        dServerSocket = new ServerSocket (PORT); // From java.net
    }

    /* Runs the WebServer */
    public void run() throws Exception { //Class method
        while (true) { //used to wait for a connection
            /* wait for a connection from a client */
            Socket s = dServerSocket.accept(); //From java.net

            /* then process the client's request */
            processRequest(s); //Should be an HTTP request
        }
    }

    /* Reads the HTTP request from the client, and
       responds with the file the user requested or
       a HTTP error code. */
    public void processRequest(Socket s) throws Exception { //Class method. Socket from java.net
        /* Used to read data from the client.
         * InputStreamReader.getInputStream can be a costly operation,
          so wrap with BufferedReader to buffer the input.  Without buffering, each invocation
          of read() or readLine() could cause bytes to be read from the file,
          converted into characters, and then returned, which can be very inefficient. */
        BufferedReader br = new BufferedReader (new InputStreamReader (s.getInputStream())); //From java.io, local variable

        /* New object that can be used to write data to the client */
        OutputStreamWriter osw = new OutputStreamWriter (s.getOutputStream()); //From java.io, local variable

        /* reads the HTTP request from the client */
        String request = br.readLine(); // local variable

        /* Initialize command and pathname to be null, these will be used later. */
        String command = null; //local variable
        String pathname = null; //local variable

        /* Tokenize the HTTP request, seperated by spaces */
        StringTokenizer st = new StringTokenizer (request, " "); //From java.util, local variable

        command = st.nextToken(); //Assumes that the command is the first token in the parsed request.
        pathname = st.nextToken(); // Assumes that the pathname (to execute the command on) is the second request.
        // Hopefully, data is sent with the correct syntax.

        if (command.equals("GET")) {
       /* if the request is a GET
         try to respond with the file
         the user is requesting */
            serveFile (osw, pathname); //call the serveFile method, passing the OutputStreamWriter and the user's requested pathname
        }
        else {
       /* if the request is a NOT a GET,
         return an error saying this server
         does not implement the requested command */
            osw.write ("HTTP/1.0 501 Not Implemented\n\n");
        }

        /* as soon as there is an error, close the connection to the client. Errors are a security risk. */
        osw.close();
    }

    public void serveFile (OutputStreamWriter osw, String pathname) throws Exception { //Class method. OutputStreamWriter from java.io
        FileReader fr = null; //initialize a new FileReader as null, for now. local variable.
        int c = -1; //initialize the c variable as -1. -1 means that read() (from InputStreamReader) has reached the end of its input stream. local variable.
        StringBuffer sb = new StringBuffer(); //new StringBuffer to provide a growable, writable character sequence. local variable.

   /* remove the initial slash at the beginning
      of the pathname in the request */
        if (pathname.charAt(0)=='/') { //if there's a '/' at the first index of the path name,
            pathname = pathname.substring(1); //make pathname a substring of itself, removing the first character
        }

   /* if there was no filename specified by the
      client, serve the "index.html" file */
        if (pathname.equals("")) { //empty pathname
            pathname = "index.html"; //replace with index.html, which is the standard default
        }

        /* try to open file specified by pathname */
        try {
            fr = new FileReader (pathname); //Filereader allows reading from the file at the pathname. From java.io, local variable
            c = fr.read(); //read the file from the pathname, returns -1 if the end of the stream has been reached.
        }
        catch (Exception e) {
       /* if the file is not found,return the
          appropriate HTTP response code  */
            osw.write ("HTTP/1.0 404 Not Found\n\n");
            return;
        }

  /* if the requested file can be successfully opened
    and read, then return an OK response code and
    send the contents of the file */
        osw.write ("HTTP/1.0 200 OK\n\n");
        while (c != -1) { //while the end of the input stream has not been reached
            sb.append((char)c); //append whatever is in the file to the StringBuffer
            c = fr.read(); //read the rest of the file
        }
        osw.write (sb.toString()); //write the file contents from the StringBuffer
    }

    /* This method is called when the program is run from
      the command line. */
    public static void main (String argv[]) throws Exception {

        /* Create a SimpleWebServer object, and run it */
        WebServer sws = new WebServer(); //Instantiates a new WebServer object.
        sws.run(); //Starts the sws web server.
    }
}

