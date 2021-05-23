package jp.aoyama.mki.thermometer.infrastructure.temperature

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import jp.aoyama.mki.thermometer.domain.models.TemperatureData
import jp.aoyama.mki.thermometer.domain.repository.TemperatureRepository

class LocalFileTemperatureRepository(
    private val mContext: Context
) : TemperatureRepository {

    private val mGson = Gson()
    private val mFileInputStream get() = mContext.openFileInput(FILE_NAME)
    private val mFileOutputStream get() = mContext.openFileOutput(FILE_NAME, Context.MODE_PRIVATE)

    override suspend fun findAll(): List<TemperatureData> {
        return kotlin.runCatching {
            val temperaturesJson = mFileInputStream.bufferedReader().readLine() ?: "[]"
            mGson.fromJson(temperaturesJson, Array<TemperatureData>::class.java).toList()
        }.fold(
            onSuccess = { it },
            onFailure = { e ->
                Log.e(TAG, "findAll: error while get users", e)
                emptyList()
            }
        )
    }

    override suspend fun add(data: TemperatureData) {
        val savedTemperatures = findAll().toMutableList()
        savedTemperatures.add(data)

        val json = mGson.toJson(savedTemperatures)
        mFileOutputStream.use {
            it.write(json.toByteArray())
        }
    }

    companion object {
        private const val TAG = "LocalFileTemperatureRep"
        private const val FILE_NAME = "Body_Temperature.txt"
    }
}