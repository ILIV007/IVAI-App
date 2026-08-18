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
        ProviderModelEntity::class,
        RouterComboEntity::class,
        RouterComboEntryEntity::class,
        ThreadExecutionTargetEntity::class,
        RouterAttemptEntity::class,
        RouterAttemptEntryEntity::class,
        AgentProfileEntity::class,
        AgentRunEntity::class,
        AgentRunStepEntity::class,
        AgentApprovalEntity::class
    ],
    version = 6,
    exportSchema = true
)
abstract class IvaiDatabase : RoomDatabase() {
    abstract fun projectDao(): WorkspaceProjectDao
    abstract fun threadDao(): ChatThreadDao
    abstract fun messageDao(): ChatMessageDao
    abstract fun providerConnectionDao(): ProviderConnectionDao
    abstract fun providerAccountDao(): ProviderAccountDao
    abstract fun providerModelDao(): ProviderModelDao
    abstract fun routerComboDao(): RouterComboDao
    abstract fun routerComboEntryDao(): RouterComboEntryDao
    abstract fun threadExecutionTargetDao(): ThreadExecutionTargetDao
    abstract fun routerAttemptDao(): RouterAttemptDao
    abstract fun routerAttemptEntryDao(): RouterAttemptEntryDao
    abstract fun agentProfileDao(): AgentProfileDao
    abstract fun agentRunDao(): AgentRunDao
    abstract fun agentRunStepDao(): AgentRunStepDao
    abstract fun agentApprovalDao(): AgentApprovalDao

    companion object {
        const val DATABASE_NAME = "ivai-workspace.db"

        fun create(context: Context): IvaiDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                IvaiDatabase::class.java,
                DATABASE_NAME
            ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6).build()

        /**
         * A visible stream that ends without completion is retained as an explicit local incomplete
         * assistant message. Existing messages remain complete by default.
         */
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `chat_messages` ADD COLUMN `is_incomplete` INTEGER NOT NULL DEFAULT 0")
            }
        }

        /**
         * Existing provider connections retain remote HTTPS/API-key behavior. No local endpoint,
         * no-auth account, network call, or secret is created during upgrade.
         */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `provider_connections` ADD COLUMN `endpoint_trust_mode` TEXT NOT NULL DEFAULT 'REMOTE_HTTPS'")
                db.execSQL("ALTER TABLE `provider_connections` ADD COLUMN `local_trust_confirmed_at_epoch_ms` INTEGER")
                db.execSQL("ALTER TABLE `provider_accounts` ADD COLUMN `auth_mode` TEXT NOT NULL DEFAULT 'API_KEY'")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_provider_accounts_credential_reference` ON `provider_accounts` (`credential_reference`)")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `agent_profiles` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `instructions` TEXT NOT NULL, `target_kind` TEXT NOT NULL, `target_id` TEXT NOT NULL, `account_id` TEXT, `project_id` TEXT, `enabled_tools_csv` TEXT NOT NULL, `max_steps` INTEGER NOT NULL, `max_tool_calls` INTEGER NOT NULL, `max_runtime_ms` INTEGER NOT NULL, `is_enabled` INTEGER NOT NULL, `created_at_epoch_ms` INTEGER NOT NULL, `updated_at_epoch_ms` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`project_id`) REFERENCES `workspace_projects`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_agent_profiles_project_id` ON `agent_profiles` (`project_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_agent_profiles_updated_at_epoch_ms` ON `agent_profiles` (`updated_at_epoch_ms`)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `agent_runs` (`id` TEXT NOT NULL, `agent_id` TEXT NOT NULL, `goal` TEXT NOT NULL, `status` TEXT NOT NULL, `started_at_epoch_ms` INTEGER NOT NULL, `completed_at_epoch_ms` INTEGER, `safe_error_message` TEXT, PRIMARY KEY(`id`), FOREIGN KEY(`agent_id`) REFERENCES `agent_profiles`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_agent_runs_agent_id_started_at_epoch_ms` ON `agent_runs` (`agent_id`, `started_at_epoch_ms`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_agent_runs_status` ON `agent_runs` (`status`)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `agent_run_steps` (`id` TEXT NOT NULL, `run_id` TEXT NOT NULL, `position` INTEGER NOT NULL, `step_kind` TEXT NOT NULL, `status` TEXT NOT NULL, `safe_summary` TEXT NOT NULL, `created_at_epoch_ms` INTEGER NOT NULL, `completed_at_epoch_ms` INTEGER, PRIMARY KEY(`id`), FOREIGN KEY(`run_id`) REFERENCES `agent_runs`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_agent_run_steps_run_id_position` ON `agent_run_steps` (`run_id`, `position`)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `agent_approvals` (`id` TEXT NOT NULL, `run_id` TEXT NOT NULL, `tool_kind` TEXT NOT NULL, `target_path` TEXT NOT NULL, `preview` TEXT NOT NULL, `status` TEXT NOT NULL, `created_at_epoch_ms` INTEGER NOT NULL, `resolved_at_epoch_ms` INTEGER, PRIMARY KEY(`id`), FOREIGN KEY(`run_id`) REFERENCES `agent_runs`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_agent_approvals_run_id_status` ON `agent_approvals` (`run_id`, `status`)")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `router_combos` (`id` TEXT NOT NULL, `display_name` TEXT NOT NULL, `description` TEXT NOT NULL, `is_enabled` INTEGER NOT NULL, `created_at_epoch_ms` INTEGER NOT NULL, `updated_at_epoch_ms` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_router_combos_updated_at_epoch_ms` ON `router_combos` (`updated_at_epoch_ms`)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `router_combo_entries` (`id` TEXT NOT NULL, `combo_id` TEXT NOT NULL, `position` INTEGER NOT NULL, `connection_id` TEXT NOT NULL, `account_id` TEXT NOT NULL, `model_id` TEXT NOT NULL, `is_enabled` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`combo_id`) REFERENCES `router_combos`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, FOREIGN KEY(`connection_id`) REFERENCES `provider_connections`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, FOREIGN KEY(`account_id`) REFERENCES `provider_accounts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, FOREIGN KEY(`model_id`) REFERENCES `provider_models`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_router_combo_entries_combo_id_position` ON `router_combo_entries` (`combo_id`, `position`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_router_combo_entries_connection_id` ON `router_combo_entries` (`connection_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_router_combo_entries_account_id` ON `router_combo_entries` (`account_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_router_combo_entries_model_id` ON `router_combo_entries` (`model_id`)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `thread_execution_targets` (`thread_id` TEXT NOT NULL, `target_kind` TEXT NOT NULL, `target_id` TEXT NOT NULL, `account_id` TEXT, `updated_at_epoch_ms` INTEGER NOT NULL, PRIMARY KEY(`thread_id`), FOREIGN KEY(`thread_id`) REFERENCES `chat_threads`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_thread_execution_targets_target_kind_target_id` ON `thread_execution_targets` (`target_kind`, `target_id`)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `router_attempts` (`id` TEXT NOT NULL, `thread_id` TEXT, `target_kind` TEXT NOT NULL, `target_id` TEXT NOT NULL, `outcome` TEXT NOT NULL, `started_at_epoch_ms` INTEGER NOT NULL, `completed_at_epoch_ms` INTEGER, `safe_error_kind` TEXT, `safe_error_message` TEXT, PRIMARY KEY(`id`), FOREIGN KEY(`thread_id`) REFERENCES `chat_threads`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_router_attempts_thread_id_started_at_epoch_ms` ON `router_attempts` (`thread_id`, `started_at_epoch_ms`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_router_attempts_target_kind_target_id` ON `router_attempts` (`target_kind`, `target_id`)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `router_attempt_entries` (`id` TEXT NOT NULL, `attempt_id` TEXT NOT NULL, `position` INTEGER NOT NULL, `connection_id` TEXT NOT NULL, `account_id` TEXT NOT NULL, `model_id` TEXT NOT NULL, `outcome` TEXT NOT NULL, `started_at_epoch_ms` INTEGER NOT NULL, `completed_at_epoch_ms` INTEGER, `safe_error_kind` TEXT, `safe_error_message` TEXT, PRIMARY KEY(`id`), FOREIGN KEY(`attempt_id`) REFERENCES `router_attempts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_router_attempt_entries_attempt_id_position` ON `router_attempt_entries` (`attempt_id`, `position`)")
            }
        }

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
