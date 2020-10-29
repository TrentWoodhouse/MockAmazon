package com.congo.rest.Classes;

public class Flag implements Comparable<Flag> {
	public long id;
	public long user;
	public String reason;
	public long listing;

	@Override
	public int compareTo(Flag compFlag) {
		long comp=compFlag.listing;
		/* For Ascending order*/
		return Integer.parseInt(String.valueOf(this.listing-comp));
	}
}
