package com.github.defsignals.service.service;

import javax.management.remote.JMXServiceURL;
import java.util.Optional;

public interface JmxService {
    JMXServiceURL getUrl(Optional<String> host, Optional<Integer> port);
    <T> T getMxBean(Optional<String> host, Optional<Integer> port, Class<T> MXBeanClass);
}