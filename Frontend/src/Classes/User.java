package Classes;

import com.sun.xml.internal.ws.wsdl.writer.document.http.Address;

import java.util.ArrayList;

public class User {

	public long id;
	public String name;
	public String password;
	public String paymentCard;
	public float balance;
	public float credibility;
	public ArrayList<Integer> messages;
	public Address address;

	public User(){}

	public boolean verifyUser(String password){
		return this.password.equals(password);
	}
}