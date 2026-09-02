package com.prammmoe.pictrim.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prammmoe.pictrim.data.OnboardingPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class OnboardingGateViewModel @Inject constructor(preferences: OnboardingPreferences) : ViewModel() {
    val completed: Flow<Boolean> = preferences.isCompleted
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(private val preferences: OnboardingPreferences) : ViewModel() {
    private val _page = MutableStateFlow(0)
    val page = _page.asStateFlow()

    fun next() { if (_page.value < PageCount - 1) _page.value++ }
    fun back() { if (_page.value > 0) _page.value-- }
    fun finish(onFinished: () -> Unit) = viewModelScope.launch {
        preferences.completeOnboarding()
        onFinished()
    }

    companion object { const val PageCount = 3 }
}
