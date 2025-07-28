package com.example.demo.service;

import com.example.demo.dto.ItemDTO;
import com.example.demo.mapper.ItemMapper;
import com.example.demo.model.Item;
import com.example.demo.model.User;
import com.example.demo.repository.ItemRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;



@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public ItemService(ItemRepository itemRepository, UserRepository userRepository, UserService userService) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    public List<ItemDTO> getAllItems() {

        long user_id = userService.getUserIdFromRequest();
        System.out.println("user_id is " + user_id);

        return itemRepository.findByUserUserId(user_id).stream()
                .map(item -> new ItemDTO(
                        item.getId(),
                        item.getTitle(),
                        item.getDescription(),
                        item.getStatus(),
                        item.getCreatedAt(),
                        item.getUpdatedAt(),
                        item.getUser().getUserId()
                ))
                .collect(Collectors.toList());
    }

    public Item createItem(ItemDTO dto) {
        User user = userRepository.findById(userService.getUserIdFromRequest()).orElseThrow(() -> new RuntimeException("User not found"));
        Item item = ItemMapper.toEntity(dto);
        item.setUser(user);
        return itemRepository.save(item);
    }

    public Item getItemById(Long id) {
        Item item = itemRepository.findById(id).orElse(null);
        if (item.getUserId() != userService.getUserIdFromRequest()) {
            throw new RuntimeException("You are not authorized to view this item");
        }
        return item;
    }

    public Item updateItem(Long id, ItemDTO dto) {
        Item existingItem = itemRepository.findById(id).orElse(null);
        if (existingItem == null) {
            return null;
        }
        if (existingItem.getUserId() == userService.getUserIdFromRequest() ==false) {
            throw new RuntimeException("You are not authorized to update this item");
        }

        if (dto.getTitle() != null) {
            existingItem.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            existingItem.setDescription(dto.getDescription());
        }
        if (dto.getStatus() != null) {
            existingItem.setStatus(dto.getStatus());
        }
        if (dto.getUserId() != null) {
            User user = userRepository.findById(dto.getUserId()).orElseThrow(() -> new RuntimeException("User not found"));
            existingItem.setUser(user);
        }
        // Add similar checks for other fields as needed

        return itemRepository.save(existingItem);
    }

    public boolean deleteItem(Long id) {
        if (itemRepository.existsById(id)) {
            if (itemRepository.findById(id).get().getUserId() == (userService.getUserIdFromRequest())) {

                itemRepository.deleteById(id);
                return true;
            } else {
            return false;
            }

        } else {
            return false;
        }
    }
}
