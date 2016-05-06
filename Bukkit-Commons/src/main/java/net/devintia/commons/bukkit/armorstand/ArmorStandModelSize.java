package net.devintia.commons.bukkit.armorstand;

/**
 * The different sizes and thier values
 *
 * @author MiniDigger
 * @version 1.0.0
 */
enum ArmorStandModelSize {
    SMALL( 0.28125 ),
    MEDIUM( 0.4375 ),
    LARGE( 0.625 ),
    SOLID( 1 );

    private double value;

    ArmorStandModelSize( double value ) {
        this.value = value;
    }

    public static ArmorStandModelSize valueOf( double value ) {
        for ( ArmorStandModelSize size : values() ) {
            if ( size.value == value ) {
                return size;
            }
        }
        return null;
    }
}