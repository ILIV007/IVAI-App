package dev.iliv007.ivai.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        WorkspaceProjectEntity::class,
        ChatThreadEntity::class,
        ChatMessageEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class IvaiDatabase : RoomDatabase() {
    abstract fun projectDao(): WorkspaceProjectDao
    abstract fun threadDao(): ChatThreadDao
    abstract fun messageDao(): ChatMessageDao

    companion object {
        const val DATABASE_NAME = "ivai-workspace.db"

        fun create(context: Context): IvaiDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                IvaiDatabase::class.java,
                DATABASE_NAME
            ).build()
    }
}
