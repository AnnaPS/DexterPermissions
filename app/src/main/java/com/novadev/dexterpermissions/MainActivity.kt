package com.novadev.dexterpermissions

import android.Manifest
import android.app.Activity
import android.os.Bundle

import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.CompositePermissionListener
import com.karumi.dexter.listener.single.DialogOnDeniedPermissionListener
import com.karumi.dexter.listener.single.PermissionListener
import com.karumi.dexter.listener.single.SnackbarOnDeniedPermissionListener
import com.novadev.dexterpermissions.enums.PermissionStatusEnum
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setButtonClicks()


    }

    private fun setButtonClicks() {
        // buttonCamera.setOnClickListener { checkCameraPermission() }
        //buttonCamera.setOnClickListener { setCameraPermissionHandlerWithDialog() }
        buttonCamera.setOnClickListener { setCameraPermissionWithSnackBar() }
        buttonContacts.setOnClickListener { checkContactsPermission() }
        buttonAudio.setOnClickListener { checkAudioPermission() }
        buttonAll.setOnClickListener { checkAllPermissions() }
    }

    private fun setPermissionHandler(permission: String, textView: TextView) {

        Dexter.withActivity(this)
            .withPermission(permission)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    setPermissionStatus(textView, PermissionStatusEnum.GRANTED)
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) {
                    if (response.isPermanentlyDenied) {
                        setPermissionStatus(textView, PermissionStatusEnum.PERMANENTLY_DENIED)
                    } else {
                        setPermissionStatus(textView, PermissionStatusEnum.DENIED)
                    }

                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken
                ) {
                    // Si se deniegan los permisos y se clickea el check de no volver a mostrar no se volverá a mostrar.
                    token.continuePermissionRequest()
                }


            }).check()
    }

    private fun setPermissionStatus(textView: TextView, status: PermissionStatusEnum) {
        when (status) {
            PermissionStatusEnum.GRANTED -> {
                textView.text = getString(R.string.permission_status_granted)
                textView.setTextColor(ContextCompat.getColor(this, R.color.colorPermissionStatusGranted))
            }
            PermissionStatusEnum.DENIED -> {
                textView.text = getString(R.string.permission_status_denied)
                textView.setTextColor(ContextCompat.getColor(this, R.color.colorPermissionStatusDenied))
            }
            PermissionStatusEnum.PERMANENTLY_DENIED -> {
                textView.text = getString(R.string.permission_status_denied_permanently)
                textView.setTextColor(ContextCompat.getColor(this, R.color.colorPermissionStatusPermanentlyDenied))
            }
        }
    }

    private fun checkAudioPermission() {
        setPermissionHandler(Manifest.permission.RECORD_AUDIO, textViewAudio)
    }

    private fun checkCameraPermission() {
        setPermissionHandler(Manifest.permission.CAMERA, textViewCamera)
    }

    private fun checkContactsPermission() {
        setPermissionHandler(Manifest.permission.READ_CONTACTS, textViewContacts)
    }

    private fun checkAllPermissions() {
        Toast.makeText(this, "Entra en check all", Toast.LENGTH_LONG).show()
        Dexter.withActivity(this)
            .withPermissions(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.RECORD_AUDIO
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    //se comprueba que los permisos vienen con valores. Si es nulo no pasa por el let.
                    report?.let {
                        checkAllPermissionGranted(Manifest.permission.CAMERA, textViewCamera, report)
                        checkAllPermissionGranted(Manifest.permission.RECORD_AUDIO, textViewAudio, report)
                        checkAllPermissionGranted(Manifest.permission.READ_CONTACTS, textViewContacts, report)

                        checkAllPermissionStatusNotGranted(Manifest.permission.CAMERA, textViewCamera, report)
                        checkAllPermissionStatusNotGranted(Manifest.permission.RECORD_AUDIO, textViewAudio, report)
                        checkAllPermissionStatusNotGranted(Manifest.permission.READ_CONTACTS, textViewContacts, report)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }

            }).check()

    }

    private fun checkAllPermissionStatusNotGranted(
        permissionManifest: String,
        textview: TextView,
        report: MultiplePermissionsReport?
    ) {
        for (permission in report!!.deniedPermissionResponses) {
            when (permission.permissionName) {
                permissionManifest -> {
                    if (permission.isPermanentlyDenied) {
                        setPermissionStatus(textview, PermissionStatusEnum.PERMANENTLY_DENIED)
                    } else {
                        setPermissionStatus(textview, PermissionStatusEnum.DENIED)
                    }
                }
            }
        }
    }

    private fun checkAllPermissionGranted(
        permissionManifest: String,
        textview: TextView,
        report: MultiplePermissionsReport?
    ) {
        for (permission in report!!.grantedPermissionResponses) {
            when (permission.permissionName) {
                permissionManifest -> setPermissionStatus(
                    textview,
                    PermissionStatusEnum.GRANTED
                )
            }
        }
    }

    /**
     * Metodo que genera un alert dialog cuando denegamos el permiso
     */
    private fun setCameraPermissionHandlerWithDialog() {
        val dialogPermissionListener = DialogOnDeniedPermissionListener.Builder
            .withContext(this)
            .withTitle("Camera Permission")
            .withMessage("El permiso de la camara es necesario para tomar fotos")
            .withButtonText(android.R.string.ok)
            .withIcon(R.mipmap.ic_launcher)
            .build()

        val permissionListener = object : PermissionListener {
            override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                setPermissionStatus(textViewCamera, PermissionStatusEnum.GRANTED)
            }

            override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                if (response!!.isPermanentlyDenied) {
                    setPermissionStatus(textViewCamera, PermissionStatusEnum.PERMANENTLY_DENIED)
                } else {
                    setPermissionStatus(textViewCamera, PermissionStatusEnum.DENIED)
                }
            }

            override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken?) {
                token?.continuePermissionRequest()
            }
        }
        val composite = CompositePermissionListener(permissionListener, dialogPermissionListener)
        Dexter.withActivity(this)
            .withPermission(Manifest.permission.CAMERA)
            .withListener(composite)
            .check()
    }

    private fun setCameraPermissionWithSnackBar() {
        val snackBarPermissionListener = SnackbarOnDeniedPermissionListener.Builder
            .with(contenedor, "El permiso de la camara es necesario para tomar fotos")
            .withOpenSettingsButton("Opciones")
            .withCallback(object : Snackbar.Callback() {
                // Manejador de eventos para  cuando el snackbar está visible
                override fun onShown(sb: Snackbar?) {

                }

                // Manejador de eventos para cuando el snackbar fue denegado
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {

                }
            }).build()


        val permissionListener = object : PermissionListener {
            override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                setPermissionStatus(textViewCamera, PermissionStatusEnum.GRANTED)
            }

            override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                if (response!!.isPermanentlyDenied) {
                    setPermissionStatus(textViewCamera, PermissionStatusEnum.PERMANENTLY_DENIED)
                } else {
                    setPermissionStatus(textViewCamera, PermissionStatusEnum.DENIED)
                }
            }

            override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken?) {
                token?.continuePermissionRequest()
            }
        }
        val composite = CompositePermissionListener(permissionListener, snackBarPermissionListener)
        Dexter.withActivity(this)
            .withPermission(Manifest.permission.CAMERA)
            .withListener(composite)
            .check()
    }
}

