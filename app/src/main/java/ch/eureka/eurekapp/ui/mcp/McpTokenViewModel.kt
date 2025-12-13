// Co-authored by Claude Code
package ch.eureka.eurekapp.ui.mcp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.data.mcp.McpToken
import ch.eureka.eurekapp.model.data.mcp.McpTokenRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class McpTokenUIState(
    val tokens: List<McpToken> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val newlyCreatedToken: String? = null
)

class McpTokenViewModel(private val repository: McpTokenRepository) : ViewModel() {

  private val _uiState = MutableStateFlow(McpTokenUIState())
  val uiState: StateFlow<McpTokenUIState> = _uiState

  init {
    loadTokens()
  }

  fun loadTokens() {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true, error = null) }
      repository
          .listTokens()
          .onSuccess { tokens -> _uiState.update { it.copy(tokens = tokens, isLoading = false) } }
          .onFailure { error ->
            _uiState.update { it.copy(error = error.message, isLoading = false) }
          }
    }
  }

  fun createToken(name: String, ttlDays: Int = 30) {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true, error = null) }
      repository
          .createToken(name, ttlDays)
          .onSuccess { result ->
            // Show the raw token to the user (only time it's visible)
            _uiState.update { it.copy(newlyCreatedToken = result.rawToken, isLoading = false) }
            loadTokens()
          }
          .onFailure { error ->
            _uiState.update { it.copy(error = error.message, isLoading = false) }
          }
    }
  }

  fun revokeToken(tokenHash: String) {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true, error = null) }
      repository
          .revokeToken(tokenHash)
          .onSuccess { loadTokens() }
          .onFailure { error ->
            _uiState.update { it.copy(error = error.message, isLoading = false) }
          }
    }
  }

  fun clearNewlyCreatedToken() {
    _uiState.update { it.copy(newlyCreatedToken = null) }
  }

  fun clearError() {
    _uiState.update { it.copy(error = null) }
  }
}
