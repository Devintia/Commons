package net.devintia.commons.i18n;

import lombok.Getter;
import lombok.Setter;
import net.devintia.commons.i18n.localization.ResourceLoadFailedException;
import net.devintia.commons.i18n.localization.ResourceLoader;
import net.devintia.commons.i18n.localization.ResourceManager;
import net.devintia.commons.i18n.localization.ResourceNotLoadedException;
import net.devintia.commons.i18n.localization.loader.PropertiesResourceLoader;
import net.devintia.commons.i18n.localization.loader.YamlResourceLoader;
import org.apache.commons.lang.Validate;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class LocaleManager {
    //The ResourceManager to use for this LocaleManager
    private ResourceManager resourceManager;

    //The fallback Locale to use
    @Getter
    private Locale defaultLocale = Locale.GERMANY;

    // Whether to use the default locale also for untranslated messages
    @Getter
    @Setter
    private boolean useDefaultLocaleForMessages = true;

    /**
     * Construct a new LocaleManager for this Plugin
     *
     * @param plugin The plugin for which this LocaleManager should be loaded
     */
    public LocaleManager( Plugin plugin ) {
        this( plugin.getClass().getClassLoader() );
    }

    /**
     * Gets the list of available locales from the specified file.
     *
     * @param path The path of the file to query.
     * @return A list of supported locales as well as their meta-information or null on faillure.
     */
    public List<LocaleSpec> getAvailableLocales( String path ) {
        LocaleSpecScanner scanner = new LocaleSpecScanner( new File( path ) );
        try {
            return scanner.read();
        } catch ( IOException | ParseException e ) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Init / Load all Locales which could be found in the given spec file. This refreshes the languages all 5 minutes
     *
     * @param plugin          Since BungeeCord uses a global Task Handler you need a plugin to register tasks
     * @param executorService Which should be use to reschedule the refresh interval
     * @param path            The path of the file to query.
     */
    public void initFromLocaleSpec( Plugin plugin, BukkitScheduler executorService, final String path ) {
        initFromLocaleSpecWithoutAutorefresh( path );
        executorService.runTaskTimerAsynchronously( plugin, () -> initFromLocaleSpecWithoutAutorefresh( path ), 5 * 60 * 20, 5 * 60 * 20 );
    }

    /**
     * Init / Load all Locales which could be found in the given spec file.
     *
     * @param path The path of the file to query.
     */
    public void initFromLocaleSpecWithoutAutorefresh( String path ) {
        LocaleSpecScanner scanner = new LocaleSpecScanner( new File( path ) );
        try {
            List<LocaleSpec> specs = scanner.read();
            for ( LocaleSpec spec : specs ) {
                try {
                    load( spec.getLocale(), "file://" + spec.getLatestVersion().getFile().getCanonicalPath() );
                } catch ( ResourceLoadFailedException e ) {
                    e.printStackTrace();
                }
            }
        } catch ( IOException | ParseException e ) {
            e.printStackTrace();
        }
    }

    /**
     * Construct a new LocaleManager for the given Classloader
     *
     * @param classLoader The classLoader for which the LocaleManager should be loaded
     */
    public LocaleManager( ClassLoader classLoader ) {
        resourceManager = new ResourceManager( classLoader );
        resourceManager.registerLoader( new YamlResourceLoader() );
        resourceManager.registerLoader( new PropertiesResourceLoader() );
    }

    /**
     * Load a new Locale into the ResourceManager for this Plugin
     *
     * @param locale Locale which should be loaded
     * @param param  The param which should be given to the ResourceLoader
     * @throws ResourceLoadFailedException if the loading has thrown any Error
     */
    public synchronized void load( Locale locale, String param ) throws ResourceLoadFailedException {
        //Validate the input
        Validate.notNull( locale );
        Validate.notNull( param );

        resourceManager.load( locale, param );
    }

    /**
     * Gets the correct String out of the Locale. If the locale given is not loaded by the underlying ResourceManager
     * it takes the set default Locale to read the String from.
     *
     * @param locale Locale which should be read for
     * @param key    The key which should be looked up
     * @return The String stored in the ResourceLoader
     * @throws ResourceNotLoadedException  If the Resource was not registered
     * @throws ResourceLoadFailedException If the Resource was cleared out and could not be reloaded into the Cache
     */
    private String getTranslationString( Locale locale, String key ) throws ResourceNotLoadedException, ResourceLoadFailedException {
        return resourceManager.get( locale, key );
    }

    /**
     * Check if the given Locale has been loaded by the ResourceManager. If not return the default Locale
     *
     * @param locale Locale which should be checked
     * @return The default locale or the param
     */
    private Locale checkForDefault( Locale locale ) {
        if ( !resourceManager.isLoaded( locale ) ) {
            return defaultLocale;
        }

        return locale;
    }

    /**
     * Change the default Locale for this plugin.
     * It must be loaded before a Locale can be set as default.
     *
     * @param locale Locale which should be used as default Fallback
     */
    public void setDefaultLocale( Locale locale ) {
        //Validate the locale
        Validate.notNull( locale, "Locale can not be null" );
        Validate.isTrue( resourceManager.isLoaded( locale ), "Locale has not been loaded" );

        defaultLocale = locale;
    }

    /**
     * Translate the Text based on the locale.
     * If the locale is not loaded the LocaleManager will try to load it, if this fails
     * it will use the default Locale. If this is also not loaded you will get a ResourceNotLoadedException
     *
     * @param locale         Locale which should be used to translate
     * @param translationKey The key in the ResourceLoader which should be translated
     * @param args           The Arguments which will be passed into the String when translating
     * @return The translated String
     * @throws ResourceNotLoadedException  when the Resource for the locale could not be loaded or the key is missing
     * @throws ResourceLoadFailedException when the GC has cleared out the ResourceLoader and it could not be reloaded into the cache
     */
    public String translate( Locale locale, String translationKey, Object... args ) throws ResourceNotLoadedException, ResourceLoadFailedException {
        //Validate the Player
        Validate.notNull( locale, "Locale can not be null" );
        Validate.notNull( translationKey, "The translationKey can not be null" );

        //Get the resource and translate
        Locale playerLocale = checkForDefault( locale );

        String translationString;
        try {
            translationString = getTranslationString( playerLocale, translationKey );
        } catch ( ResourceNotLoadedException | ResourceLoadFailedException e ) {
            translationString = getTranslationString( playerLocale = defaultLocale, translationKey );
        }

        // Check for untranslated messages
        if ( translationString == null ) {
            throw new ResourceNotLoadedException( "The key(" + translationKey + ") is not present in the Locale " + playerLocale.toString() );
        }

        MessageFormat msgFormat = new MessageFormat( translationString );
        msgFormat.setLocale( playerLocale );
        return msgFormat.format( args );
    }

    /**
     * Translate the Text based on the Player locale / default Locale.
     * If the locale from the player is not loaded the LocaleManager
     * will use the default Locale. If this is also not loaded it
     * will use the translationKey as text and give it back
     *
     * @param commandSender  CommandSender which can be a Player, if a Player the locale from it will be used otherwise the default one will be taken
     * @param translationKey The key in the ResourceLoader which should be translated
     * @param args           The Arguments which will be passed into the String when translating
     * @return The translated String
     * @throws ResourceNotLoadedException  when the Resource for the locale could not be loaded or the key is missing
     * @throws ResourceLoadFailedException when the GC has cleared out the ResourceLoader and it could not be reloaded into the cache
     */
    public String translate( CommandSender commandSender, String translationKey, Object... args ) throws ResourceNotLoadedException, ResourceLoadFailedException {
        //Validate the CommandSender
        Validate.notNull( commandSender, "Commandsender can not be null" );
        Validate.notNull( translationKey, "The translationKey can not be null" );

        //Get the resource and translate
        String translationString = getTranslationString( defaultLocale, translationKey );
        if ( translationString == null ) {
            throw new ResourceNotLoadedException( "The key(" + translationKey + ") is not present in the Locale " + defaultLocale.toString() );
        }

        MessageFormat msgFormat = new MessageFormat( translationString );
        msgFormat.setLocale( defaultLocale );
        return msgFormat.format( args );
    }

    /**
     * Register a new custom ResourceLoader. See {@link ResourceManager#registerLoader(ResourceLoader)}
     *
     * @param loader
     */
    public void registerLoader( ResourceLoader loader ) {
        //Check if loader is good
        Validate.notNull( loader );

        resourceManager.registerLoader( loader );
    }

    /**
     * Gets a list of all loaded Locales
     *
     * @return Unmodifiable List
     */
    public List<Locale> getLoadedLocales() {
        return Collections.unmodifiableList( resourceManager.getLoadedLocales() );
    }

    /**
     * Tells the ResourceManager to reload all Locale Resources which has been loaded by this Plugin
     */
    public synchronized void reload() {
        resourceManager.reload();
    }

    /**
     * Be sure to remove resources loaded and to remove refs
     */
    public synchronized void cleanup() {
        resourceManager.cleanup();
        resourceManager = null;
    }
}
