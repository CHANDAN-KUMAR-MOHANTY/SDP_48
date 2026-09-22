package soa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ContractorserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ContractorserviceApplication.class, args);
	}

}
