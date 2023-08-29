package igor.max.rafa_poc

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PocViewModel : ViewModel() {

    private val _uiFlow = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiFlow.asStateFlow()


    fun onCameraReceived(uri: Uri) {
        _uiFlow.update {
            UiState.CameraSuccess(uri)
        }
    }
}

class PocViewModelFactory constructor() : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(PocViewModel::class.java!!)) {
            PocViewModel() as T
        } else {
            throw IllegalStateException()
        }
    }
}