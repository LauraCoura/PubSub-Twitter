package appl;

import java.util.Iterator;
import java.util.List;
import core.Message;

public class OneAppl {

    public OneAppl() {
        PubSubClient client = new PubSubClient();
        client.startConsole();
    }
    
    private String[] getTweets(int n){
		String[] twt = new String[n];
		String[] clients = {"Joubert", "Joao", "Debora", "Jonata", "Maria"};
		
		for(int i = 0; i < n; i++) {
			twt[i] = "@" + clients[i] + ":" + " TESTE\n";
		}
		
		return twt;
	}

    public OneAppl(boolean flag) {
    	int n = 5;
    	String[] tweets = getTweets(n);
    	
        PubSubClient joubert = new PubSubClient("localhost", 8082);
        PubSubClient debora = new PubSubClient("localhost", 8083);
        PubSubClient jonata = new PubSubClient("localhost", 8084);
        PubSubClient joao = new PubSubClient("localhost", 8085);
        PubSubClient maria = new PubSubClient("localhost", 8086);

        joubert.subscribe("localhost", 8080);
        debora.subscribe("localhost", 8080);
        jonata.subscribe("localhost", 8081);
        joao.subscribe("localhost", 8080);
        maria.subscribe("localhost", 8081);

        Thread accessOne = new ThreadWrapper(joubert, "access Joubert- twt 1", "localhost", 8080);
        Thread accessTwo = new ThreadWrapper(debora, "access Debora- twt 2", "localhost", 8080);
        Thread accessThree = new ThreadWrapper(jonata, "access Jonata- twt 3", "localhost", 8081);
        Thread accessFour = new ThreadWrapper(joao, "access Joao- twt 4", "localhost", 8080);
        Thread accessFive = new ThreadWrapper(maria, "access Maria- twt 5", "localhost", 8081);
        
        accessOne.start();
        accessTwo.start();
        accessThree.start();
        accessFour.start();
        accessFive.start();

        try {
            accessTwo.join();
            Thread.sleep(5000);
            accessOne.join();
            Thread.sleep(5000);
            accessThree.join();
            Thread.sleep(5000);
            accessFour.join();
            Thread.sleep(5000);
            accessFive.join();
            Thread.sleep(5000);
        } catch (Exception e) {

        }

        List<Message> logJoubert = joubert.getLogMessages();
        List<Message> logDebora = debora.getLogMessages();
        List<Message> logJonata = jonata.getLogMessages();
        List<Message> logJoao = joao.getLogMessages();
        List<Message> logMaria = maria.getLogMessages();
        
        int i = 0;

        Iterator<Message> it = logJoubert.iterator();
        System.out.print("Log Joubert itens: ");
        while (it.hasNext()) {
            Message aux = it.next();
            System.out.print(aux.getContent() + " " + aux.getLogId() + " | ");
        }
        System.out.println();
        System.out.println(tweets[i]);

        it = logJonata.iterator();
        System.out.print("Log Jonata itens: ");
        while (it.hasNext()) {
            Message aux = it.next();
            System.out.print(aux.getContent() + " " + aux.getLogId() + " | ");
        }
        System.out.println();
        i++;
        System.out.println(tweets[i]);

        it = logDebora.iterator();
        System.out.print("Log Debora itens: ");
        while (it.hasNext()) {
            Message aux = it.next();
            System.out.print(aux.getContent() + " " + aux.getLogId() + " | ");
        }
        System.out.println();
        i++;
        System.out.println(tweets[i]);
        
        
        it = logJoao.iterator();
        System.out.print("Log Joao itens: ");
        while (it.hasNext()) {
            Message aux = it.next();
            System.out.print(aux.getContent() + " " + aux.getLogId() + " | ");
        }
        System.out.println();
        i++;
        System.out.println(tweets[i]);
        
        it = logMaria.iterator();
        System.out.print("Log Maria itens: ");
        while (it.hasNext()) {
            Message aux = it.next();
            System.out.print(aux.getContent() + " " + aux.getLogId() + " | ");
        }
        System.out.println();
        i++;
        System.out.println(tweets[i]); 
        
        joubert.unsubscribe("localhost", 8080);
        debora.unsubscribe("localhost", 8080);
        jonata.unsubscribe("localhost", 8080);
        joao.unsubscribe("localhost", 8080);
        maria.unsubscribe("localhost", 8080);

        joubert.stopPubSubClient();
        debora.stopPubSubClient();
        jonata.stopPubSubClient();
        joao.stopPubSubClient();
        maria.stopPubSubClient();
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
