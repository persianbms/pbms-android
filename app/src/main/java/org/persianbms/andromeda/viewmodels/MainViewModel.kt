package org.persianbms.andromeda.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.persianbms.andromeda.LiveStreamService

class MainViewModel : ViewModel(), LiveStreamService.OnLiveStreamStateChangedListener {

    private var liveLiveStreamPlaying : MutableLiveData<Boolean>? = null

    fun getLiveStreamPlaying(): LiveData<Boolean> {
        liveLiveStreamPlaying?.let { return it }

        val llsp = MutableLiveData<Boolean>()
        liveLiveStreamPlaying = llsp
        LiveStreamService.listener = this
        return llsp
    }

    override fun onLiveStreamStreamStateChanged(playing: Boolean) {
        liveLiveStreamPlaying?.postValue(playing)
    }

    override fun onCleared() {
        super.onCleared()

        LiveStreamService.listener = null
    }

}