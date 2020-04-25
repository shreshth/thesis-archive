package com.iw.db;

public class AppDataPoint {
    private String appName; // app name
    private long uid; // app uid

    private long tcpRxBytes; // bytes received over TCP
    private long tcpTxBytes; // bytes sent over TCP

    private long udpRxBytes; // bytes received over UDP
    private long udpTxBytes; // bytes sent over UDP

    private long timestamp; // timestamp of observation

    public AppDataPoint(String appName, long uid, long tcpRxBytes,
                    long tcpTxBytes, long udpRxBytes, long udpTxBytes) {
        this.appName = appName;
        this.uid = uid;
        this.tcpRxBytes = tcpRxBytes;
        this.tcpTxBytes = tcpTxBytes;
        this.udpRxBytes = udpRxBytes;
        this.udpTxBytes = udpTxBytes;

        this.timestamp = System.currentTimeMillis();
    }

    // accessors
    public String getAppName() {
        return this.appName;
    }

    public long getUID() {
        return this.uid;
    }

    public long getTcpRxBytes() {
        return this.tcpRxBytes;
    }

    public long getTcpTxBytes() {
        return this.tcpTxBytes;
    }

    public long getUdpRxBytes() {
        return this.udpRxBytes;
    }

    public long getUdpTxBytes() {
        return this.udpTxBytes;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

}
