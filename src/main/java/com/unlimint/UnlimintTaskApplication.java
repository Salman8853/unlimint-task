package com.unlimint;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.unlimint.model.OrdersResponse;
import com.unlimint.service.OrdersParsingService;

@SpringBootApplication
public class UnlimintTaskApplication implements ApplicationRunner {

	public static void main(String[] args) {
		SpringApplication.run(UnlimintTaskApplication.class, args);
	}

	@Autowired
	private OrdersParsingService ordersParsingService;

	private ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
			false);

	@Override
	public void run(ApplicationArguments args) throws Exception {

		List<String> csvFileNames = args.getNonOptionArgs();
		if (CollectionUtils.isEmpty(csvFileNames)) {
			return;
		}
		ArrayNode arrayNode = mapper.createArrayNode();

		Boolean isCompleted = false;
		while (true) {
			isCompleted = ordersParsingService.ordersProcessing(csvFileNames, arrayNode);
			List<OrdersResponse> lorderResponseList = mapper.readValue(mapper.writeValueAsString(arrayNode),
					new TypeReference<List<OrdersResponse>>() {
			});
			List<OrdersResponse> sortedList = lorderResponseList.stream()
					.sorted(Comparator.comparingInt(OrdersResponse::getId)).collect(Collectors.toList());

			String json = mapper.writeValueAsString(sortedList);
			System.out.println(json);

			try {
				int sleepTime = 20;
				TimeUnit.SECONDS.sleep(sleepTime);
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
			}
			if (isCompleted) {
				break;
			}
		}
	}
}
