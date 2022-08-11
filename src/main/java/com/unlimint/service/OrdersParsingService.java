package com.unlimint.service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.unlimint.exception.ServiceException;
import com.unlimint.model.Orders;

@Service
public class OrdersParsingService {
	
	ObjectMapper mapper = new ObjectMapper();

	public Boolean ordersProcessing(List<String> fileNames, ArrayNode arrayNode) {
		CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
			try {	
				
				
			csvFileReader(fileNames.get(0),arrayNode);
			} catch (Exception e) {
			e.printStackTrace();
			}
		});

		CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> {
			try {		
			
				String fileName=fileNames.size()==2?fileNames.get(1):null;
				readJsonFile(fileName,arrayNode);
			} catch (Exception e) {

				e.printStackTrace();
			}
		});

		CompletableFuture<Void> future = CompletableFuture.allOf(future1, future2);
		try {
			future.get(); // this line waits for all to be completed
		} catch (Exception e) {
			Thread.currentThread().interrupt();
		}
		return true;
	}
	
	
	private void csvFileReader(String csvFileName, ArrayNode arrayNode) throws ServiceException {
		List<Orders> ordersList = new ArrayList<>();
		InputStream inputStream = null;
		try {
			
			if(StringUtils.isEmpty(csvFileName)||!StringUtils.equalsIgnoreCase(csvFileName, "orders1.csv")) {
				throw new ServiceException("File not found with given name!!"+csvFileName) ;
			}
			
			ClassPathResource cpr = new ClassPathResource(csvFileName);
			  byte[] bdata = FileCopyUtils.copyToByteArray(cpr.getInputStream());
			    inputStream=  new ByteArrayInputStream(bdata);
			    
			    BufferedReader fileReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
			    CSVParser csvParser = new CSVParser(fileReader,CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());
				Iterable<CSVRecord> csvRecords = csvParser.getRecords();
				for (CSVRecord csvRecord : csvRecords) {
					Orders orders = new Orders(Integer.parseInt(csvRecord.get("Order ID")),
							Integer.parseInt(csvRecord.get("Order ID")), Long.parseLong(csvRecord.get("amount")),
							csvRecord.get("comment"), csvRecord.getRecordNumber());
					ordersList.add(orders);
				}
				
				csvParser.close();
				
				arrayNode.addAll(converListToJson(ordersList, csvFileName));
				
		} catch (FileNotFoundException e) {
			throw new ServiceException("File not found: " + e.getMessage());
		} catch (Exception ex) {
			throw new ServiceException("Exception occured while reading csv data: " + ex.getMessage());

		}


	}

	private ArrayNode converListToJson(List<Orders> ordersList, String fileName) throws ServiceException {
		String csvJsonData;
		ArrayNode nodeArray;
		try {
			csvJsonData = mapper.writeValueAsString(ordersList);
			nodeArray = (ArrayNode) mapper.readTree(csvJsonData);
			for (JsonNode node : nodeArray) {
				ObjectNode objNode = (ObjectNode) node;
				objNode.put("fileName", fileName);
				objNode.put("result", "OK");
			}
			return nodeArray;
		} catch (IOException e) {
			throw new ServiceException("Fail to parse CSV data in json: " + e.getMessage());
		}

	}
	
	public void readJsonFile(String fileName, ArrayNode arrayNode) throws ServiceException{

		ArrayNode nodeArray = mapper.createArrayNode();
		try {
			
			if(StringUtils.isEmpty(fileName)|| !(StringUtils.equalsIgnoreCase(fileName, "orders2.json"))) {
				
				throw new ServiceException("File not found with given name!!"+fileName) ;
			}

			ClassPathResource cpr = new ClassPathResource(fileName);
			String data = "";
			byte[] bdata = FileCopyUtils.copyToByteArray(cpr.getInputStream());
			data = new String(bdata, StandardCharsets.UTF_8);
			nodeArray = (ArrayNode) new ObjectMapper().readTree(data);
			int line = 0;
			for (JsonNode node : nodeArray) {
				ObjectNode objectName = (ObjectNode) node;
				objectName.remove("currency");
				objectName.set("id", objectName.get("orderId"));
				objectName.put("filename", fileName);
				objectName.put("line", ++line);
				objectName.put("result", "OK");
			}

			arrayNode.addAll(nodeArray);

		} catch (JsonParseException jps) {
			throw new ServiceException("Json parse exception get occured while parsing  order json file!!"+jps.getMessage()) ;
		} catch (IOException e) {
			throw new ServiceException(" File not found excetion getting from json order file process!!"+e.getMessage()) ;
		} catch (Exception e) {
			throw new ServiceException("Excetion occured when json order file processing!!"+e.getMessage()) ;
		}
	}
}
