# SimpleJavaFXApp

JavaFX lab work for ICT261 Advanced Java Programming by Musenga Hope Sichula (202511308).

The repository holds two separate Gradle projects:

| Project | Folder | What it does |
|---|---|---|
| SimpleJavaFXApp | repository root | My first JavaFX app: a window with a welcome message and Start and Reset buttons |
| Customer Manager | `CustomerManager/` | Unit 3 Lecture 3 lab: add, list and delete customers by province |

## Requirements

- JDK 21
- No Gradle install needed: each project has its own Gradle wrapper (`gradlew`)

## Run SimpleJavaFXApp

From the repository root:

```
.\gradlew run
```

## Run Customer Manager

Move into the `CustomerManager` folder first, then run it:

```
cd CustomerManager
.\gradlew run
```

### Customer Manager features

1. A form with a name field and a list of Zambia's 10 provinces
2. A `Customer` class and an `ObservableList<Customer>`
3. A `TableView` with name and province columns
4. Input validation before a customer is added (letters only, 2 to 50 characters, province required)
5. A confirmation dialog before a selected customer is deleted
6. Keyboard access: Tab moves between controls, Enter saves, labels are linked to their fields

The window can be resized: the fields stretch with it and the table takes the extra height.
