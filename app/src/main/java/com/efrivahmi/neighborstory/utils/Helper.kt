package com.efrivahmi.neighborstory.utils

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.media.ExifInterface.*
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

private const val FILENAME_FORMAT = "dd-MMM-yyyy"

val timeStamp: String = SimpleDateFormat(
    FILENAME_FORMAT,
    Locale.US
).format(System.currentTimeMillis())

fun createNewFile(context: Context): File {
    val storageDirectory: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile(
        SimpleDateFormat(timeStamp, Locale.US).format(System.currentTimeMillis()),
        ".jpg",
        storageDirectory
    )
}

fun rotateBitmapFromExif(context: Context, photoUri: Uri): Bitmap {
    val photoBitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, photoUri)
    val exif = ExifInterface(photoUri.path!!)

    return when (exif.getAttributeInt(TAG_ORIENTATION, ORIENTATION_UNDEFINED)) {
        ORIENTATION_ROTATE_90 -> rotateBitmap(photoBitmap, 90f)
        ORIENTATION_ROTATE_180 -> rotateBitmap(photoBitmap, 180f)
        ORIENTATION_ROTATE_270 -> rotateBitmap(photoBitmap, 270f)
        ORIENTATION_FLIP_HORIZONTAL -> flipBitmap(photoBitmap, true)
        ORIENTATION_FLIP_VERTICAL -> flipBitmap(photoBitmap, false)
        else -> photoBitmap
    }
}

private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(degrees)
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

private fun flipBitmap(bitmap: Bitmap, horizontal: Boolean): Bitmap {
    val matrix = Matrix()
    if (horizontal) {
        matrix.setScale(-1f, 1f)
    } else {
        matrix.setScale(1f, -1f)
    }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

fun uriToNewFile(selectedImg: Uri?, context: Context): File? {
    selectedImg ?: return null

    val contentResolver: ContentResolver = context.contentResolver
    val newFile = File.createTempFile(
        SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()),
        ".jpg",
        context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    )

    val inputStream = contentResolver.openInputStream(selectedImg) ?: return null
    val outputStream: OutputStream = FileOutputStream(newFile)
    val buf = ByteArray(1024)
    var len: Int
    while (inputStream.read(buf).also { len = it } > 0) outputStream.write(buf, 0, len)
    outputStream.close()
    inputStream.close()

    return newFile
}

fun reduceImage(file: File): File {
    val bitmap = BitmapFactory.decodeFile(file.path)
    var compressQuality = 100
    var streamLength: Int
    val bitmapJpeg = Bitmap.CompressFormat.JPEG
    val outputStream = FileOutputStream(file)

    do {
        val bmpStream = ByteArrayOutputStream()
        bitmap.compress(bitmapJpeg, compressQuality, bmpStream)
        val bmpPicByteArray = bmpStream.toByteArray()
        streamLength = bmpPicByteArray.size
        compressQuality -= 5
    } while (streamLength > 1000000)
    bitmap.compress(bitmapJpeg, compressQuality, outputStream)

    return file
}