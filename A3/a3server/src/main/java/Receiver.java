import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Receiver {

//  private final static String QUEUE_NAME = "hello";
  private static final String TASK_QUEUE_NAME = "task_queue";
  private static ConnectionFactory factory;
  private static Connection connection;
  private static Channel channel;
  private static final LiftRideDao liftRideDao = new LiftRideDao();
  private static final Integer THREADNUM = 8;


  public static void main(String[] args) throws Exception {
    factory = new ConnectionFactory();
    factory.setHost("localhost");
    connection = factory.newConnection();

//    channel = connection.createChannel();
//    processMsg();
    ExecutorService executorService = Executors.newFixedThreadPool(THREADNUM);
    for (int i = 0; i < THREADNUM; i++) {
      ReceiverThread thread = new ReceiverThread(connection);
      executorService.execute(thread);
    }


  }



}