package dev.noby.administrator.config;

import java.util.regex.Pattern;

public final class AdminResourceNames {

    public static final String API_ROOT = "/api/v1/rabbit";
    public static final String DEMO_PREFIX = "demo.";
    public static final Pattern RESOURCE = Pattern.compile("demo\\.[A-Za-z0-9_-]+(?:\\.[A-Za-z0-9_-]+)*");
    public static final Pattern ROUTING_KEY = Pattern.compile(
            "demo\\.(?:[A-Za-z0-9_-]+|\\*|#)(?:\\.(?:[A-Za-z0-9_-]+|\\*|#))*");

    private AdminResourceNames() {
    }
}
