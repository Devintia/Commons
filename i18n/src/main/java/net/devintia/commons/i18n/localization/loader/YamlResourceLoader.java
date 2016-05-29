package net.devintia.commons.i18n.localization.loader;

import net.devintia.commons.i18n.localization.ResourceLoadFailedException;
import net.devintia.commons.i18n.localization.ResourceLoader;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class YamlResourceLoader extends FileResourceLoader implements ResourceLoader {
    private FileConfiguration lookup;
    private String file;

    /**
     * Empty Constructor template for the {@link net.devintia.commons.i18n.localization.ResourceManager#registerLoader(net.devintia.commons.i18n.localization.ResourceLoader)}
     */
    public YamlResourceLoader() {

    }

    /**
     * Load a new YamlResource
     *
     * @param classLoader The classLoader for which this Resource should be loaded
     * @param file        The file to load
     * @throws ResourceLoadFailedException if the stream could not be closed
     */
    public YamlResourceLoader( ClassLoader classLoader, String file ) throws ResourceLoadFailedException {
        super( classLoader );

        this.file = file;

        try {
            load();
        } catch ( ResourceLoadFailedException e ) {
            throw e;
        }
    }

    private void load() throws ResourceLoadFailedException {
        InputStreamReader stream = null;
        try {
            //Get the correct InputStreamReader for this file
            stream = getFileInputStreamReader( file );

            //Read from the InputStreamReader till he is empty
            BufferedReader br = new BufferedReader( stream );
            String line;
            StringBuilder sb = new StringBuilder();
            while ( ( line = br.readLine() ) != null ) {
                sb.append( line );
                sb.append( "\n" );
            }

            //Try to read the YamlConfiguration
            lookup = new YamlConfiguration();
            lookup.loadFromString( sb.toString() );
        } catch ( IOException | InvalidConfigurationException e ) {
            lookup = null;
            throw new ResourceLoadFailedException( e );
        } catch ( ResourceLoadFailedException e ) {
            throw e;
        } finally {
            if ( stream != null ) {
                try {
                    stream.close();
                } catch ( IOException e ) {
                    throw new ResourceLoadFailedException( e );
                }
            }
        }
    }

    /**
     * Get all keys which can be handled by this Resource
     *
     * @return List of keys available
     */
    @Override
    public List<String> getKeys() {
        return new ArrayList<String>( lookup.getKeys( true ) );
    }

    /**
     * Get the key from the YamlConfiguration
     *
     * @param key Key to get
     * @return The object from YAML or null if YAML loading was an error
     */
    @Override
    public String get( String key ) {
        return lookup != null ? lookup.getString( key ) : null;
    }

    /**
     * Get the Formats this Loader can load
     *
     * @return A List of String as formats this Loader supports
     */
    @Override
    public List<String> getFormats() {
        return Arrays.asList( ".yml" );
    }

    /**
     * Force the reload of this Resource
     *
     * @throws ResourceLoadFailedException
     */
    @Override
    public void reload() throws ResourceLoadFailedException {
        try {
            load();
        } catch ( ResourceLoadFailedException e ) {
            throw e;
        }
    }

    /**
     * If plugin gets unloaded remove all refs
     */
    @Override
    public void cleanup() {
        lookup = null;
        file = null;

        super.cleanup();
    }
}
