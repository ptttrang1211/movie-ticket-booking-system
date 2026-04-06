package com.trang.gachon.movie.service;

import com.trang.gachon.movie.entity.Type;
import com.trang.gachon.movie.repository.TypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.*;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TypeService {
    private final TypeRepository typeRepository;
    public List<Type> getAllTypes() {
        return typeRepository.findAll();
    }
}
