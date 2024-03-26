import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import managers.InMemoryTaskManager;
import managers.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tasks.Task;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskServerTest {
    public TaskManager taskManager = new InMemoryTaskManager();
    public HttpTaskServer httpTaskServer = new HttpTaskServer(taskManager);
    public Gson gson;

    public HttpTaskServerTest() throws IOException {
    }

    @BeforeEach
    public void setUp() {
        taskManager.deleteAllTypesTasks();
        gson = HttpTaskServer.getGson();
        httpTaskServer.start();
    }

    @AfterEach
    public void shutDown() {
        httpTaskServer.stop(0);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "http://localhost:8080/api/v1/tasks",
            "http://localhost:8080/api/v1/epics",
            "http://localhost:8080/api/v1/subtasks",
            "http://localhost:8080/api/v1/history",
            "http://localhost:8080/api/v1/prioritized"
    })
    void checkSetUp(String apiUrl) {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create(apiUrl);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("Accept", "application/json")
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
            final List<Task> listFromHttp = gson.fromJson(response.body(), new TypeToken<List<Task>>() {
            }.getType());
            assertEquals(0, listFromHttp.size(), "Некорректное количество задач");
        } catch (IOException | InterruptedException e) {
            assertNotNull(null, "Во время выполнения запроса ресурса по URL-адресу: '" + url + "' возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
    }

    @Test
    void checkShutDown() {
        httpTaskServer.stop(0);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/api/v1/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("Accept", "application/json")
                .GET()
                .build();
        assertThrows(ConnectException.class, () -> {
            client.send(request, HttpResponse.BodyHandlers.ofString());
        }, "Http-Сервер все еще работает!");
    }

}