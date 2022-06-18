package com.kursx.parser.fb2;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.jsoup.parser.Parser;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.util.*;

public class FictionBook {

    public static final TaggedLogger FB2_LOGGER = Logger.tag("FB2");
    protected Xmlns[] xmlns;
    protected Description description;
    protected List<Body> bodies = new ArrayList<>();
    protected Map<String, Binary> binaries;

    public String encoding = "utf-8";

    public FictionBook() {
    }

    public FictionBook(File file) throws XMLStreamException, IOException, ParserConfigurationException {
        Document doc;
        try {
            String inferred = XMLInputFactory.newInstance().createXMLStreamReader(new FileReader(file)).getCharacterEncodingScheme();
            encoding = inferred == null ? encoding : inferred;

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputStream inputStream = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new FileReader(file));
            boolean foundIllegalCharacters = false;
            try {
                StringBuilder line = new StringBuilder(br.readLine().trim());
                if (!line.toString().startsWith("<")) {
                    foundIllegalCharacters = true;
                }
            } catch (Exception e) {
                FB2_LOGGER.error(e);
            }
            if (foundIllegalCharacters) {
                FB2_LOGGER.debug(() -> "Found illegal characters in " + file.getAbsolutePath());
                StringBuilder text = new StringBuilder();
                br = new BufferedReader(new FileReader(file));
                String line = br.readLine();
                if (line != null && line.contains("<")) {
                    line = line.substring(line.indexOf("<"));
                }
                while (line != null) {
                    text.append(line);
                    line = br.readLine();
                }
                br.close();
                try {
                    FB2_LOGGER.debug(() -> "Parsing " + file.getAbsolutePath() + " without explicitly specified encoding");
                    doc = db.parse(new InputSource(new StringReader(text.toString())));
                } catch (SAXException sax) {
                    FB2_LOGGER.warn(sax, () -> "Parsing " + file.getAbsolutePath() + " filed, falling back to Jsoup");
                    doc = W3CDom.convert(Jsoup.parse(file, encoding, "https://yandex.ru", Parser.xmlParser()));
                }
            } else {
                try {
                    FB2_LOGGER.debug(() -> "Parsing " + file.getAbsolutePath() + " with encoding " + encoding);
                    doc = db.parse(new InputSource(new InputStreamReader(inputStream, encoding)));
                } catch (SAXException sax) {
                    FB2_LOGGER.warn(sax, () -> "Parsing " + file.getAbsolutePath() + " filed, falling back to Jsoup");
                    doc = W3CDom.convert(Jsoup.parse(file, encoding, "https://yandex.ru", Parser.xmlParser()));
                }
            }
        } catch (IOException | ParserConfigurationException | XMLStreamException e) {
            FB2_LOGGER.error(e, () -> "Error while processing " + file.getAbsolutePath());
            throw e;
        }
        initXmlns(doc);
        description = new Description(doc);
        NodeList bodyNodes = doc.getElementsByTagName("body");
        for (int item = 0; item < bodyNodes.getLength(); item++) {
            bodies.add(new Body(bodyNodes.item(item)));
        }
        NodeList binary = doc.getElementsByTagName("binary");
        for (int item = 0; item < binary.getLength(); item++) {
            if (binaries == null) binaries = new HashMap<>();
            Binary binary1 = new Binary(binary.item(item));
            try {
                binaries.put(binary1.getId().replace("#", ""), binary1);
            } catch (Exception e) {
                FB2_LOGGER.error(e, () -> "Invalid binary " + binary1 + " in file " + file.getAbsolutePath());
            }
        }
    }

    protected void setXmlns(ArrayList<Node> nodeList) {
        xmlns = new Xmlns[nodeList.size()];
        for (int index = 0; index < nodeList.size(); index++) {
            Node node = nodeList.get(index);
            xmlns[index] = new Xmlns(node);
        }
    }

    protected void initXmlns(Document doc) {
        NodeList fictionBook = doc.getElementsByTagName("FictionBook");
        ArrayList<Node> xmlns = new ArrayList<>();
        for (int item = 0; item < fictionBook.getLength(); item++) {
            NamedNodeMap map = fictionBook.item(item).getAttributes();
            for (int index = 0; index < map.getLength(); index++) {
                Node node = map.item(index);
                xmlns.add(node);
            }
        }
        setXmlns(xmlns);
    }

    public ArrayList<Person> getAuthors() {
        return description.getDocumentInfo().getAuthors();
    }

    public Xmlns[] getXmlns() {
        return xmlns;
    }

    public Description getDescription() {
        return description;
    }

    public @Nullable Body getBody() {
        return getBody(null);
    }

    public @Nullable Body getNotes() {
        return getBody("notes");
    }

    public @Nullable Body getComments() {
        return getBody("comments");
    }

    private @NotNull Body getBody(String name) {
        for (Body body : bodies) {
            if ((name + "").equals(body.getName() + "")) {
                return body;
            }
        }
        return bodies.get(0);
    }

    @NotNull
    public Map<String, Binary> getBinaries() {
        return binaries == null ? new HashMap<String, Binary>() : binaries;
    }

    public String getTitle() {
        return description.getTitleInfo().getBookTitle();
    }

    public String getLang() {
        return description.getTitleInfo().getLang();
    }

    public @Nullable Annotation getAnnotation() {
        return description.getTitleInfo().getAnnotation();
    }
}
