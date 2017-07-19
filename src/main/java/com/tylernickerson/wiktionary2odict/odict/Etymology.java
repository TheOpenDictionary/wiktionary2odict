package com.tylernickerson.wiktionary2odict.odict;

import java.util.ArrayList;
import java.util.List;

public class Etymology
{
    private String description;
    private List<Usage> usages;

    public Etymology() {
        this.usages = new ArrayList<>();
        this.description = "";
    }

    public List<Usage> getUsages() {
        return usages;
    }

    public Usage getUsage(int i) {
        return this.usages.get(i);
    }

    public void addUsage(Usage usage) {
        this.usages.add(usage);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String toString() {
        String output = "<ety";

        if (this.description.trim().length() > 0)
            output += String.format(" description=\"%s\"", this.getDescription());
        else
            output += ">";

        for (int i = 0; i < this.getUsages().size(); i++) {
            output += this.getUsage(i).toString();
        }

        output += "</ety>";

        return output;
    }
}
