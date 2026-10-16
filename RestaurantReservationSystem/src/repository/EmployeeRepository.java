package repository;

import exception.FileOperationException;
import exception.InvalidLoginException;
import exception.ReservationSystemException;
import model.Employee;
import model.Manager;
import model.Staff;

import java.util.ArrayList;
import java.util.List;

/**
 * Staff and Manager accounts, stored in EMPLOYEE.txt.
 * The spec has no employee registration, so default accounts are created on first run.
 */
public class EmployeeRepository extends FileRepository<Employee> {

    public EmployeeRepository() {
        super(FileManager.EMPLOYEE_FILE);
    }

    @Override
    protected Employee parse(String line) {
        return Employee.fromFileString(line);
    }

    @Override
    protected String getId(Employee employee) {
        return employee.getUsername();
    }

    @Override
    protected ReservationSystemException notFound(String id) {
        // Same message as a wrong password, so attackers can't tell which usernames exist.
        return new InvalidLoginException("Invalid username or password.");
    }

    /** Creates the default staff and manager accounts if EMPLOYEE.txt is empty. */
    public void ensureDefaultAccounts() throws FileOperationException {
        if (!findAll().isEmpty()) {
            return;
        }
        List<Employee> defaults = new ArrayList<>();
        defaults.add(new Staff("staff", "staff123", "Default Staff"));
        defaults.add(new Manager("manager", "manager123", "Default Manager"));
        saveAll(defaults);
    }
}
