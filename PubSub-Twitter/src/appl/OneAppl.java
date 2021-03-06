package appl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import appl.SingleUser.ThreadWrapper;
import core.Message;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class OneAppl {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int portClient = 8080, numberClient = 1;
		String nameClient = "";
				
		Iterator<String> itArgs = Arrays.stream(args).iterator();
        while (itArgs.hasNext()) {
            String arg = itArgs.next();
            switch (arg) {
            	case "-n" -> nameClient = itArgs.next();
                case "-p" -> portClient = Integer.parseInt(itArgs.next());
                default -> {
                    System.out.println("Comando " + arg +  " invalido!");
                    return;
                }
            }
        }
        
		new OneAppl(portClient, nameClient);
	}
	
	private void printTweet() throws TwitterException {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
		  .setOAuthConsumerKey("0GEAwIeR8xaLm46vvFUf0XQoV")
		  .setOAuthConsumerSecret("kUcBGQbnnkPOkQEY19DHCziXeRGeM33Isvxa3GwwCRDjZrsgsm")
		  .setOAuthAccessToken("1466167969793327105-tZMg2ScAFNCHi3NgGJYkHL3gEpWajk")
		  .setOAuthAccessTokenSecret("WtBPQ8DwLCayBYin9X3hp5j2ruY4UAJEa97OPIjTvzrxa");
		
		TwitterFactory tf = new TwitterFactory(cb.build());
		Twitter twitter = tf.getInstance();
		
		int cont = 0;
		
		List<Status> statuses = twitter.getHomeTimeline();
		
		for (Status status : statuses){
			cont++;
		}
		
		Random randomObj = new Random();
		int randomTweet = randomObj.ints(0, cont).findFirst().getAsInt();
		
		System.out.println("TWEET PARA O USU?RIO:\n");
		System.out.println("@" + statuses.get(randomTweet).getUser().getName() + ":" +
                statuses.get(randomTweet).getText());
	}
	
	private void treatLog(List<Message> logUser){
		//aqui existe toda a l?gica do protocolo do TP2
		//se permanece neste m?todo at? que o acesso a VAR X ou VAR Y ou VAR Z ocorra
		Iterator<Message> it = logUser.iterator();
		
		List<Message> logAcquire = new ArrayList<Message>(); 
		List<Message> logRelease = new ArrayList<Message>(); 
		
		while(it.hasNext()){
			Message aux = it.next();
			String content = aux.getContent();
			String[] parts2 = content.split("_");
			int contAcquires = 0;
			
			System.out.println("\nUsuario: " + parts2[0] + " - Posicao: " + aux.getLogId());
			
			if(parts2.length > 1) {
				try {
					if (parts2[1].equals("acquire")) {
						logAcquire.add(aux);
						System.out.println("\nAcessando " + parts2[3]);
						System.out.println("e requisitando tweet: ");
					}
					else if(parts2[1].equals("release")){
						logRelease.add(aux);
					}
				} catch(Exception e){
				}
				
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				System.out.println("\nACQUIRES:\n");
				Iterator<Message> itAcq = logAcquire.iterator();
				while (itAcq.hasNext()){
					Message auxAcquire = itAcq.next();
					String contentAcquire = auxAcquire.getContent();
					System.out.print(contentAcquire + " | ");
					contAcquires++;
				}

				System.out.println("\n\nRELEASES:\n");
				Iterator<Message> itRel = logRelease.iterator();
				while (itRel.hasNext()){
					Message auxRelease = itRel.next();
					String contentRelease = auxRelease.getContent();
					System.out.print(contentRelease + " | ");
				}
			}
		
			try {
				printTweet();
			} catch (TwitterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("\n ---> Erro na API do Twitter!");
			}
			
		    	
			System.out.println("\n\nQuantidade total de acquires: " + contAcquires);
			System.out.println("\n------------------------------------------------------------------------");
			
		}
		System.out.println();
	}
	
	public OneAppl(){
		PubSubClient client = new PubSubClient();
		client.startConsole();
	}
	
	public OneAppl(int portClient, String nameClient){
		String address = "localhost";
		int brokerPort = 8085;
		String[] resources = {"var X", "var Y", "var Z"};
		Random seed = new Random();
		int seconds = (int) (Math.random()*(10000 - 1000)) + 1000;

		PubSubClient client = new PubSubClient(nameClient, address, portClient);
		
		String resource = resources[seed.nextInt(resources.length)];
		//Thread access = new ThreadWrapper(client, nameClient+"_acquire_"+resource, address, brokerPort);
		Thread access = new requestAcquire(client, nameClient, nameClient+"_acquire_"+resource, address, brokerPort);
		
		// Pausa para permitir a cria??o de outros clientes
		try {
			System.out.println("Aguardando " + seconds/1000 + " segundos...\n");
			Thread.currentThread().sleep(seconds);
	    } catch (InterruptedException e) {
	    	e.printStackTrace();
	    }
		
		//seconds = (int) (Math.random()*(10000 - 1000)) + 1000;
		
		System.out.println("ANTES DO START()");
		access.start();
		System.out.println("DEPOIS DO START()");
		
		try{
			access.join();
		}catch (Exception e){
			
		}
		
		// Checa se o Broker t? ocupado se sim, espera
		/*
		if(access.isAlive()) {
			try {
				seconds = (int) (Math.random()*(10000 - 1000)) + 1000;
				System.out.println("Broker ocupado. Aguardando " + seconds/1000 + " segundos...\n");
				Thread.sleep(seconds);
		    } catch (InterruptedException e) {
		    	e.printStackTrace();
		    }
		}*/
		
		client.unsubscribe(address, brokerPort);
		client.stopPubSubClient();	
	}
	
	class requestAcquire extends Thread {
		PubSubClient c;
		String clientName;
		String msg;
		String host;
		int port;
		
		public requestAcquire(PubSubClient client, String clientName, String msg, String hostBroker, int portBroker) {
			this.c = client;
			this.msg = msg;
			this.clientName = clientName;
			this.host = hostBroker;
			this.port = portBroker;
		}
		
		public void run() {	
			Thread access = new ThreadWrapper(c, msg, host, port);
			access.start();
			
			try {
				access.join();
			} catch (Exception ignored) {}
			
			List<Message> logs = c.getLogMessages();
			List<String> logAcquire = new ArrayList<String>(); 
			List<String> logRelease = new ArrayList<String>(); 
			
			System.out.println("ANTES DOS LOGS\n");

			Iterator<Message> it = logs.iterator();	
			
			if(!it.hasNext()) {
				System.out.println("LOG VAZIO\n");
			}
			
			while(it.hasNext()){
				Message aux = it.next();
				String content = aux.getContent();
				String[] parts2 = content.split("_");
				
				if(parts2.length > 1) {
					try {
						if (parts2[2].equals("acquire")) {
							logAcquire.add(content);
							System.out.println("ACQUIRE AQUI\n");
						}
						else if(parts2[1].equals("release")){
							logRelease.add(content);
						}
					} catch(Exception e){
					}
				}
				
				System.out.print("\nORDEM DE CHEGADA MANTIDA PELO BROKER: " + logAcquire + " \n");
				
				while (!logAcquire.isEmpty()){
					String firstClient = logAcquire.get(0);
					boolean hasRelease = false;
					
					while(!hasRelease){
						int randomInterval = (int) (Math.random()*(10000 - 1000)) + 1000;
						if(firstClient.contains(clientName)){
							try {
								access = new ThreadWrapper(c, "use"+parts2[3], host, 8080);
								access.start();
								try {
									access.join();
								} catch (Exception ignored) {}
								
								System.out.println("-------------------------------------");
								System.out.println(firstClient.split("_")[1] + " pegou o recurso "+parts2[3]);

								System.out.println("Aguardando " + randomInterval/1000 + " segundos...\n");
								Thread.currentThread().sleep(randomInterval);
								
								access = new ThreadWrapper(c, clientName.concat(":release:"+parts2[3]), host, 8080);
								access.start();
								hasRelease = true;
								try {
									access.join();
								} catch (Exception ignored) {}
							}catch (InterruptedException e) {
								e.printStackTrace();
							}
							
							try{
								Thread.currentThread().sleep(randomInterval);
								hasRelease = true;
							}catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						
						if (!logAcquire.isEmpty()){
							logAcquire.remove(0);
						}
					}
				}
		}
			
			System.out.println("FINAL DO RUN\n");
	}
	
	class ThreadWrapper extends Thread{
		PubSubClient c;
		String msg;
		String host;
		int port;
		
		public ThreadWrapper(PubSubClient c, String msg, String host, int port){
			this.c = c;
			this.msg = msg;
			this.host = host;
			this.port = port;
		}
		public void run(){
			c.publish(msg, host, port);
			//List<Message> logClient = c.getLogMessages();
			//treatLog(logClient);
			
		}
	}
}
}
