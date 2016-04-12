package solver;

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
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;


import operator.Operator;

public class Solvers{

	private static final String JNDI_CONTEXT_FACTORY_CLASS_NAME = "org.exolab.jms.jndi.InitialContextFactory";
	private static final String providerUrl = "tcp://localhost:3035/";
	private static final String tasksQueueName = "tasksQueue";
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
	
	// JMS Administrative objects references
	private QueueConnectionFactory queueConnectionFactory;
	private Queue tasksQueue;
	//... for topic
	private TopicConnectionFactory topicConnectionFactory;
	private List<Topic> topics = new ArrayList<Topic>();
	
	
	// JMS Client objects
	private QueueConnection queueConnection;
	
	// Business logic objects
	private BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	private String lineStr;
	private int solversNumber = 1;
	private List<Solver> solvers = new ArrayList<Solver>();

	// JMS Client objects
	private TopicConnection topicConnection;
	

	/************** Initialization BEGIN ******************************/
	public Solvers(int solversNumber) throws NamingException, JMSException {
		this.solversNumber = solversNumber;
	}

	private void initializeJndiContext() throws NamingException {
		// JNDI Context
		Properties props = new Properties();
		props.put(Context.INITIAL_CONTEXT_FACTORY, JNDI_CONTEXT_FACTORY_CLASS_NAME);
		props.put(Context.PROVIDER_URL, providerUrl);
		jndiContext = new InitialContext(props);
		System.out.println("JNDI context initialized!");
	}

	private void initializeAdministrativeObjects() throws NamingException {
		// ConnectionFactories
		queueConnectionFactory = (QueueConnectionFactory) jndiContext.lookup("ConnectionFactory");
		topicConnectionFactory = (TopicConnectionFactory) jndiContext.lookup("ConnectionFactory");
		// Destination
		tasksQueue = (Queue) jndiContext.lookup(tasksQueueName);
		for(int i=0; i<5; i++){
			System.out.println(topicNames.get(Operator.getOperator(i)));
			topics.add((Topic) jndiContext.lookup(topicNames.get(Operator.getOperator(i))));
		}
	}
	
	private void initializeJmsClientObjects() throws JMSException, InterruptedException {
		queueConnection = queueConnectionFactory.createQueueConnection();
		topicConnection = topicConnectionFactory.createTopicConnection();
		
		for(int i=0; i<solversNumber; i++){
			QueueSession queueSession = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE); // false - non-transactional, AUTO_ACKNOWLEDGE - messages acknowledged after receive() method returns
			TopicSession topicSession= topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
			solvers.add(new Solver("solver#" + (i+1), tasksQueue, topics, queueSession, topicSession));
		}
	}
	/************** Initialization END ******************************/
	
	
	
	
	/************** Business logic BEGIN 
	 * @throws NamingException 
	 * @throws InterruptedException ****************************/
	public void start() throws JMSException, IOException, NamingException, InterruptedException {
		initializeJndiContext();
		initializeAdministrativeObjects();
		//JMS administrative objects (ConnectionFactory, Destinations) initialized
		initializeJmsClientObjects();
		//JMS client objects (Session, MessageConsumer) initialized
		queueConnection.start();
		topicConnection.start();
		//Connection started - sending and receiving messages possible
	
		for (Solver solver : solvers) {
			solver.start();
		}
		
		System.out.println("Type /stop to stop the solvers");
		while (true) {
			lineStr = br.readLine();
			if (lineStr.equals("/stop")) {
				for (Solver solver : solvers)
					solver.stopMe();
				for (Solver solver : solvers) {
					solver.join();
				}
			}
			break;
		}
		System.out.println("The solvers stopped");
	}
	

	public void stop() {
        // close the connection
        if (topicConnection != null) {
            try {
                topicConnection.close();
            } catch (JMSException exception) {
                exception.printStackTrace();
            }
        }
        if (queueConnection != null) {
            try {
                queueConnection.close();
            } catch (JMSException exception) {
                exception.printStackTrace();
            }
        }
		// close the context
        if (jndiContext != null) {
            try {
            	jndiContext.close();
            } catch (NamingException exception) {
                exception.printStackTrace();
            }
        }
	}

	/************** Business logic END ****************************/

}
