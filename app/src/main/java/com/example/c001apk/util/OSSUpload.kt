package com.example.c001apk.util

import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.alibaba.sdk.android.oss.ClientConfiguration
import com.alibaba.sdk.android.oss.ClientException
import com.alibaba.sdk.android.oss.OSSClient
import com.alibaba.sdk.android.oss.ServiceException
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback
import com.alibaba.sdk.android.oss.common.OSSLog
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider
import com.alibaba.sdk.android.oss.model.ObjectMetadata
import com.alibaba.sdk.android.oss.model.PutObjectRequest
import com.alibaba.sdk.android.oss.model.PutObjectResult
import com.example.c001apk.logic.model.OSSUploadPrepareResponse
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun ossUpload(
    context: Context,
    responseData: OSSUploadPrepareResponse.Data,
    uriList: List<Uri>,
    typeList: List<String>,
    md5List: List<ByteArray?>,
    iOnSuccess: (Int) -> Unit,
    iOnFailure: () -> Unit,
    closeDialog: () -> Unit
) {
    runCatching {
        val accessKeyId = responseData.uploadPrepareInfo.accessKeyId
        val accessKeySecret = responseData.uploadPrepareInfo.accessKeySecret
        val securityToken = responseData.uploadPrepareInfo.securityToken

        val endPoint = responseData.uploadPrepareInfo.endPoint.replace("https://", "")
        val bucket = responseData.uploadPrepareInfo.bucket
        val callbackUrl = responseData.uploadPrepareInfo.callbackUrl

        val conf = ClientConfiguration()
        conf.connectionTimeout = 5 * 60 * 1000
        conf.socketTimeout = 5 * 60 * 1000
        conf.maxConcurrentRequest = 5
        conf.maxErrorRetry = 2
        OSSLog.enableLog()
        val credentialProvider = OSSStsTokenCredentialProvider(
            accessKeyId,
            accessKeySecret,
            securityToken
        )
        val oss = OSSClient(context, endPoint, credentialProvider, conf)

        uriList.forEachIndexed { index, uri ->
            val put = PutObjectRequest(
                bucket,
                responseData.fileInfo[index].uploadFileName,
                uri
            )
            val metadata = ObjectMetadata()
            metadata.contentType = typeList[index]
            metadata.contentMD5 = Base64.encodeToString(md5List[index], Base64.DEFAULT).trim()
            metadata.setHeader(
                "x-oss-callback",
                "eyJjYWxsYmFja0JvZHlUeXBlIjoiYXBwbGljYXRpb25cL2pzb24iLCJjYWxsYmFja0hvc3QiOiJhcGkuY29vbGFway5jb20iLCJjYWxsYmFja1VybCI6Imh0dHBzOlwvXC9hcGkuY29vbGFway5jb21cL3Y2XC9jYWxsYmFja1wvbW9iaWxlT3NzVXBsb2FkU3VjY2Vzc0NhbGxiYWNrP2NoZWNrQXJ0aWNsZUNvdmVyUmVzb2x1dGlvbj0wJnZlcnNpb25Db2RlPTIxMDIwMzEiLCJjYWxsYmFja0JvZHkiOiJ7XCJidWNrZXRcIjoke2J1Y2tldH0sXCJvYmplY3RcIjoke29iamVjdH0sXCJoYXNQcm9jZXNzXCI6JHt4OnZhcjF9fSJ9"
            )
            metadata.setHeader("x-oss-callback-var", "eyJ4OnZhcjEiOiJmYWxzZSJ9")
            put.metadata = metadata
            put.callbackParam = object : HashMap<String, String>() {
                init {
                    put("callbackUrl", callbackUrl)
                    put("callbackHost", Uri.parse(callbackUrl).host ?: "developer.coolapk.com")
                    put("callbackBodyType", "application/json")
                    put("callbackBody", "filename=${responseData.fileInfo[index].name}")
                }
            }
            oss.asyncPutObject(put, OSSCallBack(
                iOnSuccess = {
                    iOnSuccess(index)
                },
                iOnFailure = {
                    iOnFailure()
                }
            ))
        }
    }.onFailure {
        closeDialog()
        withContext(Dispatchers.Main) {
            MaterialAlertDialogBuilder(context)
                .setTitle("图片上传失败")
                .setMessage(it.message)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton("Log") { _, _ ->
                    MaterialAlertDialogBuilder(context)
                        .setTitle("Log")
                        .setMessage(it.stackTraceToString())
                        .setPositiveButton(android.R.string.ok, null)
                        .show()
                }
                .show()
        }
    }
}

class OSSCallBack(
    private val iOnSuccess: () -> Unit,
    private val iOnFailure: () -> Unit,
) : OSSCompletedCallback<PutObjectRequest?, PutObjectResult?> {
    override fun onSuccess(
        request: PutObjectRequest?,
        result: PutObjectResult?
    ) {
        iOnSuccess()
    }

    override fun onFailure(
        request: PutObjectRequest?,
        clientException: ClientException?,
        serviceException: ServiceException?
    ) {
        iOnFailure()
        // Request exception
        if (clientException != null) {
            // Local exception, such as a network exception
            Log.i("OSSUpload", "uploadFailed: clientException: ${clientException.message}")
            clientException.printStackTrace()
        }
        if (serviceException != null) {
            // Service exception
            Log.i("OSSUpload", "OSSUpload: serviceException: ${serviceException.message}")
            Log.i("OSSUpload", "ErrorCode=" + serviceException.errorCode)
            Log.i("OSSUpload", "RequestId=" + serviceException.requestId)
            Log.i("OSSUpload", "HostId=" + serviceException.hostId)
            Log.i("OSSUpload", "RawMessage=" + serviceException.rawMessage)
        }
    }
}