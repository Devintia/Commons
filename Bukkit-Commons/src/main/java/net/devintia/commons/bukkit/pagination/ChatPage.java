package net.devintia.commons.bukkit.pagination;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.chat.BaseComponent;

/**
 * Created by Martin on 29.05.2016.
 */
@Getter
@AllArgsConstructor
public class ChatPage {

    private BaseComponent[][] lines;
    private int pageNumber;
    private int totalPages;
}
