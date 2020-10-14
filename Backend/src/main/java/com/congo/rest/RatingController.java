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
public class RatingController {

	private static ArrayList<Rating> ratings;
	private File ratingFile = new File("./storage/ratings.txt");

	@GetMapping("/rating")
	public Rating getUser(@RequestParam(value = "id", defaultValue = "") String id) {

		if (ratings == null) {
			if (ratingFile.exists()) {
				scanJsonFile();
			} else {
				return null;
			}
		}

		//find the specified rating (or all)
		for(Rating r : ratings){
			if(r.id == Long.parseLong(id)){
				return r;
			}
		}

		return null;
	}

	@PostMapping("/rating")
	public String postUser(@RequestBody Rating rating) {

		if (ratings == null) {
			scanJsonFile();
			if (ratings == null) ratings = new ArrayList<>();
		}
		for(Rating r : ratings) {
			if (r.id == rating.id) {
				return "Failed to send Rating (incorrect ID)";
			}
		}

		ratings.add(rating);

		//attempt to add the new Json to a file
		try {
			(new File("./storage")).mkdir();
			if(!ratingFile.exists()) ratingFile.createNewFile();
			FileWriter writer = new FileWriter(ratingFile);
			writer.write(new Gson().toJson(ratings));
			writer.close();

			return "Successfully Sent Rating";
		} catch (Exception e){
			System.out.println(e);
		}

		return "Failed to Send Rating";
	}

	private void scanJsonFile(){
		try{
			//scan the file for the Json
			Scanner scan = new Scanner(ratingFile);
			String line = "";
			while (scan.hasNextLine()){
				line = scan.nextLine();
			}

			//parse the Json to the class
			Gson gson = new Gson();
			Rating[] ratingsArr = gson.fromJson(line, Rating[].class);
			ratings = new ArrayList<>();
			ratings.addAll(Arrays.asList(ratingsArr));
		} catch(FileNotFoundException e){
			System.out.println(e);
		}
	}
}