package net.buycraft.api;

import net.buycraft.Buycraft;
import net.buycraft.tasks.ReportTask;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import tk.coolv1994.gawdserver.player.PlayerList;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

public class Api {
    private Buycraft plugin;

    private String apiUrl;
    private String apiKey;

    public Api() {
        this.plugin = Buycraft.getInstance();
        this.apiKey = plugin.getSettings().getString("secret");

        if (plugin.getSettings().getBoolean("https")) {
            this.apiUrl = "https://api.buycraft.net/v4";
        } else {
            this.apiUrl = "http://api.buycraft.net/v4";
        }
    }

    public JsonObject authenticateAction() {
        HashMap<String, String> apiCallParams = new HashMap<String, String>();

        apiCallParams.put("action", "info");
        
        apiCallParams.put("serverPort", String.valueOf(25565)); //plugin.getServer().getServerPort()
        apiCallParams.put("onlineMode", String.valueOf(true)); //plugin.getServer().getOnlineMode()
        apiCallParams.put("playersMax", String.valueOf(9000)); // No current API support
        apiCallParams.put("version", plugin.getVersion());

        return call(apiCallParams);
    }

    public JsonObject categoriesAction() {
        HashMap<String, String> apiCallParams = new HashMap<String, String>();

        apiCallParams.put("action", "categories");

        return call(apiCallParams);
    }
    
    public JsonObject urlAction(String url) {
        HashMap<String, String> apiCallParams = new HashMap<String, String>();

        apiCallParams.put("action", "url");
        apiCallParams.put("url", url);

        return call(apiCallParams);
    }

    public JsonObject packagesAction() {
        HashMap<String, String> apiCallParams = new HashMap<String, String>();

        apiCallParams.put("action", "packages");

        return call(apiCallParams);
    }

    public JsonObject paymentsAction(int limit, boolean usernameSpecific, String username) {
        HashMap<String, String> apiCallParams = new HashMap<String, String>();

        apiCallParams.put("action", "payments");
        apiCallParams.put("limit", String.valueOf(limit));
        
        if(usernameSpecific) {
            apiCallParams.put("ign", username);
        }

        return call(apiCallParams);
    }

    public JsonObject fetchPendingPlayers() {
        HashMap<String, String> apiCallParams = new HashMap<String, String>();

        apiCallParams.put("action", "pendingUsers");

        return call(apiCallParams);
    }

    public JsonObject fetchPlayerCommands(JsonArray players, boolean offlineCommands) {
        HashMap<String, String> apiCallParams = new HashMap<String, String>();

        apiCallParams.put("action", "commands");
        apiCallParams.put("do", "lookup");

        apiCallParams.put("users", players.toString());
        apiCallParams.put("offlineCommands", String.valueOf(offlineCommands));
        apiCallParams.put("offlineCommandLimit", String.valueOf(plugin.getSettings().getInt("commandThrottleCount")));

        return call(apiCallParams);
    }

    public void commandsDeleteAction(String commandsToDelete) {
        HashMap<String, String> apiCallParams = new HashMap<String, String>();

        apiCallParams.put("action", "commands");
        apiCallParams.put("do", "removeId");

        apiCallParams.put("commands", commandsToDelete);

        call(apiCallParams);
    }

    private JsonObject call(HashMap<String, String> apiCallParams) {
        if (apiKey.length() == 0) {
            apiKey = "unspecified";
        }

        apiCallParams.put("secret", apiKey);
        apiCallParams.put("playersOnline", String.valueOf(PlayerList.onlinePlayers()));
  
        String url = apiUrl + generateUrlQueryString(apiCallParams);

        if (url != null) {
            String HTTPResponse = HttpRequest(url);

            try {
                if (HTTPResponse != null) {
                    return (JsonObject) plugin.getJsonParser().parse(HTTPResponse);
                } else {
                    return null;
                }
            } catch (JsonParseException e) {
                plugin.getLogger().severe("JSON parsing error.");
                ReportTask.setLastException(e);
            }
        }

        return null;
    }

    public static String HttpRequest(String url) {
        try {
        	
        	if(Buycraft.getInstance().getSettings().getBoolean("debug")) {
        		Buycraft.getInstance().getLogger().info("---------------------------------------------------");
        		Buycraft.getInstance().getLogger().info("Request URL: " + url);
        	}
        	
            String content = "";

            URL conn = new URL(url);
            
            HttpURLConnection yc = (HttpURLConnection) conn.openConnection();
            
            yc.setRequestMethod("GET");
            yc.setConnectTimeout(15000);
            yc.setReadTimeout(15000);
            yc.setInstanceFollowRedirects(false);
            yc.setAllowUserInteraction(false);
            
            BufferedReader in;
            
            in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
            
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                content = content + inputLine;
            }

            in.close();
            
            if(Buycraft.getInstance().getSettings().getBoolean("debug")) {
            	Buycraft.getInstance().getLogger().info("JSON Response: " + content);
            	Buycraft.getInstance().getLogger().info("---------------------------------------------------");
            }
            
            return content;
        } catch (ConnectException e) {
            Buycraft.getInstance().getLogger().severe("HTTP request failed due to connection error.");
            ReportTask.setLastException(e);
        } catch (SocketTimeoutException e) {
            Buycraft.getInstance().getLogger().severe("HTTP request failed due to timeout error.");
            ReportTask.setLastException(e);
        } catch (FileNotFoundException e) {
            Buycraft.getInstance().getLogger().severe("HTTP request failed due to file not found.");
            ReportTask.setLastException(e);
        } catch (UnknownHostException e) {
            Buycraft.getInstance().getLogger().severe("HTTP request failed due to unknown host.");
            ReportTask.setLastException(e);
        } catch (IOException e) {
        	Buycraft.getInstance().getLogger().severe(e.getMessage());
            ReportTask.setLastException(e);
        } catch (Exception e) {
            e.printStackTrace();
            ReportTask.setLastException(e);
        }
        
        return null;
    }

    private static String generateUrlQueryString(HashMap<String, String> map) {
        StringBuilder sb = new StringBuilder();

        sb.append("?");

        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (sb.length() > 1) {
                sb.append("&");
            }

            sb.append(String.format("%s=%s",
                    entry.getKey().toString(),
                    entry.getValue().toString()
            ));
        }

        return sb.toString();
    }

    public void setApiKey(String value) {
        apiKey = value;
    }
}
