package com._project._project;

import com._project._project.Department.Department;
import com._project._project.Department.Item.Item;
import com._project._project.Department.Item.InventoryController;
import com._project._project.Department.Item.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ItemTests {

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private InventoryController inventoryController;

    private Department testDepartment;
    private Item testItem;
    private byte[] testImageBytes;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup test data
        testDepartment = new Department("Test Department");
        testDepartment.setId(1L);

        testItem = new Item(
            "Test Item",
            BigDecimal.valueOf(100),
            BigDecimal.valueOf(50),
            10,
            "Test Description"
        );
        testItem.setId(1L);
        testItem.setDepartment(testDepartment);

        testImageBytes = "test image".getBytes();
        testItem.setItemImage(testImageBytes);
    }

    // Item Entity Tests
    @Test
    void testItemConstructorAndGetters() {
        Item item = new Item(
            "New Item",
            BigDecimal.valueOf(200),
            BigDecimal.valueOf(100),
            5,
            "New Description"
        );

        assertEquals("New Item", item.getItemName());
        assertEquals(BigDecimal.valueOf(200), item.getPrice());
        assertEquals(BigDecimal.valueOf(100), item.getCost());
        assertEquals(5, item.getQuantity());
        assertEquals("New Description", item.getDescription());
        assertTrue(item.isAvailable());
    }

    @Test
    void testItemSetters() {
        Item item = new Item();
        item.setItemName("Updated Item");
        item.setPrice(BigDecimal.valueOf(300));
        item.setCost(BigDecimal.valueOf(150));
        item.setQuantity(15);
        item.setDescription("Updated Description");
        item.setAvailable(false);
        item.setDepartment(testDepartment);

        assertEquals("Updated Item", item.getItemName());
        assertEquals(BigDecimal.valueOf(300), item.getPrice());
        assertEquals(BigDecimal.valueOf(150), item.getCost());
        assertEquals(15, item.getQuantity());
        assertEquals("Updated Description", item.getDescription());
        assertFalse(item.isAvailable());
        assertEquals(testDepartment, item.getDepartment());
    }

    // Controller Tests
    @Test
    void testGetInventory() {
        List<Item> items = new ArrayList<>();
        items.add(testItem);
        when(inventoryService.getInventory(1L)).thenReturn(items);

        List<Item> result = inventoryController.getInventory(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testItem.getItemName(), result.get(0).getItemName());
        verify(inventoryService).getInventory(1L);
    }

    @Test
    void testGetInventoryProduct() {
        when(inventoryService.getItemById(1L)).thenReturn(testItem);

        Item result = inventoryController.getInventoryProduct(1L);

        assertNotNull(result);
        assertEquals(testItem.getItemName(), result.getItemName());
        verify(inventoryService).getItemById(1L);
    }

    @Test
    void testBuyInventory() {
        when(inventoryService.buyItem(1L, testItem)).thenReturn(testItem);

        Item result = inventoryController.buyInventory(1L, testItem);

        assertNotNull(result);
        assertEquals(testItem.getItemName(), result.getItemName());
        verify(inventoryService).buyItem(1L, testItem);
    }

    @Test
    void testDeleteItem() {
        when(inventoryService.deleteItem(1L)).thenReturn("success");

        String result = inventoryController.deleteItem(1L);

        assertEquals("success", result);
        verify(inventoryService).deleteItem(1L);
    }

    @Test
    void testSellItem() {
        when(inventoryService.sellItem(1L, 2)).thenReturn("success");

        String result = inventoryController.sellItem(1L, 2);

        assertEquals("success", result);
        verify(inventoryService).sellItem(1L, 2);
    }

    @Test
    void testUploadItemImage() {
        MockMultipartFile imageFile = new MockMultipartFile(
            "image",
            "test.jpg",
            "image/jpeg",
            testImageBytes
        );

        when(inventoryService.updateItemImage(eq(1L), any(byte[].class))).thenReturn(testItem);

        Item result = inventoryController.uploadItemImage(1L, imageFile);

        assertNotNull(result);
        assertEquals(testItem.getItemName(), result.getItemName());
        verify(inventoryService).updateItemImage(eq(1L), any(byte[].class));
    }

    @Test
    void testGetItemImage() {
        when(inventoryService.getItemImage(1L)).thenReturn(testImageBytes);

        ResponseEntity<byte[]> response = inventoryController.getItemImage(1L);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertArrayEquals(testImageBytes, response.getBody());
        assertEquals("image/jpeg", response.getHeaders().getContentType().toString());
        verify(inventoryService).getItemImage(1L);
    }

    @Test
    void testItemImageHandling() {
        byte[] imageData = "test image data".getBytes();
        testItem.setItemImage(imageData);

        assertArrayEquals(imageData, testItem.getItemImage());
    }

}
