package net.devintia.commons.bukkit.pagination;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Martin on 29.05.2016.
 */
public class Paginator {

    public static final int GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH = 55; // Will never wrap, even with the largest characters
    public static final int AVERAGE_CHAT_PAGE_WIDTH = 65; // Will typically not wrap using an average character distribution
    public static final int UNBOUNDED_PAGE_WIDTH = Integer.MAX_VALUE;
    public static final int OPEN_CHAT_PAGE_HEIGHT = 20; // The height of an expanded chat window
    public static final int CLOSED_CHAT_PAGE_HEIGHT = 10; // The height of the default chat window
    public static final int UNBOUNDED_PAGE_HEIGHT = Integer.MAX_VALUE;

    /**
     * Breaks a chat compontent message up into pages using the default width and height.
     *
     * @param components The array with all the compontents that should be paginated
     * @param pageNumber The page number to fetch.
     * @return A single chat page.
     */
    public static ChatPage paginate( BaseComponent[] components, int pageNumber ) {
        return paginate( components, pageNumber, GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH, CLOSED_CHAT_PAGE_HEIGHT );
    }

    /**
     * Breaks a chat compontent message up into pages using the default width and height.
     *
     * @param components The array with all the compontents that should be paginated
     * @param pageNumber The page number to fetch.
     * @param lineLength The desired width of a chat line.
     * @param pageHeight The desired number of lines in a page.
     * @return A single chat page.
     */
    public static ChatPage paginate( BaseComponent[] components, int pageNumber, int lineLength, int pageHeight ) {
        BaseComponent[][] lines = wordWrap( components, lineLength );

        int totalPages = lines.length / pageHeight + ( lines.length % pageHeight == 0 ? 0 : 1 );
        int actualPageNumber = pageNumber <= totalPages ? pageNumber : totalPages;

        int from = ( actualPageNumber - 1 ) * pageHeight;
        int to = from + pageHeight <= lines.length ? from + pageHeight : lines.length;

        BaseComponent[][] page = new BaseComponent[to - from][];
        System.arraycopy( lines, from, page, 0, to - from );

        return new ChatPage( page, actualPageNumber, totalPages );
    }

    /**
     * Breaks a raw line up into a series of lines. Words are wrapped using
     * spaces as decimeters and the newline character is respected.
     *
     * @param input      The raw line to break.
     * @param lineLength The length of a line of text.
     * @return An array of word-wrapped lines.
     */
    public static BaseComponent[][] wordWrap( BaseComponent[] input, int lineLength ) {
        // A null string is a single line
        if ( input == null ) {
            return new BaseComponent[][]{ new ComponentBuilder( "" ).create() };
        }

        StringBuilder word = new StringBuilder();
        List<BaseComponent> line = new ArrayList<>();
        List<BaseComponent[]> lines = new LinkedList<>();
        int lineColorChars = 0;
        BaseComponent lastComp = null;
        for ( BaseComponent comp : input ) {
            lastComp = comp;
            String text = comp.toPlainText();
            if ( !text.endsWith( " " ) ) {
                text = text + " ";
            }
            char[] chars = text.toCharArray();

            for ( char c : chars ) {
                if ( c == ' ' || c == '\n' ) {
                    int lineSize = getLineLength( line );// TODO this is not optimal, calc size of line when something gets added to the line
                    if ( line.size() == 0 && word.length() > lineLength ) { // special case: extremely long word begins a line
                        for ( String partialWord : word.toString().split( "(?<=\\G.{" + lineLength + "})" ) ) {
                            lines.add( clone( new ComponentBuilder( partialWord ), comp ).create() );
                        }
                    } else if ( lineSize + word.length() - lineColorChars == lineLength ) { // Line exactly the correct length...newline
                        line.add( clone( new ComponentBuilder( word.toString() ), comp ).create()[0] );
                        lines.add( line.toArray( new BaseComponent[line.size()] ) );
                        line = new ArrayList<>();
                        lineColorChars = 0;
                    } else if ( lineSize + 1 + word.length() - lineColorChars > lineLength ) { // Line too long...break the line
                        for ( String partialWord : word.toString().split( "(?<=\\G.{" + lineLength + "})" ) ) {
                            lines.add( line.toArray( new BaseComponent[line.size()] ) );
                            line = new ArrayList<>();
                            line.add( clone( new ComponentBuilder( partialWord ), comp ).create()[0] );
                        }
                        lineColorChars = 0;
                    } else {
                        String space = "";
                        if ( lineSize > 0 ) {
                            space = " ";
                        }
                        line.add( clone( new ComponentBuilder( word.toString() + space ), comp ).create()[0] );
                    }
                    word = new StringBuilder();

                    if ( c == '\n' ) { // Newline forces the line to flush
                        lines.add( line.toArray( new BaseComponent[line.size()] ) );
                        line = new ArrayList<>();
                    }
                } else {
                    word.append( c );
                }
            }
        }

        if ( line.size() > 0 && lastComp != null ) { // Only add the last line if there is anything to add
            lines.add( line.toArray( new BaseComponent[line.size()] ) );
        }

        return lines.toArray( new BaseComponent[lines.size()][] );
    }

    private static int getLineLength( List<BaseComponent> l ) {
        int s = 0;
        for ( BaseComponent c : l ) {
            s += c.toPlainText().length();
        }
        return s;
    }

    /**
     * Applies all styles/events from baseCompontent to the builder
     *
     * @param builder       the builder that the styles should be applied to
     * @param baseComponent the component which styles should be copied
     * @return the builder with the styles from the baseComponent
     */
    private static ComponentBuilder clone( ComponentBuilder builder, BaseComponent baseComponent ) {
        builder.event( baseComponent.getClickEvent() ).event( baseComponent.getHoverEvent() ).bold( baseComponent.isBold() )
                .color( baseComponent.getColor() ).insertion( baseComponent.getInsertion() ).italic( baseComponent.isItalic() )
                .obfuscated( baseComponent.isObfuscated() ).strikethrough( baseComponent.isStrikethrough() ).underlined( baseComponent.isUnderlined() );
        return builder;
    }
}