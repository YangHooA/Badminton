package com.example.Badminton.service;

import com.example.Badminton.dto.InventoryDTO;
import com.example.Badminton.entity.Inventory;
import com.example.Badminton.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    public List<InventoryDTO> getAllInventory() {
        List<Inventory> inventoryList = inventoryRepository.findAll();
        return inventoryList.stream()
                .map(inventory -> new InventoryDTO(
                        inventory.getProductId(),
                        inventory.getProduct() != null ? inventory.getProduct().getName() : "Sản phẩm không tồn tại",
                        inventory.getQuantity(),
                        inventory.getLastUpdated().toString()
                ))

                .collect(Collectors.toList());
    }
}
