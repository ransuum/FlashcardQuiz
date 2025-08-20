package org.task.repository.configuration;

import java.util.List;
import java.util.Optional;

public interface BaseRepository<T, I> {
    T save(T entity);

    T update(T entity);

    Optional<T> findById(I id);

    List<T> findAll();

    boolean deleteById(I id);

    boolean delete(T entity);

    boolean existsById(I id);

    long count();
}
