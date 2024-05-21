import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.AttachmentOption;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.FileReader;
import java.io.IOException;
import javax.security.auth.login.LoginException;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.List;

public class DiscordBot extends ListenerAdapter {
        private final Map < String, List < String >> userItems = new HashMap < > ();
        private final Random random = new Random();
        private final OffsetDateTime startTime = OffsetDateTime.now();
        private String prefix = ".";
        private String statusMode = "ONLINE";
        private String statusText = "Shiii I'm Alive!";
        private Guild emojisServer; // Server to upload emojis
        private static final String ITEM_LIST_URL = "https://happywars.fandom.com/wiki/Warrior_Weapons";
        public static void main(String[] args) throws LoginException {
                DiscordBot bot = new DiscordBot();
                bot.loadConfig();
                JDABuilder.createDefault(bot.getToken())
                        .addEventListeners(bot)
                        .build();
        }
        private void loadConfig() {
                JSONParser parser = new JSONParser();
                try {
                        FileReader reader = new FileReader("conf/config.json");
                        JSONObject jsonObject = (JSONObject) parser.parse(reader);
                        prefix = (String) jsonObject.get("Prefix");
                        statusMode = (String) jsonObject.get("StatusMode");
                        statusText = (String) jsonObject.get("StatusText");
                        reader.close();
                } catch (IOException | ParseException e) {
                        e.printStackTrace();
                }
        }
        private String getToken() {
                JSONParser parser = new JSONParser();
                try {
                        FileReader reader = new FileReader("conf/config.json");
                        JSONObject jsonObject = (JSONObject) parser.parse(reader);
                        String token = (String) jsonObject.get("Token");
                        reader.close();
                        return token;
                } catch (IOException | ParseException e) {
                        e.printStackTrace();
                        return null; // Or handle the error accordingly
                }
        }
        @Override
        public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
                if (event.getAuthor().isBot()) return;
                String[] args = event.getMessage().getContentRaw().split(" ");
                String command = args[0].toLowerCase();
                User user = event.getAuthor();
                String userId = user.getId();
                switch (command) {
                        case ".draw":
                                handleDrawCommand(event, user, userId);
                                break;
                        case ".collection":
                                handleCollectionCommand(event, userId);
                                break;
                }
        }
        private void handleDrawCommand(GuildMessageReceivedEvent event, User user, String userId) {
                EmbedBuilder embed = new EmbedBuilder().setTitle("Draw Result").setColor(Color.CYAN);
                List < Item > items = fetchItemsFromWiki();
                if (items.isEmpty()) {
                        embed.setDescription("Failed to fetch item information. Please try again later.");
                } else {
                        StringBuilder description = new StringBuilder();
                        for (int i = 0; i < 5; i++) {
                                Item item = items.get(random.nextInt(items.size()));
                                description.append("You drew: ").append(item.getName()).append("\n");
                                addItemToCollection(userId, item.getName());
                        }
                        embed.setDescription(description.toString());
                }
                event.getChannel().sendMessageEmbeds(embed.build()).queue();
        }
        private void handleCollectionCommand(GuildMessageReceivedEvent event, String userId) {
                EmbedBuilder embed = new EmbedBuilder().setTitle("Your Collection").setColor(Color.GREEN);
                List < String > items = userItems.getOrDefault(userId, Collections.emptyList());
                StringBuilder description = new StringBuilder();
                if (!items.isEmpty()) {
                        for (String item: items) {
                                description.append(item).append("\n");
                        }
                } else {
                        description.append("No items in your collection.");
                }
                embed.setDescription(description.toString());
                event.getChannel().sendMessageEmbeds(embed.build()).queue();
        }
        private List < Item > fetchItemsFromWiki() {
                List < Item > items = new ArrayList < > ();
                try {
                        Document doc = Jsoup.connect(ITEM_LIST_URL).get();
                        Elements itemRows = doc.select("table.wikitable tr");
                        for (Element row: itemRows) {
                                Elements columns = row.select("td");
                                if (columns.size() >= 2) {
                                        String name = columns.get(1).text();
                                        String imageUrl = columns.get(0).selectFirst("img").absUrl("src");
                                        items.add(new Item(name, imageUrl));
                                }
                        }
                } catch (IOException e) {
                        e.printStackTrace();
                }
                return items;
        }
        private void addItemToCollection(String userId, String itemName) {
                List < String > items = userItems.getOrDefault(userId, new ArrayList < > ());
                items.add(itemName);
                userItems.put(userId, items);
        }
        private static class Item {
                private final String name;
                private final String imageUrl;
                public Item(String name, String imageUrl) {
                        this.name = name;
                        this.imageUrl = imageUrl;
                }
                public String getName() {
                        return name;
                }
                public String getImageUrl() {
                        return imageUrl;
                }
        }
}
