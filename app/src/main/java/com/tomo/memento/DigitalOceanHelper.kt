package com.tomo.memento

import android.content.Context
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.mobileconnectors.s3.transferutility.*
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import java.io.File

object DigitalOceanHelper {
    private const val ACCESS_KEY = "DO80169FK9XDKF8Q7U67"
    private const val SECRET_KEY = "key-1748432612098"
    private const val ENDPOINT = "https://memento.fra1.digitaloceanspaces.com"
    const val BUCKET_NAME = "memento"

    fun getTransferUtility(context: Context): TransferUtility {
        val credentials = BasicAWSCredentials(ACCESS_KEY, SECRET_KEY)
        val s3Client = AmazonS3Client(credentials, Regions.US_EAST_1)

        s3Client.setEndpoint(ENDPOINT)

        return TransferUtility.builder()
            .context(context)
            .s3Client(s3Client)
            .build()
    }

    fun uploadFile(context: Context, file: File, key: String, onComplete: () -> Unit, onError: (Exception) -> Unit) {
        val transferUtility = getTransferUtility(context)
        val uploadObserver = transferUtility.upload(BUCKET_NAME, key, file)

        uploadObserver.setTransferListener(object : TransferListener {
            override fun onStateChanged(id: Int, state: TransferState?) {
                if (state == TransferState.COMPLETED) {
                    onComplete()
                } else if (state == TransferState.FAILED) {
                    onError(Exception("Upload failed"))
                }
            }

            override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {}
            override fun onError(id: Int, ex: Exception?) {
                onError(ex ?: Exception("Unknown error"))
            }
        })
    }
}
