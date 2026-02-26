package com.arjtech.weatherapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(   // @Database annotation: Tells Room that this class represents a Room database.
    entities = [PlaceWeather::class],  // The tables in the database. Here, you have one table, places_weather represented by the PlaceWeather entity.
    version = 1,    // Database version. Used for migrations if the schema changes. Like if we change the table structure for eg. adding new column, then we need to increase the version to 2 and define migration. Otherwise app crashes.
    exportSchema = false   // Room won’t export the database schema into a JSON file (for version control).
)

abstract class AppDatabase : RoomDatabase() {    // Extends RoomDatabase which is a base class provided by Room.
                                                // You don’t instantiate this class directly; Room will generate the actual implementation at compile time.
    abstract fun placeWeatherDao(): PlaceWeatherDao  //Defines a getter for your DAO (Data Access Object).
                                                    // This lets you access your database operations like insert(), deletePlace(), getAllPlaces() for PlaceWeather.
                                                    // Room automatically generates the implementation for this method.
    companion object {   //A companion object in Kotlin is similar to static methods in Java.
                        // It allows you to create a single shared instance of the database, so multiple parts of your app can use the same database without creating new connections.
        @Volatile     // @Volatile ensures that all threads see the latest value of INSTANCE immediately.
                    // Without @Volatile, one thread could see INSTANCE as null while another already created it, leading to multiple database instances.
        private var INSTANCE: AppDatabase? = null  // INSTANCE stores the single instance of your database (singleton pattern).
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {  // If INSTANCE is already created, return it. If INSTANCE is null, execute the code inside synchronized.
                            //synchronized(this) { ... } means it Ensures that only one thread at a time can enter this block.
                            // Prevents multiple threads from creating multiple database instances simultaneously.
                val instance = Room.databaseBuilder( // Creates the Room database instance.
                    context.applicationContext,  // context.applicationContext → Ensures the database is tied to the application lifecycle, not an activity (avoids memory leaks).
                    AppDatabase::class.java,  // AppDatabase::class.java → The database class.
                    "weather_database" // "weather_database" → The database file name stored on the device.
                ).build() // .build() → Actually builds the Room database.

                INSTANCE = instance   // Stores the newly created database instance in the INSTANCE variable for future use.
                instance  // Returns the newly created instance.
            }
        }
    }
}

// Why we are using Room Database
//      Ans: Api data disappears when the app closes . It will be permanently stored in Room database even without the Internet.
//          Room has main 3 parts:  Entity -> the table in db,
//                                  DAO -> A manager that talks to the database. (all the operations like insert, delete, update etc)  and
//                                  Database -> tells the room which table exist and which DAO to use
//   How data flow in our app: UI → ViewModel → Repository → DAO → Database And reverse when reading.


// what do you mean by thread in our case ->
// There are mainly two important threads in Android:
// 1. Main Thread (UI Thread)
        //Handles UI updates
        //Button clicks
        //Showing text
        //Rendering screen
        //🚨 If this thread is blocked → App freezes → ANR (Application Not Responding)
// 2. Background Threads
     //Used for:
        //Network calls
        //Database operations
        //File reading
        //Heavy calculations
      //Room uses background threads via suspend functions (coroutines).


// Why Do We Care About Threads in Room?
// Database operations are slow compared to UI.
   //If you do:  dao.insert(placeWeather)
        //on the main thread → UI freezes ❌
//That’s why:
    //suspend fun insert(...)  means:  👉 This must run in a background thread (coroutine)


    //PART 1️⃣ — Only One Database Instance (Singleton)
        //What is an instance?
            //val db = AppDatabase()  -> This creates a database object in memory.

        //Imagine if every Activity does:

        //val db = Room.databaseBuilder(...).build()
        // Then:
            //Activity A → creates database
            //Activity B → creates another database
            //ViewModel → creates another database

        //🚨 Problem: Multiple database connections , Memory waste, Data inconsistency, Performance issues

        //So we use SINGLETON pattern In our code:
            //private var INSTANCE: AppDatabase? = null
                //Then:
                //fun getDatabase(context: Context): AppDatabase
            //What happens?
                //First time: INSTANCE is null -> Database is created -> Stored in INSTANCE
                //Second time: INSTANCE is NOT null -> Same database is returned

        //✅ So entire app shares ONE database object.
        //That’s what:  Only one database instance exists throughout the app means.

    //PART 2️⃣ — What is Thread-Safe?
        //Now the real interview question 😎

        //Imagine two threads run at the same time:
        //Thread A: getDatabase(context) and  Thread B: getDatabase(context)
        //Both see: INSTANCE == null . So both create database ❌. Now you have TWO databases 😱


        //That’s Why We Use:  @Volatile and synchronized(this)
        //🔹 What does @Volatile do?
            //It tells the system:
                //👉 Always read the latest value of INSTANCE from memory
                //👉 Do not cache it locally in a thread
            //Without it:
                //Thread A may think INSTANCE is null. Even though Thread B already created it

        //🔹 What does synchronized(this) do?
            //This is a LOCK. It says:
            //🛑 "Only ONE thread can enter this block at a time."

        //So:
            //Thread A enters → creates database
            //Thread B waits
            //Thread A finishes
            //Thread B checks again → INSTANCE is not null → returns existing one
        //✅ No duplicate databases.

        //So What Does Thread-Safe Mean?

        //Thread-safe means:
            // Even if multiple threads access this code at the same time, it will behave correctly and not create bugs.
            //Your database creation is protected against: Race conditions, Duplicate instances, Memory inconsistency

//Real Interview Answer (Short Version)
//If recruiter asks:
//❓ Why did you use @Volatile and synchronized in Room database?
//You say:

//I implemented Room database using Singleton pattern to ensure only one instance exists across the application.
//I used @Volatile to make sure the INSTANCE variable is immediately visible to all threads.
//I used synchronized block to prevent multiple threads from creating multiple database instances simultaneously.
//This ensures thread safety and avoids race conditions.

// Diff between SQLite and Room

//    | SQLite          | Room                |
//    | --------------- | ------------------- |
//    | Manual SQL      | Annotation-based    |
//    | Error-prone     | Compile-time checks |
//    | Harder to use   | Easier              |
//    | No Flow support | Flow support        |
