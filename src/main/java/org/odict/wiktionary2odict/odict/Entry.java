package org.odict.wiktionary2odict.odict;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Entry {
    private String term;
    private List<Etymology> etymologies;

    public Entry(String term) {
        this.term = term;
        this.etymologies = new ArrayList<>();
    }

    private Matcher getRegexMatcher(String regex, String text) {
        Pattern ptn = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
        Matcher m = ptn.matcher(text);
        return m;
    }

    public Entry setTerm(String term) {
        this.term = term;
        return this;
    }

    public List<Etymology> getEtymologies() {
        return this.etymologies;
    }

    public String getTerm() {
        return term;
    }

    public void addEtymology(Etymology etymology) {
        this.etymologies.add(etymology);
    }

    public String toString() {
        String output = String.format("<entry term=\"%s\">", this.getTerm());

        for (int i = 0; i < this.etymologies.size(); i++) {
            output += this.etymologies.get(i).toString();
        }

        output += "</entry>";

        return output;
    }
}
