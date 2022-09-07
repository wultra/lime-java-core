package com.wultra.core.audit.base.util;

import java.sql.Clob;

public class TestUtil {
    public static String clobToString(Clob clobObject) {
        try {
            return clobObject.getSubString(1, Math.toIntExact(clobObject.length()));
        } catch (Exception ex) {
            return null;
        }
    }
}
