import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class LibraryHttpHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            System.out.println("Here");
            JSONObject obj = new JSONObject();
            String uri = exchange.getRequestURI().toString();
            String[] splitedUri = uri.split("/");
            String request = splitedUri[2];
            System.out.println(request);
            obj = getResponse(exchange, request);
            sendResponse(obj, exchange, 200);
        } else if ("POST".equals(exchange.getRequestMethod())) {
            handlePostRequest(exchange);
        } else if ("DELETE".equals(exchange.getRequestMethod())) {
            handleDeleteRequest(exchange);
        } else if ("PUT".equals(exchange.getRequestMethod())) {
            handlePutRequest(exchange);
        }
    }


    public JSONObject getResponse(HttpExchange exchange, String request) {
        ArrayList<Book> arrList = new ArrayList<>();
        JSONArray jsonArray = new JSONArray();
        JSONObject obj = new JSONObject();

        if (request.equals("ALL")) { //ALL
            arrList = Library.getAllBooks();
            for (Book book : arrList) {
                jsonArray.add(book.convertToJson());
            }
            obj.put("Knygos", jsonArray);
        } else {
            if (obj == null){
                return null;
            }
            JSONObject bookJSON = Library.getBook(request);
            if (bookJSON != null) {
                obj.put("Knyga", bookJSON);
            } else {
                obj = null;
            }
        }
        return obj;
    }

    public void sendResponse(JSONObject obj, HttpExchange exchange, int responseCode){
        OutputStream outputStream = exchange.getResponseBody();

        try {
            if (obj== null) {
                exchange.sendResponseHeaders(404, -1);
            } else {
                String response = obj.toJSONString();

                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                byte[] encodedResponse = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(responseCode, encodedResponse.length);
                outputStream.write(encodedResponse);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e){
                try {
                    exchange.sendResponseHeaders(500, -1);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void handlePostRequest(HttpExchange exchange) {
        String uri = exchange.getRequestURI().toString();
        String[] splitedUri = uri.split("/");
        String request=splitedUri[2];
        InputStream is = exchange.getRequestBody();
        String requestBodyStr = "";
        try {

            int ch;
            while ((ch = is.read()) != -1) {
                requestBodyStr += (char) ch;
            }
            JSONObject obj = (JSONObject) JSONValue.parse(requestBodyStr);

            System.out.println(obj.get("Pavadinimas"));

            Library.addNewBook(obj);
            exchange.getResponseHeaders().put("location", Collections.singletonList("/books/"+obj.get("ISBN")));

            exchange.sendResponseHeaders(201, -1);


        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (Exception e){
            try {
                exchange.sendResponseHeaders(500, -1);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public void handleDeleteRequest(HttpExchange exchange) {
        String uri = exchange.getRequestURI().toString();
        String[] splitedUri = uri.split("/");
        String request=splitedUri[2];

        boolean bookWasFound = Library.deleteBook(request);


            try {
                if (bookWasFound == false) {
                    exchange.sendResponseHeaders(404, -1);
                }
                else {
                    exchange.sendResponseHeaders(204, -1);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                try {
                    exchange.sendResponseHeaders(500, -1);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }


    }

    public void handlePutRequest(HttpExchange exchange) {
        String uri = exchange.getRequestURI().toString();
        String[] splitedUri = uri.split("/");
        String isbn=splitedUri[2];
        InputStream is = exchange.getRequestBody();
        String requestBodyStr = "";
        try {

            int ch;
            while ((ch = is.read()) != -1) {
                requestBodyStr += (char) ch;
            }
            JSONObject obj = (JSONObject) JSONValue.parse(requestBodyStr);
            boolean alreadyExisted = Library.putBook(obj, isbn);

            exchange.getResponseHeaders().put("Location", Collections.singletonList("/books/" + isbn));// "/Library/"
            int responseCode;
            if (alreadyExisted == false){
                responseCode = 201;
            }
            else {
                responseCode = 200;
            }

            exchange.sendResponseHeaders(responseCode, -1);

        } catch (IOException e) {
            e.printStackTrace();

        }
        catch (Exception e){
            try {
                exchange.sendResponseHeaders(500, -1);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}




