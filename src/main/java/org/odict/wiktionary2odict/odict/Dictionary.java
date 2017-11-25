package org.odict.wiktionary2odict.odict;

import java.util.ArrayList;
import java.util.List;

public class Dictionary {
    private List<Entry> entries;

    public Dictionary() {
        this.entries = new ArrayList<>();
    }

    public void addEntry(Entry entry) {
        this.entries.add(entry);
    }

    public int count() {
        return this.entries.size();
    }

    public String toString() {
        String output = "<dictionary>";

        for (int i = 0; i < this.entries.size(); i++) {
            output += this.entries.get(i).toString();
        }

        output += "</dictionary>";

        return output;
    }
}
