package com.mpdeimos.foodbot;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map.Entry;

import org.apache.http.client.ClientProtocolException;

import com.mpdeimos.foodscraper.Retriever;
import com.mpdeimos.foodscraper.data.IBistro;
import com.mpdeimos.foodscraper.data.IDish;
import com.mpdeimos.foodscraper.data.IMenu;
import com.mpdeimos.webscraper.ScraperException;
import com.mpdeimos.webscraper.util.Strings;

import spark.Request;
import spark.Response;
import spark.Spark;

public class Server
{

	public final Config config = new Config();
	public final Slack slack = new Slack(config);

	public Server()
	{
		Spark.port(config.PORT);
		Spark.before(
				(request, response) -> response.type(
						"text/plain; charset=UTF-8"));
		Spark.get("/", this::getHome);
		Spark.get("/slack/bistro", slack::sendBistro);
		Spark.get("/config", (req, res) -> new Config(), new JsonTransformer());
		Spark.get("/get", this::getUrl);
	}

	public Object getHome(Request req, Response res)
	{
		StringWriter out = new StringWriter();
		PrintWriter writer = new PrintWriter(out);
		Retriever retriever = new Retriever();
		try
		{
			retriever.retrieve();
		}
		catch (ScraperException e)
		{
			writer.println("Error: " + e.getMessage());
			writer.println(Strings.EMPTY);
		}
		Iterable<Entry<IBistro, IMenu>> menu = retriever.getTodaysMenu();
		for (Entry<IBistro, IMenu> entry : menu)
		{
			try
			{
				writer.println("## " + entry.getKey().getName() + " ##");
				writer.println(Strings.EMPTY);
				for (IDish dish : entry.getValue().getDishes())
				{
					writer.println(" " + dish.getName());
					writer.println(
							" " + config.PRICE_FORMAT.format(dish.getPrice()));
					writer.println(Strings.EMPTY);
				}
			}
			catch (Exception e)
			{

			}
		}
		return out.getBuffer().toString();
	}

	public Object getSlackBistro(Request req, Response res)
	{
		return new Config();
	}

	public Object getUrl(Request req, Response res)
			throws ClientProtocolException, IOException
	{
		res.type("text/html; charset=UTF-8");

		return org.apache.http.client.fluent.Request.Get(
				req.queryParams("url")).connectTimeout(5000).socketTimeout(
						5000).execute().returnContent().asString();
	}

	public static void main(String[] args)
	{
		new Server();
	}
}
