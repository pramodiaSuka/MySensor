package com.maverick.mysensor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.maverick.mysensor.databinding.ActivityMainBinding
import kotlin.math.max
import kotlin.math.min

class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var binding:ActivityMainBinding
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var stepCount = 0

    private var gyroscope: Sensor? = null
    private var posX = 0f
    private var posY = 0f

    private var lightSensor: Sensor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        if (accelerometer == null){
            Toast.makeText(this, "No Accelerometer", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSensorChanged(p0: SensorEvent?) {
        p0?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER){
                val x = it.values[0]
                val y = it.values[1]
                val z = it.values[2]

                binding.txtXYZ.text = "$x\n$y\n$z"
                val magnitude = Math.sqrt((x*x+y*y+z*z).toDouble()).toFloat()
                val threshold = 12.0f
                if (magnitude > threshold){
                    stepCount++
                    binding.txtStep.text = "Steps: $stepCount"
                }
            }

            if(it.sensor.type == Sensor.TYPE_GYROSCOPE){
                // assuming 50 updates per second (20ms per update)
                val deltaTime = 0.02f //0.02 seconds

                // Calculate displacement based on angular speed
                val angularSpeedX = it.values[1]
                val angularSpeedY = it.values[0]

                posX += angularSpeedX * deltaTime * 500 //scale the movement
                posY += angularSpeedY * deltaTime * 500 // scale the movement

                //Get the screen size
                val screenWidth = resources.displayMetrics.widthPixels
                val screenHeight = resources.displayMetrics.heightPixels - 100

                // Ensure the object stays within the screen bounds
                posX = min(max(posX, 0f), screenWidth - binding.imgBall.width.toFloat())
                posY = min(max(posY, 0f), screenHeight - binding.imgBall.height.toFloat())

                //Update the position of the TextView
                binding.imgBall.translationX = posX
                binding.imgBall.translationY = posY
            }

            if (it.sensor.type == Sensor.TYPE_LIGHT){
                val lightLevel = it.values[0]
                binding.txtLight.text = "Light Level: $lightLevel lux"

                if (lightLevel < 50){
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
            }
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    override fun onResume() {
        super.onResume()
        accelerometer?.also { accel ->
            sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL)
        }

        gyroscope?.also { gyro->
            sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_GAME)
        }

        lightSensor?.also { light ->
            sensorManager.registerListener(this, light, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }
}