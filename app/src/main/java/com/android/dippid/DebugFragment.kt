package com.android.dippid

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import java.lang.NumberFormatException
import java.net.InetAddress
import java.util.*

class DebugFragment : Fragment(R.layout.fragment_debug), SensorEventListener {

    private var listener: DataListener? = null

    var ipAddress: String = ""
    private var port: Int = 0

    private var stateButton1 = 0
    private var stateButton2 = 0
    private var stateButton3 = 0
    private var stateButton4 = 0
    private var accX = 0.0F
    private var accY = 0.0F
    private var accZ = 0.0F
    private var gyroX = 0.0F
    private var gyroY = 0.0F
    private var gyroZ = 0.0F
    private var gravX = 0.0F
    private var gravY = 0.0F
    private var gravZ = 0.0F

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = when {
            context is DataListener -> context
            parentFragment is DataListener -> parentFragment as DataListener
            else -> error("You should implement MyFragmentListener")

        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

        // init sensor listening
        val sensorManager = activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL
        )
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
            SensorManager.SENSOR_DELAY_NORMAL
        )
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),
            SensorManager.SENSOR_DELAY_NORMAL
        )

        // init UI
        val ipInput = view.findViewById<EditText>(R.id.input_ip)
        val portInput = view.findViewById<EditText>(R.id.input_port)
        val button1 = view.findViewById<Button>(R.id.button_1)
        val button2 = view.findViewById<Button>(R.id.button_2)
        val button3 = view.findViewById<Button>(R.id.button_3)
        val button4 = view.findViewById<Button>(R.id.button_4)
        val sendingSwitch = view.findViewById<SwitchCompat>(R.id.switch_send)
        sendingSwitch.isChecked = (activity as MainActivity).isSendingActive()

        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
        val savedIP = sharedPref?.getString("IP_address", ipAddress)
        val savedPort = sharedPref?.getInt("PORT", port)

        if (!savedIP.equals(ipAddress) && (savedIP != null) && (savedPort != port) && (savedPort != null)) {
            ipAddress = savedIP
            port = savedPort
            ipInput?.setText(savedIP)
            portInput?.setText(savedPort.toString())
        }


        // init UI input listeners
        ipInput.doOnTextChanged { text, _, _, _ ->
            ipAddress = text.toString()

            if (sharedPref != null) {
                with(sharedPref.edit()) {
                    putString("IP_address", ipAddress)
                    apply()
                }
            }

        }
        portInput.doOnTextChanged { text, _, _, _ ->
            if (text != null) {
                try {
                    port = Integer.parseInt(text.toString())
                } catch (ex: NumberFormatException) {
                    port = 5700
                    Log.e("PORT", "Could not format Port input to Integer. Using default" +
                            " port instead (5700)")
                }
            }

            if (sharedPref != null) {
                with(sharedPref.edit()) {
                    putInt("PORT", port)
                    apply()
                }
            }
        }

        button1.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                stateButton1 = 1
                button1.setBackgroundColor(resources.getColor(R.color.purple_500))
            }
            if (event.action == MotionEvent.ACTION_UP) {
                stateButton1 = 0
                button1.setBackgroundColor(resources.getColor(R.color.teal_700))
            }

            true
        }

        button2.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                stateButton2 = 1
                button2.setBackgroundColor(resources.getColor(R.color.purple_500))
            }
            if (event.action == MotionEvent.ACTION_UP) {
                stateButton2 = 0
                button2.setBackgroundColor(resources.getColor(R.color.teal_700))
            }

            true
        }

        button3.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                stateButton3 = 1
                button3.setBackgroundColor(resources.getColor(R.color.purple_500))
            }
            if (event.action == MotionEvent.ACTION_UP) {
                stateButton3 = 0
                button3.setBackgroundColor(resources.getColor(R.color.teal_700))
            }

            true
        }

        button4.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                stateButton4 = 1
                button4.setBackgroundColor(resources.getColor(R.color.purple_500))
            }
            if (event.action == MotionEvent.ACTION_UP) {
                stateButton4 = 0
                button4.setBackgroundColor(resources.getColor(R.color.teal_700))
            }

            true
        }

        sendingSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                activateSending()
            } else {
                deactivateSending()
            }
        }

        // send data to activity every 10ms
        val task = object : TimerTask() {
            override fun run() {
                val msg1 = "{\"button_1\":$stateButton1}"
                listener?.onDataToSend(msg1)

                val msg2 = "{\"button_2\":$stateButton2}"
                listener?.onDataToSend(msg2)

                val msg3 = "{\"button_3\":$stateButton3}"
                listener?.onDataToSend(msg3)

                val msg4 = "{\"button_4\":$stateButton4}"
                listener?.onDataToSend(msg4)

                val msgAcc =
                    "{\"accelerometer\":{\"x\":$accX,\"y\":$accY,\"z\":$accZ}}"
                listener?.onDataToSend(msgAcc)

                val msgGyro =
                    "{\"gyroscope\":{\"x\":$gyroX,\"y\":$gyroY,\"z\":$gyroZ}}"
                listener?.onDataToSend(msgGyro)

                val msgGrav =
                    "{\"gravity\":{\"x\":$gravX,\"y\":$gravY,\"z\":$gravZ}}"
                listener?.onDataToSend(msgGrav)

            }
        }
        val timer = Timer()
        timer.scheduleAtFixedRate(task, 0L, 10)
    }

    override fun onStart() {
        super.onStart()

        if (view?.findViewById<SwitchCompat>(R.id.switch_send)?.isChecked == true) {
            activateSending()
        }

    }

    override fun onSensorChanged(event: SensorEvent) {
        val accValueView = view?.findViewById<TextView>(R.id.value_accelerometer)
        val gyroValueView = view?.findViewById<TextView>(R.id.value_gyroscope)
        val gravValueView = view?.findViewById<TextView>(R.id.value_gravity)

        // accelerometer: Acceleration force along the x/y/z axis (including gravity). (m/s*s)
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            accX = event.values[0] / 9.81F
            accY = event.values[1] / 9.81F
            accZ = event.values[2] / 9.81F
            accValueView?.text = getString(
                R.string.xyz_value,
                accX.toString(), accY.toString(), accZ.toString()
            )
        }

        // gyroscope: Rate of rotation around the x/y/z axis. (rad/s)
        if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
            gyroX = event.values[0]
            gyroY = event.values[1]
            gyroZ = event.values[2]
            gyroValueView?.text = getString(
                R.string.xyz_value,
                gyroX.toString(), gyroY.toString(), gyroZ.toString()
            )
        }

        // gravity: Force of gravity along the x/y/z axis. (m/s*s)
        if (event.sensor.type == Sensor.TYPE_GRAVITY) {
            gravX = event.values[0]
            gravY = event.values[1]
            gravZ = event.values[2]
            gravValueView?.text = getString(
                R.string.xyz_value,
                gravX.toString(), gravY.toString(), gravZ.toString()
            )
        }

    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    private fun activateSending() {
        if (ipAddress.matches(("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}" + "(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\$").toRegex()) &&
            port.toString()
                .matches(("^([0-9]{1,4}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])\$").toRegex())
        ) {
            listener?.onSendingActivated(port, InetAddress.getByName(ipAddress))
        } else {
            view?.findViewById<SwitchCompat>(R.id.switch_send)?.isChecked = false
            Toast.makeText(activity, "Invalid IP or port input", Toast.LENGTH_LONG).show()
        }
    }

    private fun deactivateSending() {
        listener?.onSendingDeactivated()
    }

}