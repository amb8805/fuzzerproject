package fuzzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.net.URL;


import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import com.gargoylesoftware.htmlunit.WebResponse;

public class Fuzzer {

	public static void main(String[] args) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		WebClient webClient = new WebClient();
		String url = "http://localhost:8080/bangeit";
		discoverLinks(webClient, url);
		doFormPost(webClient);
		webClient.closeAllWindows();
	}

	/**
	 * This code is for showing how you can get all the links on a given page, and visit a given URL
	 * @param webClient
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	private static void discoverLinks(WebClient webClient, String url) throws IOException, MalformedURLException {
		System.out.println("Discovering links  ");
		HtmlPage page = webClient.getPage(url);
		List<HtmlAnchor> links = page.getAnchors();
		for (HtmlAnchor link : links) {
			System.out.println("Link discovered: " + link.asText() + " @URL=" + link.getHrefAttribute());
		}
	}
	private static void guessPages(WebClient webClient, String url, String commonWordPath) {
		try{
			ArrayList<String> lines = guessHelper(commonWordPath);
			for (String line : lines) {
				HtmlPage guess = webClient.getPage(url + line);
				WebResponse response = guess.getWebResponse();
				int statusCode = response.getStatusCode();
				if (guess.isHtmlPage() && statusCode != 404) {
					System.out.println("Page discovered: " + guess.getUrl());
				}
			}
		}
		catch(IOException e) {

		}

	}
	public static ArrayList<String> guessHelper(String path) {
		File file = new File(path);
		BufferedReader reader;
		ArrayList<String> lines = new ArrayList<String>();
		try {
			reader = new BufferedReader(new FileReader(file));
			String line;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return lines;
	}

	/**
	 * This code is for demonstrating techniques for submitting an HTML form. Fuzzer code would need to be
	 * more generalized
	 * @param webClient
	 * @throws FailingHttpStatusCodeException
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	private static void doFormPost(WebClient webClient) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		HtmlPage page = webClient.getPage("http://localhost:8080/bodgeit/product.jsp?prodid=26");
		List<HtmlForm> forms = page.getForms();
		for (HtmlForm form : forms) {
			HtmlInput input = form.getInputByName("quantity");
			input.setValueAttribute("2");
			HtmlSubmitInput submit = (HtmlSubmitInput) form.getFirstByXPath("//input[@id='submit']");
			System.out.println(submit.<HtmlPage> click().getWebResponse().getContentAsString());
		}
	}

	/**
	 * Returns all inputs on a page.
	 */
	public static ArrayList<DomElement> getInputs(HtmlPage page) {
		List<HtmlForm> formsList = page.getForms();// get all forms
		Iterable<DomElement> formElements;
		ArrayList<DomElement> formChildren = new ArrayList<DomElement>();
		for (HtmlForm form : formsList) {
			formElements = form.getChildElements(); // get children of form
			for (DomElement e : formElements) {
				formChildren.add(e);
				formChildren.addAll(getElements(e));
			}
		}
		ArrayList<DomElement> inputs = new ArrayList<DomElement>();
		for (DomElement e : formChildren) {
			if (e instanceof HtmlInput) {
				inputs.add(e);// if its an input add it
			}
		}

		return inputs;
	}

	/**
	 *get all the child elements of DOM element
	 */
	private static ArrayList<DomElement>getElements(DomElement e) {
		ArrayList<DomElement> elements = new ArrayList<DomElement>();
		Iterable<DomElement> children = e.getChildElements();
		for (DomElement child : children) {
			elements.add(child);
			elements.addAll(getElements(child));
		}
		return elements;
	}

	/**
	 * Returns an ArrayList of get input from url.
	 */
	public static ArrayList<String> getUrlInputs(URL url) {
		ArrayList<String> inputs;
		String queries = url.getQuery();
		if (queries != null) {
			String[] splitQueries = queries.split("&");
			inputs = new ArrayList<String>(Arrays.asList(splitQueries));
		} else {
			inputs = null;
		}
		return inputs;
	}


}
