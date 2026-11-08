package me.jxl.breadboard.models;

import java.util.Arrays;
import java.util.List;

public class HomeViewFixtures {
    public static final String PRIMARY_USER_NAME = "Marius";
    public static final String LOCATION = "Dattenfeld, Windeck";
    public static final String TEMPERATURE = "14°";
    public static final String WEATHER_CONDITION = "Teils sonnig • H:16° T:8°";

    public static final String SIRI_QUERY = "Siri, wann muss ich zum Sport losfahren?";
    public static final String SIRI_DESTINATION = "Sportpark Windeck";
    public static final String SIRI_ROUTE = "Über L333 • Freie Fahrt";
    public static final String SIRI_DISTANCE = "4,2 km";
    public static final String SIRI_TRAVEL_TIME = "7 Min.";
    public static final String SIRI_ANSWER = "Du solltest um 10:42 Uhr losfahren, um pünktlich im Sportpark Windeck zu sein.";

    public static final String SONG_TITLE = "Midnight City";
    public static final String SONG_ARTIST = "M83";
    public static final String SONG_ALBUM = "Hurry Up, We're Dreaming";
    public static final List<String> SONG_LYRICS = Arrays.asList(
            "Waiting in a car",
            "Waiting for a ride in the dark",
            "The night city grows",
            "Look and see her eyes, they glow",
            "Waiting in a car",
            "Waiting for a ride in the dark",
            "The city is my church",
            "It wraps me in the blinding twilight"
    );

    public static final String RECIPE_TITLE = "Kürbis-Ingwer-Suppe";
    public static final String RECIPE_SUBTITLE = "Home Cookbook • 35 Min. • 4 Portionen";
    public static final List<String> RECIPE_INGREDIENTS = Arrays.asList(
            "800g Hokkaido-Kürbis (gewürfelt)",
            "1 Zwiebel & 2 Knoblauchzehen",
            "30g frischer Ingwer (gerieben)",
            "600ml Gemüsebrühe",
            "200ml Kokosmilch",
            "2 EL Olivenöl & 6 frische Salbeiblätter",
            "Prise Muskatnuss, Salz & Pfeffer"
    );
    public static final List<String> RECIPE_STEPS = Arrays.asList(
            "Hokkaido-Kürbis und Ingwer in feine Würfel schneiden und mit Schalotten in Olivenöl andünsten.",
            "Kürbiswürfel mitrösten und mit warmer Gemüsebrühe ablöschen.",
            "20 Minuten sanft köcheln lassen, bis der Kürbis weich ist.",
            "Kokosmilch einrühren und cremig fein pürieren.",
            "Mit frischen Salbeiblättern auf dunklem Leinen anrichten und servieren."
    );

    public static class MemojiProfile {
        public final String name;
        public final String detail;
        public final int[] gradientColors;

        public MemojiProfile(String name, String detail, int[] gradientColors) {
            this.name = name;
            this.detail = detail;
            this.gradientColors = gradientColors;
        }
    }

    public static final List<MemojiProfile> MEMOJIS = Arrays.asList(
            new MemojiProfile("Oma", "Weiß, Brille", new int[]{0xFFF472B6, 0xFFDB2777}),
            new MemojiProfile("Amira", "Grauer Hijab", new int[]{0xFF9CA3AF, 0xFF4B5563}),
            new MemojiProfile("Leo", "Rote Locken", new int[]{0xFFFB923C, 0xFFEA580C}),
            new MemojiProfile("Mei", "Schwarzes Haar", new int[]{0xFFC084FC, 0xFF9333EA}),
            new MemojiProfile("Dev", "Rote Brille", new int[]{0xFFFBBF24, 0xFFD97706}),
            new MemojiProfile("Marius", "Brauner Bart", new int[]{0xFF34D399, 0xFF059669})
    );
}
