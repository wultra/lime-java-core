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

import com.wultra.core.audit.base.model.AuditLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.sql.Timestamp;
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
    public void testAuditGenericParam() {
        Audit audit = auditFactory.getAudit();
        Map<String, Object> param = new LinkedHashMap<>();
        param.put("my_id", "test_id");
        audit.info("test message", param);
        audit.flush();
        jdbcTemplate.query("SELECT * FROM audit_log", rs -> {
            assertEquals("{\"my_id\":\"test_id\"}", rs.getString("param"));
        });
    }

    @Test
    public void testAuditKnownParam() {
        Audit audit = auditFactory.getAudit();
        Map<String, Object> param = new LinkedHashMap<>();
        String operationId = UUID.randomUUID().toString();
        param.put("user_id", "test_id");
        param.put("operation_id", operationId);
        audit.info("test message", param);
        audit.flush();
        jdbcTemplate.query("SELECT * FROM audit_log", rs -> {
            assertEquals("{\"user_id\":\"test_id\",\"operation_id\":\"" + operationId + "\"}", rs.getString("param"));
            assertEquals("test_id", rs.getString("param_user_id"));
            assertEquals(operationId, rs.getString("param_operation_id"));
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
