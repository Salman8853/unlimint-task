package com.unlimint.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Orders {

	private Integer id;
	private Integer orderId;
	private Long amount;
	private String comment;
	private long line;

}
