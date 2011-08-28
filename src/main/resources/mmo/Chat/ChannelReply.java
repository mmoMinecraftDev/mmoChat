/*
 * This file is part of mmoMinecraft (http://code.google.com/p/mmo-minecraft/).
 * 
 * mmoMinecraft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package mmo.Chat;

import java.util.HashSet;
import mmo.Core.mmoChatEvent;
import mmo.Core.mmoListener;
import org.bukkit.entity.Player;

public class ChannelReply extends mmoListener {

	@Override
	public void onMMOChat(mmoChatEvent event) {
		if (event.hasFilter("Reply")) {
			HashSet<Player> recipients = event.getRecipients();
			recipients.clear();
			Player from = event.getPlayer();
			String name = ChannelTell.tells.get(from.getName());
			if (name != null) {
				Player to = mmoChat.mmo.server.getPlayer(name);
				ChannelTell.tells.put(to.getName(), from.getName());
				recipients.add(from);
				recipients.add(to);
			}
		}
	}
}
