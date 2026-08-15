package dev.iliv007.ivai.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        WorkspaceProjectEntity::class,
        ChatThreadEntity::class,
        ChatMessageEntity::class,
        ProviderConnectionEntity::class,
        ProviderAccountEntity::class,
        ProviderModelEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class IvaiDatabase : RoomDatabase() {
    abstract fun projectDao(): WorkspaceProjectDao
    abstract fun threadDao(): ChatThreadDao
    abstract fun messageDao(): ChatMessageDao
    abstract fun providerConnectionDao(): ProviderConnectionDao
    abstract fun providerAccountDao(): ProviderAccountDao
    abstract fun providerModelDao(): ProviderModelDao

    companion object {
        const val DATABASE_NAME = "ivai-workspace.db"

        fun create(context: Context): IvaiDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                IvaiDatabase::class.java,
                DATABASE_NAME
            ).addMigrations(MIGRATION_1_2).build()

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `provider_connections` (" +
                        "`id` TEXT NOT NULL, `provider_kind` TEXT NOT NULL, `display_name` TEXT NOT NULL, " +
                        "`base_url` TEXT, `is_enabled` INTEGER NOT NULL, `created_at_epoch_ms` INTEGER NOT NULL, " +
                        "`updated_at_epoch_ms` INTEGER NOT NULL, PRIMARY KEY(`id`))"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_provider_connections_provider_kind` ON `provider_connections` (`provider_kind`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_provider_connections_updated_at_epoch_ms` ON `provider_connections` (`updated_at_epoch_ms`)")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `provider_accounts` (" +
                        "`id` TEXT NOT NULL, `connection_id` TEXT NOT NULL, `display_name` TEXT NOT NULL, " +
                        "`credential_reference` TEXT NOT NULL, `is_enabled` INTEGER NOT NULL, " +
                        "`created_at_epoch_ms` INTEGER NOT NULL, `updated_at_epoch_ms` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`id`), FOREIGN KEY(`connection_id`) REFERENCES `provider_connections`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_provider_accounts_connection_id` ON `provider_accounts` (`connection_id`)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_provider_accounts_credential_reference` ON `provider_accounts` (`credential_reference`)")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `provider_models` (" +
                        "`id` TEXT NOT NULL, `connection_id` TEXT NOT NULL, `provider_model_id` TEXT NOT NULL, " +
                        "`display_name` TEXT NOT NULL, `capabilities_csv` TEXT NOT NULL, `is_manual` INTEGER NOT NULL, " +
                        "`is_selectable` INTEGER NOT NULL, `updated_at_epoch_ms` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`id`), FOREIGN KEY(`connection_id`) REFERENCES `provider_connections`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_provider_models_connection_id` ON `provider_models` (`connection_id`)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_provider_models_connection_id_provider_model_id` ON `provider_models` (`connection_id`, `provider_model_id`)")
            }
        }
    }
}
