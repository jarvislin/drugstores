package com.jarvislin.domain.entity

import java.io.File

sealed class Progress(val bytesDownloaded: Long, val contentLength: Long, val file: File) {
    class Downloading(bytesDownloaded: Long, contentLength: Long, file: File) :
        Progress(bytesDownloaded, contentLength, file)

    class Done(contentLength: Long, file: File) : Progress(contentLength, contentLength, file)
}