package com.congo.rest.Controllers;

import com.congo.rest.Classes.Flag;
import com.google.gson.Gson;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.*;

@RestController
public class FlagController {

	private static ArrayList<Flag> flags;
	private File flagFile = new File("./storage/flags.txt");

	@GetMapping("/flag")
	public ArrayList<Flag> getFlag(@RequestParam Map<String, String> input) {

		//load the file into memory if it isn't already
		if (flags == null) {
			if (flagFile.exists()) {
				scanJsonFile();
			} else {
				return null;
			}
		}

		if (input.containsKey("most")) {
			long mostPopular = findPopular();
			ArrayList<Flag> popularFlags = new ArrayList<>();
			for(Flag f : flags){
				if(f.listing == mostPopular) popularFlags.add(f);
			}
			return popularFlags;
		}

		ArrayList<Flag> flagList = new ArrayList<Flag>();
		//find the specified flag (or all)
		if(input.containsKey("all")) return flags;
		for(Flag f : flags){
			if((input.containsKey("id") && f.id == Long.parseLong(input.get("id"))) || (input.containsKey("listing") && f.listing == Long.parseLong(input.get("listing")))){
				flagList.add(f);
			}
		}

		return flagList;
	}

	@PostMapping("/flag")
	public String postFlag(@RequestBody Flag flag) {

		if (flags == null) {
			scanJsonFile();
			if (flags == null) flags = new ArrayList<>();
		}
		for(Flag f : flags) {
			if (f.id == flag.id) {
				return "Failed to send Flag (incorrect ID)";
			}
		}

		flag.id = flags.size()+1;
		flags.add(flag);

		//attempt to add the new Json to a file
		try {
			(new File("./storage")).mkdir();
			if(!flagFile.exists()) flagFile.createNewFile();
			FileWriter writer = new FileWriter(flagFile);
			writer.write(new Gson().toJson(flags));
			writer.close();

			return "Successfully Sent Flag";
		} catch (Exception e){
			System.out.println(e);
		}

		return "Failed to Send Flag";
	}

	private void scanJsonFile(){
		try{
			//scan the file for the Json
			Scanner scan = new Scanner(flagFile);
			String line = "";
			while (scan.hasNextLine()){
				line = scan.nextLine();
			}

			//parse the Json to the class
			Gson gson = new Gson();
			Flag[] flagsArr = gson.fromJson(line, Flag[].class);
			flags = new ArrayList<>();
			flags.addAll(Arrays.asList(flagsArr));
		} catch(FileNotFoundException e){
			System.out.println(e);
		}
	}

	@PatchMapping("/flag")
	public String updateFlag(@RequestBody Flag flag) {

		if (flags == null) {
			scanJsonFile();
		}
		if(flags != null) {
			for (int i=0 ; i<flags.size() ; i++) {
				Flag u = flags.get(i);
				if (u.id == flag.id) {
					flags.set(i, flag);
				}
			}
		}

		try {
			(new File("./storage")).mkdir();
			if(!flagFile.exists()) flagFile.createNewFile();
			FileWriter writer = new FileWriter(flagFile);
			writer.write(new Gson().toJson(flags));
			writer.close();

			return "Successfully Updated Flag";
		} catch (Exception e){
			System.out.println(e);
		}

		return "Failed to Update Flag";
	}

	@DeleteMapping("/flag")
	public Flag deleteFlag(@RequestParam Map<String, String> input) {

		if (flags == null) {
			if (flagFile.exists()) {
				scanJsonFile();
			} else {
				return null;
			}
		}

		//find the specified flag (or all)
		flags.removeIf(f -> (input.containsKey("id") && f.id == Long.parseLong(input.get("id"))) || (input.containsKey("listing") && f.listing == Long.getLong(input.get("listing"))));

		try {
			(new File("./storage")).mkdir();
			if(!flagFile.exists()) flagFile.createNewFile();
			FileWriter writer = new FileWriter(flagFile);
			writer.write(new Gson().toJson(flags));
			writer.close();

		} catch (Exception e){
			System.out.println(e);
		}

		return null;
	}

	public long findPopular() {

		if (flags == null || flags.size() == 0) return 0;

		Collections.sort(flags);

		long previous = flags.get(0).listing;
		long popular = flags.get(0).listing;
		int count = 1;
		int maxCount = 1;

		for (int i = 1; i < flags.size(); i++) {
			if (flags.get(i).listing == previous)
				count++;
			else {
				if (count > maxCount) {
					popular = flags.get(i-1).listing;
					maxCount = count;
				}
				previous = flags.get(i).listing;
				count = 1;
			}
		}

		return count > maxCount ? flags.get(flags.size()-1).listing : popular;

	}
}
