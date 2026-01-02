package com.yahya.service;

import com.yahya.model.DataItem;

import java.util.List;

public interface DataService {
    List<DataItem> findAll();
    DataItem save(String title, String description);
}
