import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.mpdeimos.foodscraper.Retriever;
import com.mpdeimos.foodscraper.data.IBistro;
import com.mpdeimos.foodscraper.data.IDish;
import com.mpdeimos.foodscraper.data.IMenu;
import com.mpdeimos.webscraper.ScraperException;
import com.mpdeimos.webscraper.util.Strings;

public class Main extends HttpServlet {
	/** Format string for prices. */
	private static final DecimalFormat FORMAT = new DecimalFormat("#.00€"); //$NON-NLS-1$

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		if (req.getRequestURI().endsWith("/slack")) {
			try {
				Slack.main(new String[] {});
			} catch (Exception e) {
				resp.getWriter().write(e.getMessage());
			}
		} else {
			showHome(req, resp);
		}
	}

	private void showHome(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		try {
			resp.getWriter().println(getBistroString());
		} catch (ScraperException e) {
			resp.getWriter().println(e.getStackTrace());
		}

	}

	public static String getBistroString() throws ScraperException {
		StringWriter out = new StringWriter();
		PrintWriter writer = new PrintWriter(out);
		Iterable<Entry<IBistro, IMenu>> menu = new Retriever().getTodaysMenu();
		for (Entry<IBistro, IMenu> entry : menu) {
			writer.println("## " + entry.getKey().getName() + " ##"); //$NON-NLS-1$ //$NON-NLS-2$
			writer.println(Strings.EMPTY);
			for (IDish dish : entry.getValue().getDishes()) {
				writer.println(" " + dish.getName()); //$NON-NLS-1$
				writer.println(" " + FORMAT.format(dish.getPrice())); //$NON-NLS-1$
				writer.println(Strings.EMPTY);
			}
		}
		return out.getBuffer().toString();
	}

	public static void main(String[] args) throws Exception {
		Server server = new Server(Integer.valueOf(System.getenv("PORT")));
		ServletContextHandler context = new ServletContextHandler(
				ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		server.setHandler(context);
		context.addServlet(new ServletHolder(new Main()), "/*");
		server.start();
		server.join();
	}
}
