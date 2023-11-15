package net.pms.media;

import net.pms.configuration.FormatConfiguration;
import net.pms.formats.Format;

/**
 * An immutable struct/record for hold information for a particular audio
 * variant for containers that can constitute multiple "media types".
 */
public class AudioVariantInfo {

    protected final Format format;
    protected final String formatConfiguration;

    /**
     * Creates a new instance.
     *
     * @param format the {@link Format} for this {@link AudioVariantInfo}.
     * @param formatConfiguration the {@link FormatConfiguration}
     *            {@link String} constant for this {@link AudioVariantInfo}.
     */
    public AudioVariantInfo(Format format, String formatConfiguration) {
        this.format = format;
        this.formatConfiguration = formatConfiguration;
    }

    /**
     * @return the {@link Format}.
     */
    public Format getFormat() {
        return format;
    }

    /**
     * @return the {@link FormatConfiguration} {@link String} constant.
     */
    public String getFormatConfiguration() {
        return formatConfiguration;
    }
}


