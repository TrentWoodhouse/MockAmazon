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
public class UserController {

	private static ArrayList<User> users;
	private File userFile = new File("./storage/users.txt");

	@GetMapping("/user")
	public User getUser(@RequestParam(value = "id", defaultValue = "") String id) {

		if (users == null) {
			if (userFile.exists()) {
				scanJsonFile();
			} else {
				return null;
			}
		}

		//find the specified user (or all)
		for(User u : users){
			if(u.id == Long.parseLong(id)){
				return u;
			}
		}

		return null;
	}

	@PostMapping("/user")
	public String postUser(@RequestBody User user) {

		if (users == null) {
			scanJsonFile();
			if (users == null) users = new ArrayList<>();
		}
		for(User u : users) {
			if (u.id == user.id) {
				return "Failed to send User (incorrect ID)";
			}
		}

		users.add(user);

		//attempt to add the new Json to a file
		try {
			(new File("./storage")).mkdir();
			if(!userFile.exists()) userFile.createNewFile();
			FileWriter writer = new FileWriter(userFile);
			writer.write(new Gson().toJson(users));
			writer.close();

			return "Successfully Sent User";
		} catch (Exception e){
			System.out.println(e);
		}

		return "Failed to Send User";
	}

	private void scanJsonFile(){
		try{
			//scan the file for the Json
			Scanner scan = new Scanner(userFile);
			String line = "";
			while (scan.hasNextLine()){
				line = scan.nextLine();
			}

			//parse the Json to the class
			Gson gson = new Gson();
			User[] usersArr = gson.fromJson(line, User[].class);
			users = new ArrayList<>();
			users.addAll(Arrays.asList(usersArr));
		} catch(FileNotFoundException e){
			System.out.println(e);
		}
	}
}