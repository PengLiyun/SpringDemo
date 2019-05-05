package com.ericpeng.springboot.helloword.SpringDemo.controller

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.io.File
import java.io.FileInputStream

@RestController
@RequestMapping("/files")
class MacFileSystemController {

    companion object {
        const val ROOT_PATH = "xxx"
    }

    /**
     * 获取文件列表
     * path: 指定路径，为空时返回更目录列表
     */
    @RequestMapping(value = "/list", method = [RequestMethod.GET], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun listFile(@RequestParam path: String): ResponseEntity<List<DeviceFile>> {
        println("path:$path")

        // default value:"/Users/liyun"
        var path = if (path?.isNotEmpty()) path else ROOT_PATH
        var file = File(path)
        var files = file.listFiles()

        var list = ArrayList<DeviceFile>()
        files.filter {
            !it.isHidden
        }.forEach {
            list.add(DeviceFile(it.name, it.path, it.isDirectory, it.length()))
        }

        return ResponseEntity(list, HttpStatus.OK)
    }

    /**
     * 获取图片内容
     */
    @RequestMapping(value = "/image", method = [RequestMethod.GET], produces = [MediaType.IMAGE_GIF_VALUE, MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE])
    fun imageContent(@RequestParam path: String): ResponseEntity<ByteArray> {
        println("path:$path")

        path?.takeIf {
            it.isNotEmpty()
        }.let {
            var path = path
            var file = File(path)
            if (file.exists()) {
                var byteArray = ByteArray(file.length().toInt())
                var inputStream = FileInputStream(file)
                inputStream.read(byteArray).takeIf {
                    it > 0
                }.run {
                    return ResponseEntity(byteArray, HttpStatus.OK)
                }
            }
        }

        return ResponseEntity(ByteArray(0), HttpStatus.NOT_FOUND)
    }

    /**
     * 获取信息，RequestMethod.HEAD
     */
    @RequestMapping(value = "/data", method = [RequestMethod.HEAD])
    fun info(@RequestParam path: String): ResponseEntity<String> {
        println("path:$path")

        path?.takeIf {
            it.isNotEmpty()
        }.let {
            var path = path
            var file = File(path)
            if (file.exists()) {
                file.length().toInt().takeIf {
                    it > 0
                }.run {
                    var headerMap = HttpHeaders()
                    headerMap["Accept-Ranges"] = "bytes"
                    headerMap["Content-Length"] = this.toString()
                    headerMap["Content-Type"] = MediaType.MULTIPART_FORM_DATA_VALUE
                    return ResponseEntity("OK", headerMap, HttpStatus.OK)
                }
            }
        }

        return ResponseEntity("", HttpStatus.NOT_FOUND)
    }

    /**
     * 获取文件内容，支持取部分内容
     */
    @RequestMapping(value = "/data", method = [RequestMethod.GET], produces = [MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_GIF_VALUE, MediaType.IMAGE_PNG_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE])
    fun content(@RequestParam path: String, @RequestHeader headers: HttpHeaders): ResponseEntity<ByteArray> {
        println("path:$path")
        println("HttpHeaders:$headers")

        var range = headers.range

        path?.takeIf {
            it.isNotEmpty()
        }.let {
            var path = path
            var file = File(path)
            if (file.exists()) {
                val fileLength = file.length().toInt()
                var inputStream = FileInputStream(file)

                var readStart = if (range.size > 0) range[0]?.getRangeStart(0)?.toInt() ?: 0 else 0
                var readEnd = if (range.size > 0) range[0]?.getRangeEnd(fileLength.toLong())?.toInt()
                        ?: 0 else 0
                var readLength = if (readEnd - readStart == 0) fileLength else readEnd - readStart

                var byteArray = ByteArray(readLength)
                inputStream.read(byteArray, readStart, readLength).takeIf {
                    it > 0
                }.run {
                    var headerMap = HttpHeaders()
                    headerMap["Content-Range"] = "bytes $readStart-${readStart + readLength}/$fileLength"
                    headerMap.contentLength = readLength.toLong()
                    return ResponseEntity(byteArray, headerMap, HttpStatus.PARTIAL_CONTENT)
                }
            }
        }

        return ResponseEntity(ByteArray(0), HttpStatus.NOT_FOUND)
    }

}