/*
 * Copyright 2021 Wultra s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wultra.core.audit.base;

import com.wultra.core.audit.base.model.AuditDetail;
import com.wultra.core.audit.base.model.AuditLevel;
import com.wultra.core.audit.base.util.JsonUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.sql.Timestamp;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TestApplication.class)
@Sql(scripts = "/db_schema.sql")
public class AuditTest {

    private final AuditFactory auditFactory;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public AuditTest(AuditFactory auditFactory, JdbcTemplate jdbcTemplate) {
        this.auditFactory = auditFactory;
        this.jdbcTemplate = jdbcTemplate;
    }

    @BeforeEach
    public void cleanTestDb() {
        jdbcTemplate.execute("DELETE FROM audit_log");
        jdbcTemplate.execute("DELETE FROM audit_param");
    }

    @Test
    public void testAuditInfo() {
        Timestamp timestampBeforeAudit = new Timestamp(System.currentTimeMillis() - 1);
        Audit audit = auditFactory.getAudit();
        audit.info("test message");
        audit.flush();
        jdbcTemplate.query("SELECT * FROM audit_log", rs -> {
            assertEquals("test-application", rs.getString("application_name"));
            assertEquals("INFO", rs.getString("audit_level"));
            assertTrue(rs.getTimestamp("timestamp_created").after(timestampBeforeAudit));
            assertEquals("test message", rs.getString("message"));
            assertNull(rs.getString("stack_trace"));
            assertEquals("{}", rs.getString("param"));
            assertEquals("com.wultra.core.audit.base.AuditTest", rs.getString("calling_class"));
            assertEquals("main", rs.getString("thread_name"));
        });
    }

    @Test
    public void testAuditWarn() {
        Timestamp timestampBeforeAudit = new Timestamp(System.currentTimeMillis() - 1);
        Audit audit = auditFactory.getAudit();
        audit.warn("test message for warning");
        audit.flush();
        jdbcTemplate.query("SELECT * FROM audit_log", rs -> {
            assertEquals("test-application", rs.getString("application_name"));
            assertEquals("WARN", rs.getString("audit_level"));
            assertTrue(rs.getTimestamp("timestamp_created").after(timestampBeforeAudit));
            assertEquals("test message for warning", rs.getString("message"));
            assertNull(rs.getString("stack_trace"));
            assertEquals("{}", rs.getString("param"));
            assertEquals("com.wultra.core.audit.base.AuditTest", rs.getString("calling_class"));
            assertEquals("main", rs.getString("thread_name"));
        });
    }

    @Test
    public void testAuditError() {
        Timestamp timestampBeforeAudit = new Timestamp(System.currentTimeMillis() - 1);
        Audit audit = auditFactory.getAudit();
        audit.error("test message for error");
        audit.flush();
        jdbcTemplate.query("SELECT * FROM audit_log", rs -> {
            assertEquals("test-application", rs.getString("application_name"));
            assertEquals("ERROR", rs.getString("audit_level"));
            assertTrue(rs.getTimestamp("timestamp_created").after(timestampBeforeAudit));
            assertEquals("test message for error", rs.getString("message"));
            assertNull(rs.getString("stack_trace"));
            assertEquals("{}", rs.getString("param"));
            assertEquals("com.wultra.core.audit.base.AuditTest", rs.getString("calling_class"));
            assertEquals("main", rs.getString("thread_name"));
        });
    }

    @Test
    public void testAuditErrorSpecifiedLogLevel() {
        Timestamp timestampBeforeAudit = new Timestamp(System.currentTimeMillis() - 1);
        Audit audit = auditFactory.getAudit();
        audit.log("test message for error", AuditLevel.ERROR);
        audit.flush();
        jdbcTemplate.query("SELECT * FROM audit_log", rs -> {
            assertEquals("test-application", rs.getString("application_name"));
            assertEquals("ERROR", rs.getString("audit_level"));
            assertTrue(rs.getTimestamp("timestamp_created").after(timestampBeforeAudit));
            assertEquals("test message for error", rs.getString("message"));
            assertNull(rs.getString("stack_trace"));
            assertEquals("{}", rs.getString("param"));
            assertEquals("com.wultra.core.audit.base.AuditTest", rs.getString("calling_class"));
            assertEquals("main", rs.getString("thread_name"));
        });
    }

    @Test
    public void testAuditOneParam() {
        Timestamp timestampBeforeAudit = new Timestamp(System.currentTimeMillis() - 1);
        Audit audit = auditFactory.getAudit();
        audit.info("test message", AuditDetail.builder().param("my_id", "test_id").build());
        audit.flush();
        jdbcTemplate.query("SELECT * FROM audit_log al INNER JOIN audit_param ap ON al.audit_log_id=ap.audit_log_id", rs -> {
            assertNotNull(rs.getString("audit_log_id"));
            assertTrue(rs.getTimestamp("timestamp_created").after(timestampBeforeAudit));
            assertNull(rs.getString("audit_type"));
            assertEquals("my_id", rs.getString("param_key"));
            assertEquals("test_id", rs.getString("param_value"));
        });
    }

    @Test
    public void testAuditTypeAndTwoParams() {
        Audit audit = auditFactory.getAudit();
        String operationId = UUID.randomUUID().toString();
        AuditDetail detail = AuditDetail.builder()
                .type("TEST")
                .param("user_id", "test_id")
                .param("operation_id", operationId)
                .build();
        audit.info("test message", detail);
        audit.flush();
        jdbcTemplate.query("SELECT * FROM audit_log al INNER JOIN audit_param ap ON al.audit_log_id = ap.audit_log_id WHERE ap.param_key = 'user_id' AND ap.param_value = 'test_id'", rs -> {
            assertNotNull(rs.getString("audit_log_id"));
            assertEquals("TEST", rs.getString("audit_type"));
            assertEquals("user_id", rs.getString("param_key"));
            assertEquals("test_id", rs.getString("param_value"));
        });
        jdbcTemplate.query("SELECT * FROM audit_log al INNER JOIN audit_param ap ON al.audit_log_id = ap.audit_log_id WHERE ap.param_key = 'operation_id'", rs -> {
            assertNotNull(rs.getString("audit_log_id"));
            assertEquals("operation_id", rs.getString("param_key"));
            assertEquals(operationId, rs.getString("param_value"));
        });
    }

    @Test
    public void testAuditMoreParams() {
        Audit audit = auditFactory.getAudit();
        String operationId = UUID.randomUUID().toString();
        Date timestamp = new Date();
        Map<String, Object> param = new LinkedHashMap<>();
        param.put("user_id", "test_id");
        param.put("operation_id", operationId);
        param.put("sessionId", "1A532637239A03B07199A54E8D531427");
        param.put("timestamp", timestamp);
        audit.info("test message", AuditDetail.builder().params(param).build());
        audit.flush();
        jdbcTemplate.query("SELECT * FROM audit_log al INNER JOIN audit_param ap ON al.audit_log_id = ap.audit_log_id WHERE ap.param_key = 'user_id' AND ap.param_value = 'test_id'", rs -> {
            assertNotNull(rs.getString("audit_log_id"));
            assertEquals("user_id", rs.getString("param_key"));
            assertEquals("test_id", rs.getString("param_value"));
        });
        jdbcTemplate.query("SELECT * FROM audit_log al INNER JOIN audit_param ap ON al.audit_log_id = ap.audit_log_id WHERE ap.param_key = 'operation_id'", rs -> {
            assertNotNull(rs.getString("audit_log_id"));
            assertEquals(operationId, rs.getString("param_value"));
        });
        jdbcTemplate.query("SELECT * FROM audit_log al INNER JOIN audit_param ap ON al.audit_log_id = ap.audit_log_id WHERE ap.param_key = 'session_id'", rs -> {
            assertNotNull(rs.getString("audit_log_id"));
            assertEquals("1A532637239A03B07199A54E8D531427", rs.getString("param_value"));
        });
        jdbcTemplate.query("SELECT * FROM audit_log al INNER JOIN audit_param ap ON al.audit_log_id = ap.audit_log_id WHERE ap.param_key = 'timestamp'", rs -> {
            assertNotNull(rs.getString("audit_log_id"));
            assertEquals(new JsonUtil().serializeObject(timestamp), rs.getString("param_value"));
        });
    }

    @Test
    public void testAuditException() {
        Audit audit = auditFactory.getAudit();
        audit.info("test message", new Exception("test exception"));
        audit.flush();
        jdbcTemplate.query("SELECT * FROM audit_log", rs -> {
            assertEquals("test exception", rs.getString("exception_message"));
            assertTrue(rs.getString("stack_trace").contains("java.lang.Exception: test exception\n"
                    + "\tat com.wultra.core.audit.base.AuditTest.testAuditException"));
        });
    }

    @Test
    public void testAuditFormattedMessage() {
        Audit audit = auditFactory.getAudit();
        audit.info("test message with {}", "formatting");
        audit.flush();
        jdbcTemplate.query("SELECT * FROM audit_log", rs -> {
            assertEquals("test message with formatting", rs.getString("message"));
        });
    }

    @Test
    public void testAuditFormattedMessageTwoArgs() {
        Audit audit = auditFactory.getAudit();
        audit.info("test message with {} {}", "more", "formatting");
        audit.flush();
        jdbcTemplate.query("SELECT * FROM audit_log", rs -> {
            assertEquals("test message with more formatting", rs.getString("message"));
        });
    }

    @Test
    public void testAuditFormattedMessageThreeArgs() {
        Audit audit = auditFactory.getAudit();
        audit.info("test message with {} {} {}", "even", "more", "formatting");
        audit.flush();
        jdbcTemplate.query("SELECT * FROM audit_log", rs -> {
            assertEquals("test message with even more formatting", rs.getString("message"));
        });
    }

    @Test
    public void testAuditFormattedMessageException() {
        Audit audit = auditFactory.getAudit();
        audit.info("test message with {} {} and exception", "more", "formatting", new Exception("test exception"));
        audit.flush();
        jdbcTemplate.query("SELECT * FROM audit_log", rs -> {
            assertEquals("test message with more formatting and exception", rs.getString("message"));
            assertEquals("test exception", rs.getString("exception_message"));
            assertTrue(rs.getString("stack_trace").contains("java.lang.Exception: test exception\n"
                    + "\tat com.wultra.core.audit.base.AuditTest.testAuditFormattedMessageException"));
        });
    }

    @Test
    public void testAuditFormattedMessageBadArgs() {
        Audit audit = auditFactory.getAudit();
        audit.info("test message with {} {} {}", "invalid", "formatting");
        audit.flush();
        jdbcTemplate.query("SELECT * FROM audit_log", rs -> {
            assertEquals("test message with invalid formatting {}", rs.getString("message"));
        });
    }

    @Test
    public void testAuditDebug() {
        Audit audit = auditFactory.getAudit();
        audit.debug("debug message");
        audit.flush();
        jdbcTemplate.query("SELECT COUNT(*) FROM audit_log", rs -> {
            assertEquals(0, rs.getInt(1));
        });
    }

    @Test
    public void testAuditTrace() {
        Audit audit = auditFactory.getAudit();
        audit.debug("trace message");
        audit.flush();
        jdbcTemplate.query("SELECT COUNT(*) FROM audit_log", rs -> {
            assertEquals(0, rs.getInt(1));
        });
    }

}
