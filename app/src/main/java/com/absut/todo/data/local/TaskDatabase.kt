package com.absut.todo.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.absut.todo.data.Task
import com.absut.todo.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [Task::class], version = 1)
abstract class TaskDatabase: RoomDatabase() {

    abstract fun taskDao() : TaskDao

    /**
     * Just to pre populate list with some tasks
     * instead of showing empty list at start
     * **/
    class Callback @Inject constructor(
        private val database: Provider<TaskDatabase>,
        @ApplicationScope private val applicationScope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            val dao = database.get().taskDao()

            applicationScope.launch {
                dao.insert(Task(name = "Wash the dishes"))
                dao.insert(Task(name = "Do the laundry"))
                dao.insert(Task(name = "Buy groceries", important = true))
                dao.insert(Task(name = "Prepare food", completed = true))
                dao.insert(Task(name = "Call mom"))
                dao.insert(Task(name = "Visit grandma", completed = true))
                dao.insert(Task(name = "Repair my bike"))
                dao.insert(Task(name = "Call Elon Musk"))
            }
        }
    }
}