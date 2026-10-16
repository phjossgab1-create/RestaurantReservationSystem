package model;

import enums.Role;
import ui.StaffMenu;

/** Staff handle day-to-day reservations: approvals, check-in/out and payments. */
public class Staff extends Employee {

    public Staff(String username, String password, String fullName) {
        super(username, password, fullName);
    }

    @Override
    public Role getRole() {
        return Role.STAFF;
    }

    @Override
    public void showMenu() {
        new StaffMenu(this).run();
    }
}
