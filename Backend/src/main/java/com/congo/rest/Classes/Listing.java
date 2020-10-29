package com.congo.rest.Classes;

import java.util.ArrayList;

public class Listing {
	public long id;
	public String name;
	public String description;
	public int seller;
	public float cost;
	public String maxDelivery;
	public String category;
	public ArrayList<Integer> orders;
	public ArrayList<Integer> ratings;

	public Listing(long id, String name, String description, int seller, float cost, String maxDelivery, String category, ArrayList<Integer> orders, ArrayList<Integer> ratings) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.cost = cost;
		this.seller = seller;
		this.maxDelivery = maxDelivery;
		this.category = category;
		this.orders = orders;
		this.ratings = ratings;
	}
}