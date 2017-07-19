package com.tylernickerson.wiktionary2odict;

import com.tylernickerson.wiktionary2odict.constants.Languages;
import com.tylernickerson.wiktionary2odict.odict.Dictionary;
import com.tylernickerson.wiktionary2odict.wiki.WikiUtils;
import com.tylernickerson.wiktionary2odict.wiki.XMLDumpParser;
import jdk.nashorn.internal.parser.JSONParser;
import jodd.http.HttpRequest;
import jodd.json.JsonParser;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static jodd.jerry.Jerry.jerry;
import static org.apache.commons.text.StringEscapeUtils.escapeHtml4;

public class Main {
    public static void main(String[] args) throws IOException {
        Dictionary dictionary = new XMLDumpParser().process("/Users/tjnickerson/Desktop/enwiktionary-20170620-pages-articles-multistream.xml", Languages.KOREAN);
        BufferedWriter writer = new BufferedWriter(new FileWriter("korean.xml"));

        System.out.println("Writing to disk...");

        writer.write(dictionary.toString());
        writer.close();

        System.out.println(String.format("Wrote %d entries to the dictionary.", dictionary.count()));
    }
}
