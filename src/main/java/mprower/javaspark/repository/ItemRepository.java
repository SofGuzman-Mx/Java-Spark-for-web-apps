package mprower.javaspark.repository;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import mprower.javaspark.model.Item; // Importa el modelo de Item
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maneja la lógica de negocio y almacenamiento (en memoria) para los Items.
 */
public class ItemRepository {

    private static final Logger log = LoggerFactory.getLogger(ItemRepository.class);
    private static final Gson gson = new Gson();
    private static final Map<String, Item> items = new ConcurrentHashMap<>();

    static {
        try {
            String stringDB = readFile();
            Type listType = new TypeToken<List<Item>>() {}.getType();
            List<Item> itemList = gson.fromJson(stringDB, listType);
            for (Item item : itemList) {
                items.put(item.getId(), item);
            }
        } catch (IOException e) {
            log.error("readDB error: {}", e.getMessage());
        }
    }

    private static String readFile() throws IOException {
        String filePath = "./src/main/resources/items.json";
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader jsonFile = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = jsonFile.readLine()) != null) {
                stringBuilder.append(line);
            }
        }

        String stringFile = stringBuilder.toString();

        log.info("fileString: {}", stringFile);

        if (stringFile.isEmpty()) return "[]";

        if (stringFile.charAt(0) == '{') stringFile = "[" + stringFile + "]";

        return  stringFile;
    }

    public Item createItem(Item item) {
        item.validate(); // Valida el item
        item.id = UUID.randomUUID().toString();
        item.createdAt = Instant.now().toString();
        items.put(item.id, item);
        return item;
    }

    public Collection<Item> getAllItems() {
        log.info("items: {}", items.size());
        return items.values();
    }

    public Optional<Item> getItemById(String id) {
        return Optional.ofNullable(items.get(id));
    }

    public Optional<Item> updateItem(String id, Item updatedItem) {
        if (!items.containsKey(id)) {
            return Optional.empty(); // No encontrado
        }
        updatedItem.validate();

        // Preservar ID y fecha de creación
        Item existingItem = items.get(id);
        updatedItem.id = id;
        updatedItem.createdAt = existingItem.createdAt;

        items.put(id, updatedItem);
        return Optional.of(updatedItem);
    }

    public boolean deleteItem(String id) {
        return items.remove(id) != null;
    }
}