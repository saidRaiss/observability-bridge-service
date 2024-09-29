package ma.socrates.observability.bridge;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Slf4j
@Testcontainers
class BridgeIntegrationTest {


    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"));


    // @Container
    static GenericContainer<?> producer;

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @BeforeAll
    static void setUp() {
        producer = new GenericContainer<>("raiss8080/observability-producer-service:latest")
                .withExposedPorts(8081)
                .withEnv("OTEL_SDK_DISABLED", "true")
                .withEnv("kafka.bootstrap", kafka.getBootstrapServers())
                .dependsOn(kafka);
        producer.start();
    }

    @AfterAll
    static void shutDown() {
        producer.stop();
    }

    /*
    @Container
    static MySQLContainer mySQL = new MySQLContainer("mysql:8.4");

    @Container
    static GenericContainer<?> consumer = new GenericContainer<>("raiss8080/observability-consumer-service:latest")
            .withExposedPorts(8082)
            .withEnv("OTEL_SDK_DISABLED", "true")
            .dependsOn(mySQL, kafka);

     */
    @Test
    void test() {
        log.info("Producer: {}", producer.getMappedPort(8081));
    }

}
