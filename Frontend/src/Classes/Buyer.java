package Classes;

import java.util.ArrayList;

public class Buyer extends User {
	public ArrayList<Integer> cart;
	public ArrayList<Integer> orders;
	public ArrayList<Integer> ratings;
	public ArrayList<Integer> categories;
	public int congo;
	public double primePoints;
	public double rewardsCash;

	public Buyer(ArrayList<Integer> cart, ArrayList<Integer> orders, ArrayList<Integer> ratings) {
		super();
		this.cart = cart;
		this.orders = orders;
		this.ratings = ratings;
		categories = new ArrayList<Integer>();
		int i = 0;
		while (i < 9) {categories.add(0); i++;}
		congo = 0;
		primePoints = 0;
		rewardsCash = 0;
	}
}
