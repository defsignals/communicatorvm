package com.github.defsignals.service.service;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import org.springframework.stereotype.Service;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Optional;

@Service
public class JmxServiceImpl implements JmxService {

    public static final String URL = "service:jmx:rmi:///jndi/rmi://%s:%d/jmxrmi";


    @Override
    public JMXServiceURL getUrl(Optional<String> host, Optional<Integer> port) {
        JMXServiceURL url;
        try {
            if (host.isPresent() || port.isPresent()) {
                if (host.isEmpty() || port.isEmpty()) {
                    throw new IllegalArgumentException("Host and port must both be provided");
                }
                url = new JMXServiceURL(String.format(URL, host.get(), port.get()));
            } else {
                final VirtualMachineDescriptor vmd = getVMDescriptor();
                if (vmd == null) {
                    throw new RuntimeException("No external JVM could be found to connect to");
                }
                url = new JMXServiceURL(VirtualMachine.attach(vmd).startLocalManagementAgent());
            }
        } catch (IOException | AttachNotSupportedException e) {
            throw new RuntimeException(e.getMessage());
        }
        return url;
    }

    public <T> T getMxBean(Optional<String> host, Optional<Integer> port, Class<T> MXBeanClass) {
        JMXServiceURL serviceUrl = getUrl(host, port);
        try {
            JMXConnector jmxConnector = JMXConnectorFactory.connect(serviceUrl, null);
            // TODO: 02/04/2024 find out why when using try with a resource we cannot use mxbean -_-
            MBeanServerConnection mbs = jmxConnector.getMBeanServerConnection();
            ObjectName mxBeanName = new ObjectName(ManagementFactory.MEMORY_MXBEAN_NAME);
            return ManagementFactory.newPlatformMXBeanProxy(mbs, mxBeanName.toString(), MXBeanClass);
        } catch (MalformedObjectNameException | IOException e) {
            throw new RuntimeException("Failed to get MXBean " + MXBeanClass.getSimpleName(), e);
        }
    }


    private VirtualMachineDescriptor getVMDescriptor() {
        final long currentPid = ProcessHandle.current().pid();

        List<VirtualMachineDescriptor> list = VirtualMachine.list();
        for (VirtualMachineDescriptor vmd : list) {
            if (!vmd.id().equals(Long.toString(currentPid))) {
                return vmd;
            }
        }
        return null;
    }

}
