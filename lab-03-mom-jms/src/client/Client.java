package client;

import java.util.ArrayList;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Topic;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;

import operator.Operator;

public class Client extends Thread{
	private TopicSession session;
	private List<Topic> topics;
	private volatile boolean finished = false;
	private SolutionListener solutionListener;
	private List<TopicSubscriber> topicSubscribers;
	
	//
	public Client(String clientName, List<Topic> topics, TopicSession topicSession) throws JMSException, InterruptedException{
		super(clientName);
		this.topics = topics;
		this.session = topicSession;
		this.solutionListener = new SolutionListener(this);
		this.topicSubscribers = new ArrayList<TopicSubscriber>();
		for(Topic topic : this.topics){
			topicSubscribers.add(session.createSubscriber(topic));
		}
	}
	
	public void run(){
		try {
			//queueReceiver.setMessageListener(taskListener);
			Message msg;
			while (!finished) {
				msg = null;
				msg = topicSubscribers.get(Operator.getIndex(Operator.nextOperator())).receive(1000);
				if(msg != null)
					solutionListener.onMessage(msg);
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
