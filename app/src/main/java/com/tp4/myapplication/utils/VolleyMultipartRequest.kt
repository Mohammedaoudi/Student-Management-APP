package com.example.projetws.util

import com.android.volley.AuthFailureError
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.HttpHeaderParser
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.IOException
import java.io.UnsupportedEncodingException

open class VolleyMultipartRequest(
    method: Int,
    url: String,
    private val listener: Response.Listener<NetworkResponse>,
    errorListener: Response.ErrorListener
) : Request<NetworkResponse>(method, url, errorListener) {

    private val headers: MutableMap<String, String> = HashMap()
    private val boundary = "apiclient-${System.currentTimeMillis()}"
    private val lineEnd = "\r\n"
    private val twoHyphens = "--"

    override fun getHeaders(): Map<String, String> {
        return headers
    }

    override fun parseNetworkResponse(response: NetworkResponse): Response<NetworkResponse> {
        return Response.success(response, HttpHeaderParser.parseCacheHeaders(response))
    }

    override fun deliverResponse(response: NetworkResponse) {
        listener.onResponse(response)
    }

    @Throws(AuthFailureError::class)
    protected override fun getParams(): Map<String, String>? {
        return null
    }

    @Throws(AuthFailureError::class)
    protected open fun getByteData(): Map<String, DataPart>? {
        return null
    }

    override fun getBodyContentType(): String {
        return "multipart/form-data; boundary=$boundary"
    }

    @Throws(AuthFailureError::class)
    override fun getBody(): ByteArray? {
        val bos = ByteArrayOutputStream()
        val dos = DataOutputStream(bos)

        try {
            val params = getParams()
            if (params != null && params.isNotEmpty()) {
                textParse(dos, params, getParamsEncoding())
            }

            val data = getByteData()
            if (data != null && data.isNotEmpty()) {
                dataParse(dos, data)
            }

            dos.writeBytes("$twoHyphens$boundary$twoHyphens$lineEnd")

            return bos.toByteArray()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    @Throws(IOException::class)
    private fun textParse(dataOutputStream: DataOutputStream, params: Map<String, String>, encoding: String) {
        for (entry in params.entries) {
            buildTextPart(dataOutputStream, entry.key, entry.value)
        }
    }

    @Throws(IOException::class)
    private fun dataParse(dataOutputStream: DataOutputStream, data: Map<String, DataPart>) {
        for (entry in data.entries) {
            buildDataPart(dataOutputStream, entry.value, entry.key)
        }
    }

    @Throws(IOException::class)
    private fun buildTextPart(dataOutputStream: DataOutputStream, parameterName: String, parameterValue: String) {
        dataOutputStream.writeBytes("$twoHyphens$boundary$lineEnd")
        dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"$parameterName\"$lineEnd")
        dataOutputStream.writeBytes(lineEnd)
        dataOutputStream.writeBytes("$parameterValue$lineEnd")
    }

    @Throws(IOException::class)
    private fun buildDataPart(dataOutputStream: DataOutputStream, dataFile: DataPart, inputName: String) {
        dataOutputStream.writeBytes("$twoHyphens$boundary$lineEnd")
        dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"$inputName\"; filename=\"${dataFile.fileName}\"$lineEnd")
        if (!dataFile.type.isNullOrEmpty()) {
            dataOutputStream.writeBytes("Content-Type: ${dataFile.type}$lineEnd")
        }
        dataOutputStream.writeBytes(lineEnd)

        ByteArrayInputStream(dataFile.content).use { fileInputStream ->
            val buffer = ByteArray(1024)
            var bytesRead: Int
            while (fileInputStream.read(buffer).also { bytesRead = it } != -1) {
                dataOutputStream.write(buffer, 0, bytesRead)
            }
        }

        dataOutputStream.writeBytes(lineEnd)
    }

    class DataPart(val fileName: String, val content: ByteArray, val type: String? = null)
}
