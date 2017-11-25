package org.odict.wiktionary2odict.wiki;

public class PartsOfSpeech {
    public static final String ADJ = "adjective";
    public static final String ADV = "adverb";
    public static final String CONJ = "conjunction";
    public static final String GENERIC = "definitions";
    public static final String INTJ = "interjection";
    public static final String NOUN = "noun";
    public static final String NUMBER = "number";
    public static final String PARTICLE = "particle";
    public static final String PHRASE = "phrase";
    public static final String PREFIX = "prefix";
    public static final String PREP = "preposition";
    public static final String PRONOUN = "pronoun";
    public static final String PROPER_NOUN = "proper noun";
    public static final String SUFFIX = "suffix";
    public static final String VERB = "verb";

    private static String[] allParts = {ADJ, ADV, CONJ, INTJ, NOUN, NUMBER, GENERIC, PARTICLE, PHRASE, PREFIX, PREP, PROPER_NOUN, PRONOUN, SUFFIX, VERB};

    public static String asString() {
        String output = "";

        for (int i = 0; i < allParts.length; i++) {
            output += allParts[i];

            if (i != allParts.length - 1) output += "|";
        }

        return output;
    }
}