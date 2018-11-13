/**
* Contains code to set up a chat room client that connects to a chat room server.
* Also responsible for sending and receiving information from the Server class.
* Can be multiple instances of Client connected to one Server at once.
* @author	Alex Westcott
* @version	1.0
* @since	2017-12-02
*/

import java.net.Socket;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Scanner;

public class Client{
	
	/**
	* Main method for this application, used to set up the Client constructor and run the program.
	* @param args Expected arguments in the application.
	*/
	
	public static void main(String[] args){

		/**
		* Setting up the Client object.
		*/
	
		ClientInstance client = new ClientInstance();

		/**
		* Running the start() method from the Client class.
		*/

		client.start();

	}
}
	
/**
* Class used to handle the client's session in the chat room.
* Will deal with connecting to the server, incoming messages, and outgoing messages.
*/

class ClientInstance{
	
	/**
	* Setting up the Scanner object to take user input.
	*/
	
	Scanner sc = new Scanner(System.in);
	
	/**
	* Declaring Strings for the IP address of the server, and for outgoing messages.
	*/
	
	private String address = "";
	private String newMessage = "";
	
	/**
	* Socket which is connected to the server.
	*/
	
	private Socket s = null;
	
	/**
	* BufferedReader and PrintWriter are used for dealing with incoming and outgoing messages respectively.
	*/
	
	private BufferedReader in; 
	private PrintWriter out;
	
	/**
	* Method which deals with the running of the client, containing other methods.
	*/
	
	public void start(){
		connect();
		handleOutgoing();
		handleIncoming();
	}
	
	/**
	* Method used to establish a connection to the server, as well as setting up input/output streams.
	*/
	
	private void connect(){
		try{
			
			/**
			* Prompt the user to enter an IP address, take the user input from the Scanner, then (alongside the port 1303) connect to the server.
			*/
			
			System.out.println("Enter the address of the server to connect to ('localhost' to connect to a server running on this machine)");
			address = sc.nextLine();
			s = new Socket(address, 1303);
			
			/**
			* Setting up BufferedReader and PrintWriter to deal with incoming and outgoing messages respectively.
			*/
			
			in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			out = new PrintWriter(new OutputStreamWriter(s.getOutputStream()));
			
		} catch(IOException e){
			
			/**
			* Catch IOException error and print error to user, then exit program.
			*/
			
			System.err.println("Error trying to connect to server: " + e.getMessage());
			System.exit(0);
		}
	}
	
	/**
	* Method used to deal with sending messages from this client out to the server, which will then be responsible for broadcasting them to all clients.
	*/
	
	private void handleOutgoing(){
		
		try{
			
			/**
			* Prompt user to enter a display name for the chat (the Server class deals with most of this).
			*/
			
			System.out.println("Please enter a unique display name for the chat room");
			
			/**
			* Thread which is responsible for printing any messages sent by the client to the buffer.
			* The server will then receive these messages to broadcast back to all clients.
			*/
			
			Thread sendMsg = new Thread(new Runnable(){
				public void run(){
					while(true){
						newMessage = sc.nextLine();
						out.println(newMessage);
						out.flush();
					}
				}
			});
			sendMsg.start();
		} catch(Exception e){
			
			/**
			* Catch Exception error and print error to user.
			*/
			
			System.err.println("Error while outputting messages to server: " + e.getMessage());
			
		}
	}

	/**
	* Method used to deal with receiving messages from the server, and printing them to this client.
	*/
	
	private void handleIncoming(){
	
		try{
			
			/**
			* Thread which is responsible for printing any messages sent by the server to the client's screen.
			* While-loop means it is always listening out for new messages.
			*/
	
			Thread getMsg = new Thread(new Runnable(){
				public void run(){
					while(true){				
						String incomingMsg = null;
						try{
							incomingMsg = in.readLine();
							if(incomingMsg == null){
								System.exit(0);
							}
							System.out.println(incomingMsg);
						} catch(IOException e){
							
							/**
							* Catch IOException error and print error to user, then exit the program.
							*/
							
							System.err.println("Error while receiving messages from server: " + e.getMessage());
							System.exit(0);
						}
					}
				}
			});
			getMsg.start();
		} catch(Exception e){
			
			/**
			* Catch Exception error and print error to user.
			*/
			
			System.err.println("Error while receiving messages from server: " + e.getMessage());
		}
	}
	
}