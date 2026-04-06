package ru.checkdev.notification.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class EurekaUriProviderTest {

    @Mock
    private DiscoveryClient discoveryClient;
    @Mock
    private ServiceInstance serviceInstance;
    private EurekaUriProvider uriProvider;

    @BeforeEach
    void setUp() {
        uriProvider = new EurekaUriProvider(discoveryClient);
    }

    @Test
    void whenGetUri() throws URISyntaxException {
        List<ServiceInstance> serviceInstances = Collections.singletonList(serviceInstance);
        Mockito.when(discoveryClient.getInstances("serviceId")).thenReturn(serviceInstances);
        Mockito.when(serviceInstance.getUri()).thenReturn(new java.net.URI("https://example.com"));

        String uri = uriProvider.getUri("serviceId");

        assertThat(uri).isEqualTo("https://example.com");
    }
}
