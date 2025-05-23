package com._project._project.Department.Item;

import com._project._project.Project.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    Item findById(long id);
    void deleteById(long id);

    Item findByItemName(String itemName);
    Item findByPrice(double price);

}
