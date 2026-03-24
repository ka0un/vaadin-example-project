# Notes Application (Vaadin & Spring Boot Project)
## Implemented Feature: Image Upload & Note Editing

This implementation extends the Notes Application by adding support for image handling and improved user experience.

###  Features Added

- Create notes with optional image upload
- Preview image before saving
- Display images inside saved notes
- Click image to view a larger preview
- Edit notes with:
  - image replacement
  - image removal
- Delete notes with confirmation dialog
- Improved responsive layout using reusable UI components

---

###  How to Use the Feature

1. Open the application and log in
2. Enter note content in the form
3. (Optional) Upload an image
4. Click **Add Note**
5. View saved notes in the "Saved Notes" section
6. Click on an image to preview it in a larger view
7. Click **Edit** to modify a note
   - Update text
   - Upload a new image to replace existing one
   - Remove image if needed
8. Click **Update Note** to save changes
9. Click **Delete** to remove a note (confirmation required)

---

### Technical Implementation

- Images stored in database using `byte[]` with `@Lob`
- UI refactored into reusable components:
  - `NoteFormCard`
  - `NotesListCard`
  - `NoteItemCard`
- Used `FlexLayout` for responsive design
- Upload handled using Vaadin `Upload` + `MemoryBuffer`
- Added validation for empty notes and file restrictions

---



Welcome to the **Notes Application**! This is a web application built using **Java**, **Spring Boot**, and **Vaadin**. 

This guide will walk you through the absolute basics of getting this project up and running on your computer, from installing the necessary prerequisites to launching the application. If you have never run a Java project before, you're in the right place!

## Table of Contents
1. [Prerequisites (What you need installed)](#prerequisites)
2. [Downloading the Project](#downloading-the-project)
3. [How to Open the Project](#how-to-open-the-project)
4. [How to Run the Project](#how-to-run-the-project)
5. [Detailed Project Structure](#detailed-project-structure)

---

## Prerequisites
Before you can run this project, you need to have **Java** installed on your computer.

### 1. Install Java 21 (JDK)
This project requires **Java 21**. A "JDK" (Java Development Kit) contains the tools needed to develop and run Java applications.

**For Windows / macOS / Linux:**
1. Go to the [Adoptium Eclipse Temurin Website](https://adoptium.net/temurin/releases/?version=21) (a popular, free, and open-source distribution of Java).
2. Select your Operating System (e.g., Windows), Architecture (e.g., x64), and Package Type (`JDK`).
3. Download the installer (e.g., the `.msi` file for Windows or `.pkg` for macOS) and run it.
4. **Important for Windows Users:** During installation, make sure to enable the option **"Set or Add to JAVA_HOME environment variable"** and **"Add to PATH"**.

**To verify Java is installed:**
Open your terminal (Command Prompt or PowerShell on Windows, Terminal on macOS/Linux) and type:
```bash
java -version
```
You should see a message mentioning Java version 21.

*Note: You do not need to install Maven, as this project uses a "Maven Wrapper" (`mvnw`) which automatically downloads the required build tools for you.*

---

## Downloading the Project

If you haven't already downloaded the project to your computer, you will need to get it from GitHub.

**Option 1: Download as a ZIP File (Easiest)**
1. Go to the project's page on GitHub.
2. Click the green **Code** button.
3. Select **Download ZIP**.
4. Once downloaded, extract the ZIP file to a folder on your computer (e.g., `Documents/vaadin-example-project`).

**Option 2: Clone using Git (For developers)**
If you have [Git](https://git-scm.com/) installed, open your terminal and run:
```bash
git clone <URL_OF_THE_GITHUB_REPOSITORY>
```

---

## How to Open the Project

To view and edit the code, you should use an **Integrated Development Environment (IDE)**. An IDE is advanced software that makes writing and running code much easier.

**Recommended IDE: IntelliJ IDEA (Free Community Edition)**
1. Go to the [JetBrains IntelliJ IDEA Download Page](https://www.jetbrains.com/idea/download/).
2. Scroll down to the **IntelliJ IDEA Community Edition** and download it.
3. Run the installer and open IntelliJ IDEA.
4. Click **Open** (or File -> Open) and browse to the folder where you extracted or cloned the project (select the folder containing the `pom.xml` file).
5. Click **OK**. IntelliJ will start downloading necessary dependencies and indexing the project. This may take a few minutes the first time.

*(Alternatively, you can use Eclipse or Visual Studio Code with the Java Extension Pack).*

---

## How to Run the Project

You can run this project directly from your terminal/command prompt.

1. Open a terminal or command prompt.
2. Navigate to the project's main folder (the folder containing the `pom.xml` file). For example:
   ```bash
   cd path/to/vaadin-example-project
   ```
3. Run the application using the Maven wrapper:
   - **On Windows:**
     ```cmd
     .\mvnw.cmd spring-boot:run
     ```
   - **On macOS / Linux:**
     ```bash
     ./mvnw spring-boot:run
     ```

4. Wait for the project to finish downloading dependencies, compiling, and starting up. You will see several log messages. 
5. Look for a message that says `Started [ApplicationName] in X.XXX seconds`.
6. Open your web browser and navigate to:
   **[http://localhost:8080](http://localhost:8080)**

You should now see the application running! To stop the application, return to your terminal and press `Ctrl + C`.

---

## Detailed Project Structure

This project uses the standard layout for a Spring Boot and Vaadin application. Here's a breakdown of what the different files and folders do:

```text
vaadin-example-project/
│
├── pom.xml                 # The most important configuration file! It tells Maven what libraries
│                           # the project needs (Spring Boot, Vaadin, Spring Security, H2 Database).
│
├── mvnw / mvnw.cmd         # Maven Wrapper scripts. These allow you to build the project without
│                           # having to install Maven manually.
│
├── src/                    # The folder containing all the source code for the project.
│   ├── main/
│   │   ├── java/           # This directory holds all the Java backend code.
│   │   │   └── com/...     # Contains the application launcher, Vaadin Views (UI screens),
│   │   │                   # Services (business logic), entities (database models), and security configs.
│   │   │
│   │   ├── frontend/       # This folder contains frontend resources like TypeScript/JavaScript
│   │   │                   # files, Vaadin component configurations, and custom styles (CSS).
│   │   │
│   │   └── resources/      # This folder contains static assets and configuration files.
│   │       ├── application.properties  # App settings like server port, database connection configs, etc.
│   │       └── ...         # Icons, images, and HTML templates.
│   │
│   └── test/               # Contains automated tests for the project.
│       └── java/           # Java unit tests and Spring Boot integration tests.
│
├── .gitignore              # Tells Git which files/folders it should ignore (e.g., compiled code).
└── target/                 # Created automatically when the project is built. Contains compiled code.
```

### Key Technologies Used
* **Spring Boot:** The core backend framework handling the server, routing, and configuration.
* **Vaadin:** A web framework that allows developers to write user interfaces (UI) entirely in Java. It handles the frontend automatically.
* **Spring Security:** Provides authentication and authorization for the app.
* **Spring Data JPA & H2 Database:** Used for saving and loading data. H2 is configured as a file-based database (stored in the `./data` folder), meaning your notes and data will persist (be saved) even if you restart the application.

---

## Common Issues / Troubleshooting

### 1. `The JAVA_HOME environment variable is not defined correctly`
**What it means:** The application cannot find your Java installation or you haven't told your system where it is.
**How to fix:** 
- Make sure you actually installed Java 21 (see Prerequisites). 
- If Java is installed, you need to set your `JAVA_HOME` environment variable to point to the folder where Java was installed (e.g., `C:\Program Files\Java\jdk-21`). Ensure the path does **not** end with `\bin`.
- After setting the variable, you **must close your current terminal window and open a new one** before trying again.

### 2. `Port 8080 was already in use`
**What it means:** Another application (or another instance of this project) is currently running and using the default port.
**How to fix:** Find the terminal running the application and press `Ctrl+C` to stop it. If you can't find it, you can restart your computer, or change the `server.port` setting inside `src/main/resources/application.properties`.

### 3. Changes don't appear in the browser
**What it means:** Sometimes your browser caches older files.
**How to fix:** Try completely refreshing the page (`Ctrl + F5` on Windows, `Cmd + Shift + R` on Mac). If that doesn't help, restart the server by pressing `Ctrl+C` in your terminal and running the `spring-boot:run` command again.
