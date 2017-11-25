package org.odict.wiktionary2odict.odict;

import java.util.ArrayList;
import java.util.List;

public class Usage {
    private String partOfSpeech;
    private List<String> definitions;
    private List<Group> groups;

    public Usage(String partOfSpeech) {
        this.partOfSpeech = partOfSpeech;
        this.definitions = new ArrayList<>();
        this.groups = new ArrayList<>();
    }

    public List<String> getDefinitions() {
        return definitions;
    }

    public String getDefinition(int index) {
        return definitions.get(index);
    }

    public String getPartOfSpeech() {
        return partOfSpeech;
    }

    public void addDefinition(String definition) {
        this.definitions.add(definition);
    }

    public String toString() {
        String output = String.format("<usage pos=\"%s\">", this.getPartOfSpeech());

        for (int i = 0; i < this.getDefinitions().size(); i++) {
            output += String.format("<definition>%s</definition>", this.getDefinition(i));
        }

        for (int i = 0; i < this.getGroups().size(); i++) {
            output += this.getGroups().get(i).toString();
        }

        output += "</usage>";

        return output;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void addGroup(Group group) {
        this.groups.add(group);
    }
}
