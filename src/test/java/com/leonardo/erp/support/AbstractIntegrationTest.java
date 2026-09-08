package com.leonardo.erp.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import tools.jackson.databind.ObjectMapper;

/**
 * Base class for controller/repository integration tests: boots the full Spring context
 * (mock web environment, no real socket) against a real PostgreSQL container, so Flyway
 * migrations and QueryDSL queries run against the same database engine used in production.
 *
 * <p>The container is started once for the whole test run (singleton container pattern) and
 * deliberately never stopped by us — Testcontainers' Ryuk reaper removes it when the JVM
 * exits. It is intentionally NOT annotated with {@code @Container}/{@code @Testcontainers}:
 * that combination stops the container in {@code afterAll} of whichever test class happens to
 * run first, leaving every later integration test class stuck reconnecting to a dead
 * container. {@code @ServiceConnection} still auto-wires the datasource without needing that
 * lifecycle annotation.
 *
 * <p>Each test method runs in its own transaction, rolled back afterwards, so tests never see
 * data left behind by another test even though the container (and its schema) is shared for
 * the whole run.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Transactional
public abstract class AbstractIntegrationTest {

    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16");

    static {
        POSTGRES.start();
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;
}
