package dev.iliv007.ivai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dev.iliv007.ivai.IvaiRuntime

class WorkspaceViewModelFactory(
    private val runtime: IvaiRuntime
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(WorkspaceViewModel::class.java))
        return WorkspaceViewModel(
            providerChatSession = runtime.providerChatSession,
            providerResolver = runtime::resolveProvider,
            workspaceRepository = runtime.workspaceRepository,
            secretVault = runtime.secretVault,
            localDataResetter = runtime.localDataResetter
        ) as T
    }
}
