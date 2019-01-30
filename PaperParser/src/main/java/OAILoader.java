/*
package de.hsh.ik;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class OAILoader
{


	private String host;
	private String outputdir;
	*/
/**
	 * @param args
	 * Args for querying HsH Library: http://opus.bsz-bw.de/fhhv/oai2/oai2.php  Z:BIM\Webindexing\Records
	 * TU Berlin: http://opus4.kobv.de/opus4-tuberlin/oai    \\ik-serv5.fh-h.de\personal\wartena\BIM\Webindexing\Datasets\TUB
	 *//*

	public static void main(String[] args) {
		OAILoader theApp = new OAILoader();
//		theApp.setServer(args[0]);
//		theApp.setDir(args[1]);
		theApp.setServer("http://www.biomedcentral.com/oai/2.0/");
		theApp.setDir("C://downloads");
		theApp.run();
	}

	private void setServer(String string) {
		host = string;

	}

	private void setDir(String string) {
		outputdir = string;

	}

	private void run() {
		String sRequest = host + "?verb=ListIdentifiers&metadataPrefix=oai_dc";

		HttpGet request;
		DefaultHttpClient client = new DefaultHttpClient();
		HttpResponse response;
		String resumptionToken = null;
		try {
			do {
				request = new HttpGet(sRequest);
				response = client.execute(request);
				HttpEntity entity  = response.getEntity();
				resumptionToken = processList(entity);
				if(resumptionToken != null) {
					sRequest = host + "?verb=ListIdentifiers&resumptionToken="+resumptionToken;
				}
			} while (resumptionToken != null);

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private String processList(HttpEntity entity) {
		String resumptionToken = null;

		try {

			InputStream isList = entity.getContent();

			*/
/*
			BufferedReader reader = new BufferedReader(new InputStreamReader(isList));
			String line = reader.readLine();
			while(line != null) {
				System.out.println(line);
				line = reader.readLine();
			}
			*//*



			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(false);
			DocumentBuilder builder;
			Document doc = null;
			builder = factory.newDocumentBuilder();
			doc = builder.parse(isList);
			doc.getDocumentElement().normalize();

			// Create a XPathFactory
			XPathFactory xFactory = XPathFactory.newInstance();

			// Create a XPath object
			XPath xpath = xFactory.newXPath();

			// Compile the XPath expression
			XPathExpression expr = xpath.compile("OAI-PMH/ListIdentifiers/header");
			// Run the query and get a nodeset

			NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

			//NodeList nodes =  doc.getElementsByTagName("header");
			for (int i=0; i<nodes.getLength();i++){
				Node node = nodes.item(i);
				String sID = "";
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element header = (Element) node;
					NodeList ids = header.getElementsByTagName("identifier");
					for (int j=0; j<ids.getLength();j++){
						Element id = (Element) ids.item(j);
						sID = id.getTextContent();
					}

					retrieve(sID);
				}

			}

			xpath.reset();
			expr = xpath.compile("OAI-PMH/ListIdentifiers/resumptionToken");
			nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
			if(nodes.getLength() == 1) {
				Element elResTok = (Element) nodes.item(0);
				resumptionToken = elResTok.getTextContent();
			}

		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		return resumptionToken;
	}

	private void retrieve(String sID) {
		String sRequest = host + "?verb=GetRecord&metadataPrefix=oai_dc&identifier=" + sID;
		HttpGet request = new HttpGet(sRequest);
		DefaultHttpClient client = new DefaultHttpClient();
		HttpResponse response;
		try {
			response = client.execute(request);
			HttpEntity entity  = response.getEntity();
			processRecord(sID, entity);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void processRecord(String sID, HttpEntity entity) throws IllegalStateException, IOException {
		InputStream isList = entity.getContent();
		BufferedReader reader = new BufferedReader(new InputStreamReader(isList));

		String idl[] =sID.split(":");
		String nID = idl[idl.length-1];


		BufferedWriter writer = new BufferedWriter(new FileWriter(outputdir + "/record-" + nID + ".xml"));
		//writer.write(abstr);


		String line = reader.readLine();
		while(line != null) {
			//System.out.println(line);
			writer.write(line+"\n");
			line = reader.readLine();
		}

		writer.close();
	}


}
*/
