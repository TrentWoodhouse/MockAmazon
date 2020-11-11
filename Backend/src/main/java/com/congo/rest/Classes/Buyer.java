package com.congo.rest.Classes;

import org.json.JSONObject;

import java.util.ArrayList;

public class Buyer extends User {
	public ArrayList<Integer> cart;
	public ArrayList<Integer> orders;
	public ArrayList<Integer> ratings;
	public ArrayList<Integer> categories;
	public ArrayList<JSONObject> subscriptions;
	public int[] browsingHistory;
	public int[] purchaseHistory;
	public int congo;
	public double primePoints;
	public double rewardsCash;
}
