package net.devintia.commons.i18n.localization.loader;

import net.devintia.commons.i18n.localization.ResourceLoadFailedException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;

public abstract class FileResourceLoader {
    protected ClassLoader classLoader;

    /**
     * Empty Constructor template for the {@link net.devintia.commons.i18n.localization.ResourceManager#registerLoader(net.devintia.commons.i18n.localization.ResourceLoader)}
     */
    public FileResourceLoader() {

    }

    /**
     * Load a new FileResource. Every Loader which want to read from either the JAR or the Filesystem should extend this Class.
     * It first checks if the File can be found on the Disk and then if not it gets read from the Jar
     *
     * @param classLoader The classLoader from and for which this Resource should be loaded
     */
    public FileResourceLoader( ClassLoader classLoader ) {
        this.classLoader = classLoader;
    }

    /**
     * Gets the Resource at the given relative path. It first searches the Plugins Datafolder and then it checks the JAR for
     * an entry in it.
     *
     * @param path The path which should be loaded
     * @return An InputStreamReader for the Resource (either from out of the JAR or from disk)
     * @throws ResourceLoadFailedException if the Encoding is wrong or the File was not found
     */
    protected InputStreamReader getFileInputStreamReader( String path ) throws ResourceLoadFailedException {
        try {
            if ( !path.startsWith( "file://" ) ) {
                URL resourceUrl = classLoader.getResource( path );

                if ( resourceUrl != null ) {
                    System.out.println( resourceUrl.getPath() );

                    //If the file is not on the Disk read it from the JAR
                    return new InputStreamReader( resourceUrl.openStream(), "UTF8" );
                }
            } else {
                File file = new File( path.substring( 7 ) );

                if ( file.isFile() ) {
                    return new InputStreamReader( new FileInputStream( file ), "UTF8" );
                }
            }
        } catch ( UnsupportedEncodingException | FileNotFoundException e ) {
            throw new ResourceLoadFailedException( e );
        } catch ( IOException e ) {
            e.printStackTrace();
        }

        throw new ResourceLoadFailedException( "Resource not found" );
    }

    /**
     * Remove the Reference on the Plugin
     */
    protected void cleanup() {
        this.classLoader = null;
    }
}
