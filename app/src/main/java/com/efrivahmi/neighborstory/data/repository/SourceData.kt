package com.efrivahmi.neighborstory.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.paging.*
import com.efrivahmi.neighborstory.data.api.ConfigApi
import com.efrivahmi.neighborstory.data.model.NeighborModel
import com.efrivahmi.neighborstory.data.response.*
import com.efrivahmi.neighborstory.local.preferences.NeighborPreference
import com.efrivahmi.neighborstory.local.room.NeighborDatabase
import com.efrivahmi.neighborstory.paging.PagingPreference
import com.efrivahmi.neighborstory.utils.HelperToast
import com.efrivahmi.neighborstory.utils.Result
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@OptIn(ExperimentalPagingApi::class)
class SourceData private constructor(
    private val pref: NeighborPreference,
    private val apiService: ConfigApi,
    private val neighborDatabase: NeighborDatabase
) {

    companion object {
        private const val TAG = "SourceData"
        @Volatile private var instance: SourceData? = null
        fun getInstance(pref: NeighborPreference, apiService: ConfigApi, neighborDatabase: NeighborDatabase): SourceData =
            instance ?: synchronized(this) {
                instance ?: SourceData(pref, apiService, neighborDatabase).also { instance = it }
            }
    }

    private val _register = MutableLiveData<Result<Register?>>()
    val register: LiveData<Result<Register?>> = _register

    private val _login = MutableLiveData<Result<Login?>>()
    val login: LiveData<Result<Login?>> = _login

    private val _createNew = MutableLiveData<Result<AddStory?>>()
    val createNew: LiveData<Result<AddStory?>> = _createNew

    private val _list = MutableLiveData<Result<Story?>>()
    val list: LiveData<Result<Story?>> = _list

    private val _isLoading = MutableLiveData<Result<Boolean>>()
    val isLoading: MutableLiveData<Result<Boolean>> = _isLoading

    private val _toastText = MutableLiveData<HelperToast<String>>()
    val toastText: LiveData<HelperToast<String>> = _toastText

    fun writeStory(): LiveData<PagingData<ListStoryItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 5
            ),
            remoteMediator = PagingPreference(neighborDatabase, apiService, pref),
            pagingSourceFactory = {
                neighborDatabase.neighborDao().getAllStory()
            }
        ).liveData
    }

     fun writeStoryWithLocation(token: String) {
        _isLoading.value = (Result.Loading)
        apiService.getStoryWithMaps(token)
            .enqueue(object : Callback<Story> {
                override fun onResponse(call: Call<Story>, response: Response<Story>) {
                    _isLoading.value = (Result.Loading)
                    if (response.isSuccessful && response.body() != null) {
                        _list.value = Result.Success(response.body())
                        _toastText.value = HelperToast(response.body()?.message.toString())
                    } else {
                        _list.value = Result.Error(response.message().toString())
                        _toastText.value = HelperToast(response.message().toString())
                        Log.e(
                            TAG,
                            "on Failure!: ${response.message()}, ${response.body()?.message.toString()}"
                        )
                    }
                }

                override fun onFailure(call: Call<Story>, t: Throwable) {
                    _isLoading.value = (Result.Loading)
                    _list.value = Result.Error(t.message.toString())
                    _toastText.value = HelperToast(t.message.toString())
                    Log.e(TAG, "Error 404!: ${t.message.toString()}")
                }
            })
    }

    fun uploadRegister(name: String, email: String, password: String) {
        _isLoading.value = (Result.Loading)
        apiService.neighborRegister(name, email, password)
            .enqueue(object : Callback<Register> {
                override fun onResponse(call: Call<Register>, response: Response<Register>) {
                    _isLoading.value = (Result.Loading)
                    if (response.isSuccessful && response.body() != null) {
                        _register.value = Result.Success(response.body())
                        _toastText.value = HelperToast(response.body()?.message.toString())
                    } else {
                        _register.value = Result.Error(response.message().toString())
                        _toastText.value = HelperToast(response.message().toString())
                        Log.e(TAG, "on Failure!: ${response.message()}, ${response.body()?.message.toString()}")
                    }
                }
                override fun onFailure(call: Call<Register>, t: Throwable) {
                    _isLoading.value = (Result.Loading)
                    _register.value = Result.Error(t.message.toString())
                    _toastText.value = HelperToast(t.message.toString())
                    Log.e(TAG, "Failed Register: ${t.message.toString()}")
                }
            })
    }

    fun uploadLogin(email: String, password: String) {
        _isLoading.value = (Result.Loading)
        val client = apiService.neighborLogin(email, password)
        client.enqueue(object : Callback<Login> {
            override fun onResponse(call: Call<Login>, response: Response<Login>) {
                _isLoading.value = (Result.Loading)
                if (response.isSuccessful && response.body() != null) {
                    _login.value = Result.Success(response.body())
                    _toastText.value = HelperToast(response.body()?.message.toString())
                } else {
                    _login.value = Result.Error(response.message().toString())
                    _toastText.value = HelperToast(response.message().toString())
                    Log.e(TAG, "on Failure!: ${response.message()}, ${response.body()?.message.toString()}")
                }
            }

            override fun onFailure(call: Call<Login>, t: Throwable) {
                _isLoading.value = (Result.Loading)
                _login.value = Result.Error(t.message.toString())
                _toastText.value = HelperToast(t.message.toString())
                Log.e(TAG, "Failed Login: ${t.message.toString()}")
            }
        })
    }

    fun uploadNewStory(token: String, file: MultipartBody.Part, description: RequestBody, lat: RequestBody? = null, lon: RequestBody? = null) {
        _isLoading.value = (Result.Loading)
        apiService.uploadDataNeighbor(token, file, description, lat, lon)
            .enqueue(object : Callback<AddStory> {
                override fun onResponse(call: Call<AddStory>, response: Response<AddStory>) {
                    _isLoading.value = (Result.Loading)
                    if (response.isSuccessful && response.body() != null) {
                        _createNew.value = Result.Success(response.body())
                        _toastText.value = HelperToast(response.body()?.message.toString())
                    } else {
                        _createNew.value = Result.Error(response.message().toString())
                        _toastText.value = HelperToast(response.message().toString())
                        Log.e(
                            TAG,
                            "on Failure!: ${response.message()}, ${response.body()?.message.toString()}"
                        )
                    }
                }

                override fun onFailure(call: Call<AddStory>, t: Throwable) {
                    _isLoading.value = (Result.Loading)
                    _createNew.value = Result.Error(t.message.toString())
                    _toastText.value = HelperToast(t.message.toString())
                    Log.e(TAG, "Failed Upload Story!: ${t.message.toString()}")
                }
            })
    }

    fun getNeighbor(): LiveData<Result<NeighborModel>> {
        return pref.getNeighborSession().asLiveData().map {
            Result.Success(it)
        }
    }

    suspend fun saveUser(session: NeighborModel) {
        pref.saveNeighborSession(session)
    }

    suspend fun login() {
        pref.login()
    }

    suspend fun logout() {
        pref.logout()
    }
}