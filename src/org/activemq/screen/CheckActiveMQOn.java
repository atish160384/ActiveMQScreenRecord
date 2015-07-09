package org.activemq.screen;

import javax.jms.JMSException;

public final class CheckActiveMQOn {
	
	private CheckActiveMQOn(){
	
	}
	
	private static final String USER_HOME = "user.home";

	private static final String FILE_APPENDER ="/";

	public static boolean check() {
		boolean on;
		ActiveMQConnection activeMQConnection = ActiveMQConnection
				.getInstance();
		try {
			on = activeMQConnection.test();
			if (on == true) {
				System.out.println("Active MQ is Up and running.");
			}
		} catch (JMSException e) {
			on = false;
		}
		return on;
	}

	public static final String getSystemRootPath() {
		return System.getProperty(USER_HOME) + FILE_APPENDER;
	}

}
