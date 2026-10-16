# Restaurant Reservation System (Java)

A console-based restaurant reservation system with login, CRUD operations, three roles
(Customer, Staff, Manager) and text-file storage.

## How to run in VS Code

1. Install a JDK (Java 17 or newer) and the **Extension Pack for Java** in VS Code.
2. Open the `RestaurantReservationSystem` folder (File > Open Folder).
3. Open `src/Main.java` and click **Run** above `main`, or press F5 and choose
   "Run Restaurant Reservation System".

The program runs in the terminal panel so you can type input.

**Run from a terminal instead** (from the project folder):

```
javac -d bin src/*.java src/*/*.java src/*/*/*.java
java -cp bin Main
```

## Logging in

| Role     | How to log in                                    |
|----------|--------------------------------------------------|
| Customer | Register first, then log in with your email address |
| Staff    | username `staff`, password `staff123`           |
| Manager  | username `manager`, password `manager123`       |

Staff and manager accounts are created automatically on first run in `data/EMPLOYEE.txt`.
You can edit that file to add more accounts (`username|password|Full Name|STAFF or MANAGER`).

## Using the menus

- Type `\` at any prompt to go back one question (fix your previous answer).
  On the first question of a screen, `\` returns to the menu.
- Menu choices must be typed exactly: `1` works, `01` or ` 1` does not.
- If you type something invalid, the program explains why and asks the same question again.
- Reservation IDs are typed as the 6 digits only (for `RN-123456`, type `123456`).

## Data files (created in the `data` folder)

| File          | Contents                                   |
|---------------|--------------------------------------------|
| CUSTOMER.txt  | Registered customers                       |
| RESERVE.txt   | Active reservations                        |
| STATUS.txt    | Log of every check-in and check-out        |
| CANCELLED.txt | Cancelled reservations (moved, not erased) |
| EMPLOYEE.txt  | Staff and manager accounts                 |

Delete the `data` folder to start fresh.

## Project structure

```
src/
  Main.java                    Entry point, main menu, register and login
  enums/                       Role, TableType, ReservationStatus, PaymentStatus
  exception/                   ReservationSystemException + 9 specific exceptions, GoBackException
  model/                       User, Customer, Employee, Staff, Manager,
                               Reservation, PendingChange, Storable
  repository/                  FileManager (Singleton), CrudRepository,
                               FileRepository, Customer/Reservation/EmployeeRepository
  service/                     SessionManager (Singleton), CustomerService,
                               ReservationService
  strategy/charge/             ChargeStrategy, NoCharge, RescheduleCharge, CancellationCharge
  strategy/sort/               SortStrategy, SortByDate, SortByPaymentStatus
  ui/                          Menu, EmployeeMenu, CustomerMenu, StaffMenu,
                               ManagerMenu, ConsoleInput, StepForm, ReservationForm, ReservationPrinter
  util/                        IdGenerator, Validator, InputParser, BookingRules
```

## Where each requirement is

**Encapsulation**: all fields are `private`. `Reservation` has no setters for status or money,
only methods like `checkIn()` and `recordPayment()` that enforce the rules.
`Employee` never exposes its password, it only offers `checkPassword()`.

**Abstraction**: `User`, `Employee`, `FileRepository` and `Menu` are abstract classes.
`Storable`, `CrudRepository`, `ChargeStrategy` and `SortStrategy` are interfaces.

**Inheritance**: `User -> Customer`, `User -> Employee -> Staff / Manager`,
`Menu -> EmployeeMenu -> StaffMenu / ManagerMenu`, `FileRepository -> 3 repositories`,
and all exceptions extend `ReservationSystemException`.

**Polymorphism**: `user.showMenu()` in `Main` opens a different menu per role.
`ReservationRepository.delete()` overrides the normal delete to archive instead.
`toFileString()` differs per class. The service calls whichever strategy it holds.

**Singleton**: `FileManager` (the only class that touches files) and
`SessionManager` (the one logged-in user). Private constructor + `getInstance()`.

**Strategy**: `ReservationService` holds a `ChargeStrategy` (reschedule fee, cancellation
fee, or none, chosen at runtime) and a `SortStrategy` (by date or by payment status).

**Exception handling**: custom checked exceptions for login, not found, validation,
permissions, invalid status changes, payments and file errors. `Menu.run()` and `Main` catch
them so the program never crashes. `ConsoleInput` catches `NumberFormatException` and
`DateTimeParseException` for bad typing.

**CRUD**:
- Create: register, make reservation.
- Read: view, search, sort, filter.
- Update: approve update, payment, check-in/out.
- Delete: approved cancellation moves the record to CANCELLED.txt.

## Business rules (easy to change)

- Table types, seats and prices: `enums/TableType.java`
  - Regular: 4 seats, PHP 500
  - Family: 8 seats, PHP 1,200
  - VIP: 6 seats, PHP 2,500
- Reschedule fee: PHP 200 (`RescheduleCharge.java`).
- Cancellation fee: 20% of the table cost (`CancellationCharge.java`).
- Booking hours: 10:00 to 21:00, max 10 tables, up to 1 year ahead (`util/BookingRules.java`).
- Names: first and last name, letters only; the same first + last name gets (1), (2) ... Contact: 11 digits starting with 09. Email: standard format (`util/Validator.java`).
- Customer updates wait for staff approval. Cancellations wait for manager approval.
- Check-out requires the balance to be fully paid.
