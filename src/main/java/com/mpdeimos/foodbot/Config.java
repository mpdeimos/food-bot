package com.mpdeimos.foodbot;

import java.text.DecimalFormat;

public class Config
{
	public String SLACK_CHANNEL = System.getenv("SLACK_CHANNEL");

	public transient String SLACK_URL = System.getenv("SLACK_URL");

	public int PORT = Integer.parseInt(System.getenv("PORT"));

	public transient final DecimalFormat PRICE_FORMAT = new DecimalFormat(
			"#.00€");
}
