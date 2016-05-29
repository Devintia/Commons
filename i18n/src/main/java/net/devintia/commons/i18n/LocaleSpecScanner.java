package net.devintia.commons.i18n;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * This scanner will check a given locale specification file (description of which
 * locales are currently available) and then return a list of supported locales as
 * defined in the corresponding file.
 * <p>
 * Example file:
 * <p>
 * de:DE {
 * file: (localedir)/de_DE.lang;
 * }
 * en:US {
 * latest: 1.0.0;
 * }
 * ...
 * </p>
 *
 * @author BlackyPaw
 * @version 1.0
 */
class LocaleSpecScanner {

    private static final String LOCALE_DIR_PREPROCESSOR = "(localedir)";

    private final File file;

    private StreamTokenizer tokenizer;
    private int lookahead;

    private String localeDir;

    LocaleSpecScanner( File file ) {
        this.file = file;
    }

    /**
     * Reads the file the scanner is supposed to parse.
     *
     * @return The list of locales on success.
     * @throws IOException    Thrown if an I/O error occurs whilst reading.
     * @throws ParseException Thrown if the file contains syntactical / semantical errors.
     */
    public List<LocaleSpec> read() throws IOException, ParseException {
        Reader reader = new FileReader( this.file );
        this.tokenizer = new StreamTokenizer( reader );
        this.tokenizer.resetSyntax();
        this.tokenizer.commentChar( '#' );
        this.tokenizer.wordChars( 'a', 'z' );
        this.tokenizer.wordChars( 'A', 'Z' );
        this.tokenizer.wordChars( '0', '9' );
        this.tokenizer.wordChars( '.', '.' );
        this.tokenizer.wordChars( '_', '_' );
        this.tokenizer.wordChars( '$', '$' );
        this.tokenizer.wordChars( '%', '%' );
        this.tokenizer.wordChars( '&', '&' );
        this.tokenizer.wordChars( '(', 'z' );
        this.tokenizer.ordinaryChar( ':' );
        this.tokenizer.ordinaryChar( ';' );
        this.tokenizer.whitespaceChars( '\u0000', '\u0020' );

        this.localeDir = this.file.getParent();
        if ( this.localeDir.endsWith( "/" ) ) {
            this.localeDir = this.localeDir.substring( 0, this.localeDir.length() - 1 );
        }

        try {
            this.next();
            return this.parse();
        } catch ( IOException | ParseException e ) {
            // Rethrow
            throw e;
        } finally {
            reader.close();
        }
    }

    // ----------------------------------------------------------- Utilities
    private void next() throws IOException {
        this.lookahead = this.tokenizer.nextToken();
    }

    private void match( int token ) throws IOException, ParseException {
        if ( this.lookahead != token ) {
            throw new ParseException(
                    String.format( "Unexpected token %c in line %d; expected %c", (char) this.lookahead, this.tokenizer.lineno(), (char) token ),
                    this.tokenizer.lineno() );
        }

        this.next();
    }

    private String ensureWord() throws IOException, ParseException {
        if ( this.lookahead != StreamTokenizer.TT_WORD ) {
            throw new ParseException(
                    String.format( "Unexpected token %c in line %d; expected IDENTIFIER", (char) this.lookahead, this.tokenizer.lineno() ),
                    this.tokenizer.lineno() );
        }
        String ident = this.tokenizer.sval;
        this.next();
        return ident;
    }

    // ----------------------------------------------------------- Parsing
    private List<LocaleSpec> parse() throws IOException, ParseException {
        List<LocaleSpec> locales = new ArrayList<>();

        while ( this.lookahead != StreamTokenizer.TT_EOF ) {
            locales.add( this.parseSpec() );
        }

        return locales;
    }

    private LocaleSpec parseSpec() throws IOException, ParseException {
        String localeName = this.ensureWord();

        String[] split = localeName.split( "_" );
        if ( split.length != 2 ) {
            throw new ParseException( String.format( "Invalid locale tag '%s' in line %d", localeName, this.tokenizer.lineno() ), this.tokenizer.lineno() );
        }

        LocaleSpec spec = new LocaleSpec( new Locale( split[0], split[1] ) );

        Map<String, LocaleSpec.LocaleVersion> history = new HashMap<>();
        Map<String, String> properties = new HashMap<>();

        this.match( '{' );
        while ( this.lookahead != '}' ) {
            String propertyName = this.ensureWord();

            switch ( propertyName ) {
                case "history": {
                    history = this.parseHistory();
                }
                break;

                default: {
                    this.match( ':' );
                    String propertyValue = this.ensureWord();
                    this.match( ';' );
                    properties.put( propertyName, propertyValue );
                }
                break;
            }
        }
        this.match( '}' );

        // Post-Processing:
        spec.setVersionHistory( history );
        if ( properties.containsKey( "latest" ) ) {
            spec.setLatestVersion( history.get( properties.get( "latest" ) ) );
        }

        return spec;
    }

    private Map<String, LocaleSpec.LocaleVersion> parseHistory() throws IOException, ParseException {
        this.match( '{' );

        Map<String, LocaleSpec.LocaleVersion> history = new HashMap<>();
        while ( this.lookahead != '}' ) {
            LocaleSpec.LocaleVersion version = this.parseVersion();
            history.put( version.getName(), version );
        }

        this.match( '}' );

        return history;
    }

    private LocaleSpec.LocaleVersion parseVersion() throws IOException, ParseException {
        String name = this.ensureWord();
        this.match( '{' );

        LocaleSpec.LocaleVersion version = new LocaleSpec.LocaleVersion();

        while ( this.lookahead != '}' ) {
            String propertyName = this.ensureWord();
            this.match( ':' );
            String propertyValue = this.ensureWord();
            this.match( ';' );

            switch ( propertyName ) {
                case "file": {
                    version.setFile( new File( propertyValue.replace( LOCALE_DIR_PREPROCESSOR, this.localeDir ) ) );
                }
                break;
            }
        }

        version.setName( name );
        this.match( '}' );

        return version;
    }
}
