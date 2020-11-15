import com.google.gson.Gson;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.time.temporal.ValueRange;


@WebServlet(name = "SkierServlet")
public class SkierServlet extends HttpServlet {

  protected void doPost(HttpServletRequest req,
      HttpServletResponse res)
      throws ServletException, IOException {
    String urlPath = req.getPathInfo();
//    System.out.println(urlPath);
    res.setContentType("application/json");

    // check we have a URL!
    if (urlPath == null || urlPath.length() == 0 || urlPath.equals("/")) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write(new Gson().toJson("missing parameter"));
      return;
    }

    // and now validate url path and return the response status code
    if (isValidSkierPostUrl(urlPath)) {
      sendPostSuccess(res);
    } else {
      sendPostFailure(res);
    }
  }

  private boolean isValidSkierPostUrl(String urlPath) {
    return urlPath.matches("/liftrides");
  }

  private void sendPostSuccess(HttpServletResponse res) throws IOException {
    res.setStatus(HttpServletResponse.SC_CREATED);
//    res.getWriter().write(new Gson().toJson("Received Skiers Post request /liftrides"));
  }

  private void sendPostFailure(HttpServletResponse res) throws IOException {
    res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    res.getWriter().write(new Gson().toJson("Wrong url for Skiers Post request /liftrides"));
  }

  protected void doGet(HttpServletRequest req,
      HttpServletResponse res)
      throws ServletException, IOException {
    // url: /skiers/"resortID"/days/"dayID"/skiers/"skierID"
    // e.g: /skiers/Mission Ridge/days/32/skiers/7889
    String urlPath = req.getPathInfo();
//    System.out.println(urlPath);
    res.setContentType("application/json");

    // check we have a URL!
    if (urlPath == null || urlPath.length() == 0 || urlPath.equals("/")) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
//      res.getWriter().write(new Gson().toJson("{'message': 'missing parameters'}"));
      res.getWriter().write("{'message': 'missing parameters'}");
      return;
    }

    // and now validate url path and return the response status code
    if (isValidSkierGetVerticalByDayUrl(urlPath)) {
      sendGetSuccess(res);
    } else {
      sendGetFailure(res);
    }
  }

  private boolean isValidSkierGetVerticalByDayUrl(String urlPath) {
    // e.g: /Mission Ridge/days/32/skiers/7889
    // days range 1 - 366
    String[] urlParts = urlPath.split("/");
    int dayID;
    try {
      dayID = Integer.parseInt(urlParts[3]);;
    } catch (Exception e) {
      return false;
    }

    return urlPath.matches(
        "/(.*)"
            + "/days/([0-9]+)"
            + "/skiers/([0-9]+)")
        && ValueRange.of(1, 366).isValidIntValue(dayID);
  }

  private void sendGetSuccess(HttpServletResponse res) throws IOException {
    res.setStatus(HttpServletResponse.SC_OK);
    String body = "{'resortID' : 'SilverMt',"
        + " 'totalVert' : 56734}";
//    res.getWriter().write(new Gson().toJson(
//        body));
    res.getWriter().write(
        body);
//    res.getWriter().write(new Gson().toJson(
//        "Received Skiers Get request /{resortID}/days/{dayID}/skiers/{skierID}"));
  }

  private void sendGetFailure(HttpServletResponse res) throws IOException {
    res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//    res.getWriter().write(new Gson().toJson(
//        "Invalid params for Skiers Get request "
//        + "/{resortID}/days/{dayID}/skiers/{skierID}"));
//    res.getWriter().write(new Gson().toJson(
//        "{'message': '404 bad request'}"));
    res.getWriter().write(
        "{'message': '404 bad request'}");
  }

}

