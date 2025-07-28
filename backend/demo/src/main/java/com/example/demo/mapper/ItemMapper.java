package com.example.demo.mapper;

import com.example.demo.dto.ItemDTO;
import com.example.demo.model.Item;

public class ItemMapper {
    public static Item toEntity(ItemDTO dto) {
        Item item = new Item();
        item.setTitle(dto.getTitle());
        item.setDescription(dto.getDescription());
        item.setStatus(dto.getStatus());
        return item;
    }
}
