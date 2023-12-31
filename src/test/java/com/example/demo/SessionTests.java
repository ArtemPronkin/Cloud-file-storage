package com.example.demo;

import com.example.demo.repository.UserRepository;
import com.redis.testcontainers.RedisContainer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;

@Slf4j
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
public class SessionTests {
    @Container
    @ServiceConnection
    static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql");
    @Container
    static RedisContainer redisContainer = new RedisContainer(DockerImageName.parse("redis:5.0.3-alpine"))
            .withExposedPorts(6379);
    @Autowired
    UserRepository userRepository;
    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    private static void registerRedisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port",
                () -> redisContainer.getMappedPort(6379).toString());

        log.info(redisContainer.getHost());
        log.info(redisContainer.getMappedPort(6379).toString());
    }

    @Test
    void Must_RedisContainerIsRun_WhenTestsStarted() {
        Assertions.assertTrue(redisContainer.isRunning());
    }

    @Test
    void Must_MySQLContainerIsRun_WhenTestsStarted() {
        Assertions.assertTrue(mySQLContainer.isRunning());
    }

    @Test
    void Must_RedisContainsSpringSession_WhenUserIsAuthenticated() throws Exception {
        JedisPool jedisPool = new JedisPool(redisContainer.getHost(),
                redisContainer.getMappedPort(6379));
        try (jedisPool; Jedis jedis = jedisPool.getResource()) {

            mockMvc.perform(MockMvcRequestBuilders.post("/regitration")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("username", "name")
                    .param("email", "email")
                    .param("password", "password"));

            mockMvc.perform(formLogin("/login")
                    .user("username", "name")
                    .password("password", "password"));

            Assertions.assertFalse(jedis.keys("spring:session:sessions*").isEmpty());
        }


    }


}
