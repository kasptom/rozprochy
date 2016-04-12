package solver;

import java.util.ArrayList;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;

import operator.Operator;

public class Solver extends Thread {
	private QueueSession qSession;
	private TopicSession tSession;
	private Queue queue;
	private List<Topic> topics;
	private volatile boolean finished = false;
	private QueueReceiver queueReceiver;
	private TasksListener taskListener;
	private List<TopicPublisher> topicPublishers;
	
	
	public Solver(String solverName, Queue queue, List<Topic> topics, QueueSession qSession, TopicSession tSession) throws JMSException, InterruptedException{
		super(solverName);
		this.qSession = qSession;
		this.tSession = tSession;
		this.queue = queue;
		this.topics = topics;
		this.taskListener = new TasksListener(this);
		this.topicPublishers = new ArrayList<TopicPublisher>();
		for(Topic topic : this.topics){
			topicPublishers.add(this.tSession.createPublisher(topic));
		}
		this.queueReceiver = this.qSession.createReceiver(this.queue);
	}
	
	public void run(){
		try {
			//queueReceiver.setMessageListener(taskListener);
			Message msg;
			while (!finished) {
				msg = null;
				msg = queueReceiver.receive(1000);
				if(msg != null)
					taskListener.onMessage(msg);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			System.out.println(this.getName()+" stopped");
		}
	}
	
	public void computeTask(int a, int b, String strOp) throws JMSException{
		String strResult = String.valueOf(a)+" "+strOp+" "+String.valueOf(b);
		double result;
		TextMessage message = tSession.createTextMessage();
		
		switch(strOp){
			case "+":
				result = a + b;
				strResult = strResult + " = " + result; 
				break;
			case "-":
				result = a - b;
				strResult = strResult + " = " + result; 
				break;
			case "*":
				result = a * b;
				strResult = strResult + " = " + result; 
				break;
			case "/":
				if(b != 0){
					result = a / (double)b;
					strResult = strResult + " = " + result;
				}
				else{
					strResult = strResult + " <--- [error] (division by zero)";
				}
				break;
			case "%":
				if(b != 0){
					result = a % b;
					strResult = strResult + " = " + result;
				}
				else{
					strResult = strResult + " <--- [error] (mod zero undefined)";
				}
				break;
			default:
				System.out.println("[error] unknown operator");
				return;
		}
		message.setText(strResult);
		topicPublishers.get(Operator.getIndex(Operator.getOperator(strOp))).publish(message);
	}
	

	public void stopMe() {
		finished = true;
	}
}
