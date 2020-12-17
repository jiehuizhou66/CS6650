import org.apache.commons.dbcp2.*;
public class DBCPDataSource {
  private static BasicDataSource dataSource;
  private static BasicDataSource dataSource2;

  // NEVER store sensitive information below in plain text!
  private static final String HOST_NAME = System.getProperty("MySQL_IP_ADDRESS");
  private static final String PORT = System.getProperty("MySQL_PORT");
  private static final String DATABASE = "LiftRides";
  private static final String USERNAME = System.getProperty("DB_USERNAME");
  private static final String PASSWORD = System.getProperty("DB_PASSWORD");

  private static final String HOST_NAME2 = System.getProperty("MySQL_IP_ADDRESS_2");
  private static final String DATABASE2 = "LiftRides2";


  static {
    // https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-reference-jdbc-url-format.html
    dataSource = new BasicDataSource();
    dataSource2 = new BasicDataSource();

    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    String url = String.format("jdbc:mysql://%s:%s/%s?serverTimezone=UTC", HOST_NAME, PORT, DATABASE);
    dataSource.setUrl(url);
    dataSource.setUsername(USERNAME);
    dataSource.setPassword(PASSWORD);
    dataSource.setInitialSize(20);
    dataSource.setMaxTotal(100);


    String url2 = String.format("jdbc:mysql://%s:%s/%s?serverTimezone=UTC", HOST_NAME2, PORT, DATABASE2);
    dataSource2.setUrl(url2);
    dataSource2.setUsername(USERNAME);
    dataSource2.setPassword(PASSWORD);
    dataSource2.setInitialSize(20);
    dataSource2.setMaxTotal(100);
  }

  public static BasicDataSource getDataSource() {
    return dataSource;
  }

  public static BasicDataSource getDataSource2() {
    return dataSource2;
  }
}


