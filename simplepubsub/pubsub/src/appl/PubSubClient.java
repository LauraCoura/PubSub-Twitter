package appl;

import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import core.Message;
import core.MessageImpl;
import core.Server;
import core.client.Client;

public class PubSubClient {

    private Server observer;
    private ThreadWrapper clientThread;

    private String clientAddress;
    private int clientPort;
    
    private String primaryAddress;
    private int primaryPort;
    private String backupAddress;
    private int backupPort;

    public PubSubClient() {
        //this constructor must be called only when the method
        //startConsole is used
        //otherwise the other constructor must be called
    	this.primaryAddress = "";
		this.primaryPort = 0;
		this.backupAddress = "";
		this.backupPort = 0;
    }

    public PubSubClient(String clientAddress, int clientPort) {
        this.clientAddress = clientAddress;
        this.clientPort = clientPort;
        
        this.primaryAddress = "";
		this.primaryPort = 0;
		this.backupAddress = "";
		this.backupPort = 0;
		
        observer = new Server(clientPort);
        clientThread = new ThreadWrapper(observer);
        clientThread.start();
    }

    public void subscribe(String brokerAddress, int brokerPort) {

        Message msgBroker = new MessageImpl();
        msgBroker.setBrokerId(brokerPort);
        msgBroker.setType("sub");
        msgBroker.setContent(clientAddress + ":" + clientPort);
        
        try {
        	Client subscriber = new Client(brokerAddress, brokerPort);
            Message response = subscriber.sendReceive(msgBroker);
            
            //System.out.println(clientPort + " Trying to subscribe to " + brokerAddress + ":" + brokerPort);
            System.out.println("Response content: " + response.getContent());
            //System.out.println("Response type: " + response.getType());
            
            if (response.getType().equals("backup")) {
            	// backup will send back info on the primary broker
                brokerAddress = response.getContent().split(":")[0];
                brokerPort = Integer.parseInt(response.getContent().split(":")[1]);
                subscriber = new Client(brokerAddress, brokerPort);
                subscriber.sendReceive(msgBroker);
                
                // guardar address e porta globalmente na classe para nao ter que perguntar sempre se é backup
                this.primaryAddress = brokerAddress;
                this.primaryPort = brokerPort;
            }
        } catch (Exception e) {
        	System.out.println(e);
        	System.out.println("Trying to connect to backup...");  
        	
        	// MEXER AQUI
        }
    }

    public void unsubscribe(String brokerAddress, int brokerPort) {

        Message msgBroker = new MessageImpl();
        msgBroker.setBrokerId(brokerPort);
        msgBroker.setType("unsub");
        msgBroker.setContent(clientAddress + ":" + clientPort);
        
        try {
        	Client subscriber = new Client(brokerAddress, brokerPort);
            Message response = subscriber.sendReceive(msgBroker);
            
            if (response.getType().equals("backup")) {
                brokerAddress = response.getContent().split(":")[0];
                brokerPort = Integer.parseInt(response.getContent().split(":")[1]);
                subscriber = new Client(brokerAddress, brokerPort);
                subscriber.sendReceive(msgBroker);
            }
        } catch (Exception e) {
        	System.out.print(e);
        }
    }

    public void publish(String message, String brokerAddress, int brokerPort) {
        Message msgPub = new MessageImpl();
        msgPub.setBrokerId(brokerPort);
        msgPub.setType("pub");
        msgPub.setContent(message);

        try {
        	Client publisher = new Client(brokerAddress, brokerPort);
        	Message response = publisher.sendReceive(msgPub);
        	
        	System.out.println(clientPort + " Trying to publish '" + message + "' to " + brokerAddress + ":" + brokerPort);
        	System.out.println("Response content: " + response.getContent());
            //System.out.println("Response type: " + response.getType());

	        if (response.getType().equals("backup")) {
	            brokerAddress = response.getContent().split(":")[0];
	            brokerPort = Integer.parseInt(response.getContent().split(":")[1]);
	            publisher = new Client(brokerAddress, brokerPort);
	            publisher.sendReceive(msgPub);
	        }
        } catch (Exception e) {
        	System.out.println(e);
        	
        	// rodar portas 8080 e 8081
        	// cancelar 8080
        	// vai dar erro pq joubert nao conectar a porta 8080
        	
        	System.out.println("Trying to publish to backup");
        	
        	// MEXER AQUI
        	
        	// Achar o backup aqui e mandar função de novo
        }
    }

    public List<Message> getLogMessages() {
        return observer.getLogMessages();
    }

    public void stopPubSubClient() {
        System.out.println("Client stopped...");
        observer.stop();
        clientThread.interrupt();
    }

    public void startConsole() {
        Scanner reader = new Scanner(System.in);  // Reading from System.in

        System.out.print("Enter the client port (ex.8080): ");
        int clientPort = reader.nextInt();
        System.out.println("Now you need to inform the broker credentials...");
        System.out.print("Enter the broker address (ex. localhost): ");
        String brokerAddress = reader.next();
        System.out.print("Enter the broker port (ex.8080): ");
        int brokerPort = reader.nextInt();

        observer = new Server(clientPort);
        clientThread = new ThreadWrapper(observer);
        clientThread.start();

        subscribe(brokerAddress, brokerPort);

        System.out.println("Do you want to subscribe for more brokers? (Y|N)");
        String resp = reader.next();

        if (resp.equals("Y") || resp.equals("y")) {
            String message = "";

            while (!message.equals("exit")) {
                System.out.println("You must inform the broker credentials...");
                System.out.print("Enter the broker address (ex. localhost): ");
                brokerAddress = reader.next();
                System.out.print("Enter the broker port (ex.8080): ");
                brokerPort = reader.nextInt();
                subscribe(brokerAddress, brokerPort);
                System.out.println(" Write exit to finish...");
                message = reader.next();
            }
        }

        
        System.out.println("Do you want to publish messages? (Y|N)");
        resp = reader.next();
        if (resp.equals("Y") || resp.equals("y")) {
            String message = "";

            while (!message.equals("exit")) {
                System.out.println("Enter a message (exit to finish submissions): ");
                message = reader.next();

                System.out.println("You must inform the broker credentials...");
                System.out.print("Enter the broker address (ex. localhost): ");
                brokerAddress = reader.next();
                System.out.print("Enter the broker port (ex.8080): ");
                brokerPort = reader.nextInt();

                publish(message, brokerAddress, brokerPort);

                List<Message> log = observer.getLogMessages();

                Iterator<Message> it = log.iterator();
                System.out.print("Log itens: ");
                while (it.hasNext()) {
                    Message aux = it.next();
                    System.out.print(aux.getContent() + aux.getLogId() + " | ");
                }
                System.out.println();

            }
        }

        System.out.print("Shutdown the client (Y|N)?: ");
        resp = reader.next();
        if (resp.equals("Y") || resp.equals("y")) {
            System.out.println("Client stopped...");
            observer.stop();
            clientThread.interrupt();

        }

        //once finished
        reader.close();
    }

    class ThreadWrapper extends Thread {
        Server s;

        public ThreadWrapper(Server s) {
            this.s = s;
        }

        public void run() {
            s.begin();
        }
    }

}
