package com._project._project.Department.Item;

import com._project._project.Department.Department;
import com._project._project.Department.DepartmentData.DepartmentData;
import com._project._project.Department.DepartmentData.DepartmentDataRepository;
import com._project._project.Department.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class InventoryService {

    @Autowired
    DepartmentRepository departmentRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private DepartmentDataRepository departmentDataRepository;

    private final String success = "success";

    public List<Item> getInventory(long department_id) {
        Department department = departmentRepository.findById(department_id);
        if (department == null) throw new RuntimeException("Invalid department id.");

        return department.getInventory();
    }

    public Item getItemById(long id) {
        Item item = itemRepository.findById(id);
        if (item == null) throw new RuntimeException("Invalid item id.");

        return item;
    }

    public Item buyItem(long department_id, Item item) {
        Department department = departmentRepository.findById(department_id);
        if (department == null) throw new RuntimeException("Invalid department id.");

        Item newItem = new Item(
                item.getItemName(),
                item.getPrice(),
                item.getCost(),
                item.getQuantity(),
                item.getDescription());

        department.addItem(newItem);
        itemRepository.save(newItem);
        buyAndUpdateDepartmentData(newItem.getId());

        return newItem;
    }

    public String deleteItem(long itemToDelete_id){
        Item item = itemRepository.findById(itemToDelete_id);
        if (item == null) throw new RuntimeException("Invalid item id.");

        item.getDepartment().removeItem(item);
        itemRepository.delete(item);

        System.out.println("Deleted Item.");
        return success;
    }

    public String sellItem(long itemToSell_id, int quantity){
        Item item = itemRepository.findById(itemToSell_id);
        if (item == null) throw new RuntimeException("Invalid item id.");

        int newQuantity = item.getQuantity() - quantity;
        item.setQuantity(newQuantity);

        if(item.getQuantity() <= 0) item.setAvailable(false);
        itemRepository.save(item);

        sellAndUpdateDepartmentData(itemToSell_id, quantity);
        return success;
    }

    public Item updateItemImage(long item_id, byte[] imageBytes){
        Item item = itemRepository.findById(item_id);
        if (item == null) throw new RuntimeException("Invalid item id.");

        item.setItemImage(imageBytes);
        return itemRepository.save(item);
    }

    public byte[] getItemImage(long item_id){
        Item item = itemRepository.findById(item_id);
        if (item == null) throw new RuntimeException("Invalid item id.");

        return item.getItemImage();
    }

    private void buyAndUpdateDepartmentData(long item_id) {
        Item item = itemRepository.findById(item_id);
        Department department = item.getDepartment();

        BigDecimal expenses = item.getCost().multiply(BigDecimal.valueOf(item.getQuantity()));

        DepartmentData dataToUpdate = null;
        for (DepartmentData data : department.getDepartmentData()) {
            if (data.getDate().equals(LocalDate.now())) {
                dataToUpdate = data;
                dataToUpdate.setExpense(data.getExpense().add(expenses));
                dataToUpdate.setRevenue(dataToUpdate.getIncome().subtract(dataToUpdate.getExpense()));
                break;
            }
        }
        if (dataToUpdate == null) {
            dataToUpdate = new DepartmentData(LocalDate.now(), BigDecimal.ZERO, expenses);
            department.addDepartmentData(dataToUpdate);
        }
        departmentDataRepository.save(dataToUpdate);
    }

    private void sellAndUpdateDepartmentData(long item_id, int quantity) {
        Item item = itemRepository.findById(item_id);
        Department department = item.getDepartment();

        BigDecimal income = item.getPrice().multiply(BigDecimal.valueOf(quantity));

        DepartmentData dataToUpdate = null;
        for(DepartmentData data : department.getDepartmentData()) {
            if(data.getDate().equals(LocalDate.now())) {
                dataToUpdate = data;
                dataToUpdate.setIncome(data.getIncome().add(income));
                dataToUpdate.setRevenue(dataToUpdate.getRevenue().add(income));
            }
        }
        if (dataToUpdate == null) {
            dataToUpdate = new DepartmentData(LocalDate.now(), income, BigDecimal.ZERO);
            department.addDepartmentData(dataToUpdate);
        }
        departmentDataRepository.save(dataToUpdate);
    }
}
