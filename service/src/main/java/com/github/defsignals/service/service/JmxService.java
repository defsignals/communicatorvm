package com.github.defsignals.service.service;

import org.springframework.stereotype.Service;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JmxService {
    public static final String URL = "service:jmx:rmi:///jndi/rmi://%s:%d/jmxrmi";

    private final ConcurrentHashMap<HostPort, JMXConnector> connectionMap = new ConcurrentHashMap<>();

    public <T> T getMxBean(String host, Integer port, Class<T> MXBeanClass,
                           String mxBeanConstantName) throws IOException, MalformedObjectNameException {

        var jmxConnector = getConnection(host, port);
        MBeanServerConnection mbs = jmxConnector.getMBeanServerConnection();

        ObjectName mxBeanName = new ObjectName(mxBeanConstantName);

        return ManagementFactory.newPlatformMXBeanProxy(mbs, mxBeanName.toString(), MXBeanClass);
    }

    public JMXConnector getConnection(String host, Integer port) {
        var hostPort = new HostPort(host, port);
        // TODO: 02/09/2024 create a scheduler for closing connections
        return connectionMap.computeIfAbsent(hostPort, this::createConnector);
    }

    private JMXConnector createConnector(HostPort hostPort) {
        try {
            return JMXConnectorFactory.connect(
                    new JMXServiceURL(String.format(URL, hostPort.host, hostPort.port)));
        } catch (IOException e) {
            throw new RuntimeException("Failed to create JMX connector for: " + hostPort, e);
        }
    }

    private record HostPort(String host, Integer port) {
    }
}
