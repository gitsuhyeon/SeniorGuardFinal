package com.example.seniorguard.token


import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seniorguard.data.repository.TokenRepository
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TokenViewModel @Inject constructor(
    private val tokenRepository: TokenRepository,
    private val dataStore: DataStore<Preferences> // Hilt를 통해 DataStore 주입
) : ViewModel() {

    // DataStore에서 사용할 키를 정의합니다.
    companion object {
        private val IS_TOKEN_REGISTERED_KEY = booleanPreferencesKey("is_token_registered")
    }

    /**
     * 기기의 역할("monitoring" 또는 "guardian")에 따라 FCM 토큰을 서버에 등록합니다.
     * 이미 등록된 경우, 불필요한 API 호출을 건너뜁니다.
     * @param role 등록할 기기의 역할
     */
    fun registerDeviceToken(role: String) {
        viewModelScope.launch {
            /*
            // 1. DataStore에서 "이미 등록되었는지" 여부를 확인합니다.
            val isAlreadyRegistered = dataStore.data.first()[IS_TOKEN_REGISTERED_KEY] ?: false
            if (isAlreadyRegistered) {
                Log.d("TokenViewModel", "토큰이 이미 등록되어 있으므로 API 호출을 스킵합니다.")
                return@launch
            }

             */

            // 2. 등록되지 않았다면, Firebase로부터 FCM 토큰을 가져옵니다.
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("FCM", "FCM 토큰 가져오기 실패", task.exception)
                    return@addOnCompleteListener
                }
                val token = task.result
                Log.i("FCM", "($role) 역할로 FCM 토큰 등록 시도: $token")

                // 3. 가져온 토큰으로 Repository 함수를 호출합니다.
                viewModelScope.launch {
                    try {
                        // TODO: 실제 userId를 가져오는 로직으로 교체해야 합니다.
                        val userId = "senior_user_1"
                        tokenRepository.registerToken(userId, token, role)

                        /*
                        // 4. 등록 성공 시, DataStore에 등록 완료 플래그를 저장합니다.
                        dataStore.edit { preferences ->
                            preferences[IS_TOKEN_REGISTERED_KEY] = true
                        }
                        Log.i("TokenViewModel", "($role) 역할로 토큰 등록 및 플래그 저장 성공.")
                         */
                    } catch (e: Exception) {
                        Log.e("TokenViewModel", "($role) 역할로 토큰 등록 중 오류 발생", e)
                    }
                }
            }
        }
    }
}
