package com.congo.rest;

import com.google.gson.Gson;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

@RestController
public class OrderController {

	private static ArrayList<Order> orders;
	private File orderFile = new File("./storage/orders.txt");

	@GetMapping("/order")
	public Order getUser(@RequestParam(value = "id", defaultValue = "") String id) {

		if (orders == null) {
			if (orderFile.exists()) {
				scanJsonFile();
			} else {
				return null;
			}
		}

		//find the specified order (or all)
		for(Order o : orders){
			if(o.id == Long.parseLong(id)){
				return o;
			}
		}

		return null;
	}

	@PostMapping("/order")
	public String postUser(@RequestBody Order order) {

		if (orders == null) {
			scanJsonFile();
			if (orders == null) orders = new ArrayList<>();
		}
		for(Order o : orders) {
			if (o.id == order.id) {
				return "Failed to send Order (incorrect ID)";
			}
		}

		orders.add(order);

		//attempt to add the new Json to a file
		try {
			(new File("./storage")).mkdir();
			if(!orderFile.exists()) orderFile.createNewFile();
			FileWriter writer = new FileWriter(orderFile);
			writer.write(new Gson().toJson(orders));
			writer.close();

			return "Successfully Sent Order";
		} catch (Exception e){
			System.out.println(e);
		}

		return "Failed to Send Order";
	}

	private void scanJsonFile(){
		try{
			//scan the file for the Json
			Scanner scan = new Scanner(orderFile);
			String line = "";
			while (scan.hasNextLine()){
				line = scan.nextLine();
			}

			//parse the Json to the class
			Gson gson = new Gson();
			Order[] ordersArr = gson.fromJson(line, Order[].class);
			orders = new ArrayList<>();
			orders.addAll(Arrays.asList(ordersArr));
		} catch(FileNotFoundException e){
			System.out.println(e);
		}
	}
}