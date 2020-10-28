package com.congo.rest;

import java.util.ArrayList;

public class Listing {
	public long id;
	public String name;
	public String description;
	public int seller;
	public float cost;
	public String maxDelivery;
	public ArrayList<Integer> orders;
	public ArrayList<Integer> ratings;

	public Listing(long id, String name, String description, int seller, float cost, String maxDelivery, ArrayList<Integer> orders, ArrayList<Integer> ratings) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.cost = cost;
		this.seller = seller;
		this.maxDelivery = maxDelivery;
		this.orders = orders;
		this.ratings = ratings;
	}
}
