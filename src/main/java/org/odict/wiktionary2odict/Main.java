package org.odict.wiktionary2odict;

import org.apache.commons.cli.*;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.odict.wiktionary2odict.odict.Dictionary;
import org.odict.wiktionary2odict.wiki.XMLDumpParser;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class Main {
    public static void showHelp(Options options) {
        Iterator<Option> it = options.getOptions().iterator();
        System.out.println("Usage: wiktionary2odict [options] [target_language_code]\n");
        System.out.println("Options:");

        while (it.hasNext()) {
            Option op = it.next();
            System.out.printf("  %-10s  %-10s %-10s\n", op.getLongOpt(), "(-" + op.getOpt() + ")", op.getDescription());
        }
    }

    public static void showAvailableLanguages() {
        Iterator<HashMap.Entry<String, String>> it = LanguageLookup.getInstance().getMap().entrySet().iterator();
        System.out.println("Available language codes:");

        while(it.hasNext()) {
            HashMap.Entry<String, String> entry = it.next();
            System.out.printf("  %-10s  %-10s\n", entry.getKey(), entry.getValue());
        }
    }

    public static void main(String[] args) throws IOException {
        Options options = new Options();

        options.addOption("o", "output", true, "Output file (XML)");
        options.addOption("h", "help", false, "Show help menu");

        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);
            List<String> argList = cmd.getArgList();

            if (cmd.hasOption("h")) showHelp(options);
            else if (argList.size() != 1) showHelp(options);
            else {
                String originLanguageCode = "en";
                String targetLanguageCode = argList.get(0);
                String targetLanguageName = LanguageLookup.getInstance().lookup(targetLanguageCode);
                String filename = originLanguageCode + "_" + targetLanguageCode + ".xml";

                if (cmd.hasOption("o")) filename = cmd.getOptionValue("o");

                if (originLanguageCode == null) {
                    System.out.println("Unrecognized origin language code: " + originLanguageCode);
                    showAvailableLanguages();
                }
                if (targetLanguageCode == null) {
                    System.out.println("Unrecognized target language code: " + targetLanguageCode);
                    showAvailableLanguages();
                }
                else {
                    System.out.println("Retrieving latest Wikimedia dumps...");
                    Document listPage = Jsoup.connect("https://dumps.wikimedia.org/backup-index.html").get();

                    System.out.println("Locating matching Wiktionary dump for language...");
                    Element item = listPage.select(String.format("a:contains(%swiktionary)", originLanguageCode)).first();

                    if (item == null)
                        System.out.println("Could not find Wiktionary language for language code: " + originLanguageCode + ".");
                    else {
                        String href = item.attr("href");

                        Document downloadPage = Jsoup.connect("https://dumps.wikimedia.org/" + href).get();
                        Element previousElement = downloadPage.select("p.previous:contains(This dump is in progress)").first();

                        if (previousElement != null) {
                            String destURL = previousElement.select("a").first().attr("href");
                            System.out.println("A dump is currently in progress. Defaulting to last available dump...");
                            downloadPage = Jsoup.connect("https://dumps.wikimedia.org/" + href + "/" + destURL).get();
                        }

                        Element fileLink = downloadPage.select(String.format("a:matches((?i)%swiktionary-(?:\\d+)-pages-articles-multistream\\.xml\\.bz2)", originLanguageCode)).first();

                        if (fileLink == null) System.out.println("Could not find correct file to download. Please open an issue in the project's repo.");
                        else {
                            System.out.println("Reading dump from URL stream...");

                            String fileURL = "https://dumps.wikimedia.org" + fileLink.attr("href");
                            URL bz2 = new URL(fileURL);
                            BufferedInputStream bis = new BufferedInputStream(bz2.openStream());
                            CompressorInputStream input = new MultiStreamBZip2InputStream(bis);
                            BufferedReader br2 = new BufferedReader(new InputStreamReader(input));

                            System.out.println("Processing XML...");

                            Dictionary dictionary = new XMLDumpParser().process(br2, targetLanguageName);
                            BufferedWriter writer = new BufferedWriter(new FileWriter("french.xml"));

                            System.out.println("Writing to disk...");

                            writer.write(dictionary.toString());
                            writer.close();

                            System.out.println(String.format("Wrote %d entries to the dictionary.", dictionary.count()));

                            br2.close();
                            input.close();
                            bis.close();
                        }
                    }
                }
            }
        } catch (UnrecognizedOptionException e) {
            showHelp(options);
        } catch (ParseException e) {
            e.printStackTrace();
        }
//        catch (CompressorException e) {
//            e.printStackTrace();
//        }
    }
}
