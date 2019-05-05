package com.ericpeng.springboot.helloword.SpringDemo.controller

import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import java.io.File
import java.io.FileInputStream


@RestController
@RequestMapping("/index")
class HelloController {

    @RequestMapping(value = "/download", method = [RequestMethod.GET], produces = [MediaType.IMAGE_PNG_VALUE])
    fun hello(): ResponseEntity<ByteArray> {
        var file = File("/Users/liyun/Downloads/壁纸/gdtlawamfhw-artur-rutkowski.jpg")
        var length = file.length().toInt()
        var byteArray = ByteArray(length)

        var inputStream = FileInputStream(file)
        inputStream.read(byteArray, 0, length)

        return ResponseEntity(byteArray, HttpStatus.OK)
    }

    @RequestMapping(value = "/list", method = [RequestMethod.GET], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun listFile(): ResponseEntity<List<String>> {
        var file = File("/Users/liyun")
        var files = file.listFiles()

        var list = ArrayList<String>()
        files.forEach {
            list.add(it.path)
        }
        return ResponseEntity(list, HttpStatus.OK)
    }

}