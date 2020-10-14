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
public class SellerController {

	private static ArrayList<Seller> sellers;
	private File sellerFile = new File("./storage/sellers.txt");

	@GetMapping("/seller")
	public Seller getUser(@RequestParam(value = "id", defaultValue = "") String id) {

		if (sellers == null) {
			if (sellerFile.exists()) {
				scanJsonFile();
			} else {
				return null;
			}
		}

		//find the specified seller (or all)
		for(Seller s : sellers){
			if(s.id == Long.parseLong(id)){
				return s;
			}
		}

		return null;
	}

	@PostMapping("/seller")
	public String postUser(@RequestBody Seller seller) {

		if (sellers == null) {
			scanJsonFile();
			if (sellers == null) sellers = new ArrayList<>();
		}
		for(Seller s : sellers) {
			if (s.id == seller.id) {
				return "Failed to send Seller (incorrect ID)";
			}
		}

		sellers.add(seller);

		//attempt to add the new Json to a file
		try {
			(new File("./storage")).mkdir();
			if(!sellerFile.exists()) sellerFile.createNewFile();
			FileWriter writer = new FileWriter(sellerFile);
			writer.write(new Gson().toJson(sellers));
			writer.close();

			return "Successfully Sent Seller";
		} catch (Exception e){
			System.out.println(e);
		}

		return "Failed to Send Seller";
	}

	private void scanJsonFile(){
		try{
			//scan the file for the Json
			Scanner scan = new Scanner(sellerFile);
			String line = "";
			while (scan.hasNextLine()){
				line = scan.nextLine();
			}

			//parse the Json to the class
			Gson gson = new Gson();
			Seller[] sellersArr = gson.fromJson(line, Seller[].class);
			sellers = new ArrayList<>();
			sellers.addAll(Arrays.asList(sellersArr));
		} catch(FileNotFoundException e){
			System.out.println(e);
		}
	}
}