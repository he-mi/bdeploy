package io.bdeploy.minion;

import java.util.Collections;
import java.util.Map;

import org.apache.logging.log4j.core.util.ContextDataProvider;

public class MinionLoggingContextDataProvider implements ContextDataProvider {

    private static final String LOG_DIR_KEY = "TARGET_LOG_DIR";
    private static final String LOG_NAME_KEY = "TARGET_LOG_FILE";
    private static final String LOG_NAME = "server";
    private static String logDir = null;

    @Override
    public Map<String, String> supplyContextData() {
        if (logDir != null) {
            return Map.of(LOG_DIR_KEY, logDir, LOG_NAME_KEY, LOG_NAME);
        }
        return Collections.emptyMap();
    }

    public static void setLogDir(String dir) {
        logDir = dir;
    }

}
