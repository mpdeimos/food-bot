package com.mpdeimos.foodbot;

import com.mpdeimos.foodscraper.Retriever;
import com.mpdeimos.foodscraper.data.IBistro;
import com.mpdeimos.foodscraper.data.IDish;
import com.mpdeimos.foodscraper.data.IMenu;
import com.mpdeimos.webscraper.ScraperException;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;

import com.google.gson.Gson;

public class Slack
{
	public static final DecimalFormat PRICE_FORMAT = new DecimalFormat(
			"#.00€");

	private final String[] COLORS = new String[] { "#FF8800", "#669900",
			"#9933CC", "#0099CC", };

	private final AppConfig config;

	public Slack(AppConfig config)
	{
		this.config = config;

	}

	public List<String> sendBistro(spark.Request req, spark.Response res)
			throws IOException
	{

		Iterator<String> colors = Arrays.asList(COLORS).iterator();

		List<String> responses = new ArrayList<String>();

		Retriever retriever = new Retriever();
		try
		{
			retriever.retrieve();
		}
		catch (ScraperException e)
		{
			Message message = new Message("Error");
			message.channel = config.slackChannel();

			Attachment attachment = new Attachment("fallback");
			attachment.color = "#FF0000";
			Field field = new Field(e.getClass().getSimpleName());
			field.value = e.getMessage();
			attachment.fields.add(field);
			message.attachments.add(attachment);
			Request.Post(config.slackUrl()).bodyString(
					new Gson().toJson(message),
					ContentType.APPLICATION_JSON).execute().returnContent();
		}
		for (Entry<IBistro, IMenu> entry : retriever.getTodaysMenu())
		{
			Message message = new Message(entry.getKey().getName());
			message.channel = config.slackChannel();

			Attachment attachment = new Attachment("fallback");
			attachment.color = colors.next();
			for (IDish dish : entry.getValue().getDishes())
			{
				Field field = new Field(dish.getName());
				if (dish.getPrice() > 0)
				{
					field.value = PRICE_FORMAT.format(dish.getPrice());
				}
				attachment.fields.add(field);
			}
			if (attachment.fields.size() == 0)
			{
				Field field = new Field(
						"...hat heute nichts im Angebot (oder eine bescheidene Website)");
				attachment.fields.add(field);
			}
			message.attachments.add(attachment);

			Content content = Request.Post(config.slackUrl()).bodyString(
					new Gson().toJson(message),
					ContentType.APPLICATION_JSON).execute().returnContent();
			responses.add(content.asString());
		}

		return responses;
	}

	private static class Message
	{
		public String channel = "test";

		public String icon_emoji = ":fork_and_knife:";

		public List<Attachment> attachments = new ArrayList<Attachment>();

		public final String text;

		public Message(String text)
		{
			this.text = text;
		}
	}

	private static class Attachment
	{
		public final String fallback;
		public String text;
		public String pretext;
		public String color;
		public List<Field> fields = new ArrayList<Field>();

		public Attachment(String fallback)
		{
			this.fallback = fallback;
		}

	}

	public static class Field
	{
		private final String title;
		private String value;

		public Field(String title)
		{
			this.title = title;
		}
	}
}
