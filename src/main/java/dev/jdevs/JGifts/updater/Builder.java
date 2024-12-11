package dev.jdevs.JGifts.updater;

import org.bukkit.configuration.file.FileConfiguration;

public class Builder implements Cloneable {
    private final FileConfiguration config;
    private final char separator;
    private final StringBuilder builder;
    public Builder(FileConfiguration config, char separator) {
        this.config = config;
        this.separator = separator;
        this.builder = new StringBuilder();
    }
    private Builder(Builder builder) {
        this.config = builder.config;
        this.separator = builder.separator;
        this.builder = new StringBuilder(builder.toString());
    }
    public void parseLine(String line, boolean checkIfExists) {
        line = line.trim();
        String[] currentSplitLine = line.split(":");
        if (currentSplitLine.length > 2)
            currentSplitLine = line.split(": ");
        String key = currentSplitLine[0].replace("'", "").replace("\"", "");
        if (checkIfExists) {
            while (builder.length() > 0 && !config.contains(builder.toString() + separator + key)) {
                removeLastKey();
            }
        }
        if (builder.length() > 0)
            builder.append(separator);
        builder.append(key);
    }
    public boolean isEmpty() {
        return builder.length() == 0;
    }
    public void clear() {
        builder.setLength(0);
    }
    public void removeLastKey() {
        if (builder.length() == 0)
            return;
        String keyString = builder.toString();
        String[] split = keyString.split("[" + separator + "]");
        int minIndex = Math.max(0, builder.length() - split[split.length - 1].length() - 1);
        builder.replace(minIndex, builder.length(), "");
    }

    @Override
    public String toString() {
        return builder.toString();
    }

    @Override
    @SuppressWarnings("all")
    protected Builder clone() {
        return new Builder(this);
    }
}
