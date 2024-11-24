
# iLib - A Library Web Application

iLib is a library management system built as part of the final project for the Software Architectures and Methodologies course at the University of Florence. The system provides both citizen and administrator users the ability to manage and interact with various articles, such as books, magazines, and DVDs. The application is built with a Java EE backend running on WildFly and a MySQL database.

## Features

### Citizen User
- Search for articles in the library's catalogue.
- Book available articles.
- View current loans and loan history.

### Administrator
- Add, modify, or remove articles from the library catalogue.
- Register and manage users.
- Book articles or extend loans on behalf of citizens.
- Manage loans and returns.
- Consult loan and booking history of users.

## Technologies Used

- **Backend**: Java EE (Jakarta EE) using Hibernate ORM.
- **Frontend**: Angular-based UI.
- **Database**: MySQL for persistent data storage.
- **Application Server**: WildFly for running the backend.
- **Security**: JWT-based authentication for secure access.

## Setup Instructions

### Prerequisites
- **Java 11 or later**
- **WildFly Application Server**
- **MySQL Database** with schemas `iLib` and `iLib_test`.
- **Maven** for building the project.

### Database Setup

You must create two MySQL schemas for the application to function:
1. `iLib`: The main schema used by the application.
2. `iLib_test`: Schema used for end-to-end tests.

Ensure both schemas are created before running the application, as the system assumes their existence.

### WildFly Configuration

1. Add a data source for each schema in WildFly:
   - **iLibDS** for the `iLib` schema.
   - **iLibTestDS** for the `iLib_test` schema (for testing purposes).

2. Configure your WildFly `standalone.xml` to include these data sources with appropriate connection URLs, usernames, and passwords.

### Application Configuration

At startup, the application runs a script that initializes the database with a default administrator user if none exists. This user is registered with the following credentials:
- **Email**: `admin@example.com`
- **Password**: `admin password`

You can modify these credentials by changing the values in the `InitDatabase` class, located in `it.gurzu.swam.iLib.utils`.

### Building and Running the Application

1. Build the application using Maven:
   ```bash
   mvn clean install
   ```

2. Deploy the generated WAR file (`target/ilib.war`) to the WildFly server.

3. Ensure the MySQL schemas are in place, and the data sources (`iLibDS`, `iLibTestDS`) are correctly configured in WildFly.

### Tests

End-to-end tests connect to the `iLib_test` schema. To run end-to-end tests with the test db you need to modify the target DS in the `iLib/src/main/reources/META-INF/persistence.xml` file to `iLibTestDS` to avoid overriding data.
