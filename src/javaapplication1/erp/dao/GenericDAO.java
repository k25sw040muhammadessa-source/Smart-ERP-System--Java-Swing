package javaapplication1.erp.dao;

import java.util.List;
import java.util.Optional;

public interface GenericDAO<T> {
    T create(T t) throws Exception;
    Optional<T> findById(int id) throws Exception;
    List<T> search(String term, int limit, int offset) throws Exception;
    boolean update(T t) throws Exception;
    boolean delete(int id) throws Exception;
}
