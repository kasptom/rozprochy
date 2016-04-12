package generator;

import java.util.Random;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Queue;
import javax.jms.QueueSender;
import javax.jms.QueueSession;

import operator.Operator;

public class Generator extends Thread {
	private QueueSession session;
	private QueueSender tasksSender;
	private Queue queue;
	private volatile boolean finished = false;
	private MapMessage taskMessage;
	private Random random;
	private int sleepTime;

	public Generator(String generatorName, Queue queue, QueueSession session) throws JMSException, InterruptedException {
		super(generatorName);
		this.session = session;
		this.queue = queue;
		this.tasksSender = session.createSender(this.queue);
		random = new Random();
	}

	public void run() {
		try {
			while (!finished) {
				taskMessage = session.createMapMessage();
				taskMessage.setString("Operator", Operator.getString(Operator.nextOperator()));
				taskMessage.setInt("a", random.nextInt() % 1000);
				taskMessage.setInt("b", random.nextInt() % 1000);
				System.out.print(this.getName() + " sends: " + taskMessage.getInt("a") + " "
						+ taskMessage.getString("Operator") + " " + taskMessage.getInt("b"));
				tasksSender.send(taskMessage);
				sleepTime = 5000 + random.nextInt(5000);
				System.out.println(" and sleeps for " + sleepTime / 1000.0 + " [s]");
				Thread.sleep(sleepTime);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			System.out.println(this.getName()+" stopped");
		}
	}

	public void stopMe() {
		finished = true;
	}
}
