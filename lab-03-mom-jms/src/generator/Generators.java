package generator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class Generators {
	
	private static final String JNDI_CONTEXT_FACTORY_CLASS_NAME = "org.exolab.jms.jndi.InitialContextFactory";
	private static final String providerUrl = "tcp://localhost:3035/";
	private static final String tasksQueueName = "tasksQueue";

	// Application JNDI context
	private Context jndiContext;

	// JMS Administrative objects references
	private QueueConnectionFactory queueConnectionFactory;
	private Queue tasksQueue;

	// JMS Client objects
	private QueueConnection queueConnection;

	// Business logic object
	private BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	private String lineStr;
	private int generatorsNumber = 1;
	private List<Generator> generators = new ArrayList<Generator>();

	/************** Initialization BEGIN ******************************/
	public Generators(int generatorsNumber) throws NamingException, JMSException {
		this.generatorsNumber = generatorsNumber;
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
		// ConnectionFactory
		queueConnectionFactory = (QueueConnectionFactory) jndiContext.lookup("ConnectionFactory");
		// Destination
		tasksQueue = (Queue) jndiContext.lookup(tasksQueueName);
	}

	private void initializeJmsClientObjects() throws JMSException, InterruptedException {
		queueConnection = queueConnectionFactory.createQueueConnection();
		
		for (int i = 0; i < generatorsNumber; i++) {
			QueueSession queueSession = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE); // false - non-transactional, AUTO_ACKNOWLEDGE - messages acknowledged after receive() method returns
			generators.add(new Generator("generator#" + (i + 1), tasksQueue, queueSession));
		}
	}

	/************** Initialization END ******************************/

	/*************** Business logic BEGIN **************************/

	public void start() throws JMSException, IOException, InterruptedException, NamingException {
		initializeJndiContext();
		initializeAdministrativeObjects();
		System.out.println("JMS administrative objects (ConnectionFactory, Destinations) initialized!");
		initializeJmsClientObjects();
		System.out.println("JMS client objects (Session, MessageConsumer) initialized!");
		queueConnection.start();
		System.out.println("Connection started - receiving messages possible!");

		// create and start generators
		for (Generator generator : generators) {
			generator.start();
		}

		// Receive messages synchronously
		while (true) {
			lineStr = br.readLine();
			if (lineStr.equals("/stop")) {
				for (Generator generator : generators)
					generator.stopMe();
				for (Generator generator : generators) {
					generator.join();
				}
			}
			break;
		}
		System.out.println("The generators stopped");
	}

	public void stop() {
		// close the connection
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
