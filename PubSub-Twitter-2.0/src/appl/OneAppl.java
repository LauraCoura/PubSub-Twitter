package appl;

import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.List;
import core.Message;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class OneAppl {

	public static void main(String[] args) throws TwitterException {
		// TODO Auto-generated method stub
		new OneAppl(true);
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
		
		System.out.println("--> TWEET: ");
		System.out.println("@" + statuses.get(randomTweet).getUser().getName() + ":" +
                statuses.get(randomTweet).getText()+"\n\n");
	}
	
	private String[] getTweets(int n) throws TwitterException{
		String[] twt = new String[n];
		
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
		
		for(int i = 0; i < cont; i++) {
			twt[i] = "@" + statuses.get(i).getUser().getName() + ":" +
	                statuses.get(i).getText()+"\n\n";
		}
		
		return twt;
	}
	
	public static String playMusic(){
        String[] songNames = {
            "Xuxa - Doce Mel",
            "Xuxa - Lua de Cristal",
            "Up - Cardi B",
            "34+35- Ariana Grande",
            "Wants And Needs - Drake Featuring Lil Baby",
            "Therefore I Am - Billie Eilish"
        };
        return songNames [new Random().nextInt(songNames.length)];
    }
	
	public OneAppl(boolean flag) throws TwitterException{
		//String brokersIp = "34.70.208.126";
		//String[] clientIp = {"34.67.100.60", "104.154.105.80", "35.222.64.135"};
		
		String brokersIp = "localhost";
		String[] clientNames = {"Flavia", "Douglas", "Dani"};
		int n = 3; // Quantidade de vari�veis
		int stateTweet[] = new int[n]; // Vetor com o estado das vari�veis: locked (1) ou unlocked (0)
		
		for(int i = 0; i < n; i++) {
			stateTweet[i] = 0; // Inicialmente, todas as vari�veis est�o desbloqueadas (unlocked)
		}
		
		String[] tweets = getTweets(n); // Basicamente, s�o as vari�veis da aplica��o
		
		int client = 0;
		PubSubClient listener = new PubSubClient(brokersIp, 8083);
		listener.subscribe(brokersIp, 8080);
		//Integer n = ThreadLocalRandom.current().nextInt(3, 10);
		
		// Mensagem � definida pelo nome do cliente, tipo de transa��o e vari�vel. Exemplo: Flavia_read_twt0
		listener.publish(clientNames[client] + "_read_twt" + Integer.toString(client), brokersIp, 8080);
			
		Set<Message> log = listener.getLogMessages();
		Iterator<Message> it = log.iterator();
		while(it.hasNext()){
			Message aux = it.next();
			String content = aux.getContent();
			String[] parts = content.split("_");
				
			String msgType = "";
			String msgName = "";
				
			if(parts.length > 1) {
				msgType = parts[1];
				msgName = parts[0];
			}
				
			if(msgType == "read"){
				// Checa se est� bloqueado, se sim espera desbloquear para ler
				while(stateTweet[client] == 1) {
					System.out.println("Aguardando desbloquear variavel para leitura...\n");
				}
						
				// Se n�o, bloqueia (lock) e le
				if(stateTweet[client] == 0) { // Desbloqueado
					// Le tweet
					System.out.println(tweets[client]);
					
					// Bloqueia (lock)
					stateTweet[client] = 1;
				}
			}
			else if(msgType == "write") {
				// Checa se est� bloqueado, se sim espera desbloquear para escrever
				while(stateTweet[client] == 1) {
					System.out.println("Aguardando desbloquear variavel para escrita...\n");
				}	
					
				// Se n�o, bloqueia e escreve
				if(stateTweet[client] == 0) { // Desbloqueado
					// Escreve tweet
					System.out.println("Digite o tweet: ");
					Scanner reader = new Scanner(System.in); 
					String tweet = reader.nextLine();
					
					// Atualiza o tweet
					tweets[client] = "@" + clientNames[client] + ":" + tweet + "\n\n";
					
					reader.close();
					// Bloqueia (lock)
					stateTweet[client] = 1;
				}
			}
				
			// Feito a transa��o, desbloqueia (unlock)
			stateTweet[client] = 0;	
		}
		
		listener.stopPubSubClient();
	}
	
	public void sleep(int time, String msg) {
		try {
			System.out.print(msg);
		    Thread.sleep(time);
		} 
		catch(InterruptedException ex) {
		    Thread.currentThread().interrupt();
		}
	}
}
