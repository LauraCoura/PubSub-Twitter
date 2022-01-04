package appl;

import java.util.Iterator;
import java.util.List;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import core.Message;

public class OneAppl {

    public OneAppl() {
        PubSubClient client = new PubSubClient();
        client.startConsole();
    }

    public OneAppl(boolean flag) throws TwitterException{
        PubSubClient joubert = new PubSubClient("localhost", 8082);
        PubSubClient debora = new PubSubClient("localhost", 8083);
        PubSubClient jonata = new PubSubClient("localhost", 8084);

        joubert.subscribe("localhost", 8080);
        debora.subscribe("localhost", 8080);
        jonata.subscribe("localhost", 8081);
        
        int n = 3; // Quantidade de variáveis
        int stateTweet[] = new int[n]; // Vetor com o estado das variáveis: locked (1) ou unlocked (0)
		
		for(int i = 0; i < n; i++) {
			stateTweet[i] = 0; // Inicialmente, todas as variáveis estão desbloqueadas (unlocked)
		}
		
		String[] tweets = getTweets(n); // Basicamente, são as variáveis da aplicação
		
		// Mensagem é definida pelo nome do cliente, tipo de transação e variável. Exemplo: Flavia_read_twt0
		//listener.publish(clientNames[client] + "_read_twt" + Integer.toString(client), brokersIp, 8080);
		
		/*Set<Message> log = listener.getLogMessages();
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
				
		}*/

		Thread accessOne = new ThreadWrapper(joubert, "access_Joubert_var X", "localhost", 8080);
        Thread accessTwo = new ThreadWrapper(debora, "access_Debora_var X", "localhost", 8080);
        Thread accessThree = new ThreadWrapper(jonata, "access_Jonata_var X", "localhost", 8081);
        
        accessOne.start();
        accessTwo.start();
        accessThree.start();

        try {
            accessTwo.join();
            accessOne.join();
            accessThree.join();
        } catch (Exception e) {

        }

        List<Message> logJoubert = joubert.getLogMessages();
        List<Message> logDebora = debora.getLogMessages();
        List<Message> logJonata = jonata.getLogMessages();

        Iterator<Message> it = logJoubert.iterator();
        System.out.print("Log Joubert itens: ");
        while (it.hasNext()) {
            Message aux = it.next();
            System.out.print(aux.getContent() + aux.getLogId() + " | ");
        }
        System.out.println();

        it = logJonata.iterator();
        System.out.print("Log Jonata itens: ");
        while (it.hasNext()) {
            Message aux = it.next();
            System.out.print(aux.getContent() + aux.getLogId() + " | ");
        }
        System.out.println();

        it = logDebora.iterator();
        System.out.print("Log Debora itens: ");
        while (it.hasNext()) {
            Message aux = it.next();
            System.out.print(aux.getContent() + aux.getLogId() + " | ");
        }
        System.out.println();

        joubert.unsubscribe("localhost", 8080);
        debora.unsubscribe("localhost", 8080);
        jonata.unsubscribe("localhost", 8080);

        joubert.stopPubSubClient();
        debora.stopPubSubClient();
        jonata.stopPubSubClient();
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

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        new OneAppl(true);
    }

    class ThreadWrapper extends Thread {
        PubSubClient c;
        String msg;
        String host;
        int port;

        public ThreadWrapper(PubSubClient c, String msg, String host, int port) {
            this.c = c;
            this.msg = msg;
            this.host = host;
            this.port = port;
        }

        public void run() {
            c.publish(msg, host, port);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}
