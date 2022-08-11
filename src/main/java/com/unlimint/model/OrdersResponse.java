package com.unlimint.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class OrdersResponse {
	
	private Integer id;
	private Integer orderId;
	private Long amount;
	private String comment;
	private long line;
	private String filename;
	private String result;

}
