package com.kursx.parser.fb2;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.ArrayList;

//http://www.fictionbook.org/index.php/Элемент_annotation
@SuppressWarnings("unused")
public class Annotation extends IdElement {

    private String text = "";
    protected String lang;
    protected ArrayList<Element> elements;

    public Annotation() {
    }

    Annotation(Node node) {
        super(node);
        try {
            StringWriter writer = new StringWriter();
            TransformerFactory.newInstance().newTransformer().transform(new DOMSource(node), new StreamResult(writer));
            String xml = writer.toString();
            this.text = Jsoup.parseBodyFragment(xml).body().text();
        } catch (TransformerException ignored) {
        }
        NamedNodeMap map = node.getAttributes();
        for (int index = 0; index < map.getLength(); index++) {
            Node attr = map.item(index);
            if (attr.getNodeName().equals("xml:lang")) {
                lang = attr.getNodeValue();
            }
        }
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node paragraph = nodeList.item(i);
            if (elements == null) elements = new ArrayList<>();
            switch (paragraph.getNodeName()) {
                case "p":
                    elements.add(new P(paragraph));
                    break;
                case "poem":
                    elements.add(new Poem(paragraph));
                    break;
                case "cite":
                    elements.add(new Cite(paragraph));
                    break;
                case "subtitle":
                    elements.add(new Subtitle(paragraph));
                    break;
                case "empty-line":
                    elements.add(new EmptyLine());
                    break;
                case "table":
                    elements.add(new Table());
                    break;
            }
        }
    }

    @NotNull
    public ArrayList<Element> getAnnotations() {
        return elements == null ? new ArrayList<>() : elements;
    }

    @Nullable
    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public ArrayList<Element> getElements() {
        return elements;
    }

    public void setElements(ArrayList<Element> elements) {
        this.elements = elements;
    }

    public String getText() {
        return text;
    }
}
