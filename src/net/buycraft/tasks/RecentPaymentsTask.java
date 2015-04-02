package net.buycraft.tasks;

import net.buycraft.Buycraft;
import net.buycraft.api.ApiTask;
import net.buycraft.util.Chat;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import tk.coolv1994.gawdserver.utils.ColorCodes;

import static tk.coolv1994.gawdserver.utils.Chat.sendMessage;

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
                    sendMessage(receiver, Chat.header());
                    sendMessage(receiver, Chat.seperator());
                    
                    if(playerLookup.isEmpty())
                    {
                        sendMessage(receiver, Chat.seperator() + "Displaying recent payments over all users: ");
                    }
                    else
                    {
                        sendMessage(receiver, Chat.seperator() + "Displaying recent payments from the user " + playerLookup + ":");
                    }

                    sendMessage(receiver, Chat.seperator());
                    
                    for(int i=0; i<entries.size(); i++) {

                        JsonObject entry = entries.get(i).getAsJsonObject();

                        sendMessage(receiver, Chat.seperator() + "[" + entry.get("humanTime").getAsString() + "] " + ColorCodes.LIGHT_PURPLE + entry.get("ign").getAsString() + ColorCodes.GREEN + " (" + entry.get("price").getAsString() + " " + entry.get("currency").getAsString() + ")");
                    }

                    sendMessage(receiver, Chat.seperator());
                    sendMessage(receiver, Chat.footer());
                }
                else
                {
                    sendMessage(receiver, Chat.header());
                    sendMessage(receiver, Chat.seperator());
                    sendMessage(receiver, Chat.seperator() + ColorCodes.RED + "No recent payments to display.");
                    sendMessage(receiver, Chat.seperator());
                    sendMessage(receiver, Chat.footer());
                }
            } 
        } catch (JsonParseException e) {
            getLogger().severe("JSON parsing error.");
            ReportTask.setLastException(e);
        }
    }
}