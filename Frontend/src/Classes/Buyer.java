package Classes;

import java.util.ArrayList;

public class Buyer extends User {
	public ArrayList<Integer> cart;
	public ArrayList<Integer> orders;
	public ArrayList<Integer> ratings;

	public Buyer(ArrayList<Integer> cart) {
		super();
		this.cart = cart;
	}
}
