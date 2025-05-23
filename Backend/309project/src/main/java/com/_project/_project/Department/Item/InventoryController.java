package com._project._project.Department.Item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    InventoryService inventoryService;

    /*
    Returns the inventory(products) listed under one department
    based on the department id.
     */
    @GetMapping(path = "/get-inventory/{department_id}")
    public List<Item> getInventory(@PathVariable long department_id){
        return inventoryService.getInventory(department_id);
    }

    /*
    Returns a single item specified by the item id. Nate, you probably
    need to call the above GET request first to actually have data
    for specific items. If we need a different solution let me know.
     */
    @GetMapping(path = "/get-inventory-product/{item_id}")
    public Item getInventoryProduct(@PathVariable long item_id){
        return inventoryService.getItemById(item_id);
    }

    /*
    Call this when adding an item, for simplicity reasons, I'll make it
    so when you create an item it acts as if the company buys them and calculates
    the cost to update the data.
     */
    @PostMapping(path = "/buy-item/{department_id}")
    public Item buyInventory(@PathVariable long department_id,@RequestBody Item item){
        return inventoryService.buyItem(department_id, item);
    }

    /*
    Basic item Deletion request. Does not update anything apart from deleting
    item from the database.
     */
    @DeleteMapping(path = "/delete-item/{itemToDelete_id}")
    public String deleteItem(@PathVariable long itemToDelete_id){
        return inventoryService.deleteItem(itemToDelete_id);
    }

    /*
    Endpoint to sell a number of items, pass the id, as well as the quantity of how many
    items are being sold, this way you don't need to pass in a full object, I can just work
    off this data.
     */
    @PutMapping(path = "/sell-item/{item_id}/{quantity}")
    public String sellItem(@PathVariable long item_id, @PathVariable int quantity){
        return inventoryService.sellItem(item_id, quantity);
    }

    /*
    Endpoint to add/update an image for an item
     */
    @PostMapping(path = "/upload-image/{item_id}", consumes = "multipart/form-data")
    public Item uploadItemImage(@PathVariable long item_id, @RequestParam("image") MultipartFile imageFile) {
        try {
            byte[] imageBytes = imageFile.getBytes();
            return inventoryService.updateItemImage(item_id, imageBytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload image", e);
        }
    }

    @GetMapping("/image/{item_id}")
    public ResponseEntity<byte[]> getItemImage(@PathVariable long item_id) {
        byte[] imageBytes = inventoryService.getItemImage(item_id);

        return ResponseEntity.ok()
                .header("Content-Type", "image/jpeg") // or infer from file metadata
                .body(imageBytes);
    }
}
