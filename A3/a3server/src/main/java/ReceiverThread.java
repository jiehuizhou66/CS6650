import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.MessageProperties;
import java.io.IOException;
import java.sql.SQLException;


public class ReceiverThread extends Thread {

  private static final String TASK_QUEUE_NAME = "task_queue";
  private static ConnectionFactory factory;
  private static Connection connection;
  private static Channel channel;
  private static final LiftRideDao liftRideDao = new LiftRideDao();

  public ReceiverThread(Connection connection) throws IOException {
    channel = connection.createChannel();
    // Mark queue durable to make sure it is resumeble after restart
    boolean durable = true;
    channel.queueDeclare(TASK_QUEUE_NAME, durable, false, false, null);
    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

    //Set RabbitMQ to only send i msg to client
    channel.basicQos(100);
  }


  public void run() {
    try {
    // This call back function will be invoked one this class received a msg.
    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
      String message = new String(delivery.getBody(), "UTF-8");

      System.out.println(" [x] Received '" + message + "'");
      try {
        createLiftRide(message);
      } catch (SQLException e){
        System.out.println(" [x] Retry");
        channel.basicPublish("", TASK_QUEUE_NAME,
            MessageProperties.PERSISTENT_TEXT_PLAIN,
            message.getBytes("UTF-8"));
      }
      finally {
        System.out.println(" [x] Done");
        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), true);
      }
    };
//    boolean autoAck = true; // acknowledgment is covered below
    boolean autoAck = false;
    channel.basicConsume(TASK_QUEUE_NAME, autoAck, deliverCallback, consumerTag -> { });
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  private static void createLiftRide(String message) throws SQLException {

//    System.out.println("################");
//    System.out.println(message);
    JsonParser parser = new JsonParser();
    JsonElement element = parser.parse(message);
    JsonObject jsonObject = element.getAsJsonObject();

    String resortID = jsonObject.get("resortID").getAsString();
    Integer dayID = jsonObject.get("dayID").getAsInt();
    Integer skierID = jsonObject.get("skierID").getAsInt();
    Integer time = jsonObject.get("time").getAsInt();
    Integer liftID = jsonObject.get("liftID").getAsInt();

    liftRideDao.createLiftRide(new LiftRide(resortID, dayID.toString(), skierID.toString(), time.toString(), liftID.toString()));

  }

}
