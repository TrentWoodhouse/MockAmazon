package com.congo.rest.Controllers;

import com.congo.rest.Classes.Admin;
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
public class AdminController {

	private static ArrayList<Admin> admins;
	private File adminFile = new File("./storage/admins.txt");

	@GetMapping("/admin")
	public Admin getAdmin(@RequestParam Map<String, String> input) {

		//load the file into memory if it isn't already
		if (admins == null) {
			if (adminFile.exists()) {
				scanJsonFile();
			} else {
				return null;
			}
		}

		//find the specified admin (or all)
		for(Admin a : admins){
			if((input.containsKey("id") && a.id == Long.parseLong(input.get("id"))) || (input.containsKey("name") && a.name.equals(input.get("name")))){
				return a;
			}
		}

		return null;
	}

	@PostMapping("/admin")
	public String postAdmin(@RequestBody Admin admin) {

		if (admins == null) {
			scanJsonFile();
			if (admins == null) admins = new ArrayList<>();
		}
		for(Admin a : admins) {
			if (a.id == admin.id || a.name.equals(admin.name)) {
				return "Failed to send Admin (incorrect ID)";
			}
		}

		admins.add(admin);

		//attempt to add the new Json to a file
		try {
			(new File("./storage")).mkdir();
			if(!adminFile.exists()) adminFile.createNewFile();
			FileWriter writer = new FileWriter(adminFile);
			writer.write(new Gson().toJson(admins));
			writer.close();

			return "Successfully Sent Admin";
		} catch (Exception e){
			System.out.println(e);
		}

		return "Failed to Send Admin";
	}

	private void scanJsonFile(){
		try{
			//scan the file for the Json
			Scanner scan = new Scanner(adminFile);
			String line = "";
			while (scan.hasNextLine()){
				line = scan.nextLine();
			}

			//parse the Json to the class
			Gson gson = new Gson();
			Admin[] adminsArr = gson.fromJson(line, Admin[].class);
			admins = new ArrayList<>();
			admins.addAll(Arrays.asList(adminsArr));
		} catch(FileNotFoundException e){
			System.out.println(e);
		}
	}

	@PatchMapping("/admin")
	public String updateAdmin(@RequestBody Admin admin) {

		if (admins == null) {
			scanJsonFile();
		}
		if(admins != null) {
			for (int i=0 ; i<admins.size() ; i++) {
				Admin a = admins.get(i);
				if (a.id == admin.id || a.name.equals(admin.name)) {
					admins.set(i, admin);
					//return "Succeeded In Updating Admin";
				}
			}
		}

		try {
			(new File("./storage")).mkdir();
			if(!adminFile.exists()) adminFile.createNewFile();
			FileWriter writer = new FileWriter(adminFile);
			writer.write(new Gson().toJson(admins));
			writer.close();

			return "Successfully Sent Admin";
		} catch (Exception e){
			System.out.println(e);
		}

		return "Failed to Update Admin";
	}

	@DeleteMapping("/admin")
	public Admin deleteAdmin(@RequestParam Map<String, String> input) {

		if (admins == null) {
			if (adminFile.exists()) {
				scanJsonFile();
			} else {
				return null;
			}
		}

		//find the specified admin (or all)
		admins.removeIf(a -> (input.containsKey("id") && a.id == Long.parseLong(input.get("id"))) || (input.containsKey("name") && a.name.equals(input.get("name"))));

		try {
			(new File("./storage")).mkdir();
			if(!adminFile.exists()) adminFile.createNewFile();
			FileWriter writer = new FileWriter(adminFile);
			writer.write(new Gson().toJson(admins));
			writer.close();

		} catch (Exception e){
			System.out.println(e);
		}

		return null;
	}
}