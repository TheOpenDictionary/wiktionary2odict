package org.odict.wiktionary2odict;

import java.util.HashMap;

public class LanguageLookup {
    static LanguageLookup instance = null;

    HashMap<String, String> map = new HashMap();

    private LanguageLookup() {
        map.put("af", "Afrikaans");
        map.put("sq", "Albanian");
        map.put("ar", "Arabic");
        map.put("hy", "Armenian");
        map.put("eu", "Basque");
        map.put("bs", "Bosnian");
        map.put("br", "Breton");
        map.put("bg", "Bulgarian");
        map.put("ca", "Catalan");
        map.put("zh", "Chinese");
        map.put("hr", "Croatian");
        map.put("cs", "Czech");
        map.put("da", "Danish");
        map.put("nl", "Dutch");
        map.put("en", "English");
        map.put("eo", "Esperanto");
        map.put("et", "Estonian");
        map.put("fi", "Finnish");
        map.put("fr", "French");
        map.put("gl", "Galician");
        map.put("ka", "Georgian");
        map.put("de", "German");
        map.put("el", "Greek");
        map.put("he", "Hebrew");
        map.put("hi", "Hindu");
        map.put("hu", "Hungarian");
        map.put("is", "Icelandic");
        map.put("id", "Indonesian");
        map.put("it", "Italian");
        map.put("ja", "Japanese");
        map.put("kk", "Kazakh");
        map.put("ko", "Korean");
        map.put("lv", "Latvian");
        map.put("lt", "Lithuanian");
        map.put("mk", "Macedonian");
        map.put("ms", "Malay");
        map.put("ml", "Malayalam");
        map.put("no", "Norsk");
        map.put("fa", "Persian");
        map.put("pl", "Polish");
        map.put("pt", "Portuguese");
        map.put("ro", "Romanian");
        map.put("ru", "Russian");
        map.put("sr", "Serbian");
        map.put("si", "Sinhalese");
        map.put("sk", "Slovak");
        map.put("sl", "Slovene");
        map.put("es", "Spanish");
        map.put("sv", "Swedish");
        map.put("tl", "Tagalog");
        map.put("ta", "Tamil");
        map.put("te", "Telugu");
        map.put("th", "Thai");
        map.put("uk", "Ukrainian");
        map.put("ur", "Urdu");
        map.put("vi", "Vietnamese");
    }

    public String lookup(String languageCode) {
        return this.map.get(languageCode);
    }

    public HashMap<String, String> getMap() {
        return this.map;
    }

    public static LanguageLookup getInstance() {
        if (instance == null)
            instance = new LanguageLookup();
        return instance;
    }
}