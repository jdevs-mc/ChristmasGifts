package dev.jdevs.JGifts.updater;

import com.google.common.base.Preconditions;
import dev.jdevs.JGifts.Christmas;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.YamlConstructor;
import org.bukkit.configuration.file.YamlRepresenter;
import org.bukkit.plugin.Plugin;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Updater {
    private final Christmas plugin;
    public Updater(Christmas plugin) {
        this.plugin = plugin;
    }
    private final char SEPARATOR = '.';
    public void update(String resourceName, File toUpdate, String... ignoredSections) throws IOException {
        update(plugin, resourceName, toUpdate, Arrays.asList(ignoredSections));
    }
    private void update(Plugin plugin, String resourceName, File toUpdate, List<String> ignoredSections) throws IOException {
        Preconditions.checkArgument(toUpdate.exists(), "Update file doesn't exist!");
        FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource(resourceName), StandardCharsets.UTF_8));
        FileConfiguration currentConfig = YamlConfiguration.loadConfiguration(toUpdate);
        Map<String, String> comments = parseComments(plugin, resourceName, defaultConfig);
        Map<String, String> ignoredSectionsValues = parseIgnoredSections(toUpdate, comments, ignoredSections == null ? Collections.emptyList() : ignoredSections);
        StringWriter writer = new StringWriter();
        write(defaultConfig, currentConfig, new BufferedWriter(writer), comments, ignoredSectionsValues);
        String value = writer.toString();
        Path toUpdatePath = toUpdate.toPath();
        if (!value.equals(new String(Files.readAllBytes(toUpdatePath), StandardCharsets.UTF_8))) {
            Files.write(toUpdatePath, value.getBytes(StandardCharsets.UTF_8));
        }
    }

    private void write(FileConfiguration defaultConfig, FileConfiguration currentConfig, BufferedWriter writer, Map<String, String> comments, Map<String, String> ignoredSectionsValues) throws IOException {
        FileConfiguration parserConfig = new YamlConfiguration();
       for (String fullKey : defaultConfig.getKeys(true)) {
           String indents = getIndents(fullKey);
           if (!ignoredSectionsValues.isEmpty()) {
               if (writeIgnoredSectionValueIfExists(ignoredSectionsValues, writer, fullKey))
                   continue;
           }
           writeCommentIfExists(comments, writer, fullKey, indents);
           Object currentValue = currentConfig.get(fullKey);
           if (currentValue == null)
               currentValue = defaultConfig.get(fullKey);
           String[] splitFullKey = fullKey.split("[" + SEPARATOR + "]");
           String trailingKey = splitFullKey[splitFullKey.length - 1];
           if (currentValue instanceof ConfigurationSection) {
               writeConfigurationSection(writer, indents, trailingKey, (ConfigurationSection) currentValue);
               continue;
           }
           writeYamlValue(parserConfig, writer, indents, trailingKey, currentValue);
       }
        String danglingComments = comments.get(null);
        if (danglingComments != null)
            writer.write(danglingComments);
        writer.close();
    }
    private Map<String, String> parseComments(Plugin plugin, String resourceName, FileConfiguration defaultConfig) throws IOException {
        List<String> keys = new ArrayList<>(defaultConfig.getKeys(true));
        BufferedReader reader = new BufferedReader(new InputStreamReader(plugin.getResource(resourceName)));
        Map<String, String> comments = new LinkedHashMap<>();
        StringBuilder commentBuilder = new StringBuilder();
        Builder builder = new Builder(defaultConfig, SEPARATOR);
        String currentValidKey = null;
        String line;
        while ((line = reader.readLine()) != null) {
            String trimmedLine = line.trim();
            if (trimmedLine.startsWith("-")) continue;
            if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
                commentBuilder.append(trimmedLine).append("\n");
            } else {
                if (!line.startsWith(" ")) {
                    builder.clear();
                    currentValidKey = trimmedLine;
                }
                builder.parseLine(trimmedLine, true);
                String key = builder.toString();
                if (commentBuilder.length() > 0) {
                    comments.put(key, commentBuilder.toString());
                    commentBuilder.setLength(0);
                }
                int nextKeyIndex = keys.indexOf(builder.toString()) + 1;
                if (nextKeyIndex < keys.size()) {
                    String nextKey = keys.get(nextKeyIndex);
                    while (!builder.isEmpty() && !nextKey.startsWith(builder.toString())) {
                        builder.removeLastKey();
                    }
                    if (builder.isEmpty()) {
                        builder.parseLine(currentValidKey, false);
                    }
                }
            }
        }
        reader.close();
        if (commentBuilder.length() > 0)
            comments.put(null, commentBuilder.toString());
        return comments;
    }
    @SuppressWarnings("all")
    private Map<String, String> parseIgnoredSections(File toUpdate, Map<String, String> comments, List<String> ignoredSections) throws IOException {
        Map<String, String> ignoredSectionValues = new LinkedHashMap<>(ignoredSections.size());
        DumperOptions options = new DumperOptions();
        options.setLineBreak(DumperOptions.LineBreak.UNIX);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(new YamlConstructor(), new YamlRepresenter(), options);
        Map<Object, Object> root = (Map<Object, Object>) yaml.load(new FileReader(toUpdate));
        ignoredSections.forEach(section -> {
            String[] split = section.split("[" + SEPARATOR + "]");
            String key = split[split.length - 1];
            Map<Object, Object> map = getSection(section, root);
            StringBuilder keyBuilder = new StringBuilder();
            for (int i = 0; i < split.length; i++) {
                if (i != split.length - 1) {
                    if (keyBuilder.length() > 0)
                        keyBuilder.append(SEPARATOR);

                    keyBuilder.append(split[i]);
                }
            }
            ignoredSectionValues.put(section, buildIgnored(key, map, comments, keyBuilder, new StringBuilder(), yaml));
        });
        return ignoredSectionValues;
    }
    @SuppressWarnings("all")
    private Map<Object, Object> getSection(String fullKey, Map<Object, Object> root) {
        String[] keys = fullKey.split("[" + SEPARATOR + "]", 2);
        String key = keys[0];
        Object value = root.get(getKeyAsObject(key, root));
        if (keys.length == 1) {
            if (value instanceof Map)
                return root;
            throw new IllegalArgumentException("Ignored sections must be a ConfigurationSection not a value!");
        }
        if (!(value instanceof Map))
            throw new IllegalArgumentException("Invalid ignored ConfigurationSection specified!");
        return getSection(keys[1], (Map<Object, Object>) value);
    }
    @SuppressWarnings("all")
    private String buildIgnored(String fullKey, Map<Object, Object> ymlMap, Map<String, String> comments, StringBuilder keyBuilder, StringBuilder ignoredBuilder, Yaml yaml) {
        String[] keys = fullKey.split("[" + SEPARATOR + "]", 2);
        String key = keys[0];
        Object originalKey = getKeyAsObject(key, ymlMap);
        if (keyBuilder.length() > 0)
            keyBuilder.append(".");

        keyBuilder.append(key);
        if (!ymlMap.containsKey(originalKey)) {
            if (keys.length == 1)
                throw new IllegalArgumentException("Invalid ignored section: " + keyBuilder);

            throw new IllegalArgumentException("Invalid ignored section: " + keyBuilder + "." + keys[1]);
        }
        String comment = comments.get(keyBuilder.toString());
        String indents = getIndents(keyBuilder.toString());
        if (comment != null)
            ignoredBuilder.append(addIndentation(comment, indents)).append("\n");

        ignoredBuilder.append(addIndentation(key, indents)).append(":");
        Object obj = ymlMap.get(originalKey);
        if (obj instanceof Map) {
            Map<Object, Object> map = (Map<Object, Object>) obj;
            if (map.isEmpty()) {
                ignoredBuilder.append(" {}\n");
            } else {
                ignoredBuilder.append("\n");
            }
            StringBuilder preLoopKey = new StringBuilder(keyBuilder);
            for (Object o : map.keySet()) {
                buildIgnored(o.toString(), map, comments, keyBuilder, ignoredBuilder, yaml);
                keyBuilder = new StringBuilder(preLoopKey);
            }
        } else {
            writeIgnoredValue(yaml, obj, ignoredBuilder, indents);
        }
        return ignoredBuilder.toString();
    }
    private void writeIgnoredValue(Yaml yaml, Object toWrite, StringBuilder ignoredBuilder, String indents) {
        String yml = yaml.dump(toWrite);
        if (toWrite instanceof Collection) {
            ignoredBuilder.append("\n").append(addIndentation(yml, indents)).append("\n");
        } else {
            ignoredBuilder.append(" ").append(yml);
        }
    }

    private String addIndentation(String s, String indents) {
        StringBuilder builder = new StringBuilder();
        String[] split = s.split("\n");
        for (String value : split) {
            if (builder.length() > 0)
                builder.append("\n");

            builder.append(indents).append(value);
        }
        return builder.toString();
    }
    private void writeCommentIfExists(Map<String, String> comments, BufferedWriter writer, String fullKey, String indents) throws IOException {
        String comment = comments.get(fullKey);
        if (comment != null)
            writer.write(indents + comment.substring(0, comment.length() - 1).replace("\n", "\n" + indents) + "\n");
    }
    private Object getKeyAsObject(String key, Map<Object, Object> sectionContext) {
        if (sectionContext.containsKey(key))
            return key;
        try {
            Float keyFloat = Float.parseFloat(key);

            if (sectionContext.containsKey(keyFloat))
                return keyFloat;
        } catch (NumberFormatException ignored) {}
        try {
            Double keyDouble = Double.parseDouble(key);

            if (sectionContext.containsKey(keyDouble))
                return keyDouble;
        } catch (NumberFormatException ignored) {}
        try {
            Integer keyInteger = Integer.parseInt(key);

            if (sectionContext.containsKey(keyInteger))
                return keyInteger;
        } catch (NumberFormatException ignored) {}
        try {
            Long longKey = Long.parseLong(key);

            if (sectionContext.containsKey(longKey))
                return longKey;
        } catch (NumberFormatException ignored) {}
        return null;
    }
	private void writeYamlValue(final FileConfiguration parserConfig, final BufferedWriter bufferedWriter, final String indents, final String trailingKey, final Object currentValue) throws IOException {
		parserConfig.set(trailingKey, currentValue);
		String yaml = parserConfig.saveToString();
		yaml = yaml.substring(0, yaml.length() - 1).replace("\n", "\n" + indents);
		final String toWrite = indents + yaml + "\n";
		parserConfig.set(trailingKey, null);
		bufferedWriter.write(toWrite);
	}
    private boolean writeIgnoredSectionValueIfExists(final Map<String, String> ignoredSectionsValues, final BufferedWriter bufferedWriter, final String fullKey) throws IOException {
        String ignored = ignoredSectionsValues.get(fullKey);
        if (ignored != null) {
            bufferedWriter.write(ignored);
            return true;
        }
        for (final Map.Entry<String, String> entry : ignoredSectionsValues.entrySet()) {
            if (isSubKeyOf(entry.getKey(), fullKey)) {
                return true;
            }
        }
        return false;
    }
	private void writeConfigurationSection(final BufferedWriter bufferedWriter, final String indents, final String trailingKey, final ConfigurationSection configurationSection) throws IOException {
		bufferedWriter.write(indents + trailingKey + ":");
		if (!(configurationSection).getKeys(false).isEmpty()) {
			bufferedWriter.write("\n");
		} else {
			bufferedWriter.write(" {}\n");
		}
	}
    boolean isSubKeyOf(final String parentKey, final String subKey) {
        if (parentKey.isEmpty())
            return false;
        return subKey.startsWith(parentKey)
                && subKey.substring(parentKey.length()).startsWith(String.valueOf(SEPARATOR));
    }
    private String getIndents(final String key) {
        final String[] splitKey = key.split("[" + SEPARATOR + "]");
        final StringBuilder builder = new StringBuilder();
        for (int i = 1; i < splitKey.length; i++) {
            builder.append("  ");
        }
        return builder.toString();
    }
}
