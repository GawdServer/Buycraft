package net.buycraft.tasks;

import io.github.gawdserver.api.utils.Chat;
import net.buycraft.Buycraft;
import net.buycraft.api.ApiTask;
import net.buycraft.util.ChatUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.github.gawdserver.api.utils.ColorCodes;

public class RecentPaymentsTask extends ApiTask {
    
    private String receiver;
    private String playerLookup;
    
    public static void call(String receiver, String player) {
        Buycraft.getInstance().addTask(new RecentPaymentsTask(receiver, player));
    }

    private RecentPaymentsTask(String receiver, String playerLookup) {
        this.receiver = receiver;
        this.playerLookup = playerLookup;
    }

    public void run() {
        try {
            
            JsonObject apiResponse = getApi().paymentsAction(10, playerLookup.length() > 0, playerLookup);
            
            if (apiResponse != null) {
                
                JsonArray entries = apiResponse.get("payload").getAsJsonArray();
                
                if(entries != null && entries.size() > 0) {
                    Chat.sendMessage(receiver, ChatUtils.header());
                    Chat.sendMessage(receiver, ChatUtils.seperator());
                    
                    if(playerLookup.isEmpty())
                    {
                        Chat.sendMessage(receiver, ChatUtils.seperator() + "Displaying recent payments over all users: ");
                    }
                    else
                    {
                        Chat.sendMessage(receiver, ChatUtils.seperator() + "Displaying recent payments from the user " + playerLookup + ":");
                    }

                    Chat.sendMessage(receiver, ChatUtils.seperator());
                    
                    for(int i=0; i<entries.size(); i++) {

                        JsonObject entry = entries.get(i).getAsJsonObject();

                        Chat.sendMessage(receiver, ChatUtils.seperator() + "[" + entry.get("humanTime").getAsString() + "] " + ColorCodes.LIGHT_PURPLE + entry.get("ign").getAsString() + ColorCodes.GREEN + " (" + entry.get("price").getAsString() + " " + entry.get("currency").getAsString() + ")");
                    }

                    Chat.sendMessage(receiver, ChatUtils.seperator());
                    Chat.sendMessage(receiver, ChatUtils.footer());
                }
                else
                {
                    Chat.sendMessage(receiver, ChatUtils.header());
                    Chat.sendMessage(receiver, ChatUtils.seperator());
                    Chat.sendMessage(receiver, ChatUtils.seperator() + ColorCodes.RED + "No recent payments to display.");
                    Chat.sendMessage(receiver, ChatUtils.seperator());
                    Chat.sendMessage(receiver, ChatUtils.footer());
                }
            } 
        } catch (JsonParseException e) {
            getLogger().severe("JSON parsing error.");
            ReportTask.setLastException(e);
        }
    }
}