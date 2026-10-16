package repository;

import exception.FileOperationException;
import exception.ReservationSystemException;

import java.util.List;

/**
 * The four CRUD operations every repository must provide (Abstraction).
 * T is the type being stored, e.g. Customer or Reservation.
 */
public interface CrudRepository<T> {

    /** C - Create */
    void create(T item) throws ReservationSystemException;

    /** R - Read one */
    T findById(String id) throws ReservationSystemException;

    /** R - Read all */
    List<T> findAll() throws FileOperationException;

    /** U - Update */
    void update(T item) throws ReservationSystemException;

    /** D - Delete */
    void delete(String id) throws ReservationSystemException;
}
