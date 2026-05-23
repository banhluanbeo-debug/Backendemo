package com.tranvanluan.backend.service;

import com.tranvanluan.backend.entity.Cinema;
import java.util.List;

public interface CinemaService {
    List<Cinema> getAll();

    Cinema getById(Long id);

    Cinema create(Cinema cinema);

    Cinema update(Long id, Cinema cinema);

    void delete(Long id);
}
