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
public class MessageController {

	private static ArrayList<Message> messages;
	private File messageFile = new File("./storage/messages.txt");

	@GetMapping("/message")
	public ArrayList<Message> getMessage(@RequestParam(value = "id", defaultValue = "") String id) {

		if (messages == null) {
			if (messageFile.exists()) {
				scanJsonFile();
			} else {
				return null;
			}
		}

		ArrayList<Message> messageList = new ArrayList<Message>();
		//find the specified message (or all)
		for(Message m : messages){
			if(m.receiver == Long.parseLong(id) || m.sender == Long.parseLong(id)){
				messageList.add(m);
			}
		}

		return messageList;
	}

	@PostMapping("/message")
	public String postMessage(@RequestBody Message message) {

		if (messages == null) {
			scanJsonFile();
			if (messages == null) messages = new ArrayList<>();
		}
		for(Message m : messages) {
			if (m.id == message.id) {
				return "Failed to send Message (incorrect ID)";
			}
		}

		message.id = messages.size()+1;
		messages.add(message);

		//attempt to add the new Json to a file
		try {
			(new File("./storage")).mkdir();
			if(!messageFile.exists()) messageFile.createNewFile();
			FileWriter writer = new FileWriter(messageFile);
			writer.write(new Gson().toJson(messages));
			writer.close();

			return "Successfully Sent Message";
		} catch (Exception e){
			System.out.println(e);
		}

		return "Failed to Send Message";
	}

	private void scanJsonFile(){
		try{
			//scan the file for the Json
			Scanner scan = new Scanner(messageFile);
			String line = "";
			while (scan.hasNextLine()){
				line = scan.nextLine();
			}

			//parse the Json to the class
			Gson gson = new Gson();
			Message[] messagesArr = gson.fromJson(line, Message[].class);
			messages = new ArrayList<>();
			messages.addAll(Arrays.asList(messagesArr));
		} catch(FileNotFoundException e){
			System.out.println(e);
		}
	}

	@PatchMapping("/message")
	public String updateMessage(@RequestBody Message message) {

		if (messages == null) {
			scanJsonFile();
		}
		if(messages != null) {
			for (int i=0 ; i<messages.size() ; i++) {
				Message u = messages.get(i);
				if (u.id == message.id) {
					messages.set(i, message);
					return "Succeeded In Updating Message";
				}
			}
		}

		try {
			(new File("./storage")).mkdir();
			if(!messageFile.exists()) messageFile.createNewFile();
			FileWriter writer = new FileWriter(messageFile);
			writer.write(new Gson().toJson(messages));
			writer.close();

			return "Successfully Sent Message";
		} catch (Exception e){
			System.out.println(e);
		}

		return "Failed to Update Message";
	}

	@DeleteMapping("/message")
	public Message deleteMessage(@RequestParam(value = "id", defaultValue = "") String id) {

		if (messages == null) {
			if (messageFile.exists()) {
				scanJsonFile();
			} else {
				return null;
			}
		}

		//find the specified message (or all)
		for(Message m : messages){
			if(m.id == Long.parseLong(id)){
				messages.remove(m);
			}
		}

		return null;
	}
}