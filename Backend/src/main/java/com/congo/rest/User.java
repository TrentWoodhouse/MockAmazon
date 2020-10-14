package main.java.com.congo.rest;

public class User {

	private long id;
	private String name;
	private String password;
	private int[] messages;

	public User(long id, String name) {
		this.id = id;
		this.name = name;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public boolean verifyUser(String password){
		return this.password == password;
	}
}