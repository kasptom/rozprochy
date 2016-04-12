package solver;
import java.util.Random;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 *	TasksListener waits for incoming messages and sends
 *	them using Solver solver method: computeAndSend(int a, int b, Operator operator)
 *	to all subscribed writers
 */

public class TasksListener implements MessageListener{
	private Solver solver = null;
	private Random random;
	private int miliSec = 1000;
	
	public TasksListener(Solver solver){
		this.solver = solver;
		this.random = new Random();
	}
	
	public void onMessage(Message message) {
		MapMessage msg;
		int a, b;
		String opStr;
		try {
            if (message instanceof MapMessage) {
                msg = (MapMessage)message;
                a = msg.getInt("a");
                b = msg.getInt("b");
                opStr = msg.getString("Operator");
                System.out.print(solver.getName() + " got: "+a+" "+opStr+" "+b);
                miliSec = 3000 + random.nextInt(3000);
                solver.computeTask(a,b,opStr);
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
