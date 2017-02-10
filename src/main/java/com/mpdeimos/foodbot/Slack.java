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

import net.gpedro.integrations.slack.SlackApi;
import net.gpedro.integrations.slack.SlackAttachment;
import net.gpedro.integrations.slack.SlackField;
import net.gpedro.integrations.slack.SlackMessage;

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
		SlackApi slack = new SlackApi(this.config.slackUrl());

		Iterator<String> colors = Arrays.asList(this.COLORS).iterator();

		List<String> responses = new ArrayList<String>();

		Retriever retriever = new Retriever();
		try
		{
			retriever.retrieve();
		}
		catch (ScraperException e)
		{
			SlackMessage message = new SlackMessage("Error").setChannel(
					this.config.slackChannel());

			SlackAttachment attachment = new SlackAttachment().setFallback(
					"Error");

			attachment.setColor("#FF0000");
			SlackField field = new SlackField();
			field.setTitle(e.getClass().getSimpleName());
			field.setValue(e.getMessage());
			attachment.addFields(field);
			message.addAttachments(attachment);
			slack.call(message);
		}
		for (Entry<IBistro, IMenu> entry : retriever.getTodaysMenu())
		{
			SlackMessage message = new SlackMessage(entry.getKey().getName());
			message.setChannel(this.config.slackChannel());

			SlackAttachment attachment = new SlackAttachment().setFallback(
					"fallback");
			attachment.setColor(colors.next());

			boolean hasDish = false;
			for (IDish dish : entry.getValue().getDishes())
			{
				hasDish = true;
				SlackField field = new SlackField();
				field.setTitle(dish.getName());
				if (dish.getPrice() > 0)
				{
					field.setValue(PRICE_FORMAT.format(dish.getPrice()));
				}
				attachment.addFields(field);
			}
			if (!hasDish)
			{
				SlackField field = new SlackField().setValue(
						"...hat heute nichts im Angebot (oder eine bescheidene Website)");
				attachment.addFields(field);
			}
			message.addAttachments(attachment);

			slack.call(message);
		}

		return responses;
	}
}
