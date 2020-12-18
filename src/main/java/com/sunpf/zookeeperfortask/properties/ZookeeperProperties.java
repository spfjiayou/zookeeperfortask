package com.sunpf.zookeeperfortask.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "zookeeper")
@Component
public class ZookeeperProperties {

    private String server;

    private int sessionTimeoutMs;

    private int connectionTimeoutMs;

    private int maxRetries;

    private int baseSleepTimeMs;

    private String rootPath;

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public void setSessionTimeoutMs(int sessionTimeoutMs) {
        this.sessionTimeoutMs = sessionTimeoutMs;
    }

    public void setConnectionTimeoutMs(int connectionTimeoutMs) {
        this.connectionTimeoutMs = connectionTimeoutMs;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public void setBaseSleepTimeMs(int baseSleepTimeMs) {
        this.baseSleepTimeMs = baseSleepTimeMs;
    }

    public String getServer() {
        return server;
    }

    public int getSessionTimeoutMs() {
        return sessionTimeoutMs;
    }

    public int getConnectionTimeoutMs() {
        return connectionTimeoutMs;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public int getBaseSleepTimeMs() {
        return baseSleepTimeMs;
    }
}
