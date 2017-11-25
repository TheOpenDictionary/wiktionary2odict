package org.odict.wiktionary2odict.odict;

import java.util.ArrayList;
import java.util.List;

public class Group {
    private int id;
    private String description;
    private List<String> definitions;

    public Group(int id, String description) {
        this.id = id;
        this.description = description;
        this.definitions = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getDefinitions() {
        return definitions;
    }

    public String getDefinition(int index) {
        return definitions.get(index);
    }

    public void addDefinition(String definition) {
        this.definitions.add(definition);
    }

    @Override
    public String toString() {
        String output = String.format("<group id=\"%d\" description=\"%s\">", this.getId(), this.getDescription());

        for (int i = 0; i < this.getDefinitions().size(); i++) {
            output += String.format("<definition>%s</definition>", this.getDefinition(i));
        }

        output += "</group>";

        return output;
    }
}
