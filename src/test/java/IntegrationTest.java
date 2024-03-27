import org.example.threadpool.ThreadPooledServer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class IntegrationTest {

   //Do tests
   public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {

        ThreadPooledServer server = new ThreadPooledServer(9001);
        new Thread(server).start();
        HttpClient  client = HttpClient.newHttpClient();
        // This should return 404
       HttpRequest request = HttpRequest.newBuilder().uri(new URI("http://localhost:9001/test"))
               .GET()
       .build();

       HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());

       System.out.println(response.statusCode());
       System.out.println(response.body());

       // This should return 200
       HttpRequest request2 = HttpRequest.newBuilder().uri(new URI("http://localhost:9001/test/uuid"))
               .GET()
               .build();

       HttpResponse response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());

       System.out.println(response2.statusCode());
       System.out.println(response2.body());


       HttpRequest postRequest = HttpRequest.newBuilder().uri(new URI("http://localhost:9001/test/test"))
               .POST(HttpRequest.BodyPublishers.ofString("Hello World"))
                .build();

         HttpResponse postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());

         System.out.println(postResponse.statusCode());
         System.out.println(postResponse.body());


    }
}
