package repository;

import exception.CustomerNotFoundException;
import exception.FileOperationException;
import exception.ReservationSystemException;
import model.Customer;

/** CRUD for customers, stored in CUSTOMER.txt. */
public class CustomerRepository extends FileRepository<Customer> {

    public CustomerRepository() {
        super(FileManager.CUSTOMER_FILE);
    }

    @Override
    protected Customer parse(String line) {
        return Customer.fromFileString(line);
    }

    @Override
    protected String getId(Customer customer) {
        return customer.getUserId();
    }

    @Override
    protected ReservationSystemException notFound(String id) {
        return new CustomerNotFoundException(id);
    }

    /** Returns the customer with this email, or null if no one registered with it. */
    public Customer findByEmail(String email) throws FileOperationException {
        for (Customer c : findAll()) {
            if (c.getEmail().equalsIgnoreCase(email)) {
                return c;
            }
        }
        return null;
    }

    public boolean existsByEmail(String email) throws FileOperationException {
        return findByEmail(email) != null;
    }

    public boolean existsByContactNumber(String contactNumber) throws FileOperationException {
        for (Customer c : findAll()) {
            if (c.getContactNumber().equals(contactNumber)) {
                return true;
            }
        }
        return false;
    }
}
