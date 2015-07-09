package org.activemq.screen;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

public final class ActiveMQConnection {

	private ActiveMQConnectionFactory factory;

	private Connection connection;

	private Session session;

	private Destination destination;

	private MessageProducer producer;

	private MessageConsumer consumer;

	private ActiveMQConnection() {

	}

	private static class SingletonHelper {
		private static final ActiveMQConnection INSTANCE = new ActiveMQConnection();
	}

	public static ActiveMQConnection getInstance() {

		return SingletonHelper.INSTANCE;
	}

	private void connect() throws JMSException {
		factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
		connection = factory.createConnection();
		connection.start();

	}

	private void init() throws JMSException {
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		destination = session.createQueue("TEST");
		producer = session.createProducer(destination);
		producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
		consumer = session.createConsumer(destination);
	}

	private boolean sendAndRecieve() throws JMSException {

		boolean recievedAck = false;
		TextMessage messageToSend = session.createTextMessage("PING");
		producer.send(messageToSend);
		Message messageRecieved = consumer.receive(8000);
		if (messageRecieved instanceof TextMessage) {
			TextMessage textMessage = (TextMessage) messageRecieved;
			String text = textMessage.getText();
			if (text.equals("PING")) {
				recievedAck = true;
			}
		}
		return recievedAck;
	}

	private void disconnect() throws JMSException {
		consumer.close();
		session.close();
		connection.close();
	}

	public boolean test() throws JMSException {
		boolean ack = false;
		connect();
		init();
		ack = sendAndRecieve();
		disconnect();
		return ack;
	}

}
