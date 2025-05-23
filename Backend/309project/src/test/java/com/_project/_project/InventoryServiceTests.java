package com._project._project;

import com._project._project.Department.Department;
import com._project._project.Department.DepartmentData.DepartmentData;
import com._project._project.Department.DepartmentData.DepartmentDataRepository;
import com._project._project.Department.DepartmentRepository;
import com._project._project.Department.Item.InventoryService;
import com._project._project.Department.Item.Item;
import com._project._project.Department.Item.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class InventoryServiceTests {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private DepartmentDataRepository departmentDataRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private Department testDepartment;
    private Item testItem;
    private DepartmentData testDepartmentData;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup test department
        testDepartment = new Department("Test Department");
        testDepartment.setId(1L);

        // Setup test item
        testItem = new Item(
            "Test Item",
            BigDecimal.valueOf(100),
            BigDecimal.valueOf(50),
            10,
            "Test Description"
        );
        testItem.setId(1L);
        testItem.setDepartment(testDepartment);

        // Setup test department data
        testDepartmentData = new DepartmentData(
            LocalDate.now(),
            BigDecimal.valueOf(1000),
            BigDecimal.valueOf(500)
        );
        testDepartmentData.setId(1L);
        testDepartmentData.setDepartment(testDepartment);

        // Setup department relationships
        List<Item> inventory = new ArrayList<>();
        inventory.add(testItem);
        testDepartment.setInventory(inventory);

        List<DepartmentData> departmentData = new ArrayList<>();
        departmentData.add(testDepartmentData);
        testDepartment.setDepartmentData(departmentData);
    }

    @Test
    void testGetInventory() {
        when(departmentRepository.findById(1L)).thenReturn(testDepartment);

        List<Item> result = inventoryService.getInventory(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testItem.getItemName(), result.get(0).getItemName());
        verify(departmentRepository).findById(1L);
    }

    @Test
    void testGetInventory_DepartmentNotFound() {
        when(departmentRepository.findById(999L)).thenReturn(null);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            inventoryService.getInventory(999L);
        });

        assertEquals("Invalid department id.", exception.getMessage());
    }

    @Test
    void testGetItemById() {
        when(itemRepository.findById(1L)).thenReturn(testItem);

        Item result = inventoryService.getItemById(1L);

        assertNotNull(result);
        assertEquals(testItem.getItemName(), result.getItemName());
        verify(itemRepository).findById(1L);
    }

    @Test
    void testGetItemById_ItemNotFound() {
        when(itemRepository.findById(999L)).thenReturn(null);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            inventoryService.getItemById(999L);
        });

        assertEquals("Invalid item id.", exception.getMessage());
    }


    @Test
    void testBuyItem_DepartmentNotFound() {
        when(departmentRepository.findById(999L)).thenReturn(null);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            inventoryService.buyItem(999L, testItem);
        });

        assertEquals("Invalid department id.", exception.getMessage());
    }

    @Test
    void testDeleteItem() {
        when(itemRepository.findById(1L)).thenReturn(testItem);
        doNothing().when(itemRepository).delete(any(Item.class));

        String result = inventoryService.deleteItem(1L);

        assertEquals("success", result);
        verify(itemRepository).delete(testItem);
    }

    @Test
    void testDeleteItem_ItemNotFound() {
        when(itemRepository.findById(999L)).thenReturn(null);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            inventoryService.deleteItem(999L);
        });

        assertEquals("Invalid item id.", exception.getMessage());
    }

    @Test
    void testSellItem() {
        when(itemRepository.findById(1L)).thenReturn(testItem);
        when(itemRepository.save(any(Item.class))).thenReturn(testItem);
        when(departmentDataRepository.save(any())).thenReturn(testDepartmentData);

        String result = inventoryService.sellItem(1L, 2);

        assertEquals("success", result);
        assertEquals(8, testItem.getQuantity());
        assertTrue(testItem.isAvailable());
        verify(itemRepository).save(testItem);
        verify(departmentDataRepository).save(any(DepartmentData.class));
    }

    @Test
    void testSellItem_SetUnavailableWhenZeroQuantity() {
        when(itemRepository.findById(1L)).thenReturn(testItem);
        when(itemRepository.save(any(Item.class))).thenReturn(testItem);
        when(departmentDataRepository.save(any())).thenReturn(testDepartmentData);

        String result = inventoryService.sellItem(1L, 10);

        assertEquals("success", result);
        assertEquals(0, testItem.getQuantity());
        assertFalse(testItem.isAvailable());
        verify(itemRepository).save(testItem);
    }

    @Test
    void testUpdateItemImage() {
        byte[] imageBytes = "test image".getBytes();
        when(itemRepository.findById(1L)).thenReturn(testItem);
        when(itemRepository.save(any(Item.class))).thenReturn(testItem);

        Item result = inventoryService.updateItemImage(1L, imageBytes);

        assertNotNull(result);
        assertArrayEquals(imageBytes, result.getItemImage());
        verify(itemRepository).save(testItem);
    }

    @Test
    void testGetItemImage() {
        byte[] imageBytes = "test image".getBytes();
        testItem.setItemImage(imageBytes);
        when(itemRepository.findById(1L)).thenReturn(testItem);

        byte[] result = inventoryService.getItemImage(1L);

        assertNotNull(result);
        assertArrayEquals(imageBytes, result);
    }
}
    