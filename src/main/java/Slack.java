import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;

import com.google.gson.Gson;
import com.mpdeimos.foodscraper.Retriever;
import com.mpdeimos.foodscraper.data.IBistro;
import com.mpdeimos.foodscraper.data.IDish;
import com.mpdeimos.foodscraper.data.IMenu;

public class Slack {

	private final String[] COLORS = new String[] { "#FF8800", "#669900",
			"#9933CC", "#0099CC", };

	private Config config;

	public Slack(Config config) {
		this.config = config;

	}

	public String sendBistro(spark.Request req, spark.Response res)
			throws Exception {
		Message message = new Message("Das gibt's heute zu essen:");
		message.channel = config.SLACK_CHANNEL;

		Iterator<String> colors = Arrays.asList(COLORS).iterator();

		for (Entry<IBistro, IMenu> entry : new Retriever().getTodaysMenu()) {
			Attachment attachment = new Attachment("fallback");
			attachment.pretext = entry.getKey().getName();
			attachment.color = colors.next();
			for (IDish dish : entry.getValue().getDishes()) {
				Field field = new Field(dish.getName());
				field.value = config.PRICE_FORMAT.format(dish.getPrice());
				attachment.fields.add(field);
			}
			if (attachment.fields.size() == 0) {
				Field field = new Field(
						"...hat heute nichts im Angebot oder eine bescheidene Website :(");
				attachment.fields.add(field);
			}
			message.attachments.add(attachment);
		}

		Content content = Request
				.Post(config.SLACK_URL)
				.bodyString(new Gson().toJson(message),
						ContentType.APPLICATION_JSON).execute().returnContent();

		return content.asString();
	}

	private static class Message {
		public String channel = "test";

		public String icon_emoji = ":fork_and_knife:";

		public List<Attachment> attachments = new ArrayList<Attachment>();

		public final String text;

		public Message(String text) {
			this.text = text;
		}
	}

	private static class Attachment {
		public final String fallback;
		public String text;
		public String pretext;
		public String color;
		public List<Field> fields = new ArrayList<Field>();

		public Attachment(String fallback) {
			this.fallback = fallback;
		}

	}

	public static class Field {
		private final String title;
		private String value;

		public Field(String title) {
			this.title = title;
		}
	}
}
