package com.yahya.service;

import com.yahya.model.DataItem;
import com.yahya.repository.DataItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class DataServiceImpl implements DataService {

    private final DataItemRepository repository;

    public DataServiceImpl(DataItemRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<DataItem> findAll() {
        return repository.findAll();
    }

    @Override
    public DataItem save(String title, String description) {
        var item = new DataItem();
        item.setTitle(title);
        item.setDescription(description);
        item.setCreatedAt(LocalDateTime.now());
        return repository.save(item);
    }
}
