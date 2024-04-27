package com.example.c001apk.logic.model

data class OSSUploadPrepareResponse(
    val status: Int?,
    val error: Int?,
    val message: String?,
    val messageStatus: String?,
    val data: Data?
) {
    data class Data(
        val fileInfo: List<FileInfo>,
        val uploadPrepareInfo: UploadPrepareInfo
    )

    data class FileInfo(
        val name: String,
        val resolution: String,
        val md5: String,
        val url: String,
        val uploadFileName: String
    )

    data class UploadPrepareInfo(
        val accessKeySecret: String,
        val accessKeyId: String,
        val securityToken: String,
        val expiration: String,
        val uploadImagePrefix: String,
        val endPoint: String,
        val bucket: String,
        val callbackUrl: String,
    )
}
