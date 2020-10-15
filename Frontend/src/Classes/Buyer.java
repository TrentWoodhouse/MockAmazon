package Classes;

import java.util.ArrayList;

public class Buyer extends User {
	public ArrayList<Integer> cart;
	public ArrayList<Integer> orders;
	public ArrayList<Integer> ratings;

	public Buyer(ArrayList<Integer> cart, ArrayList<Integer> orders, ArrayList<Integer> ratings) {
		super();
		this.cart = cart;
		this.orders = orders;
		this.ratings = ratings;
	}
}
