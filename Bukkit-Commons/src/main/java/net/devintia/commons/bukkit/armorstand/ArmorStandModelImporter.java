package net.devintia.commons.bukkit.armorstand;

import lombok.extern.java.Log;
import net.minecraft.server.v1_9_R1.Item;
import net.minecraft.server.v1_9_R1.MinecraftKey;
import org.bukkit.craftbukkit.v1_9_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Imports ArmorstandModels
 *
 * @author MiniDigger
 * @version 1.0.0
 */
@Log
public class ArmorStandModelImporter {

    private static ArmorStandModelHandler handler;

    /**
     * Sets the handler for this importer, used to store models and despawning them onDisable
     *
     * @param handler the new handler
     */
    public static void setHandler( ArmorStandModelHandler handler ) {
        ArmorStandModelImporter.handler = handler;
    }

    /**
     * Imports a new model. A model is a file with commands, retried from mrgarrets model editor.<br>
     * HowTo generate the commands: http://puu.sh/oRjt2/f308433992.png
     *
     * @param name the name that should be assigned to the model
     * @param file the file to load the model from
     * @return the imported model
     */
    public static ArmorStandModel importModel( String name, File file ) {
        List<String> commands = new ArrayList<>();
        try ( BufferedReader reader = new BufferedReader( new FileReader( file ) ) ) {
            String buffer;
            while ( ( buffer = reader.readLine() ) != null ) {
                commands.add( buffer );
            }
        } catch ( IOException e ) {
            e.printStackTrace();
            return null;
        }

        Collections.reverse( commands );
        return importModel( name, commands );
    }

    /**
     * Imports a new model. A model consists of a list of commands. The are in the REVERSED order when generated with mrgarrets model editor!
     *
     * @param modelName the name that should be assigned to the model
     * @param commands  the list with the commands
     * @return the imported model
     */
    public static ArmorStandModel importModel( String modelName, List<String> commands ) {
        List<ArmorStandModelEntity> entites = new ArrayList<>();
        String tag = null;
        for ( String command : commands ) {
            // tag
            if ( command.startsWith( "INIT:scoreboard objectives add " ) ) {
                tag = command.replace( "INIT:scoreboard objectives add ", "" ).replace( " dummy", "" );
            } else if ( command.startsWith( "execute @e[type=Squid] ~ ~ ~ " ) ) {
                // skip last marker armorstand
                if ( tag != null && command.contains( tag + "_M" ) ) {
                    continue;
                }

                command = command.replace( "execute @e[type=Squid] ~ ~ ~ ", "" );
                // armorstand
                if ( command.startsWith( "summon" ) ) {
                    command = command.replace( "summon ArmorStand ", "" );
                    //location
                    String locString = command.substring( 0, command.indexOf( "{" ) - 1 );
                    Vector location;
                    try {
                        location = getVector( locString );
                    } catch ( Exception e ) {
                        log.warning( e.getMessage() );
                        continue;
                    }
                    command = command.replace( locString + " ", "" );
                    //tags
                    try {
                        ArmorStandModelEntity entity = parseTags( command );
                        entity.setLocation( location );
                        entites.add( entity );
                    } catch ( Exception e ) {
                        e.printStackTrace();
                        log.warning( e.getMessage() );
                        continue;
                    }
                } else if ( command.startsWith( "setblock" ) ) {
                    command = command.replace( "setblock ", "" );
                    //location
                    String[] temp = command.split( " " );
                    if ( temp.length < 4 ) {
                        log.warning( "Could not get location for solid block " + command );
                        continue;
                    }

                    Vector location;
                    try {
                        double x = getDouble( temp[0] );
                        double y = getDouble( temp[1] );
                        double z = getDouble( temp[2] );
                        location = new Vector( x, y, z );
                    } catch ( Exception ex ) {
                        log.warning( ex.getMessage() );
                        continue;
                    }
                    //item
                    MinecraftKey mk = new MinecraftKey( temp[3] );
                    ItemStack item = CraftItemStack.asNewCraftStack( Item.REGISTRY.get( mk ) );
                    if ( temp.length == 5 ) {
                        item.setDurability( Short.parseShort( temp[4] ) );
                    }

                    ArmorStandModelEntity entity = new ArmorStandModelEntity( item, ArmorStandModelSize.SOLID, "", new Vector( 0, 0, 0 ) );
                    entity.setLocation( location );
                    entites.add( entity );
                } else {
                    log.warning( "Unknown spawn command: " + command );
                    continue;
                }
            } else if ( command.startsWith( "tp @e[type=ArmorStand" ) ) {
                // hanlde tp
                if ( tag == null ) {
                    log.warning( "Can't handle tp command if tag is undefined!" );
                    continue;
                }

                command = command.replace( "tp @e[type=ArmorStand,tag=" + tag + ",name=", "" ).replace( ",score_" + tag + "_min=1,score_" + tag + "=21]", "" );
                String[] temp = command.split( " " );
                if ( temp.length != 4 ) {
                    log.warning( "Unexpected arr lenght: " + command );
                    continue;
                }

                String name = temp[0];
                Vector loc;
                try {
                    double x = getDouble( temp[1] );
                    double y = getDouble( temp[2] );
                    double z = getDouble( temp[3] );
                    loc = new Vector( x, y, z );
                } catch ( Exception ex ) {
                    log.warning( ex.getMessage() );
                    continue;
                }
            }
        }

        ArmorStandModel model = new ArmorStandModel( modelName, entites );

        handler.add( model );

        return model;
    }

    /**
     * Converts a relative location string into a relative location vector<br>
     * ~1 ~ ~-2 -> (1,0,-2)
     *
     * @param input the string to parse
     * @return the parsed vector
     */
    private static Vector getVector( String input ) throws Exception {
        String[] split = input.split( " " );
        if ( split.length != 3 ) {
            throw new Exception( "Could not getVector from " + input + ": doesn't has 3 values" );
        }

        try {
            double x = getDouble( split[0] );
            double y = getDouble( split[1] );
            double z = getDouble( split[2] );

            return new Vector( x, y, z );
        } catch ( Exception ex ) {
            throw new Exception( "Could not getVector from: " + input + ": " + ex.getMessage() );
        }
    }

    /**
     * Converts strings like ~1, ~-2.5 or ~ into doubles like 1, -2,5 or 0
     *
     * @param input the string to parse
     * @return the result
     */
    private static double getDouble( String input ) throws Exception {
        if ( !input.startsWith( "~" ) ) {
            throw new Exception( input + " is not a valid input!" );
        }

        input = input.replace( "~", "" );
        if ( input.length() == 0 ) {
            return 0;
        } else {
            return Double.parseDouble( input );
        }
    }


    private static ArmorStandModelEntity parseTags( String input ) throws Exception {
        if ( !input.startsWith( "{" ) || !input.endsWith( "}" ) ) {
            throw new Exception( "Could not parse tags, input needs to start with '{' and end with '}' : " + input );
        }
        input = input.replaceFirst( "\\{", "" ).substring( 0, input.length() - 1 );

        //custom name
        String customName = input.substring( input.indexOf( "CustomName:" ) + 11, input.indexOf( ",Tags:" ) );

        //items and size
        // size = small
        if ( input.contains( "Passengers" ) ) {
            Vector headPose = parseRotation( input );
            return new ArmorStandModelEntity( parseItem( input ), ArmorStandModelSize.SMALL, customName, headPose );
        }
        // size = medium
        else if ( input.contains( "Small" ) ) {
            Vector headPose = parsePose( input );
            return new ArmorStandModelEntity( parseItem( input ), ArmorStandModelSize.MEDIUM, customName, headPose );
        }
        // size = large
        else {
            Vector headPose = parsePose( input );
            return new ArmorStandModelEntity( parseItem( input ), ArmorStandModelSize.LARGE, customName, headPose );
        }
    }

    /**
     * Parses the armorcontent to find the head item stack
     * the string should contain<br>
     * {@code ArmorItems:[{},{},{},{id:<mat>:[damage}]}}
     *
     * @param input the string to parse
     * @return the parsed itemstack
     */
    private static ItemStack parseItem( String input ) {
        String mat = input.substring( input.indexOf( "ArmorItems:[{},{},{},{id:" ) + 25, input.indexOf( "}]}" ) );
        int damage = -1;
        if ( mat.contains( ",Damage:" ) ) {
            damage = Integer.parseInt( mat.split( ":" )[1] );
            mat = mat.replace( ",Damage:" + damage, "" );
        }
        MinecraftKey mk = new MinecraftKey( mat );
        ItemStack item = CraftItemStack.asNewCraftStack( Item.REGISTRY.get( mk ) );
        if ( damage != -1 ) {
            item.setDurability( (short) damage );
        }
        return item;
    }

    /**
     * Parses the head pose out of a summon command
     * Format: {Pose:{Head:[0f,0f,-50f]}
     *
     * @param input the input to parse
     * @return the parsed head pose vector
     */
    private static Vector parsePose( String input ) {
        if ( !input.contains( "Pose:{Head:[" ) ) {
            return new Vector( 0, 0, 0 );
        }

        String arr = input.substring( input.indexOf( "Pose:{Head:[" ) + 12, input.indexOf( "}" ) - 1 );
        String[] values = arr.split( "," );
        float x = Float.parseFloat( values[0] );
        float y = Float.parseFloat( values[1] );
        float z = Float.parseFloat( values[2] );
        return new Vector( x, y, z );
    }

    /**
     * Parses the rotation of a mounted villager out of a summon command
     * Format: Rotation:[-50f,0f],ArmorItems
     *
     * @param input the input to parse
     * @return the parsed head pose vector
     */
    private static Vector parseRotation( String input ) {
        if ( !input.contains( "Rotation:[" ) || !input.contains( "],ArmorItems" ) ) {
            return new Vector( 0, 0, 0 );
        }

        String arr = input.substring( input.indexOf( "Rotation:[" ) + 10, input.indexOf( "],ArmorItems" ) - 1 );
        String[] values = arr.split( "," );
        float pitch = Float.parseFloat( values[0] );
        float yaw = Float.parseFloat( values[1] );
        return new Vector( pitch, yaw, 0 );
    }
}
