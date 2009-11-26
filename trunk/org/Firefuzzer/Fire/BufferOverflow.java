package org.Firefuzzer.Fire;

import static java.lang.System.out;
import static java.lang.System.err;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.htmlparser.jericho.Attributes;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;

class BufferOverflow {
	private static int countForms = 0;
	private static int countInputs = 0;
	private static String var;
	private static int[] arrayBuffer = new int[5];
	public static String globalURL;
	public static boolean globalDetailFlag = false;
	public static boolean flipFlop = false;

	/**
	 * Initialize the Array
	 * */
	public BufferOverflow() {
		for (int i = 0; i < arrayBuffer.length; i++) {
			arrayBuffer[i] = 0;
		}
	}

	/**
	 * Showcases the analysis of Buffer Overflow
	 * */
	public static void analyzeBufferOverflow() {
		System.out
				.println("########################################################################################################################");
		System.out.println("<---BUFFER OVERFLOW ANALYSIS--->");
		System.out.println("Total # of Forms: " + countForms);
		System.out.println("Total # of Input tags: " + countInputs);
		System.out
				.println("<<-Categorizing the available data on basis of HTTP Status Codes->>");
		System.out.println("Informational Codes 1xx Series: " + arrayBuffer[0]);
		System.out.println("Successful Client Interaction related 2xx Series: "
				+ arrayBuffer[1]);
		System.out.println("Redirection related 3xx Series: " + arrayBuffer[2]);
		System.out
				.println("Client Error related 4xx Series: " + arrayBuffer[3]);
		System.out
				.println("Server Error related 5xx Series: " + arrayBuffer[4]);
		System.out
				.println("########################################################################################################################");
		System.out
				.println("########################################################################################################################");
		out
				.println("For more Information on HTTP Status Code Series, refer the 'HTTP_STATUS_CODE.pdf' in Document folder.");
		out
				.println("########################################################################################################################");
	}

	/**
	 * Traverses over the HTML source file and embeds random String into the
	 * value of the 'input' tag
	 */
	private static void sendBack(String data) throws MalformedURLException,
			IOException {
		HttpClient client = new HttpClient();
		client
				.getParams()
				.setParameter(
						"http.useragent",
						"Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.10) Gecko/2009042708 Fedora/3.0.10-1.fc10 Firefox/3.0.10");
		if (globalDetailFlag == true) {
			out.println("URL: " + var);
			out.println("Data passed: " + data);
		}

		PostMethod method = new PostMethod(var);
		BufferedReader br = null;
		StringTokenizer str = new StringTokenizer(data, "#");
		if (globalDetailFlag == true)
			out.println("Total # of Input Fields: " + str.countTokens());
		countInputs += str.countTokens();
		while (str.hasMoreTokens()) {
			StringTokenizer strr = new StringTokenizer(str.nextToken(), ",");
			String attrib = strr.nextToken();
			String value = strr.nextToken();
			method.addParameter(attrib, value);
		}

		try {

			int returnCode = client.executeMethod(method);
			if (globalDetailFlag == true)
				out.println("Return: " + method.getStatusCode() + " "
						+ method.getStatusText());
			arrayBuffer[(method.getStatusCode() / 100) - 1]++;
			if (returnCode == HttpStatus.SC_NOT_IMPLEMENTED) {
				err.println("The Post method is not implemented by this URI");
				method.getResponseBodyAsString();
			} else {
				br = new BufferedReader(new InputStreamReader(method
						.getResponseBodyAsStream()));
				String readLine;
				PrintWriter pw = new PrintWriter("temp.html");
				pw.println("Address: " + var);
				while (((readLine = br.readLine()) != null)) {
					pw.println(readLine);
					pw.flush();
				}
			}
		} catch (Exception e) {
			err.println(e);
		} finally {
			// new ProcessBuilder("firefox", "temp.html").start();
			method.releaseConnection();

			if (br != null)
				try {
					br.close();
				} catch (Exception fe) {
				}
		}
	}

	/**
	 * Randomly generates a long String
	 * */
	private static String randomizer() {
		String str1 = new String(
				"QAa0bcLdUK2eHfJgTP8XhiFj61DOklNm9nBoI5pGqYVrs3CtSuMZvwWx4yE7zR");
		StringBuffer sb = new StringBuffer();
		Random r = new Random();
		int te = 0;
		for (int i = 1; i <= 300; i++) {
			te = r.nextInt(62);
			sb.append(str1.charAt(te));
		}
		String token1 = sb.toString();
		return token1;
	}

	/** Parses over the HTML file and populates the List data structure */
	public static void parseInput() throws IOException {
		Source source = null;
		try {
			source = new Source(new FileReader("page.loaded"));
		} catch (FileNotFoundException fnfe) {
			err.println("File not found. Error: " + fnfe.getMessage());
		} catch (IOException ioe) {
			err.println("IOException occurred. Error: " + ioe.getMessage());
		}
		int currentForm = 0;

		List<StartTag> branches = source.getAllStartTags(HTMLElementName.FORM);
		countForms = branches.size();
		out
				.println("########################################################################################################################");

		Attributes attr;
		String data = "";
		String str = "", pattern = "", temp = "";
		String[] tempStr = null;

		for (StartTag sj : branches) {
			currentForm++;
			attr = sj.getAttributes();
			data = "";
			List<StartTag> segments = sj.getElement().getAllStartTags(
					HTMLElementName.INPUT);
			str = "";
			pattern = "";
			temp = "";
			tempStr = null;
			boolean radioSelect = false;

			for (StartTag startTag : segments) {
				str = "";
				temp = "";

				Attributes attributes = startTag.getAttributes();
				String random = randomizer();
				String rel = attributes.getValue("type");
				if (rel == null)
					continue;
				if (rel.equalsIgnoreCase("text")
						|| rel.equalsIgnoreCase("hidden")
						|| rel.equalsIgnoreCase("password")) {
					str = startTag.toString();
					if (str.contains("value")) {
						String atrString = attributes.toString();
						StringTokenizer strAtr = new StringTokenizer(atrString);
						String strtag = "<input ";
						while (strAtr.hasMoreTokens()) {
							String strA = strAtr.nextToken();
							if (!strA.contains("value")) {
								strtag = strtag + strA + " ";
							}
						}
						strtag = strtag + "value=\"" + random + "\"/>";
						temp = strtag;
					} else {
						pattern = "/>";
						Pattern p = Pattern.compile(pattern);
						Matcher m = null;
						if (p.matcher(str).find()) {
							tempStr = str.split(" ");
							int last = tempStr.length - 1;
							pattern = "/>";
							String replace = " value=\"" + random + "\"/>";
							p = Pattern.compile(pattern);
							m = p.matcher(tempStr[last]);
							tempStr[last] = m.replaceFirst(replace);
							temp = "";
							for (int j = 0; j < tempStr.length; j++)
								temp = temp + tempStr[j] + " ";
						} else {
							tempStr = str.split(" ");
							int last = tempStr.length - 1;
							pattern = ">";
							String replace = " value=\"" + random + "\"/>";
							p = Pattern.compile(pattern);
							m = p.matcher(tempStr[last]);
							tempStr[last] = m.replaceFirst(replace);
							temp = "";
							for (int j = 0; j < tempStr.length; j++)
								temp = temp + tempStr[j] + " ";
						}
					}
					try {
						data += URLEncoder.encode(attributes.getValue("name"),
								"UTF-8")
								+ ","
								+ URLEncoder.encode(random, "UTF-8")
								+ "#";
					} catch (UnsupportedEncodingException uee) {
						err.println("Unsupported error");
					}
				} else if (rel.equalsIgnoreCase("radio")
						&& radioSelect == false) {
					radioSelect = true;
					str = startTag.toString();
					if (str.contains("checked")) {
						String atrString = attributes.toString();
						StringTokenizer strAtr = new StringTokenizer(atrString);
						String strtag = "<input ";
						while (strAtr.hasMoreTokens()) {
							String strA = strAtr.nextToken();
							if (!strA.contains("")) {
								strtag = strtag + strA + " ";
							}
						}
						strtag = strtag + "value=\"" + randomizer() + "\"/>";
						temp = strtag;
						out.println("Line: " + rel + " is checked");
					} else {
						pattern = "/>";
						Pattern p = Pattern.compile(pattern);
						Matcher m = null;
						if (p.matcher(str).find()) {
							tempStr = str.split(" ");
							int last = tempStr.length - 1;
							pattern = "/>";
							String replace = " checked/>";
							p = Pattern.compile(pattern);
							m = p.matcher(tempStr[last]);
							tempStr[last] = m.replaceFirst(replace);
							temp = "";
							for (int j = 0; j < tempStr.length; j++)
								temp = temp + tempStr[j] + " ";
						} else {
							tempStr = str.split(" ");
							int last = tempStr.length - 1;
							pattern = ">";
							String replace = " checked/>";
							p = Pattern.compile(pattern);
							m = p.matcher(tempStr[last]);
							tempStr[last] = m.replaceFirst(replace);
							temp = "";
							for (int j = 0; j < tempStr.length; j++)
								temp = temp + tempStr[j] + " ";
						}
					}

					try {
						data += URLEncoder.encode(attributes.getValue("name"),
								"UTF-8")
								+ ","
								+ URLEncoder.encode("checked", "UTF-8")
								+ "#";
					} catch (UnsupportedEncodingException uee) {
						err.println("Unsupported error");
					}
				} else if (rel.equalsIgnoreCase("checkbox")) {
					str = startTag.toString();
					if (str.contains("checked")) {
						String atrString = attributes.toString();
						StringTokenizer strAtr = new StringTokenizer(atrString);
						String strtag = "<input ";
						while (strAtr.hasMoreTokens()) {
							String strA = strAtr.nextToken();
							if (!strA.contains("")) {
								strtag = strtag + strA + " ";
							}
						}
						strtag = strtag + "value=\"" + randomizer() + "\"/>";
						temp = strtag;
						out.println("Line: " + rel + " is checked");
					} else {
						pattern = "/>";
						Pattern p = Pattern.compile(pattern);
						Matcher m = null;
						if (p.matcher(str).find()) {
							tempStr = str.split(" ");
							int last = tempStr.length - 1;
							pattern = "/>";
							String replace = " checked/>";
							p = Pattern.compile(pattern);
							m = p.matcher(tempStr[last]);
							tempStr[last] = m.replaceFirst(replace);
							temp = "";
							for (int j = 0; j < tempStr.length; j++)
								temp = temp + tempStr[j] + " ";
						} else {
							tempStr = str.split(" ");
							int last = tempStr.length - 1;
							pattern = ">";
							String replace = " checked/>";
							p = Pattern.compile(pattern);
							m = p.matcher(tempStr[last]);
							tempStr[last] = m.replaceFirst(replace);
							temp = "";
							for (int j = 0; j < tempStr.length; j++)
								temp = temp + tempStr[j] + " ";
						}
					}

					try {
						data += URLEncoder.encode(attributes.getValue("name"),
								"UTF-8")
								+ ","
								+ URLEncoder.encode("checked", "UTF-8")
								+ "#";
					} catch (UnsupportedEncodingException uee) {
						err.println("Unsupported error");
					}
				}
			}

			var = attr.getValue("action");
			// new FileWriter("temp.html").append(var);
			if (var == null || var.equals("")) {
				continue;
			}
			if (var.charAt(0) == '/') {
				var = globalURL + var;
			} else if (!var.contains("http") & !var.contains("https")
					& !var.contains("www")) {
				int ind = globalURL.lastIndexOf("/");
				int cind = globalURL.lastIndexOf("//");
				String rt;
				if (ind == cind + 1) {
					rt = globalURL;
				} else {
					rt = globalURL.substring(0, ind);
				}
				var = rt + '/' + var;
			}
			// new ProcessBuilder("C:\\Program Files\\Mozilla Firefox\\firefox",
			// "temp.html").start();
			if (globalDetailFlag == true) {
				out.println("data: " + data);
				out.println("Form #: " + currentForm);
			}
			if (globalDetailFlag == false)
				if (flipFlop == false) {
					out.println(">>");
					flipFlop = true;
				} else {
					out.println("<<");
					flipFlop = false;
				}
			sendBack(data);
			if (globalDetailFlag == true)
				out
						.println("########################################################################################################################");
		}
	}
}