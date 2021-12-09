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
                case "-m" -> numberClient = Integer.parseInt(itArgs.next());
                default -> {
                    System.out.println("Comando " + arg +  " invalido!");
                    return;
                }
            }
        }
        
		new OneAppl(portClient, nameClient, numberClient);
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
		
		System.out.println("TWEET PARA O USUÁRIO:\n");
		System.out.println("@" + statuses.get(randomTweet).getUser().getName() + ":" +
                statuses.get(randomTweet).getText());
	}
	
	private void treatLog(List<Message> logUser){
		//aqui existe toda a lógica do protocolo do TP2
		//se permanece neste método até que o acesso a VAR X ou VAR Y ou VAR Z ocorra
		Iterator<Message> it = logUser.iterator();
		
		List<Message> logAcquire = new ArrayList<Message>(); 
		List<Message> logRelease = new ArrayList<Message>(); 
		
		while(it.hasNext()){
			Message aux = it.next();
			String content = aux.getContent();
			String[] parts2 = content.split("_");
			int contAcquires = 0;
			
			System.out.println("\nUsuario: " + parts2[1] + " - Posicao: " + parts2[0]);
			
			if(parts2.length > 1) {
				try {
					if (parts2[2].equals("acquire")) {
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
	
	public OneAppl(int portClient, String nameClient, int numberClient){
		String address = "localhost";
		int brokerPort = 8085;
		boolean startToken = numberClient == 1;
		String[] resources = {"var X", "var Y", "var Z"};
		Random seed = new Random();
		int seconds = (int) (Math.random()*(10000 - 1000)) + 1000;

		PubSubClient client = new PubSubClient(nameClient, address, portClient);
		
		String resource = resources[seed.nextInt(resources.length)];
		Thread access = new ThreadWrapper(client, numberClient+"_"+nameClient+"_acquire_"+resource, address, brokerPort);
		
		// Pausa para permitir a criação de outros clientes
		try {
			System.out.println("Aguardando " + seconds/1000 + " segundos...\n");
			Thread.sleep(seconds);
	    } catch (InterruptedException e) {
	    	e.printStackTrace();
	    }
		
		seconds = (int) (Math.random()*(10000 - 1000)) + 1000;
		
		// Checa se o Broker tá ocupado se sim, espera
		if(access.isAlive()) {
			try {
				seconds = (int) (Math.random()*(10000 - 1000)) + 1000;
				System.out.println("Broker ocupado. Aguardando " + seconds/1000 + " segundos...\n");
				Thread.sleep(seconds);
				
				access.start();
				
				try{
					access.join();
				}catch (Exception e){
					
				}
				
		    } catch (InterruptedException e) {
		    	e.printStackTrace();
		    }
		}
		
		client.unsubscribe(address, portClient);
		client.stopPubSubClient();	
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
			List<Message> logClient = c.getLogMessages();
			treatLog(logClient);
		}
	}

}
