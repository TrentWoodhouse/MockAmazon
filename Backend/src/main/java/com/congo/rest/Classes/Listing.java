package com.congo.rest.Classes;

import java.util.ArrayList;
import java.util.Date;

public class Listing {
	public long id;
	public String name;
	public String description;
	public int seller;
	public float cost;
	public float salePercentage;
	public String maxDelivery;
	public String category;
	public ArrayList<Integer> orders;
	public ArrayList<Integer> ratings;
	public int views;
	public String dateCreated;

	public Listing(long id, String name, String description, int seller, float cost, float salePercentage, String maxDelivery, String category, ArrayList<Integer> orders, ArrayList<Integer> ratings, int views, String dateCreated) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.cost = cost;
		this.salePercentage = salePercentage;
		this.seller = seller;
		this.maxDelivery = maxDelivery;
		this.category = category;
		this.orders = orders;
		this.ratings = ratings;
		this.views = views;
		this.dateCreated = dateCreated;
	}
}
