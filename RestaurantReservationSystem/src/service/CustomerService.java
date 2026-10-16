package service;

import enums.Role;
import exception.FileOperationException;
import exception.InvalidInputException;
import exception.ReservationSystemException;
import model.Customer;
import repository.CustomerRepository;
import util.IdGenerator;
import util.Validator;

import java.util.ArrayList;
import java.util.List;

/** Customer registration and customer record viewing. */
public class CustomerService {

    private final CustomerRepository customerRepository = new CustomerRepository();
    private final SessionManager session = SessionManager.getInstance();

    /** Checks the format AND that no other customer already uses this contact number. */
    public String validateNewContact(String input) throws InvalidInputException {
        String contact = Validator.contactNumber(input);
        try {
            if (customerRepository.existsByContactNumber(contact)) {
                throw new InvalidInputException("That contact number is already registered. Use a different one.");
            }
        } catch (FileOperationException e) {
            throw new InvalidInputException("Could not check existing customers: " + e.getMessage());
        }
        return contact;
    }

    /** Checks the format AND that no other customer already uses this email. */
    public String validateNewEmail(String input) throws InvalidInputException {
        String email = Validator.email(input);
        try {
            if (customerRepository.existsByEmail(email)) {
                throw new InvalidInputException("That email address is already registered. Use a different one.");
            }
        } catch (FileOperationException e) {
            throw new InvalidInputException("Could not check existing customers: " + e.getMessage());
        }
        return email;
    }

    /** CREATE: registers a new customer and returns it with its generated Customer ID. */
    public Customer register(String firstName, String lastName, String contactNumber, String email)
            throws ReservationSystemException {
        // Checked again here so the service is safe even if called without the menus.
        String first = Validator.firstName(firstName);
        String last = Validator.lastName(lastName);
        String contact = validateNewContact(contactNumber);
        String mail = validateNewEmail(email);

        List<String> existingIds = new ArrayList<>();
        for (Customer c : customerRepository.findAll()) {
            existingIds.add(c.getUserId());
        }
        String customerId = IdGenerator.generateCustomerId(existingIds);

        Customer customer = new Customer(customerId, first, last, nextDuplicateNumber(first, last), contact, mail);
        customerRepository.create(customer);
        return customer;
    }

    /**
     * 0 if no one has this first + last name yet; otherwise the next free number,
     * so the names become Juan Dela Cruz, Juan Dela Cruz(1), Juan Dela Cruz(2) ...
     * Names are compared ignoring upper/lower case.
     */
    private int nextDuplicateNumber(String first, String last) throws ReservationSystemException {
        List<Integer> used = new ArrayList<>();
        for (Customer c : customerRepository.findAll()) {
            if (c.hasSameName(first, last)) {
                used.add(c.getDuplicateNumber());
            }
        }
        int number = 0;
        while (used.contains(number)) {
            number++;
        }
        return number;
    }

    /** READ: all customers (staff and manager only). */
    public List<Customer> getAllCustomers() throws ReservationSystemException {
        session.requireRole(Role.STAFF, Role.MANAGER);
        return customerRepository.findAll();
    }
}
