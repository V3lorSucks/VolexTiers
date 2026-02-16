package com.volextierlist.tiertagger.version;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MinecraftVersion implements Comparable<MinecraftVersion> {
    private static final Pattern VERSION_PATTERN = Pattern.compile("^(\\d+)\\.(\\d+)(?:\\.(\\d+))?.*$");

    private final int major;
    private final int minor;
    private final int patch;

    private MinecraftVersion(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    public static MinecraftVersion parse(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Version string is empty");
        }

        Matcher matcher = VERSION_PATTERN.matcher(raw.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Unsupported version format: " + raw);
        }

        int major = Integer.parseInt(matcher.group(1));
        int minor = Integer.parseInt(matcher.group(2));
        int patch = matcher.group(3) == null ? 0 : Integer.parseInt(matcher.group(3));
        return new MinecraftVersion(major, minor, patch);
    }

    @Override
    public int compareTo(MinecraftVersion other) {
        if (major != other.major) return Integer.compare(major, other.major);
        if (minor != other.minor) return Integer.compare(minor, other.minor);
        return Integer.compare(patch, other.patch);
    }

    public boolean isBetweenInclusive(MinecraftVersion min, MinecraftVersion max) {
        return compareTo(min) >= 0 && compareTo(max) <= 0;
    }

    @Override
    public String toString() {
        return major + "." + minor + "." + patch;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof MinecraftVersion other)) return false;
        return major == other.major && minor == other.minor && patch == other.patch;
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor, patch);
    }
}
