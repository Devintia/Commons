package net.devintia.commons.bukkit.pagination;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by Martin on 03.06.2016.
 */
public class PaginatorTest {

    @Test
    public void test() {
        BaseComponent[] comps = new ComponentBuilder( "Long string that fills up a whole page." ).color( ChatColor.RED ).event( new ClickEvent( ClickEvent.Action.SUGGEST_COMMAND, "say" ) ).
                append( "bunch " ).append( "of " ).append( "smaller " ).append( "strings " ).append( "that " ).append( "will " ).append( "be " ).append( "wrapped " )
                .append( "force\nnewline" ).append( "force" ).append( "\n" ).append( "newline" ).create();

        ChatPage page1 = Paginator.paginate( comps, 1, 10, 2 );
        assertThat( getLineText( page1.getLines()[0] ), is( "Long string" ) );
        assertThat( getLineText( page1.getLines()[1] ), is( "that fills" ) );

        ChatPage page2 = Paginator.paginate( comps, 2, 10, 2 );
        assertThat( getLineText( page2.getLines()[0] ), is( "up a whole" ) );
        assertThat( getLineText( page2.getLines()[1] ), is( "page" ) );
    }


    private String getLineText( BaseComponent[] comps ) {
        String line = "";
        for ( BaseComponent comp : comps ) {
            line += comp.toPlainText() + " ";
        }
        return line.trim();
    }
}
