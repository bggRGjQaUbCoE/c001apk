package com.example.c001apk.util


import org.jsoup.nodes.Document

object LoginUtils {
    fun Document.createRequestHash() = this.getElementsByTag("Body").attr("data-request-hash")

    fun createRandomNumber() = Math.random().toString().replace(".", "undefined")
}