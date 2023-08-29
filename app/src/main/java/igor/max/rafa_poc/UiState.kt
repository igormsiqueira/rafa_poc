package igor.max.rafa_poc

import android.net.Uri

sealed interface UiState {
    object Loading : UiState

    data class CameraSuccess(
        val cameraData: Uri
    ) : UiState

    data class Error(
        val throwable: Throwable? = null
    ) : UiState
}