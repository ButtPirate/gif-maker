package ru.bpirate;

public enum DitherFilter {
    NONE("none", "none"),
    BAYER1("bayer:bayer_scale=1", "bayer_1"),
    BAYER2("bayer:bayer_scale=2", "bayer_2"),
    BAYER5("bayer:bayer_scale=5", "bayer_5"),
    FLOYDSTEINBERG("floyd_steinberg", "floyd"),
    SIERRA("sierra2", "sierra"),
    SIERRA4A("sierra2_4a", "");


    private final String fullFilter;
    private final String shortName;

    DitherFilter(String fullFilter, String shortName) {
        this.fullFilter = fullFilter;
        this.shortName = shortName;
    }

    public String getFullFilter() {
        return fullFilter;
    }

    public String getShortName() {
        return shortName;
    }
}
