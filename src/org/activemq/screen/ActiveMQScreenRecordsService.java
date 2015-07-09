package org.activemq.screen;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQObjectMessage;
import org.motechproject.event.MotechEvent;

public class ActiveMQScreenRecordsService {

	private static final String QUEUE_NAME = "ActiveMQ.DLQ";

	private static final String EXTENTION = ".txt";

	private static final String PATH = CheckActiveMQOn.getSystemRootPath()
			+ "Desktop";

	private static final String PATH_APPENDER = "/";

	private static final String FILE_APPENDER = ".";

	private static final String FILE_NAME = "active_mq_bkp_";

	private static final String POST_MSG = "MESSAGE : ";

	private static final String URL = "URL : ";

	private static final String TIMESTAMP = "TIMESTAMP : ";

	private static final String SAPERATOR = "-";

	public static void main(String[] args) throws JMSException, IOException {
		log();
		if (CheckActiveMQOn.check() == true) {
			ActiveMQScreenRecordsService activeMQScreenRecordsService = new ActiveMQScreenRecordsService();
			activeMQScreenRecordsService.readActiveMqMessages();
		} else {
			System.out.println("Active MQ is down. Please start active mq");
		}
	}

	private void readActiveMqMessages() throws JMSException, IOException {

		ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(
				"tcp://localhost:61616");
		Connection connection = factory.createConnection();
		connection.start();
		Session session = connection.createSession(false,
				Session.AUTO_ACKNOWLEDGE);
		Queue queue = session.createQueue(QUEUE_NAME);
		QueueBrowser browser = session.createBrowser(queue);
		Enumeration<?> messagesInQueue = browser.getEnumeration();
		Map<String, String> map = new LinkedHashMap<String, String>();
		Set<String> urls = new LinkedHashSet<String>();
		int counter = 1;
		while (messagesInQueue.hasMoreElements()) {
			Message messageAMQObj = (Message) messagesInQueue.nextElement();
			if (messageAMQObj instanceof ActiveMQObjectMessage) {
				ActiveMQObjectMessage msg = (ActiveMQObjectMessage) messageAMQObj;
				MotechEvent motechEvent = (MotechEvent) msg.getObject();
				System.out.println(counter + " .");
				System.out.println(POST_MSG
						+ motechEvent.getParameters().get("data"));
				System.out
						.println(URL + motechEvent.getParameters().get("url"));
				System.out.println(TIMESTAMP + new Date(msg.getJMSTimestamp()));

				StringBuilder message = new StringBuilder();

				message.append("\n");
				message.append(POST_MSG
						+ motechEvent.getParameters().get("data"));
				message.append("\n");
				message.append(URL + motechEvent.getParameters().get("url"));
				message.append("\n");
				message.append(TIMESTAMP + new Date(msg.getJMSTimestamp()));
				message.append("\n");
				message.append("\n");

				map.put(motechEvent.getParameters().get("url").toString()
						+ SAPERATOR + counter, message.toString());
				urls.add(motechEvent.getParameters().get("url").toString());
				counter++;
			}
		}
		System.out.println("Total No. of Urls : " + urls.size());
		System.out.println("Total No. of Failed Messages : " + map.size());
		StringBuilder sb = new StringBuilder();
		for (String url : urls) {
			sb.append("for Url " + url + ", following are messages");
			sb.append("\n");
			int counterUrlBased = 1;
			for (String urlWithCount : map.keySet()) {
				if (urlWithCount.contains(url)) {
					sb.append(counterUrlBased);
					sb.append("\n");
					sb.append(map.get(urlWithCount));
					sb.append("\n");
					counterUrlBased++;
				}
			}
		}
		write(sb.toString());
		connection.stop();
		connection.close();
	}

	private void write(String messages) throws IOException {
		Date now = new Date();
		DateFormat dateFormatter = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
		String date = dateFormatter.format(now);
		String filename = PATH + PATH_APPENDER + "logs" + PATH_APPENDER + "amq"
				+ PATH_APPENDER + FILE_NAME + date + EXTENTION;
		File file = new File(filename);
		if (!file.exists()) {
			file.createNewFile();
			file.setWritable(true,true);
			file.setReadable(true,true);
			file.setExecutable(true,true);
			
		}
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(messages);
		bw.close();
		System.out.println("Writing to File " + filename + " Done. ");
	}

	public static void log() throws IOException {
		createLogDirectory();
		Date now = new Date();
		DateFormat dateFormatter = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
		PrintStream out = new PrintStream(new FileOutputStream(PATH
				+ PATH_APPENDER + "logs" + PATH_APPENDER + "log.out"
				+ FILE_APPENDER + dateFormatter.format(now)));
		System.setOut(out);

	}

	private static void createLogDirectory() {
		final String logDirectory = PATH + PATH_APPENDER + "logs"
				+ PATH_APPENDER + "amq";
		File file = new File(logDirectory);
		if (!file.exists()) {
			if (file.mkdirs()) {
				System.out.println("Log Directory is successfully created!");
			} else {
				System.out.println("Failed to create log directory!");
			}
		}
		System.out.println("Please find your logs in : " + logDirectory);
	}
}
