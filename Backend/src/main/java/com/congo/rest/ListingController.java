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
public class ListingController {

	private static ArrayList<Listing> listings;
	private File listingFile = new File("./storage/listings.txt");

	@GetMapping("/listing")
	public Listing getUser(@RequestParam(value = "id", defaultValue = "") String id) {

		if (listings == null) {
			if (listingFile.exists()) {
				scanJsonFile();
			} else {
				return null;
			}
		}

		//find the specified listing (or all)
		for(Listing l : listings){
			if(l.id == Long.parseLong(id)){
				return l;
			}
		}

		return null;
	}

	@PostMapping("/listing")
	public String postUser(@RequestBody Listing listing) {

		if (listings == null) {
			scanJsonFile();
			if (listings == null) listings = new ArrayList<>();
		}
		for(Listing l : listings) {
			if (l.id == listing.id) {
				return "Failed to send Listing (incorrect ID)";
			}
		}

		listing.id = listings.size()+1;
		listings.add(listing);

		//attempt to add the new Json to a file
		try {
			(new File("./storage")).mkdir();
			if(!listingFile.exists()) listingFile.createNewFile();
			FileWriter writer = new FileWriter(listingFile);
			writer.write(new Gson().toJson(listings));
			writer.close();

			return "Successfully Sent Listing";
		} catch (Exception e){
			System.out.println(e);
		}

		return "Failed to Send Listing";
	}

	private void scanJsonFile(){
		try{
			//scan the file for the Json
			Scanner scan = new Scanner(listingFile);
			String line = "";
			while (scan.hasNextLine()){
				line = scan.nextLine();
			}

			//parse the Json to the class
			Gson gson = new Gson();
			Listing[] listingsArr = gson.fromJson(line, Listing[].class);
			listings = new ArrayList<>();
			listings.addAll(Arrays.asList(listingsArr));
		} catch(FileNotFoundException e){
			System.out.println(e);
		}
	}
}
