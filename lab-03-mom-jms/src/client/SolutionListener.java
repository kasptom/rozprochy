package client;
import java.util.Random;


import javax.jms.*;

public class SolutionListener{
	private Client cli;
	private Random random;
	private int miliSec = 1000;
	
	public SolutionListener(Client cli){
		this.cli = cli;
		this.random = new Random();
	}
    /**
     * Casts the message to a TextMessage and displays its text.
     *
     * @param message the incoming message
     */
	
    public void onMessage(Message message) {
        TextMessage msg = null;
        try {
            if (message instanceof TextMessage) {
                msg = (TextMessage) message;
                System.out.print(cli.getName() + " got " + msg.getText()+ " ");
                miliSec = 3000 + random.nextInt(3000);
                System.out.println(" and sleeps for: "+ (miliSec / 1000.0)+ " [s]");
                Thread.sleep(miliSec);
            } else {
                System.out.println("Message of wrong type: " +
                    message.getClass().getName());
            }
        } catch (JMSException e) {
            System.out.println("JMSException in onMessage(): " +
                e.toString());
        } catch (Throwable t) {
            System.out.println("Exception in onMessage():" +
                t.getMessage());
        }
    }
}