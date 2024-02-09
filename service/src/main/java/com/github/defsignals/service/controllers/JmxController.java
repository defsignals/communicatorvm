package com.github.defsignals.service.controllers;

import com.github.defsignals.service.service.JmxService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.management.MalformedObjectNameException;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

@RestController
@RequestMapping("/jmx")
public class JmxController {
    private final JmxService jmxService;

    public JmxController(JmxService jmxService) {
        this.jmxService = jmxService;
    }

    @GetMapping("/memory")
    public Mono<MemoryUsage> getMemoryUsage(@RequestParam String host,
                                            @RequestParam Integer port) throws MalformedObjectNameException, IOException {
        final var memoryMxBean = jmxService.getMxBean(host, port,
                MemoryMXBean.class, ManagementFactory.MEMORY_MXBEAN_NAME);

        final var heapMemoryUsage = memoryMxBean.getHeapMemoryUsage();
        final var nonHeapMemoryUsage = memoryMxBean.getNonHeapMemoryUsage();

        return Mono.just(new MemoryUsage(heapMemoryUsage.getMax(),
                heapMemoryUsage.getUsed(),
                nonHeapMemoryUsage.getMax(),
                nonHeapMemoryUsage.getUsed()));
    }

    public record MemoryUsage(Long maxHeap, Long usedHeap, Long maxNonHeap, Long usedNonHeap) {
    }
}

