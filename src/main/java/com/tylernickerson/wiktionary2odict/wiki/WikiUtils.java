package com.tylernickerson.wiktionary2odict.wiki;

import com.tylernickerson.wiktionary2odict.constants.PartsOfSpeech;
import com.tylernickerson.wiktionary2odict.odict.Entry;
import com.tylernickerson.wiktionary2odict.odict.Etymology;
import com.tylernickerson.wiktionary2odict.odict.Group;
import com.tylernickerson.wiktionary2odict.odict.Usage;
import jodd.http.HttpException;
import jodd.http.HttpRequest;
import jodd.json.JsonParser;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static jodd.jerry.Jerry.jerry;
import static org.apache.commons.text.StringEscapeUtils.escapeHtml4;

public class WikiUtils {
    public static final String KEY_REQUIRED_NETWORK = "required_network";
    public static final String KEY_ENTRY = "entry";
    public static final String KEY_TEXT = "text";

    private static int connectionRetries = 0;

    /**
     * Gets a regex matcher with given flags
     *
     * @param regex
     * @param text
     * @param flags
     * @return
     */
    private static Matcher getRegexMatcher(String regex, String text, int flags) {
        Pattern ptn = Pattern.compile(regex, flags);
        Matcher m = ptn.matcher(text);
        return m;
    }

    /**
     * Gets a regex matcher with default flags
     *
     * @param regex
     * @param text
     * @return
     */
    private static Matcher getRegexMatcher(String regex, String text) {
        return getRegexMatcher(regex, text, Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
    }

    /**
     * Asks the Wiktionary server to decode wikitext if the internal regex can't do it
     *
     * @param wikitext
     * @return
     */
    private static String askServer(String wikitext) {
        try {
            HttpRequest request = new HttpRequest();
            String rawJSON = request.get("https://en.wiktionary.org/w/api.php")
                    .contentType("application/json")
                    .query("action", "parse")
                    .query("text", wikitext)
                    .query("format", "json")
                    .query("contentmodel", "wikitext")
                    .send().bodyText();
            Map map = new JsonParser().parse(rawJSON);
            String html = ((Map) ((Map) map.get("parse")).get("text")).get("*").toString();
            connectionRetries = 0;
            return jerry(html
                    .replace(" API", "")
                    .replaceAll("<i>(.*)<\\/i>", "_$1_")
                    .replaceAll("<b>(.*)<\\/b>", "__$1__")
            ).text().trim();
        } catch (HttpException exception) {
            if (connectionRetries > 3) {
                throw new Error("Tried connecting to the API over 3 times to no avail. Is the site down?");
            } else {
                try {
                    System.out.println("\nAn issue occurred connecting to the API: " + exception.getMessage() +
                            "\nTrying again in 5 seconds...\n");
                    connectionRetries++;
                    Thread.sleep(5000);
                    return askServer(wikitext);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    public static String getLanguageSearchString(String language) {
        return String.format("==%s==", language);
    }

    private static boolean wikiContainsLanguage(String wikitext, String language) {
        Matcher matcher = getRegexMatcher(getLanguageSearchString(language), wikitext);
        return matcher.find();
    }

    public static String getSectionTerminator() {
        return "----";
    }

    public static Entry wikiToEntry(String title, String wikitext, String language) {
        return (Entry) wikiToEntryMap(title, wikitext, language).get(KEY_ENTRY);
    }

    /**
     * Converts a wikitext definition to a sanitized plain text string map
     *
     * @return
     */
    private static HashMap<String, Object> wikiToPlainTextMap(String wikidefinition) {
        HashMap<String, Object> map = new HashMap<>();
        map.put(KEY_REQUIRED_NETWORK, false);

        String definition = wikidefinition
                .replaceAll("\\[\\[(?:.*?)?([^|]*?)]]", "$1") // unwrap text in [[ ]]
                .replaceAll("''(.*?)''", "__$1__") // replace all italics annotations ('') with Markdown __
                .replaceAll("\\n", "")  // remove new lines
                .replaceAll("\\t", "")  // remove tabs
                .replaceAll("\\r", ""); // remove carriage returns

        if (definition.contains("{{") || definition.contains("}}")) {
            definition = askServer(definition);
            map.put(KEY_REQUIRED_NETWORK, true);
        }

        definition = escapeHtml4(definition)
                .replaceAll("<!--.*-->", "") // remove comments
                .replaceAll("\\n", "")  // remove new lines
                .replaceAll("\\t", "")  // remove tabs
                .replaceAll("\\r", "") // remove carriage returns
                .replaceAll("\\s\\s+", " ") // remove excess white space
                .trim();

        map.put(KEY_TEXT, definition);

        return map;
    }

    /**
     * Converts wikitext to an Entry object for a specified language
     *
     * @param wikitext
     * @param title
     * @param language
     * @return
     */
    public static Map wikiToEntryMap(String title, String wikitext, String language) {
        if (!wikiContainsLanguage(wikitext, language)) return null;
        if (title.trim().length() == 0) return null;

//        System.out.println("processing word " + title);
        Entry entry = new Entry(title);
        HashMap<String, Object> map = new HashMap<>();
        int groupCount = 0;

        map.put(KEY_REQUIRED_NETWORK, false);

        wikitext += String.format("\n\n%s", getSectionTerminator()); // Required for some regex to work

        // Matches the part of the wikicode for the specified language
        Matcher languageGroupMatcher = getRegexMatcher(String.format("(==%s==(.*?))(?:\\n----)", language), wikitext);

        // If the language was found, continue
        if (languageGroupMatcher.find()) {
            // Matches the content of the language section
            String contents = languageGroupMatcher.group(2) + String.format("\n\n%s", getSectionTerminator());
            boolean useDefault = false; // forces a default etymology

            // Match all etymologies
            Matcher etyMatcher = getRegexMatcher(
                    "(?:^===+Etymology.*?===+)(.+?)^(?:(?=----)|(?====Etymology.*===))",
                    contents
            );

            if (!etyMatcher.find()) useDefault = true;
            else etyMatcher.reset();

            // If we found etymologies or are sticking with the default
            while(etyMatcher.find() || useDefault) {
                String etymologyBody = (useDefault) ? contents : etyMatcher.group(1);
                Etymology etymology = new Etymology();

                // Matches all sections for a designated part of speech (noun, verb, etc.)
                Matcher usageMatcher = getRegexMatcher(
                        String.format("(^===+(%s)===+.+?)^(?:(?=----)|(?==))", PartsOfSpeech.asString()),
                        etymologyBody
                );

                // Go to each part of speech
                while (usageMatcher.find()) {
                    // Get the text of the full definition and the part of speech title
                    String fullDefinition = usageMatcher.group(0);
                    String partOfSpeech = usageMatcher.group(2);

                    // Matches all parent definitions
                    Matcher definitionBlockMatcher = getRegexMatcher(
                            // Gets definition block, of parent definition, sub-definitions, and examples
                            "(?=^#\\s)(.+?)(?=(^#\\s)|\\Z)",
                            fullDefinition
                    );

                    // Create a new usage and loop through each definition, adding each to the usage
                    Usage usage = new Usage(partOfSpeech.toLowerCase());
                    while (definitionBlockMatcher.find()) {
                        String definitionBlock = definitionBlockMatcher.group(1);

                        Matcher parentDefinition = getRegexMatcher(
                                "(?<=^#\\s)(.+?)(?=#|(?:^\\{)|\\Z)", // Gets the top-most definition (only one #)
                                definitionBlock
                        );

                        Matcher subDefinitions = getRegexMatcher(
                                "(?<=^##\\s)(.+?)(?=#|(?:^\\{)|\\Z)",
                                definitionBlock
                        );

                        if (parentDefinition.find()) {
                            String definition = parentDefinition.group(0);

                            if (!definition.contains("rfdef")) { // make sure word is defined
                                HashMap<String, Object> ptm = wikiToPlainTextMap(definition);

                                definition = ptm.get(KEY_TEXT).toString();
                                map.put(KEY_REQUIRED_NETWORK, ptm.get(KEY_REQUIRED_NETWORK));

                                // If we found any subdefinitions
                                if (subDefinitions.find()) {
                                    Group group = new Group(groupCount, definition);

                                    subDefinitions.reset();

                                    while(subDefinitions.find()) {
                                        String subDefinition = subDefinitions.group(0);
                                        HashMap<String, Object> sptm = wikiToPlainTextMap(subDefinition);

                                        subDefinition = sptm.get(KEY_TEXT).toString();

                                        // Determine whether the network was used for any subdefinitions
                                        map.put(KEY_REQUIRED_NETWORK,
                                                (boolean) map.get(KEY_REQUIRED_NETWORK) || (boolean) sptm.get(KEY_REQUIRED_NETWORK)
                                        );

                                        if (subDefinition.length() > 0) {
                                            // Add the definition to the current group
                                            group.addDefinition(subDefinition);
                                        }
                                    }

                                    // Add the group if it has some definitions
                                    if (group.getDefinitions().size() > 0) {
                                        usage.addGroup(group);
                                        groupCount++;
                                    }

                                } else if (definition.length() > 0) {
                                    // If there is a non-empty definition, add it to the usage
                                    usage.addDefinition(definition);
                                }
                            }
                        }
                    }

                    // If there are at least some found definitions, add the usage to the entry
                    if (usage.getDefinitions().size() > 0 || usage.getGroups().size() > 0) etymology.addUsage(usage);
                }

                if (etymology.getUsages().size() > 0) entry.addEtymology(etymology);

                // Turn off the default switch to prevent an infinite loop
                useDefault = false;
            }

            // If there are no usages, what's the point in having an empty entry?
            if (entry.getEtymologies().size() == 0) {
//                System.out.println("Skipped " + title);
                return null;
            }

            map.put(KEY_ENTRY, entry);

            return map;
        }

        // Return null if language not found
        return null;
    }
}
