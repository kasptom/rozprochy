package client;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import operator.Operator;


public class Clients{
	volatile boolean finished = false;
	
	private static final String JNDI_CONTEXT_FACTORY_CLASS_NAME = "org.exolab.jms.jndi.InitialContextFactory";
	private static final String providerUrl = "tcp://localhost:3035/";
	private static final Map<Operator, String> topicNames = 
			Collections.unmodifiableMap(
					new HashMap<Operator, String>(){
						private static final long serialVersionUID = 1L;
					{
					put(Operator.ADD, "additionTopic");
					put(Operator.SUB, "subtractionTopic");
					put(Operator.MUL, "multiplicationTopic");
					put(Operator.DIV, "divisionTopic");
					put(Operator.MOD, "moduloTopic");
					
			}});
        	
	// Application JNDI context
	private Context jndiContext;
	// JMS Administrative objects
	private TopicConnectionFactory topicConnectionFactory;
	private List<Topic> topics = new ArrayList<Topic>();

	// JMS Client objects
	private TopicConnection topicConnection;

	// Business Logic
	private BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	private String lineStr;
	private int clientsNumber = 1;
	private List<Client> clients = new ArrayList<Client>();

	
	public Clients(int clientsNumber) throws NamingException, JMSException {
		this.clientsNumber = clientsNumber;
	}
	
	private void initializeJndiContext() throws NamingException {
		// JNDI Context
		Properties props = new Properties();
		props.put(Context.INITIAL_CONTEXT_FACTORY, JNDI_CONTEXT_FACTORY_CLASS_NAME);
		props.put(Context.PROVIDER_URL, providerUrl);
		jndiContext = new InitialContext(props);
	}	

	private void initializeAdministrativeTopicObjects() throws NamingException {
		// ConnectionFactory
		topicConnectionFactory = (TopicConnectionFactory) jndiContext.lookup("ConnectionFactory");
		// Destination
		for(int i=0; i<5; i++){
			System.out.println(topicNames.get(Operator.getOperator(i)));
			topics.add((Topic) jndiContext.lookup(topicNames.get(Operator.getOperator(i))));
		}
	}

	private void initializeJmsClientTopicObjects() throws JMSException, InterruptedException {
		topicConnection = topicConnectionFactory.createTopicConnection();
		
		for(int i=0; i<clientsNumber; i++){
			TopicSession topicSession = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
			clients.add(new Client("client#" + (i+1), topics, topicSession));
		}
	}

	
	
	public void start() throws JMSException, IOException, NamingException, InterruptedException {
		initializeJndiContext();
		initializeAdministrativeTopicObjects();
		//JMS administrative objects (ConnectionFactory, Destinations) initialized
		initializeJmsClientTopicObjects();
		//JMS client objects (Session, MessageConsumer) initialized
		topicConnection.start();
		//Connection started - receiving messages possible

		for (Client client : clients) {
			client.start();
		}
		
		System.out.println("Type /stop to stop the clients");
		while (true) {
			lineStr = br.readLine();
			if (lineStr.equals("/stop")) {
				for (Client client : clients)
					client.stopMe();
				for (Client client : clients)
					client.join();
			}
			break;
        }
		System.out.println("The clients stopped");
	}


	public void stop() {
		// close the context
		if (jndiContext != null) {
			try {
				jndiContext.close();
			} catch (NamingException exception) {
				exception.printStackTrace();
			}
		}

		// close the connection
		if (topicConnection != null) {
			try {
				topicConnection.close();
			} catch (JMSException exception) {
				exception.printStackTrace();
			}
		}
	}
}
