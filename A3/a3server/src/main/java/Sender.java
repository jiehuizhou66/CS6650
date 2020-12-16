
import com.google.gson.JsonObject;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;


public class Sender {
  //  private final static String QUEUE_NAME = "hello";
  private static final String TASK_QUEUE_NAME = "task_queue";
  private Connection connection;
  private Channel channel;


  public Sender() throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    // connect rabbitMQ node on local machine/local host, change host name/ip to change the connection
    factory.setHost("localhost");

    connection = factory.newConnection();
    channel = connection.createChannel(); {

    // Mark queue durable to make sure it is resumeble after restart
    boolean durable = true;
    channel.queueDeclare(TASK_QUEUE_NAME, durable, false, false, null);
    }
  }

  public void publishMsg(String msg) throws Exception{
    channel.basicPublish("", "task_queue",
        MessageProperties.PERSISTENT_TEXT_PLAIN,
        msg.getBytes("UTF-8"));
    System.out.println(" [x] Sent '" + msg + "'");

  }

}

