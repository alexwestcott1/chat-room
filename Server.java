/**
* Contains code to set up a chat room server that can accept incoming connection from clients.
* Also deals with clients while they are connected to the server, as well as sending/receiving information from said clients.
* @author	Alex Westcott
* @version	1.0
* @since	2017-12-02
*/

import java.net.Socket;
import java.net.ServerSocket;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.net.InetAddress;

public class Server{
	
	/**
	* Server socket which will run the chat room program.
	*/
	
	private static ServerSocket ss;
	
	/**
	* HashSet containing all of the sessions currently connected to the server.
	* Object type HandleSession, as this allows the use of all methods from the HandleSession class.
	*/
	
	HashSet<HandleSession> connectedUsers;
	
	/**
	* HashSet containing all of the usernames of the users connected to the server.
	*/
	
	HashSet<String> listOfUsernames;
	
	/**
	* Long number which records the time when the server was first instantiated.
	*/
	
	long openTime = System.currentTimeMillis();
	
	/**
	* Main method for this application, used to set up the Server constructor and run the program.
	* @param args Expected arguments in the application.
	*/
	
	public static void main(String[] args){
		
		/**
		* Setting up the Server object.
		*/
		
		Server server = new Server();
		
		/**
		* Running the start() method from the Server class.
		*/
		
		server.start();
	}
	
	/**
	* Method used to deal with the running of the chat room server.
	* Includes dealing with incoming connections, and updating the lists of connected users.
	*/
	
	public void start(){
		
		try{
			
			/**
			* Declaring the ServerSocket object with a randomly chosen port number (in this case, 1303).
			*/
			
			ss = new ServerSocket(1303);
			
			/**
			* Declaring Socket and Thread objects
			*/
			
			Socket s;
			Thread t;
			
			/**
			* HashSet objects for the sessions connected to the server, and for the list of usernames currently connected.
			*/
			
			connectedUsers = new HashSet<HandleSession>();
			listOfUsernames = new HashSet<String>();
			
			/**
			* Print message to server to inform that the chat room is open and ready for incoming connections.
			*/
			
			System.out.println("Chat room open");
			
			while(true){
				
				/**
				* Socket object is accepted by the server
				*/
				
				s = ss.accept();
				
				/**
				* Set up the client's session, with the newly-established socket as the argument.
				*/
				
				HandleSession session = new HandleSession(s);
				
				/**
				* Set up the thread for this session with the client's session as the argument.
				*/
				
				t = new Thread(session);
				
				/**
				* Add the client's session to the list of connected sessions.
				*/
				
				connectedUsers.add(session);
				
				/**
				* Establish the session's position in the ConnectedUsers list using a method from the HandleSession class.
				* This will be used for when the session needs to be disconnected gracefully.
				*/
				
				session.setPosition(session);
				
				/**
				* Start the thread for this session.
				*/
				
				t.start();
			}
		} catch(IOException e){
			
			/**
			* Catch IOException error and print error to user.
			*/
			
			System.err.println("Error while starting the server: " + e.getMessage());
		} finally {
			try{
				
				/**
				* Close server socket down, shutting the server down completely.
				*/
				
				ss.close();
			} catch(IOException e){
				
				/**
				* Catch IOException error and print error to user.
				*/
				
				System.err.println("Error while starting the server: " + e.getMessage());
			}
		}
	}

	/**
	* Class used to handle the client's session in the chat room.
	* Implements Runnable so multiple clients can send/receive messages concurrently.
	*/
	
	class HandleSession implements Runnable{
	
		/**
		* Socket which is connected to the server.
		*/
		
		private Socket s;
		
		/**
		* Strings to represent usernames of the users, and to represent any incoming messages to the server.
		*/
		
		private String username = "";
		private String tmpUsername = "";
		private String incomingMsg = "";
		
		/**
		* Long number representing the time when the client joins the server.
		*/
		
		private long clientJoinTime;
		
		/**
		* Boolean which checks to see if the client's requested username has been approved.
		*/
		
		private boolean usernameApproved;
		
		/**
		* HandleSession object used to represent this session's position in the connectedUsers HashSet.
		*/
		
		private HandleSession position;
	
		/**
		* Constructor for the HandleSession class, to be used by the main class to gain access to the methods in this class.
		* @param s the client socket to be associated with this session.
		*/
	
		HandleSession(Socket s){
			this.s = s;
		}
		
		/**
		* Method responsible for handling the client's session, and listening out for any incoming messages.
		* Also approves the client's username.
		*/
	
		public synchronized void run(){
			try{
				
				/**
				* Declaring the BufferedReader object that is responsible for storing any incoming information from the client.
				*/
				
				BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
				
				/**
				* Declaring the PrintWriter object that is responsible for broadcasting messages to clients.
				*/
				
				PrintWriter out = new PrintWriter(new OutputStreamWriter(s.getOutputStream()));
				
				/**
				* Run this while-loop until the client's username is approved.
				*/
				
				while(!usernameApproved){
					
					/**
					* Read the next line from the client and trim any whitespace.
					*/
					
					tmpUsername = in.readLine().trim();
					
					/**
					* If the selected username is already being used by a user in the chat room, prompt the user to enter another username.
					* Else, if the name selected is unique to the chat room, approve it and add the username to the list of connected usernames.
					*/
					
					if(listOfUsernames.contains(tmpUsername)){
						out.println("Username already in use, please try again");
						out.flush();
					} else {
						usernameApproved = true;
						listOfUsernames.add(tmpUsername);
					}
				}
				
				username = tmpUsername;
				
				/**
				* Broadcast a message to all clients informing them of a new user connecting.
				*/
				
				sendToAll(username + " has connected");
				
				/**
				* Record the time that the client joined the server.
				*/
				
				clientJoinTime = System.currentTimeMillis();
				
				/**
				* Inform the newly-connected user of how to get help.
				*/
				
				out.println("Type /help to see a list of possible commands");
				out.flush();
				
				while(true){
					
					/**
					* Read the next message in the BufferedReader stream.
					*/
					
					incomingMsg = in.readLine();
					
					/**
					* If the message sent is a specific command, respond accordingly.
					* Else, assuming the message is not empty, broadcast the message to all connected clients.
					*/
					
					if(incomingMsg.equals("/help")){
						out.println("List of commands:");
						out.println("/help: List all possible commands");
						out.println("/duration: Display amount of time server has been running");
						out.println("/since: Display amount of time since user connected");
						out.println("/ip: Display IP address of server");
						out.println("/usercount: Display number of users currently in server");
						out.println("/userlist: List of all users currently in server");
						out.println("/quit: Disconnect from server");
						out.flush();
					} else if(incomingMsg.equals("/duration")) {
						
						/**
						* Calculate the amount of time the server has been running for, and display it to the user.
						*/
						
						long serverElapsed = (System.currentTimeMillis() - openTime) / 1000;
						out.println("Server has been running for " + serverElapsed + " seconds");
						out.flush();
					} else if(incomingMsg.equals("/since")) {
						
						/**
						* Calculate the amount of time the client has been connected for, and display it to the user.
						*/
						
						long clientElapsed = (System.currentTimeMillis() - clientJoinTime) / 1000;
						out.println("Client has been in server for " + clientElapsed + " seconds");
						out.flush();
					} else if(incomingMsg.equals("/ip")) {
						
						/**
						* Display the IP address of the server using InetAddress' getLocalHost() method.
						*/
						
						out.println("IP address of the server is " + InetAddress.getLocalHost());
						out.flush();
					} else if(incomingMsg.equals("/usercount")) {
						
						/**
						* Display the number of users currently connected by counting the number of elements in connectedUsers.
						*/
						
						out.println("There are currently " + connectedUsers.size() + " users connected");
						out.flush();
					} else if(incomingMsg.equals("/userlist")){
						
						/**
						* Display a list of usernames currently connected to the server using the getUserList() method.
						*/
						
						out.println("List of users: " + getUserList());
						out.flush();
					} else if(incomingMsg.equals("/quit")){
						
						/**
						* If the user requests to quit the chat room, call the shutDownClient() method to deal with the client disconnecting.
						*/
						
						shutDownClient();
					} else if (!(incomingMsg.equals(""))){
						
						/**
						* If the message is not empty, broadcast the username and the method to all connected users.
						*/
						
						sendToAll(username + ": " + incomingMsg);
					}
				}
			}
			catch(IOException e){
				
				/**
				* Catch IOException error and print error to user.
				*/
				
				System.err.println("Client socket closed: " + e.getMessage());
				
				/**
				* If user closes application without using '/quit' command, close socket properly and broadcast message to all users.
				*/
				
				if(listOfUsernames.contains(username)){
					sendToAll(username + " has disconnected");
					listOfUsernames.remove(username);
					connectedUsers.remove(position);
				}
			}
		}
		
		/**
		* Method used to broadcast messages to all connected clients.
		* @param message the message to be broadcast to all clients.
		*/
		
		private void sendToAll(String message){
			try{
				
				/**
				* Run through all connected users, and set up a PrintWriter object for each user.
				* Then broadcast message to every connected user using PrintWriter.
				*/
				
				for(HandleSession session:connectedUsers){
					PrintWriter tmpOut = new PrintWriter(new OutputStreamWriter(session.getSocket().getOutputStream()));
					tmpOut.println(message);
					tmpOut.flush();
				}
				
				/**
				* Also print message to the server.
				*/
				
				System.out.println(message);
			} catch(IOException e){
				
				/**
				* Catch IOException error and print error to user.
				*/
				
				System.err.println("Error while broadcasting message to clients: " + e.getMessage());
			}
		}
		
		/**
		* Method used to return the socket of this session.
		* @return The socket of this session.
		*/
		
		private Socket getSocket(){
			return s;
		}
		
		/**
		* Method used to record the position in the connectedUsers HashSet of a session.
		* @param session the session to save.
		*/
		
		public void setPosition(HandleSession session){
			position = session;
		}
		
		/**
		* Method used to make a list of all the currently connected users.
		* @return The list of users recorded as a String.
		*/
		
		private String getUserList(){
			String userList = "";
			
			/**
			* Runs through the list of users, adding each user to a String.
			*/
			
			for(String user:listOfUsernames){
				userList = userList + "    " + user;
			}
			
			return userList;
		}
		
		/**
		* Method used to disconnect the client from the server.
		*/
		
		private void shutDownClient(){
			try{
				
				/**
				* Broadcast a message to all clients informing them of the user disconnecting.
				*/
				
				sendToAll(username + " has disconnected");
				
				/**
				* Remove this username from the list of usernames, and remove the session from connectedUsers.
				*/
				
				listOfUsernames.remove(username);
				connectedUsers.remove(position);
				
				/**
				* Close this socket.
				*/
				
				s.close();
			} catch(Exception e){
				
				/**
				* Catch IOException error and print error to user.
				*/
				
				System.err.println("Error while disconnecting client: " + e.getMessage());
			}
		}
	}
}