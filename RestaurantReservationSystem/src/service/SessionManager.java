package service;

import enums.Role;
import exception.InvalidLoginException;
import exception.ReservationSystemException;
import exception.UnauthorizedActionException;
import model.Customer;
import model.Employee;
import model.User;
import repository.CustomerRepository;
import repository.EmployeeRepository;

/**
 * SINGLETON PATTERN.
 * Holds the one user who is currently logged in, and checks role permissions.
 * Every service asks this same instance "who is logged in?".
 */
public final class SessionManager {

    private static SessionManager instance;

    private final CustomerRepository customerRepository = new CustomerRepository();
    private final EmployeeRepository employeeRepository = new EmployeeRepository();
    private User currentUser;

    private SessionManager() {
    }

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /** Customers log in with the email address they registered with. */
    public Customer loginCustomer(String email) throws ReservationSystemException {
        Customer customer = customerRepository.findByEmail(email);
        if (customer == null) {
            throw new InvalidLoginException("No account is registered with " + email
                    + ". Check the spelling, or go back and register first.");
        }
        currentUser = customer;
        return customer;
    }

    /** Staff and managers log in with username and password. */
    public Employee loginEmployee(String username, String password) throws ReservationSystemException {
        Employee employee = employeeRepository.findById(username);
        if (!employee.checkPassword(password)) {
            throw new InvalidLoginException("Invalid username or password.");
        }
        currentUser = employee;
        return employee;
    }

    public void logout() {
        currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    /** Throws UnauthorizedActionException unless the current user has one of the allowed roles. */
    public void requireRole(Role... allowedRoles) throws UnauthorizedActionException {
        if (currentUser == null) {
            throw new UnauthorizedActionException("You must be logged in to do that.");
        }
        for (Role role : allowedRoles) {
            if (currentUser.getRole() == role) {
                return;
            }
        }
        throw new UnauthorizedActionException("Access denied: this action is not available to "
                + currentUser.getRole().getLabel() + " accounts.");
    }

    public Customer getCurrentCustomer() throws UnauthorizedActionException {
        requireRole(Role.CUSTOMER);
        return (Customer) currentUser;
    }
}
