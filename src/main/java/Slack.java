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
import com.mpdeimos.foodscraper.Retriever;
import com.mpdeimos.foodscraper.data.IBistro;
import com.mpdeimos.foodscraper.data.IDish;
import com.mpdeimos.foodscraper.data.IMenu;

public class Slack {
	private static String SLACK_URL = System.getenv().get("SLACK_URL");

	private static final DecimalFormat FORMAT = new DecimalFormat("#.00€"); //$NON-NLS-1$

	private static final String[] COLORS = new String[] { "#FF8800", "#9933CC",
			"#669900", "#0099CC", };

	public static void main(String[] args) throws Exception {
		Message message = new Message("Das gibt's heute zu essen:");
		message.channel = "#general";

		Iterator<String> colors = Arrays.asList(COLORS).iterator();

		for (Entry<IBistro, IMenu> entry : new Retriever().getTodaysMenu()) {
			Attachment attachment = new Attachment("fallback");
			attachment.pretext = entry.getKey().getName();
			attachment.color = colors.next();
			for (IDish dish : entry.getValue().getDishes()) {
				Field field = new Field(dish.getName());
				field.value = FORMAT.format(dish.getPrice());
				attachment.fields.add(field);
			}
			if (attachment.fields.size() == 0) {
				Field field = new Field("...hat eine bescheidene Website");
				attachment.fields.add(field);
			}
			message.attachments.add(attachment);
		}

		Content content = Request
				.Post(SLACK_URL)
				.bodyString(new Gson().toJson(message),
						ContentType.APPLICATION_JSON).execute().returnContent();

		System.out.println(content.asString());
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

		// "fallback":
		// "Required text summary of the attachment that is shown by clients that understand attachments but choose not to show them.",
		//
		// "text": "Optional text that should appear within the attachment",
		// "pretext":
		// "Optional text that should appear above the formatted data",
		//
		// "color": "#36a64f", // Can either be one of 'good', 'warning',
		// 'danger', or any hex color code
		//
		// // Fields are displayed in a table on the message
		// "fields": [
		// {
		// "title": "Required Field Title", // The title may not contain markup
		// and will be escaped for you
		// "value":
		// "Text value of the field. May contain standard message markup and must be escaped as normal. May be multi-line.",
		// "short": false // Optional flag indicating whether the `value` is
		// short enough to be displayed side-by-side with other values
		// }
		// ]
	}

	public static class Field {
		private final String title;
		private String value;

		public Field(String title) {
			this.title = title;
		}
	}
}
