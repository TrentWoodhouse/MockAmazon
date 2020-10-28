package com.congo.rest;

import com.google.gson.Gson;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;

@RestController
public class DeliveryReportController {

	private static ArrayList<DeliveryReport> deliveryReports;
	private File deliveryReportFile = new File("./storage/deliveryReports.txt");

	@GetMapping("/deliveryReport")
	public ArrayList<DeliveryReport> getDeliveryReport(@RequestParam Map<String, String> input) {

		//load the file into memory if it isn't already
		if (deliveryReports == null) {
			if (deliveryReportFile.exists()) {
				scanJsonFile();
			} else {
				return null;
			}
		}

		ArrayList<DeliveryReport> deliveryReportList = new ArrayList<DeliveryReport>();
		//find the specified deliveryReport (or all)
		if(input.containsKey("all")) return deliveryReports;
		if(input.containsKey("allUnread")){
			ArrayList<DeliveryReport> unreadReports = new ArrayList<>();
			for(DeliveryReport d : deliveryReports){
				if(d.appealed == false) unreadReports.add(d);
			}
		}
		for(DeliveryReport l : deliveryReports){
			if((input.containsKey("id") && l.id == Long.parseLong(input.get("id"))) || (input.containsKey("seller") && l.seller == Long.parseLong(input.get("seller")) && l.appealed == false && l.buyerMessage == null) || (input.containsKey("buyer") && l.buyer == Long.parseLong(input.get("buyer")) && l.appealed == true)){
				deliveryReportList.add(l);
			}
		}

		return deliveryReportList;
	}

	@PostMapping("/deliveryReport")
	public String postDeliveryReport(@RequestBody DeliveryReport deliveryReport) {

		if (deliveryReports == null) {
			scanJsonFile();
			if (deliveryReports == null) deliveryReports = new ArrayList<>();
		}
		for(DeliveryReport l : deliveryReports) {
			if (l.id == deliveryReport.id) {
				return "Failed to send Delivery Report (incorrect ID)";
			}
		}

		deliveryReport.id = deliveryReports.size()+1;
		deliveryReports.add(deliveryReport);

		//attempt to add the new Json to a file
		try {
			(new File("./storage")).mkdir();
			if(!deliveryReportFile.exists()) deliveryReportFile.createNewFile();
			FileWriter writer = new FileWriter(deliveryReportFile);
			writer.write(new Gson().toJson(deliveryReports));
			writer.close();

			return "Successfully Sent Delivery Report";
		} catch (Exception e){
			System.out.println(e);
		}

		return "Failed to Send Delivery Report";
	}

	private void scanJsonFile(){
		try{
			//scan the file for the Json
			Scanner scan = new Scanner(deliveryReportFile);
			String line = "";
			while (scan.hasNextLine()){
				line = scan.nextLine();
			}

			//parse the Json to the class
			Gson gson = new Gson();
			DeliveryReport[] deliveryReportsArr = gson.fromJson(line, DeliveryReport[].class);
			deliveryReports = new ArrayList<>();
			deliveryReports.addAll(Arrays.asList(deliveryReportsArr));
		} catch(FileNotFoundException e){
			System.out.println(e);
		}
	}

	@PatchMapping("/deliveryReport")
	public String updateDeliveryReport(@RequestBody DeliveryReport deliveryReport) {

		if (deliveryReports == null) {
			scanJsonFile();
		}
		if(deliveryReports != null) {
			for (int i=0 ; i<deliveryReports.size() ; i++) {
				DeliveryReport u = deliveryReports.get(i);
				if (u.id == deliveryReport.id) {
					deliveryReports.set(i, deliveryReport);
				}
			}
		}

		try {
			(new File("./storage")).mkdir();
			if(!deliveryReportFile.exists()) deliveryReportFile.createNewFile();
			FileWriter writer = new FileWriter(deliveryReportFile);
			writer.write(new Gson().toJson(deliveryReports));
			writer.close();

			return "Successfully Updated DeliveryReport";
		} catch (Exception e){
			System.out.println(e);
		}

		return "Failed to Update DeliveryReport";
	}

	@DeleteMapping("/deliveryReport")
	public DeliveryReport deleteDeliveryReport(@RequestParam Map<String, String> input) {

		if (deliveryReports == null) {
			if (deliveryReportFile.exists()) {
				scanJsonFile();
			} else {
				return null;
			}
		}

		//find the specified deliveryReport (or all)
		for(DeliveryReport d : deliveryReports){
			if(input.containsKey("id") && d.id == Long.parseLong(input.get("id"))){
				deliveryReports.remove(d);
			}
		}

		return null;
	}
}