package com.spcreations.qrcodescanner

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.util.Log
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.spcreations.qrcodescanner.databinding.ActivityMainBinding
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


typealias LumaListener = (luma: Double) -> Unit


class MainActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityMainBinding

    private lateinit var resultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // Set up the listeners for taking photos and scanning from gallary
        viewBinding.imgBtnCamera.setOnClickListener {
            launchCamera() }
        viewBinding.imgBtnGallery.setOnClickListener {

            scanFromGallery() }



        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    val photoUri: Uri? = data?.data

                    val barcodeScanner: BarcodeScanner = BarcodeScanning.getClient()

                    processImage(barcodeScanner, photoUri)


                }


            }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun processImage(barcodeScanner: BarcodeScanner, photoUri: Uri?) {


            val inputImage: InputImage

             inputImage = photoUri?.let { InputImage.fromFilePath(this, it) }!!

            barcodeScanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    barcodes.forEach {
                        it.rawValue?.let { it1 ->
                            Log.d("TAG", it1)
                            viewBinding.tvScanvalueGallery.text = it1
                            Linkify.addLinks(viewBinding.tvScanvalueGallery, Linkify.WEB_URLS);
                        }
                    }
                }
                .addOnFailureListener {
                    it.message?.let { it1 -> Log.e("TAG", it1) }
                }


    }

    private fun launchCamera() {
        val i = Intent(this,CameraActivity::class.java)
        startActivity(i)
    }

    /*
      Function to launch Gallery intent to scan the images
     */
    private fun scanFromGallery() {

        viewBinding.tvScanvalueGallery.text=null

        Log.d("TAG", "insidelaunch gallery")
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"

        resultLauncher.launch(intent)


    }


    override fun onDestroy() {
        super.onDestroy()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {


        var messageBody = "Hello, check out this QR Code Scanner App, it is simple and cool!\n\n"
       messageBody+="https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID +"\n\n";
        val messageSubject = "QR Code Scanner App"

        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(Intent.EXTRA_TEXT, messageBody)
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT,messageSubject)

        startActivity(Intent.createChooser(sharingIntent, "Share Using"))

        return super.onOptionsItemSelected(item)


    }
}

