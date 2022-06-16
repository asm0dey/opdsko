package com.kursx.parser.fb2;

import org.w3c.dom.Node;

@SuppressWarnings("unused")
public class Image {

    protected String name;
    protected String value;

    Image(Node node) {
        this.name = node.getAttributes().item(0).getNodeName();
        this.value = node.getAttributes().item(0).getNodeValue();
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public Image() {
        super();
    }

    @Override
    public String toString() {
        return "Image{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
