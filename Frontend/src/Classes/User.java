package Classes;

import java.util.ArrayList;

public class User {

	public long id;
	public String name;
	public String password;
	public ArrayList<Integer> messages;

	public User(){}

	public boolean verifyUser(String password){
		return this.password.equals(password);
	}
}