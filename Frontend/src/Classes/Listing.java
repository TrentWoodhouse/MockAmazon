package Classes;

import java.util.ArrayList;
import java.util.Date;

public class Listing {
	public long id;
	public String name;
	public String description;
	public int seller;
	public float cost;
	public String maxDelivery;
	public ArrayList<Integer> orders;
	public ArrayList<Integer> ratings;
	public int views;
	public String dateCreated;

//	public Listing(long id, String name, String description, int seller, float cost, ArrayList<Integer> orders, ArrayList<Integer> ratings) {
//		this.id = id;
//		this.name = name;
//		this.description = description;
//		this.cost = cost;
//		this.seller = seller;
//		this.orders = orders;
//		this.ratings = ratings;
//	}
}
