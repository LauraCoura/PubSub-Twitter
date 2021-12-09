package appl;

import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.List;
import core.Message;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class OneAppl {

	public static void main(String[] args) {
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
		
		System.out.println("TWEET PARA O USU�RIO:\n");
		System.out.println("@" + statuses.get(randomTweet).getUser().getName() + ":" +
                statuses.get(randomTweet).getText());
	}
	
	public static String playMusic(){
        String[] songNames = {
            "Xuxa - Abecedário da Xuxa",
            "Xuxa - Doce Mel",
            "Xuxa - Lua de Cristal",
            "Xuxa - Ilariê",
            "Up - Cardi B",
            "Leave The Door OpenSilk Sonic - Bruno Mars & Anderson",
            "Drivers License - Olivia Rodrigo",
            "What's Next - Drake",
            "Save Your Tears - The Weeknd",
            "Blinding Lights - The Weeknd",
            "Levitating - Dua Lipa Ft DaBaby",
            "34+35- Ariana Grande",
            "Wants And Needs - Drake Featuring Lil Baby",
            "Go Crazy- Chris Brown & Young Thug",
            "Therefore I Am – Billie Eilish"
        };
        return songNames [new Random().nextInt(songNames.length)];
    }
	
	public OneAppl(boolean flag){
		//String brokersIp = "34.70.208.126";
		//String[] clientIp = {"34.67.100.60", "104.154.105.80", "35.222.64.135"};
		String brokersIp = "localhost";
		String[] clientIp = {"34.67.100.60", "104.154.105.80", "35.222.64.135"};
		String[] clientNames = {"Flavia", "Douglas", "Dani"};
		
		int client = 0;
		
		PubSubClient listener = new PubSubClient(brokersIp, 8081);

		listener.subscribe(brokersIp, 8080);
		Integer n = ThreadLocalRandom.current().nextInt(3, 50);
		
		for (int i = 0; i<n; i++) {
			
		    listener.publish("Toca-ai " + clientNames[client] + " ", brokersIp, 8080);
			
			Integer position = 0;
			Integer releasesCount = 0;
			Boolean logMeOut = false;
			
			while(logMeOut == false) {
				Set<Message> log = listener.getLogMessages();
				Set<Message> log2 = new HashSet<>(log);
				Iterator<Message> it = log2.iterator();
				System.out.print("Log " + clientNames[client] + " itens: ");
				Integer index = 0;
				while(it.hasNext()){
					
					Message aux = it.next();
					System.out.println(aux.getContent() + aux.getLogId() + " | ");
					String[] words = aux.getContent().split(" ");
					String logType = "";
					String logName = "";
					
					if (words.length > 1) {
						logType = words[0];
						logName = words[1];
					}
	
					if (position == 0) {
						if (logName.equals(clientNames[client])) {
							position = index + 1;
							releasesCount = position - 1;
						}
					}
										
					if (position > 0 && releasesCount == 0) {                          
						listener.publish(clientNames[client] + " Tocando: " + playMusic() + " " , brokersIp, 8080);
						
						System.out.print("Tweet para o cliente: ");
						try {
							printTweet();
						} catch (TwitterException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							System.out.println("\n -> Erro na API do Twitter!");
						}
						
					    sleep(3000, "");
					    listener.publish("Tocou " + clientNames[client] + " ", brokersIp, 8080);
					    position = 0;
					    logMeOut = true;
					} 
					
					else if (position > 0 && logType.equals("Tocou")) {
						releasesCount -= 1;
					}
					
					if (logType.equals("Tocou") || logType.equals("Toca-ai")) {
						index += 1;
					}
				}
				sleep(3000, "Aguardando...");
			}
			sleep(3000, "Aguardando...");
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
