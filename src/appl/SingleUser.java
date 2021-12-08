package appl;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.ArrayList;

import core.Message;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class SingleUser {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new SingleUser();
	}
	
	public SingleUser(){
		Scanner reader = new Scanner(System.in);  // Reading from System.in
		System.out.print("Enter the Broker port number: ");
		int brokerPort = reader.nextInt();
		
		System.out.print("Enter the Broker address: ");
		String brokerAdd = reader.next();
		
		System.out.print("Enter the User name: ");
		String userName = reader.next();
		
		System.out.print("Enter the User port number: ");
		int userPort = reader.nextInt();
		
		System.out.print("Enter the User address: ");
		String userAdd = reader.next();
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		PubSubClient user = new PubSubClient(userAdd, userPort);
		
		user.subscribe(brokerAdd, brokerPort);
		
		startTP2(user, userName, brokerPort, brokerAdd);
	}
	
	private void startTP2 (PubSubClient user, String userName, int brokerPort, String brokerAdd){
		String[] resources = {"var X", "var Y", "var Z"};
		
		Random seed = new Random();
		
		for(int i =0; i<5; i++){
			//fazendo um pub no broker
			String oneResource = resources[seed.nextInt(resources.length)];
			Thread sendOneMsg = new ThreadWrapper(user, userName+"_acquire_"+oneResource, brokerAdd, brokerPort);
			
			//PubSubClient user2 = new PubSubClient("localhost", 8083);
			//Thread sendTwoMsg = new ThreadWrapper(user2, "maria"+"_release_"+oneResource, brokerAdd, brokerPort);
			
			sendOneMsg.start();
			//sendTwoMsg.start();
			
			try{
				sendOneMsg.join();	
				//sendTwoMsg.join();
			}catch (Exception e){
				e.printStackTrace();
			}
			
			//fazendo a obtencao dos notifies do broker
			List<Message> logUser = user.getLogMessages();
			//List<Message> logUser2 = user2.getLogMessages();
			
			treatLog(logUser);
			//treatLog(logUser2);
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
						
		user.unsubscribe(brokerAdd, brokerPort);
		user.stopPubSubClient();
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
	    //System.out.println("Showing home timeline.");
	    /*for (Status status : statuses) {
	        System.out.println(status.getUser().getName() + ":" +
	                           status.getText());
	    }*/
		
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
		
		System.out.print("Log User itens: ");
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
					}
					else if(parts2[1].equals("release")){
						logRelease.add(aux);
					}
				} catch(Exception e){
				}
				
				System.out.println("\nACQUIRE:\n");
				Iterator<Message> itAcq = logAcquire.iterator();
				while (itAcq.hasNext()){
					Message auxAcquire = itAcq.next();
					String contentAcquire = auxAcquire.getContent();
					System.out.print(contentAcquire + " | ");
					contAcquires++;
				}

				System.out.println("\n\nRELEASE:\n");
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
				System.out.println("\n ------------> Erro na API do Twitter!");
			}
			
		    	
			System.out.println("\n\nQuantidade total de acquires: " + contAcquires);
			System.out.println("\n------------------------------------------------------------------------");
			
		}
		System.out.println();
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
		}
	}

}
