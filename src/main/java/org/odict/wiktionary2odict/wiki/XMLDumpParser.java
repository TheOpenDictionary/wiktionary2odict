package org.odict.wiktionary2odict.wiki;

import org.odict.wiktionary2odict.odict.Dictionary;
import org.odict.wiktionary2odict.odict.Entry;
import jodd.util.buffer.FastCharBuffer;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.FileReader;
import java.io.Reader;
import java.util.Map;

public class XMLDumpParser {
    private static final String NODE_PAGE = "page";
    private static final String NODE_TITLE = "title";
    private static final String NODE_TEXT = "text";

    public Dictionary process(Reader reader, String targetLanguage) {
        try {
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            XMLStreamReader xmlReader = inputFactory.createXMLStreamReader(reader);

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
                        if (doWriteTextBuffer)
                            textBuffer.append(text);
                        else if (text.contains(languageSearchString)) {
                            textBuffer.append(text);
                            doWriteTextBuffer = true;
                        } else if (text.contains(WikiUtils.getSectionTerminator())) {
                            textBuffer.append(text);
                            doWriteTextBuffer = false;
                        }
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
