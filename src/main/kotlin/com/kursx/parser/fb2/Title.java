package com.kursx.parser.fb2;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

@SuppressWarnings("unused")
public class Title {

    protected ArrayList<P> paragraphs = new ArrayList<>();

    public Title() {
    }

    Title(Node root) {
        NodeList body = root.getChildNodes();
        for (int item = 0; item < body.getLength(); item++) {
            Node node = body.item(item);
            if ("p".equals(node.getNodeName())) {
                paragraphs.add(new P(node));
            }
        }
    }

    @NotNull
    public ArrayList<P> getParagraphs() {
        return paragraphs;
    }
}
