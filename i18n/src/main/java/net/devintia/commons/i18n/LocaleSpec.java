package net.devintia.commons.i18n;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.Locale;
import java.util.Map;

/**
 * @author BlackyPaw
 * @version 1.0
 */
public class LocaleSpec {

    @Data
    public static class LocaleVersion {

        private String name;
        private File file;

    }

    @Getter
    private Locale locale;
    @Setter( AccessLevel.PACKAGE )
    private Map<String, LocaleVersion> versionHistory;
    @Setter( AccessLevel.PACKAGE )
    @Getter
    private LocaleVersion latestVersion;

    LocaleSpec( final Locale locale ) {
        this.locale = locale;
    }

}
