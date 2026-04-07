package com.sankalp.orderservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.cloud.discovery.enabled=false",
		"spring.cloud.service-registry.auto-registration.enabled=false",
		"spring.cloud.config.enabled=false",
		"eureka.client.enabled=false",
		"spring.task.scheduling.enabled=false",
		"order.evaluator.fixed-delay-ms=600000",
		"order.outbox.fixed-delay-ms=600000"
})
class OrderServiceApplicationTests {

	@Test
	void contextLoads() {
	}
}