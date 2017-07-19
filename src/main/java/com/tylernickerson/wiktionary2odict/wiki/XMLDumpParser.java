package com.tylernickerson.wiktionary2odict.wiki;

import com.tylernickerson.wiktionary2odict.odict.Dictionary;
import com.tylernickerson.wiktionary2odict.odict.Entry;
import jodd.util.buffer.FastCharBuffer;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.FileReader;
import java.util.Map;

public class XMLDumpParser {
    private static final String NODE_PAGE = "page";
    private static final String NODE_TITLE = "title";
    private static final String NODE_TEXT = "text";

    public Dictionary process(String filename, String targetLanguage) {
        try {
            System.out.println("Processing XML...");

            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            XMLStreamReader xmlReader = inputFactory.createXMLStreamReader(
                    new FileReader(filename));

            String prevNode = "";
            String curNode = "";
            FastCharBuffer titleBuffer = new FastCharBuffer();
            FastCharBuffer textBuffer = new FastCharBuffer();
            Dictionary dictionary = new Dictionary();
            String languageSearchString = WikiUtils.getLanguageSearchString(targetLanguage);
            boolean doWriteTextBuffer = false;

            while (xmlReader.hasNext()) {

                Integer eventType = xmlReader.next();

                // IF WE HIT THE START OF A NODE
                if (eventType.equals(XMLEvent.START_ELEMENT)) {
                    String nodeName = xmlReader.getName().getLocalPart();

                    prevNode = curNode;
                    curNode = nodeName;
                }
                // IF WE HIT A CHARACTER NODE
                else if (eventType.equals(XMLEvent.CHARACTERS)) {
                    String text = xmlReader.getText();

                    // Process the title
                    if (curNode.equals(NODE_TITLE)) {

                        // Colons usually denote a wiki-specific page. Skip it if this is the case.
                        if (!text.contains(":")) {
                            titleBuffer.append(text);
                        }
                    }
                    // Process the body of the entry
                    else if (curNode.equals(NODE_TEXT)) {
                        if (text.contains(languageSearchString)) doWriteTextBuffer = true;
                        else if (text.contains(WikiUtils.getSectionTerminator())) doWriteTextBuffer = false;
                        if (doWriteTextBuffer) textBuffer.append(text);
                    }
                }
                // IF WE HIT THE CLOSING OF A NODE
                else if (eventType.equals(XMLEvent.END_ELEMENT)) {
                    String nodeName = xmlReader.getName().getLocalPart();

                    curNode = prevNode;
                    prevNode = "";

                    // Clean stuff up and write the current entry at the end of a page
                    if (nodeName.equals(NODE_PAGE)) {
                        Map entryMap = WikiUtils.wikiToEntryMap(
                                titleBuffer.toString(),
                                textBuffer.toString(),
                                targetLanguage
                        );

                        if (entryMap != null) {
                            Entry entry = (Entry) entryMap.get(WikiUtils.KEY_ENTRY);
                            boolean usedNetwork = (boolean) entryMap.get(WikiUtils.KEY_REQUIRED_NETWORK);

                            dictionary.addEntry(entry);

                            System.out.println("\rAdded entry for word " +
                                    entry.getTerm() +
                                    ((usedNetwork) ? " (used network)" : "")
                            );

                            Thread.sleep(100);
                        }

                        titleBuffer.clear();
                        textBuffer.clear();
                        doWriteTextBuffer = false;
                    }
                }
            }

            xmlReader.close();
            System.out.println("Processing complete");

            return dictionary;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
