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
public class BuyerController {

	private static ArrayList<Buyer> buyers;
	private File buyerFile = new File("./storage/buyers.txt");

	@GetMapping("/buyer")
	public Buyer getUser(@RequestParam(value = "id", defaultValue = "") String id) {

		if (buyers == null) {
			if (buyerFile.exists()) {
				scanJsonFile();
			} else {
				return null;
			}
		}

		//find the specified buyer (or all)
		for(Buyer b : buyers){
			if(b.id == Long.parseLong(id)){
				return b;
			}
		}

		return null;
	}

	@PostMapping("/buyer")
	public String postUser(@RequestBody Buyer buyer) {

		if (buyers == null) {
			scanJsonFile();
			if (buyers == null) buyers = new ArrayList<>();
		}
		for(Buyer b : buyers) {
			if (b.id == buyer.id) {
				return "Failed to send Buyer (incorrect ID)";
			}
		}

		buyers.add(buyer);

		//attempt to add the new Json to a file
		try {
			(new File("./storage")).mkdir();
			if(!buyerFile.exists()) buyerFile.createNewFile();
			FileWriter writer = new FileWriter(buyerFile);
			writer.write(new Gson().toJson(buyers));
			writer.close();

			return "Successfully Sent Buyer";
		} catch (Exception e){
			System.out.println(e);
		}

		return "Failed to Send Buyer";
	}

	private void scanJsonFile(){
		try{
			//scan the file for the Json
			Scanner scan = new Scanner(buyerFile);
			String line = "";
			while (scan.hasNextLine()){
				line = scan.nextLine();
			}

			//parse the Json to the class
			Gson gson = new Gson();
			Buyer[] buyersArr = gson.fromJson(line, Buyer[].class);
			buyers = new ArrayList<>();
			buyers.addAll(Arrays.asList(buyersArr));
		} catch(FileNotFoundException e){
			System.out.println(e);
		}
	}
}