package com.congo.rest.Controllers;

import com.congo.rest.Classes.Listing;
import com.google.gson.Gson;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
public class ListingController {

	private static ArrayList<Listing> listings;
	private File listingFile = new File("./storage/listings.txt");

	@GetMapping("/listing")
	public ArrayList<Listing> getListing(@RequestParam Map<String, String> input) {

		//load the file into memory if it isn't already
		if (listings == null) {
			if (listingFile.exists()) {
				scanJsonFile();
			} else {
				return null;
			}
		}

		ArrayList<Listing> listingList = new ArrayList<Listing>();
		//find the specified listing (or all)
		if(input.containsKey("all")) return listings;
		for(Listing l : listings){
			if((input.containsKey("id") && l.id == Long.parseLong(input.get("id"))) || (input.containsKey("name") && l.name.equals(input.get("name"))) || (input.containsKey("seller") && l.seller == Long.parseLong(input.get("seller")))){
				listingList.add(l);
			}
		}

		return listingList;
	}

	@PostMapping("/listing")
	public String postListing(@RequestBody Listing listing) {

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
		listing.salePercentage = 1;
		listing.views = 0;

		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		listing.dateCreated = sdf.format(new Date());
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

	@PatchMapping("/listing")
	public String updateListing(@RequestBody Listing listing) {

		if (listings == null) {
			scanJsonFile();
		}
		if(listings != null) {
			for (int i=0 ; i<listings.size() ; i++) {
				Listing u = listings.get(i);
				if (u.id == listing.id || u.name.equals(listing.name)) {
					listings.set(i, listing);
				}
			}
		}

		try {
			(new File("./storage")).mkdir();
			if(!listingFile.exists()) listingFile.createNewFile();
			FileWriter writer = new FileWriter(listingFile);
			writer.write(new Gson().toJson(listings));
			writer.close();

			return "Successfully Updated Listing";
		} catch (Exception e){
			System.out.println(e);
		}

		return "Failed to Update Listing";
	}

	@DeleteMapping("/listing")
	public Listing deleteListing(@RequestParam Map<String, String> input) {

		if (listings == null) {
			if (listingFile.exists()) {
				scanJsonFile();
			} else {
				return null;
			}
		}

		//find the specified listing (or all)
		listings.removeIf(u -> (input.containsKey("id") && u.id == Long.parseLong(input.get("id"))) || (input.containsKey("name") && u.name.equals(input.get("name"))));

		try {
			(new File("./storage")).mkdir();
			if(!listingFile.exists()) listingFile.createNewFile();
			FileWriter writer = new FileWriter(listingFile);
			writer.write(new Gson().toJson(listings));
			writer.close();

		} catch (Exception e){
			System.out.println(e);
		}

		return null;
	}
}
