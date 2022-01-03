package appl;

import core.Server;
import java.util.Scanner;

public class Broker {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new Broker();
	}
	
	public Broker(){		
		boolean isPrimary = true;
		int primaryPort = 8080;
		int backupPort = 8081;
		String address = "localhost";
		
		int currentPort;
		String currentAddress;
		int otherPort;
		String otherAddress;

		Scanner reader = new Scanner(System.in);  // Reading from System.in
		System.out.print("Enter the Broker port number: ");
		int port = reader.nextInt(); // Scans the next token of the input as an int.
		
		//Server s = new Server(port);
		//ThreadWrapper brokerThread = new ThreadWrapper(s);
		//brokerThread.start();
		
		// Confere se o broker criado é o primário
		System.out.print("Broker primary? (Y|N) ");
		String resp = reader.next(); 
		
		if (resp.equals("Y") || resp.equals("y")){
			isPrimary = true;
		}
		else if(resp.equals("N") || resp.equals("n")) {
			isPrimary = false;
		}
		
		if(isPrimary) {
			// Se for primário, cria um novo server primário e o backup dele
			currentPort = port;
			currentAddress = address;
			otherPort = backupPort;
			otherAddress = address;
		}
		else {
			// Se for secundário, cria um primário e define o secundário como backup dele
			currentPort = port;
			currentAddress = address;
			otherPort = primaryPort;
			otherAddress = address;
		}
		
		Server s = new Server(currentPort, isPrimary, currentAddress, otherAddress, otherPort);
		ThreadWrapper brokerThread = new ThreadWrapper(s);
		brokerThread.start();
		
		System.out.print("Shutdown the broker (Y|N)?: ");
		resp = reader.next(); 
		if (resp.equals("Y") || resp.equals("y")){
			System.out.println("Broker stopped...");
			s.stop();
			brokerThread.interrupt();
		}
		
		//once finished
		reader.close();
	}
	
	class ThreadWrapper extends Thread{
		Server s;
		public ThreadWrapper(Server s){
			this.s = s;
		}
		public void run(){
			s.begin();
		}
	}

}
