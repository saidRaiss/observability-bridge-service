package ma.socrates.observability.bridge;

import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
import ma.socrates.observability.bridge.core.model.Message;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
@Testcontainers
class BridgeIntegrationTest {

    private static final Network network = Network.newNetwork();

    @LocalServerPort
    private static int port;

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"))
            .withNetwork(network)
            .withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "true")
            .withEnv("KAFKA_CREATE_TOPICS", "topic-observability")
            .withEnv("KAFKA_BROKER_ID", "1")
            .withEnv("KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR", "1")
            .withEnv("KAFKA_TRANSACTION_STATE_LOG_MIN_ISR", "1")
            .withEnv("KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR", "1");

    @Container
    static GenericContainer<?> mySQL = new MySQLContainer("mysql:8.4")
            .withDatabaseName("observabiliy_db")
            .withNetwork(network);

    static GenericContainer<?> kafkaUi;
    static GenericContainer<?> producer;
    static GenericContainer<?> consumer;

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("app.outbound.rest-clients.consumer.base-url",
                () -> "http://localhost:" + consumer.getMappedPort(8082));
    }

    @BeforeAll
    static void setUp() {

        try (AdminClient adminClient = AdminClient
                .create(Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers()))) {
                    adminClient.createTopics(List.of(new NewTopic("topic-observability", 1, (short)1)));
        }

        kafkaUi = new GenericContainer<>("provectuslabs/kafka-ui:latest")
                .withNetwork(network)
                .withExposedPorts(8080)
                .withEnv("DYNAMIC_CONFIG_ENABLED", "true")
                .withEnv("KAFKA_CLUSTERS_0_NAME", "local")
                .withEnv("KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS", kafka.getBootstrapServers())
                .withEnv("KAFKA_CLUSTERS_0_ZOOKEEPER", kafka.getEnvMap().get("KAFKA_ZOOKEEPER_CONNECT"))
                .dependsOn(kafka);
        producer = new GenericContainer<>("raiss8080/observability-producer-service:latest")
                .withNetwork(network)
                .withExposedPorts(8080)
                .withEnv("OTEL_SDK_DISABLED", "true")
                .withEnv("APP_OUTBOUND_REST_CLIENTS_BRIDGE_BASE_URL", "http://localhost:" + port)
                .withEnv("APP_KAFKA_BOOTSTRAP", kafka.getBootstrapServers())
                .withEnv("APP_KAFKA_TOPIC", "topic-observability")
                .dependsOn(kafka);
        consumer = new GenericContainer<>("raiss8080/observability-consumer-service:latest")
                .withNetwork(network)
                .withExposedPorts(8082)
                .withEnv("OTEL_SDK_DISABLED", "true")
                .withEnv("APP_KAFKA_BOOTSTRAP", kafka.getBootstrapServers())
                .withEnv("SPRING_DATASOURCE_URL", ((MySQLContainer<?>)mySQL).getJdbcUrl())
                .withEnv("SPRING_DATASOURCE_USERNAME", ((MySQLContainer<?>)mySQL).getUsername())
                .withEnv("SPRING_DATASOURCE_PASSWORD", ((MySQLContainer<?>)mySQL).getPassword())
                .dependsOn(kafka, mySQL);

        kafkaUi.start();
        producer.start();
        consumer.start();

    }

    @AfterAll
    static void shutDown() {
        producer.stop();
        consumer.stop();
    }

    @Test
    void test() {
        try {
            JSONObject payload = new JSONObject(Map.of("key", "KEY_1", "content", "Some message here !"));
            given().body(payload.toString())
                    .contentType(ContentType.JSON)
                    .when()
                    .post("http://localhost:" + producer.getMappedPort(8080) + "/producer/publish")
                    .andReturn();

            given().body(payload.toString())
                    .contentType(ContentType.JSON)
                    .when()
                    .post("http://localhost:" + producer.getMappedPort(8080) + "/producer/publish")
                    .then()
                    .statusCode(200);

            Message message = given().param("id", "1")
                    .when()
                    .get("http://localhost:" + port + "/message")
                    .then()
                    .extract()
                    .as(Message.class);

            assertThat(message.content()).isEqualTo("Some message here !");
        } finally {
            log.info("Kafka logs: {}", kafka.getLogs(OutputFrame.OutputType.STDOUT));
            log.info("mySQL logs: {}", mySQL.getLogs(OutputFrame.OutputType.STDOUT));
            log.info("Producer logs: {}", producer.getLogs(OutputFrame.OutputType.STDOUT));
            log.info("Consumer logs: {}", consumer.getLogs(OutputFrame.OutputType.STDOUT));
        }
    }

}
