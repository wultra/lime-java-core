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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.test.context.jdbc.Sql;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TestApplication.class, properties = {"audit.db.table.param.enabled=false"})
@Sql(scripts = "/db_schema.sql")
class AuditTest {

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
    void testAuditInfo() {
        Timestamp timestampBeforeAudit = new Timestamp(System.currentTimeMillis() - 1);
        Audit audit = auditFactory.getAudit();
        audit.info("test message");
        audit.flush();
        final SqlRowSet rs = jdbcTemplate.queryForRowSet("SELECT * FROM audit_log");
        assertTrue(rs.next());
        assertEquals("test-application", rs.getString("application_name"));
        assertEquals("INFO", rs.getString("audit_level"));
        assertTrue(rs.getTimestamp("timestamp_created").after(timestampBeforeAudit));
        assertEquals("test message", rs.getObject("message"));
        assertNull(rs.getString("stack_trace"));
        assertEquals("{}", rs.getObject("param"));
        assertEquals("com.wultra.core.audit.base.AuditTest", rs.getString("calling_class"));
        assertEquals("main", rs.getString("thread_name"));
    }

    @Test
    void testAuditWarn() {
        Timestamp timestampBeforeAudit = new Timestamp(System.currentTimeMillis() - 1);
        Audit audit = auditFactory.getAudit();
        audit.warn("test message for warning");
        audit.flush();
        final SqlRowSet rs = jdbcTemplate.queryForRowSet("SELECT * FROM audit_log");
        assertTrue(rs.next());
        assertEquals("test-application", rs.getString("application_name"));
        assertEquals("WARN", rs.getString("audit_level"));
        assertTrue(rs.getTimestamp("timestamp_created").after(timestampBeforeAudit));
        assertEquals("test message for warning", rs.getObject("message"));
        assertNull(rs.getString("stack_trace"));
        assertEquals("{}", rs.getObject("param"));
        assertEquals("com.wultra.core.audit.base.AuditTest", rs.getString("calling_class"));
        assertEquals("main", rs.getString("thread_name"));
    }

    @Test
    void testAuditError() {
        Timestamp timestampBeforeAudit = new Timestamp(System.currentTimeMillis() - 1);
        Audit audit = auditFactory.getAudit();
        audit.error("test message for error");
        audit.flush();
        final SqlRowSet rs = jdbcTemplate.queryForRowSet("SELECT * FROM audit_log");
        assertTrue(rs.next());
        assertEquals("test-application", rs.getString("application_name"));
        assertEquals("ERROR", rs.getString("audit_level"));
        assertTrue(rs.getTimestamp("timestamp_created").after(timestampBeforeAudit));
        assertEquals("test message for error", rs.getObject("message"));
        assertNull(rs.getString("stack_trace"));
        assertEquals("{}", rs.getObject("param"));
        assertEquals("com.wultra.core.audit.base.AuditTest", rs.getString("calling_class"));
        assertEquals("main", rs.getString("thread_name"));
    }

    @Test
    void testAuditErrorSpecifiedLogLevel() {
        Timestamp timestampBeforeAudit = new Timestamp(System.currentTimeMillis() - 1);
        Audit audit = auditFactory.getAudit();
        audit.log("test message for error", AuditLevel.ERROR);
        audit.flush();
        final SqlRowSet rs = jdbcTemplate.queryForRowSet("SELECT * FROM audit_log");
        assertTrue(rs.next());
        assertEquals("test-application", rs.getString("application_name"));
        assertEquals("ERROR", rs.getString("audit_level"));
        assertTrue(rs.getTimestamp("timestamp_created").after(timestampBeforeAudit));
        assertEquals("test message for error", rs.getObject("message"));
        assertNull(rs.getString("stack_trace"));
        assertEquals("{}", rs.getObject("param"));
        assertEquals("com.wultra.core.audit.base.AuditTest", rs.getString("calling_class"));
        assertEquals("main", rs.getString("thread_name"));
    }

    @Test
    void testAuditParamDisabled() {
        Timestamp timestampBeforeAudit = new Timestamp(System.currentTimeMillis() - 1);
        Audit audit = auditFactory.getAudit();
        audit.info("test message", AuditDetail.builder().param("my_id", "test_id").build());
        audit.flush();
        final SqlRowSet rs = jdbcTemplate.queryForRowSet("SELECT * FROM audit_log al LEFT JOIN audit_param ap ON al.audit_log_id=ap.audit_log_id");
        assertTrue(rs.next());
        assertNotNull(rs.getString("audit_log_id"));
        assertTrue(rs.getTimestamp("timestamp_created").after(timestampBeforeAudit));
        assertNull(rs.getString("audit_type"));
        assertNull(rs.getString("param_key"));
        assertNull(rs.getString("param_value"));
    }

    @Test
    void testAuditException() {
        Audit audit = auditFactory.getAudit();
        audit.info("test message", new Exception("test exception"));
        audit.flush();
        final SqlRowSet rs = jdbcTemplate.queryForRowSet("SELECT * FROM audit_log");
        assertTrue(rs.next());
        assertEquals("test exception", rs.getObject("exception_message"));
        assertTrue(rs.getObject("stack_trace").toString().contains("java.lang.Exception: test exception\n"
                + "\tat com.wultra.core.audit.base.AuditTest.testAuditException"));
    }

    @Test
    void testAuditFormattedMessage() {
        Audit audit = auditFactory.getAudit();
        audit.info("test message with {}", "formatting");
        audit.flush();
        final SqlRowSet rs = jdbcTemplate.queryForRowSet("SELECT * FROM audit_log");
        assertTrue(rs.next());
        assertEquals("test message with formatting", rs.getObject("message"));
    }

    @Test
    void testAuditFormattedMessageTwoArgs() {
        Audit audit = auditFactory.getAudit();
        audit.info("test message with {} {}", "more", "formatting");
        audit.flush();
        final SqlRowSet rs = jdbcTemplate.queryForRowSet("SELECT * FROM audit_log");
        assertTrue(rs.next());
        assertEquals("test message with more formatting", rs.getObject("message"));
    }

    @Test
    void testAuditFormattedMessageThreeArgs() {
        Audit audit = auditFactory.getAudit();
        audit.info("test message with {} {} {}", "even", "more", "formatting");
        audit.flush();
        final SqlRowSet rs = jdbcTemplate.queryForRowSet("SELECT * FROM audit_log");
        assertTrue(rs.next());
        assertEquals("test message with even more formatting", rs.getObject("message"));
    }

    @Test
    void testAuditFormattedMessageException() {
        Audit audit = auditFactory.getAudit();
        audit.info("test message with {} {} and exception", "more", "formatting", new Exception("test exception"));
        audit.flush();
        final SqlRowSet rs = jdbcTemplate.queryForRowSet("SELECT * FROM audit_log");
        assertTrue(rs.next());
        assertEquals("test message with more formatting and exception", rs.getObject("message"));
        assertEquals("test exception", rs.getObject("exception_message"));
        assertTrue(rs.getObject("stack_trace").toString().contains("java.lang.Exception: test exception\n"
                + "\tat com.wultra.core.audit.base.AuditTest.testAuditFormattedMessageException"));
    }

    @Test
    void testAuditFormattedMessageBadArgs() {
        Audit audit = auditFactory.getAudit();
        audit.info("test message with {} {} {}", "invalid", "formatting");
        audit.flush();
        final SqlRowSet rs = jdbcTemplate.queryForRowSet("SELECT * FROM audit_log");
        assertTrue(rs.next());
        assertEquals("test message with invalid formatting {}", rs.getObject("message"));
    }

    @Test
    void testAuditDebug() {
        Audit audit = auditFactory.getAudit();
        audit.debug("debug message");
        audit.flush();
        final SqlRowSet rs = jdbcTemplate.queryForRowSet("SELECT COUNT(*) FROM audit_log");
        assertTrue(rs.next());
        assertEquals(0, rs.getInt(1));
    }

    @Test
    void testAuditTrace() {
        Audit audit = auditFactory.getAudit();
        audit.debug("trace message");
        audit.flush();
        final SqlRowSet rs = jdbcTemplate.queryForRowSet("SELECT COUNT(*) FROM audit_log");
        assertTrue(rs.next());
        assertEquals(0, rs.getInt(1));
    }
}
