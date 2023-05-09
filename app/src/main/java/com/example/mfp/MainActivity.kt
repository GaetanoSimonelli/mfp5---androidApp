package com.example.mfp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.telephony.SmsManager
import android.text.TextUtils.concat
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mfp.api_mitch.*
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import org.w3c.dom.Text
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt


private const val LOCATION_PERMISSION_REQUEST_CODE = 2

class MainActivity : AppCompatActivity() {

    private var timer_testV: CountDownTimer ?= null
    var mostra_principale = true
    var elaborazione_local = false
    var allarme_elastic = true
    var nglog = 0//recuperato dal backup
    private fun addGlog(testo: String): Unit {
        if(mostra_principale == true) {
            val glog = findViewById<TextView>(R.id.textlog)
            glog.text = concat("[", nglog.toString(), "]", testo, "\n", glog.text)
            nglog++
        }
    }

    private fun addjfri(f: Int,r: Int,i: Int){
        try {
            if (jList.isEmpty() == true) {
                delete()//se la lista in locale è vuota allora significa che bisogna eliminare il database
                addGlog("deleted database")
            }
            postfri(f, r, i)
            jList.add("POST test_mov/_doc\n{\n  \"content\": {\n    \"ni\": ${jNumber.toString()},\n    \"f\": ${f.toString()},\n    \"r\": ${r.toString()},\n    \"i\": ${i.toString()}\n  }\n}")
            jNumber += 1
        }
        catch(ex: Exception){
            addVlog("eccezione addjfri: ${ex.toString()}")
        }
    }


    var nElog = 0//recuperato dal backup
    var jList = mutableListOf<String>()
    val base_url = "http://192.168.17.7:9200/move"//sostituire il tuo ip
    var jNumber = 0
    private fun addElog(testo: String){
        if(mostra_principale == false) {
            val log = findViewById<TextView>(R.id.textlog)
            if (nElog == 0) {
                log.text = ""
            } else {
                log.text = "($nElog) $testo\n${log.text}"
            }
            nElog++
            if(nElog == 1000)
                log.text ="Per prevenire errori nel programma il log è stato cancellato\n"
        }
    }

    public var minitlog = false
    public var testing_so = true
    public var old_x_gyro = 0;
    public var old_y_gyro = 0;
    public var old_z_gyro = 0;

    public var old_x_mag = 0
    public var old_y_mag = 0
    public var old_z_mag = 0

    public var old_x_axl = 0
    public var old_y_axl = 0
    public var old_z_axl = 0


    public var r_var = 0
    public var f_var = 0
    public var i_var = 0


    public var f_status = false
    public var r_status = false
    public var i_status = false


    private fun addTlog(replay : ByteArray): Unit {
        val tlog = findViewById<TextView>(R.id.tempLogBox)

        tlog.setOnClickListener(){
            addGlog(tlog.text as String)
        }

        var so = ""
        if(minitlog == false)
        {

            var temp_arr = byteArrayOf(replay[2], replay[3])
            var x_gyro = littleEndianConversion(temp_arr)

            temp_arr = byteArrayOf(replay[4], replay[5])
            var y_gyro = littleEndianConversion(temp_arr)

            temp_arr = byteArrayOf(replay[6], replay[7])
            var z_gyro = littleEndianConversion(temp_arr)

            so += "gyro X:${x_gyro-old_x_gyro} Y:${y_gyro-old_y_gyro} Z:${z_gyro-old_z_gyro}"

            //--
            temp_arr = byteArrayOf(replay[8], replay[9])
            var x_axl = littleEndianConversion(temp_arr)

            temp_arr = byteArrayOf(replay[10], replay[11])
            var y_axl = littleEndianConversion(temp_arr)

            temp_arr = byteArrayOf(replay[12], replay[13])
            var z_axl = littleEndianConversion(temp_arr)
            so += "\naxl X:${x_axl-old_x_axl} Y:${y_axl-old_y_axl} Z:${z_axl-old_z_axl}"
            //--
            temp_arr = byteArrayOf(replay[14], replay[15])
            var x_mag = littleEndianConversion(temp_arr)

            temp_arr = byteArrayOf(replay[16], replay[17])
            var y_mag = littleEndianConversion(temp_arr)

            temp_arr = byteArrayOf(replay[18], replay[19])
            var z_mag = littleEndianConversion(temp_arr)
            so += "\nmag: X:${x_mag-old_x_mag} Y:${y_mag-old_y_mag} Z:${z_mag-old_z_mag}"

            var calc_rot = abs(x_gyro-old_x_gyro) + abs(y_gyro-old_y_gyro)
            if(calc_rot > 2000)
            {
                r_var +=1
                if (r_var > 4)
                {
                    //addGlog("ROTATION DETECTED")
                    r_status = true
                }
                else
                {
                    r_status = false
                }
            }
            else if (r_var > 0)
            {
                r_var-= 1
            }

            var calc_im = abs(x_gyro-old_x_gyro) + abs(y_gyro-old_y_gyro) + abs(z_gyro-old_z_gyro)
            if(calc_im < 470)//immobile puo essere solo se il resto è false
            {
                i_var +=1
                if (i_var > 85) {
                    //addGlog("IMMOBILITA' DETECTED")
                    i_status = true
                }
                else
                {
                    i_status = false
                }
            }
            else if (i_var > 0)
            {
                i_var-= 35
            }

            var calc_fal = abs(z_axl-old_z_axl)+abs(x_axl-old_x_axl)+abs(y_axl-old_y_axl)
            if(calc_fal > 2800)
            {
                f_var +=1
                if (f_var > 3) {
                    //addGlog("FALL DETECTED")
                    f_status = true
                }
                else
                {
                    f_status = false
                }
            }
            else if (f_var > 0)
            {
                f_var-= 1
            }

            var s_so =""
            if(r_status == true)
                s_so += "R"
            else
                s_so += "_"

            if(f_status == true)
                s_so += "F"
            else
                s_so += "_"

            if(i_status == true)
                s_so += "I"
            else
                s_so += "_"

            so = s_so + so
            so += "[a:$calc_im,b:$calc_fal,c:$calc_rot],[$i_var,$f_var,$r_var]"

            old_x_gyro = x_gyro
            old_y_gyro = y_gyro
            old_z_gyro = z_gyro
            old_x_axl = x_axl
            old_y_axl = y_axl
            old_z_axl = z_axl
            old_x_mag = x_mag
            old_y_mag = y_mag
            old_z_mag = z_mag
        }
        else
        {

            var temp_arr = byteArrayOf(replay[2], replay[3])
            var x_gyro = littleEndianConversion(temp_arr)

            temp_arr = byteArrayOf(replay[4], replay[5])
            var y_gyro = littleEndianConversion(temp_arr)

            temp_arr = byteArrayOf(replay[6], replay[7])
            var z_gyro = littleEndianConversion(temp_arr)

            so += "gyro X:$x_gyro Y:$y_gyro Z:$z_gyro"
            //--
            temp_arr = byteArrayOf(replay[8], replay[9])
            var x_axl = littleEndianConversion(temp_arr)

            temp_arr = byteArrayOf(replay[10], replay[11])
            var y_axl = littleEndianConversion(temp_arr)

            temp_arr = byteArrayOf(replay[12], replay[13])
            var z_axl = littleEndianConversion(temp_arr)
            so += "     axl X:$x_axl Y:$y_axl Z:$z_axl"
            //--
            temp_arr = byteArrayOf(replay[14], replay[15])
            var x_mag = littleEndianConversion(temp_arr)

            temp_arr = byteArrayOf(replay[16], replay[17])
            var y_mag = littleEndianConversion(temp_arr)

            temp_arr = byteArrayOf(replay[18], replay[19])
            var z_mag = littleEndianConversion(temp_arr)
            so += "     mag: X:$x_mag Y:$y_mag Z:$z_mag"

            var roll = 180*atan2(x_axl.toDouble(),(sqrt((y_axl*2 + z_axl * 2).toDouble()))/PI)
            var pitch = 180*atan2(y_axl.toDouble(),(sqrt((x_axl*2 + z_axl * 2).toDouble()))/PI)
            so += "     roll:$roll pitch:$pitch"

        }


        tlog.text = so
    }

    private fun addSlog(testo: String): Unit {
        val glog = findViewById<TextView>(R.id.textlog)
        glog.text = concat("\n",testo.uppercase(),"\n\n",glog.text)
        nglog++
    }
    var verbLog = false
    private fun addVlog(testo: String){
        if(mostra_principale == true) {
            if (verbLog == true)
                addGlog("[v]" + testo)
        }
    }

    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }


    private val scanSettings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()

    //---

    fun BluetoothGattCharacteristic.isReadable(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_READ)

    fun BluetoothGattCharacteristic.isWritable(): Boolean =
    containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE)

    fun BluetoothGattCharacteristic.isWritableWithoutResponse(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)

    fun BluetoothGattCharacteristic.containsProperty(property: Int): Boolean {
        return properties and property != 0
    }

    fun BluetoothGattCharacteristic.isIndicatable(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_INDICATE)

    fun BluetoothGattCharacteristic.isNotifiable(): Boolean =
        containsProperty(BluetoothGattCharacteristic.PROPERTY_NOTIFY)

    //---
    //---notifiche e dintorni






    //-- fine notifiche e dintorni

    private val ENABLE_BLUETOOTH_REQUEST_CODE = 1
    val isLocationPermissionGranted get() =
        hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)

    private var isScanning = false
        set(value) {
            val scan_button = findViewById<Button>(R.id.scan_button)
            field = value
            runOnUiThread { scan_button.text = if (value) "Stop Scan" else "Start Scan" }
        }


    private val bluetoothAdapter: BluetoothAdapter by lazy { val bluetoothManager =
        getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }


    fun littleEndianConversion(bytes: ByteArray): Int {
        var result = 0
        for (i in bytes.indices) {
            result = result or (bytes[i].toInt() shl 8 * i)
        }
        return result
    }

    override fun onResume() { super.onResume()
        if (!bluetoothAdapter.isEnabled) {
            promptEnableBluetooth()
            addGlog("!bluetoothAdapter.isEnabled")
        }
    }

    fun writeDescriptor(descriptor: BluetoothGattDescriptor, payload: ByteArray) {
        actual_gatt?.let { gatt ->
            descriptor.value = payload
            gatt.writeDescriptor(descriptor)
            addVlog("writedescriptor: payload: ${payload.toString()} payload in hex: ${payload.toHexString()} descriptor: ${descriptor.toString()}")
        } ?: addGlog("Not connected to a BLE device!")

    }

    fun enableNotifications(characteristic: BluetoothGattCharacteristic) { //l'ho messo qui ma non sembrano esserci cambiamenti

        var cccdUuid = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb") //valore temporaneo
        addGlog("BluetoothGattDescriptor for ${characteristic.uuid.toString()}")
        for (descriptor in characteristic.descriptors) {
            addGlog("> " + descriptor.uuid.toString())
            cccdUuid = descriptor.uuid
        }

        val payload = when {
            characteristic.isIndicatable() -> BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
            characteristic.isNotifiable() -> BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            else -> {
                addGlog("ConnectionManager: ${characteristic.uuid} doesn't support notifications/indications")
                return
            }
        }


        characteristic.getDescriptor(cccdUuid)?.let { cccDescriptor ->
            if (actual_gatt?.setCharacteristicNotification(characteristic, true) == false) {
                addGlog("ConnectionManager: setCharacteristicNotification failed for ${characteristic.uuid}")
                return
            }
            addVlog("trying to call writeDescriptor")
            writeDescriptor(cccDescriptor, payload)
            addGlog("\uD83D\uDD14 enableNotification with cccDescriptor: ${cccDescriptor.toString()} and cccdUuid ${cccdUuid.toString()} completed")

        } ?: addGlog("ConnectionManager: ${characteristic.uuid} doesn't contain the CCC descriptor!")
    }

    @SuppressLint("MissingPermission")
    fun disableNotifications(characteristic: BluetoothGattCharacteristic) {
        if (!characteristic.isNotifiable() && !characteristic.isIndicatable()) {
            addGlog("ConnectionManager: ${characteristic.uuid} doesn't support indications/notifications")
            return
        }

        val cccdUuid = UUID.fromString("")//TODO
        characteristic.getDescriptor(cccdUuid)?.let { cccDescriptor ->
            if (actual_gatt?.setCharacteristicNotification(characteristic, false) == false) {
                addGlog("ConnectionManager: setCharacteristicNotification failed for ${characteristic.uuid}")
                return
            }
            writeDescriptor(cccDescriptor, BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)
        } ?: addGlog("ConnectionManager: ${characteristic.uuid} doesn't contain the CCC descriptor!")
    }

    private fun promptEnableBluetooth() { if (!bluetoothAdapter.isEnabled) {
        val enableBtIntent =
            Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBtIntent, ENABLE_BLUETOOTH_REQUEST_CODE) }//potrebbe rifiutarlo ma ... chissene
    }

    fun Context.hasPermission(permissionType: String): Boolean {
        val reshp = ContextCompat.checkSelfPermission(this, permissionType) == PackageManager.PERMISSION_GRANTED
        addGlog( concat("hasPermission (",permissionType,")") as String)
        return reshp
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ENABLE_BLUETOOTH_REQUEST_CODE -> {
                if (resultCode != RESULT_OK) {
                    promptEnableBluetooth()
                }
            }
        }
    }

    private val scanResults = mutableListOf<ScanResult>()
    var san = 0;

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val indexQuery = scanResults.indexOfFirst { it.device.address == result.device.address }
            if (indexQuery != -1) { // A scan result already exists with the same address
                scanResults[indexQuery] = result
                //scanResultAdapter.notifyItemChanged(indexQuery) USELESs
            } else {

                val sc_name = result.device.name
                val sc_addr = result.device.address
                addGlog("Found BLE device! Name: $sc_name Address: $sc_addr")
                scanResults.add(result)
                //scanResultAdapter.notifyItemInserted(scanResults.size - 1)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            addGlog("scan failed")
        }
        //Log.e(“ScanCallback”, "onScanFailed: code $errorCode")
    }

    var actual_gatt: BluetoothGatt? = null
    var actual_status: Int = 0
    var actual_newState: Int = 0

    private val gattCallback = object : BluetoothGattCallback() { //callback
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val deviceAddress = gatt.device.address
            val deviceName = gatt.device.name
            addGlog("gattcallback called on ${deviceAddress.toString()} + ( ${deviceName.toString()} )")

            if (status == BluetoothGatt.GATT_SUCCESS) {
                addVlog("gattcallback called successufully but still not connected")

                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    addSlog("Successfully connected to $deviceAddress")
                    findViewById<Button>(R.id.discoveryButton).visibility = View.VISIBLE
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        var bluetoothGatt = gatt
                        addVlog("${bluetoothGatt.toString()}.discoverServices()")
                        actual_gatt = gatt
                        actual_newState = newState
                        actual_status = status
                        // TODO: Store a reference to BluetoothGatt
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        addSlog("Successfully disconnected to $deviceAddress")
                        gatt.close()
                    }
                } else {
                    addSlog("Error $status encountered for $deviceAddress! Disconnecting...")
                    gatt.close()
                }
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            with(characteristic) {
                if(characteristic.uuid.toString() != "09bf2c52-d1d9-c0b7-4145-475964544307") {
                    var temp =
                        " BluetoothGattCallback: Characteristic $uuid changed | value: ${value.toHexString()}"
                    if (value[3] == 0.toByte()) {
                        temp = "\uD83D\uDCE2 \uD83D\uDFE2" + temp
                    } else {
                        temp = "\uD83D\uDCE2 \uD83D\uDEA9" + temp
                    }
                    addGlog(temp)
                }
                else
                {
                    addTlog(value)
                }
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            addVlog("onCharacteristicRead called, status: $status")
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    var temp =" read characteristic ${characteristic.uuid}:\n${characteristic.value.toHexString()}"
                    if (characteristic.value[3] == 0.toByte())
                    {
                        temp = "\uD83D\uDCE2 \uD83D\uDFE2" + temp
                    }
                    else
                    {
                        temp = "\uD83D\uDCE2 \uD83D\uDEA9" + temp
                    }

                    var cv = characteristic.value
                    var longcv = "\uD83D\uDCCE"
                    longcv +="ack: ${cv[0].toString()} len: ${cv[1].toString()} [cmd: ${cv[2].toString()} "
                    var ti = 2
                    while(ti < 19){
                        if (ti == (cv[1].toInt() +2))
                        {
                            longcv += "|||"
                        }
                        else
                        {
                            longcv += " "
                        }
                        longcv += cv[ti].toString()
                        ti = ti + 1
                    }
                    longcv += "]"
                    addGlog(longcv)
                    addGlog(temp)
                }
                BluetoothGatt.GATT_READ_NOT_PERMITTED -> {
                    addGlog("Read not permitted for ${characteristic.uuid}!")
                }
                else -> {
                    addGlog("Characteristic read failed for ${characteristic.uuid}, error: $status")
                }
            }

        }

    }


    fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {

        addGlog( "Discovered ${gatt.services.size} services for ${gatt.device.address}")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            gatt.printGattTable()
        }

    }

    private fun startBleScan() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isLocationPermissionGranted) {//!isLocationPermissionGranted fa crashare quindi l'ho eliminato ma deve essere aggiunto ( TODO )

            requestLocationPermission()
            addGlog("startBleScan asked for locationPermission")

        }
        else {
            //BleResult.setAdapter(null) USELESS
            scanResults.clear()
            //scanResultAdapter.notifyDataSetChanged() USELESS
            bleScanner.startScan(null, scanSettings, scanCallback)
            isScanning = true
            addGlog("startBleScan startScan")
        }
    }

    private fun stopBleScan() {
        bleScanner.stopScan(scanCallback)
        isScanning = false
    }


    private val list_service = mutableListOf<String>()
    private val list_characteristics = mutableListOf<String>()

    private var always_read = false

    private fun copyTextToClipboard(text : String){
        val textToCopy = text
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("text", textToCopy)
        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_LONG).show()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun BluetoothGatt.printGattTable() {
        if (services.isEmpty()) {
            addGlog( "No service and characteristic available, call discoverServices() first?")
            return
        }
        list_characteristics.clear()
        list_service.clear()
        var tavola = ""
        var temp_always_read = false
        services.forEach { service ->
            /*val characteristicsTable = service.characteristics.joinToString(
                separator = "\n|--",
                prefix = "|--",
            ) { it.uuid.toString() }*/
            tavola += "\n\n|SERVICE:" + service.uuid.toString()
            list_service.add(service.uuid.toString())
            service.characteristics.forEach { characteristic ->
                tavola += "\n|  >"+characteristic.uuid.toString()+" ["
                val rwc = actual_gatt?.getService(service.uuid)?.getCharacteristic(characteristic.uuid)
                if (rwc?.isReadable() == false && rwc?.isWritable() == false) {
                    tavola += "-"
                }
                else{
                    if (rwc?.isReadable() == true) {
                        tavola += "R"
                        if(always_read == true) {
                            temp_always_read = true
                            tavola += "*"
                        }
                    }
                    if (rwc?.isWritable() == true) {
                        tavola += "W"
                    }
                    if (rwc?.isNotifiable() == true) {
                        tavola += "N"
                    }
                    if (rwc?.isIndicatable() == true) {
                        tavola += "I"
                    }
                }
                tavola += "]"
                if (temp_always_read == true) {
                    readBleVal(service.uuid.toString(), characteristic.uuid.toString())
                    tavola += "+"
                }
                list_characteristics.add(characteristic.uuid.toString())
            }
        }
        addGlog(tavola)
        addVlog("IN MUTABLELIST service: $list_service characteristics: $list_characteristics")
        putInSpinnerGatt()
    }

    private fun putInSpinnerGatt()
    {
        addVlog("putinspinnergatt called")

        val my_c = findViewById<Spinner>(R.id.chspinner)
        val my_s = findViewById<Spinner>(R.id.srspinner)


        val my_s_array = arrayOfNulls<String?>(list_service.size)
        val my_c_array = arrayOfNulls<String?>(list_characteristics.size)//attenzione questi negli array non vanno usati

        var i = 0
        while(i < list_service.size) {
            my_s_array.set(i,"S: ${list_service.get(i)}")
            i++
        }

        i = 0
        while(i < list_characteristics.size) {
            my_c_array.set(i,"C: ${list_characteristics.get(i)}")
            i++
        }

        val temp_s = ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,my_s_array)
        val temp_c = ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,my_c_array)

        my_s.adapter = temp_s
        my_c.adapter = temp_c

        my_s.onItemSelectedListener = object  : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                addVlog("selezionato servizio "+my_s_array[p2].toString())
                selected_service = list_service[p2].toString()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                //do nothing
            }

        }

        my_c.onItemSelectedListener = object  : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                addVlog("selezionato ch "+my_c_array[p2].toString())
                selected_ch = list_characteristics[p2].toString()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                //do nothing
            }

        }

        //addVlog("IN ARRAY name_sc_list : ${name_sc_list.toString()}")
    }


   //non ho messo la requestcode cioè rivate fun Activity.requestPermission
   override fun onRequestPermissionsResult(
       requestCode: Int,
       permissions: Array<out String>,
       grantResults: IntArray
   ) {
       super.onRequestPermissionsResult(requestCode, permissions, grantResults)
       when (requestCode) {
           LOCATION_PERMISSION_REQUEST_CODE -> {
               if (grantResults.firstOrNull() == PackageManager.PERMISSION_DENIED) {
                   addGlog("onRequestPermissionResult -> grantResults.firstOrNull()")
                   requestLocationPermission()
               } else {
                   addGlog("onRequestPermissionResult ha avviato bleScan")
                   startBleScan()
               }
           }
       }
   }

    private fun requestLocationPermission() {
        requestPermission(
            Manifest.permission.ACCESS_FINE_LOCATION,
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun requestSmsPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS),1)
    }

    private fun Activity.requestPermission(permission: String, requestCode: Int) {
        ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
    }

    var sent_yet = false
    private fun sendSMS(phoneNumber: String, message: String) {
        if(sent_yet == false) {
            val sentPI: PendingIntent = PendingIntent.getBroadcast(this, 0, Intent("SMS_SENT"), 0)
            SmsManager.getDefault().sendTextMessage(phoneNumber, null, message, sentPI, null)
            sent_yet = true
        }else{
            addGlog("sms sent yet. To avoid too much sms to be sent")
        }
    }

    private fun readBatteryLevel(): String {
            addVlog("readbatterylevel called")
            val batteryServiceUuid = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb")
            val batteryLevelCharUuid = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb")
            val batteryLevelChar = actual_gatt?.getService(batteryServiceUuid)?.getCharacteristic(batteryLevelCharUuid)
            if (batteryLevelChar?.isReadable() == true) {
                addGlog("B) on $actual_gatt batterylevel is ${actual_gatt?.readCharacteristic(batteryLevelChar).toString()} ")
                return actual_gatt?.readCharacteristic(batteryLevelChar).toString()
            }
        return "Error battery is not readable"
    }


    var selected_service = ""
    var selected_ch = ""
    private fun readBleVal(my_serviceUuid: String,my_charactertiscUuid: String ): String {
        try {
            addVlog("readbatterylevel called")
            val usable_serviceUuid = UUID.fromString(my_serviceUuid)
            val usable_charactertiscUuid = UUID.fromString(my_charactertiscUuid)
            val usable_x = actual_gatt?.getService(usable_serviceUuid)
                ?.getCharacteristic(usable_charactertiscUuid)
            if (usable_x?.isReadable() == true) {
                addVlog("is readable")
                return actual_gatt?.readCharacteristic(usable_x).toString()
            }
            else
            {
                return "Error on readBleVal: val not readable"
            }
            return "Unknown Error on readBleVal"
        }
        catch(e: Exception){
            return "exception on readBleVal: $e"
        }
    }



    fun writeBleVal(my_serviceUuid: String,my_charactertiscUuid: String, payload: ByteArray) {
        addVlog("writebleval start $my_serviceUuid , $my_charactertiscUuid")
        try {
            val usable_serviceUuid = UUID.fromString(my_serviceUuid)
            val usable_charactertiscUuid = UUID.fromString(my_charactertiscUuid)
            val rwc = actual_gatt?.getService(usable_serviceUuid)
                ?.getCharacteristic(usable_charactertiscUuid)
            if (rwc != null) {
                writeCharacteristic(rwc, payload)
                addVlog("writebleval: rwc not null and completed")
            }
            else{
                addGlog("writebleval error rwc null")
            }
        } catch (e: Exception){
            addGlog("Errore su writeBleVal: $e")
        }
    }

    @SuppressLint("MissingPermission")
    fun writeCharacteristic(characteristic: BluetoothGattCharacteristic, payload: ByteArray) {
        try{
            addVlog("writeCharacteristic start")
            var writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            if(characteristic.isWritable())
                writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            else if (characteristic.isWritableWithoutResponse())
                writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            else
                addGlog("Characteristic ${characteristic.uuid} cannot be written to")

            characteristic.writeType = writeType
            characteristic.value = payload
            val x = actual_gatt?.writeCharacteristic(characteristic)
            addGlog("✏️writeCharacteristic for ${payload.toHexString()} completed and returned: $x ")
        }
        catch (e: Exception){
            addGlog("Errore su writeCharacteristic: $e")
        }
    }

    fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
        addGlog("onCharacteristicWrite called, status: $status")
        when (status) {
            BluetoothGatt.GATT_SUCCESS -> {
                addGlog( "✏️Wrote to characteristic $characteristic.uuid | value: ${characteristic.value.toHexString()}")
            }
            BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH -> {
                addGlog( "Write exceeded connection ATT MTU!")
            }
            BluetoothGatt.GATT_WRITE_NOT_PERMITTED -> {
                addGlog( "Write not permitted for ${characteristic.uuid}")
            }
            else -> {
                addGlog( "Characteristic write failed for $characteristic.uuid, error: $status")
            }
        }

    }


    fun BluetoothGattDescriptor.isReadable(): Boolean =
        containsPermission(BluetoothGattDescriptor.PERMISSION_READ) ||
                containsPermission(BluetoothGattDescriptor.PERMISSION_READ_ENCRYPTED) ||
                containsPermission(BluetoothGattDescriptor.PERMISSION_READ_ENCRYPTED_MITM)
    fun BluetoothGattDescriptor.isWritable(): Boolean =
        containsPermission(BluetoothGattDescriptor.PERMISSION_WRITE) ||
                containsPermission(BluetoothGattDescriptor.PERMISSION_WRITE_ENCRYPTED) ||
                containsPermission(BluetoothGattDescriptor.PERMISSION_WRITE_ENCRYPTED_MITM) ||
                containsPermission(BluetoothGattDescriptor.PERMISSION_WRITE_SIGNED) ||
                containsPermission(BluetoothGattDescriptor.PERMISSION_WRITE_SIGNED_MITM)




    fun BluetoothGattDescriptor.containsPermission(permission: Int): Boolean =
        permissions and permission != 0

    fun ByteArray.toHexString(): String =
        joinToString(separator = " ", prefix = "0x") { String.format("%02X", it) }

    fun byteArrayOfInts(vararg ints: Int) = ByteArray(ints.size) { pos -> ints[pos].toByte() }

    //ooooo

    private lateinit var timer_sensor : CountDownTimer
    var timer_phase = "A"
    var timer_log_result = "generic text"

    private var timer_monitor:CountDownTimer?= null


    public var tf_var = 0
    public var tr_var = 0
    public var ti_var = 0 //attenzione questa va al contrario
    public var old_t = false
    public var old_f = false

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)

            addGlog(">Start< (with base url: $base_url)")
            requestSmsPermission()
            val scan_button = findViewById<Button>(R.id.scan_button)
            val sensor_btt = findViewById<Button>(R.id.sensorbutton)
            val inpTx2 = findViewById<EditText>(R.id.inputTextField)

            sensor_btt.setOnClickListener {
                mostra_principale = mostra_principale.not()
                addVlog("mostra_principale $mostra_principale")
            }

            timer_sensor = object : CountDownTimer(3500,35){
                override fun onTick(p0: Long) {
                    val tlog = findViewById<TextView>(R.id.tempLogBox)
                    timer_log_result += "[$timer_phase = ${p0.toString()}] ${tlog.text}\n\n"
                }

                override fun onFinish() {
                    if(timer_phase == "A"){
                        timer_phase = "B"
                        timer_sensor.start()
                    }
                    else if(timer_phase == "B"){
                        timer_phase = "C"
                        timer_sensor.start()
                    }
                    else if(timer_phase == "C"){
                        timer_phase = "A"
                        copyTextToClipboard(timer_log_result)
                        timer_sensor.cancel()
                    }
                    sensor_btt.text = timer_phase
                }
            }

            timer_testV = object : CountDownTimer(14000, 100) {
                var x = 0
                override fun onTick(p0: Long) {
                    addVlog("agg valore tv")
                    addjfri((0..99).random(),x,(0..99).random())
                    x++
                }

                override fun onFinish() {
                    addGlog("test value inviati")
                }
            }

            timer_monitor = object : CountDownTimer(180000,300){
                override fun onTick(p0: Long) {
                    if(i_status == false){
                        ti_var = 0

                        if(f_status == true)
                            tf_var = 30
                        else
                            tf_var -= 1


                        if(r_status == true)
                            tr_var = 30
                        else
                            tr_var -= 1

                        if (tf_var < 0)
                            tf_var = 0
                        if (tr_var < 0)
                            tr_var = 0
                    }
                    else
                    {
                        ti_var++

                        if(((tf_var > 0)||(tr_var > 0))&&(ti_var > 18))//condizioni che hanno generato l'allarme
                        {
                            addGlog("ALLARME LOCALE")
                            addElog("allarme locale") //qui viene dato l'allarme
                            sendSMS("+393497506302", "allarme!")
                            sent_yet = true
                        }
                        get(false)
                    }
                    val t = findViewById<TextView>(R.id.scan_button)
                    addElog("f: $tf_var  r: $tr_var i: $ti_var")
                    addjfri(tf_var,tr_var,ti_var)
                }

                override fun onFinish() {
                    timer_monitor?.start()
                    addElog("timer restart")
                }
            }

            //on start e on create lo eredita dall'altro timer

            scan_button.setOnClickListener {
                addGlog("scan button clicked")
                val tmr = timer_monitor?.start()
                addGlog("tmr: $tmr")
                if (isScanning) {
                    stopBleScan()

                    val scan_array_name = arrayOfNulls<String?>(scanResults.size)
                    var i = 0
                    for (dev in scanResults) {
                        if (dev.device.name != null) {
                            scan_array_name[i] = dev.device.name
                        } else {
                            scan_array_name[i] = "unknown address:" + dev.device.address
                        }
                        i++
                    }

                    val my_spinner = findViewById<Spinner>(R.id.spinnerBLE)
                    val arrayadd = ArrayAdapter<String>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        scan_array_name
                    )
                    my_spinner.adapter = arrayadd
                    my_spinner.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                p0: AdapterView<*>?,
                                p1: View?,
                                p2: Int,
                                p3: Long
                            ) {
                                addGlog("selezionato " + scan_array_name[p2])
                                if (isScanning) {
                                    stopBleScan()
                                }
                                san = p2.toInt()
                                val tempbutton = findViewById<Button>(R.id.codeButton)
                                tempbutton.visibility = View.VISIBLE
                            }

                            override fun onNothingSelected(p0: AdapterView<*>?) {
                                addVlog("my spinner onNothingSelected")
                            }

                        }
                } else {
                    startBleScan()
                }
            }


            val cb = findViewById<Button>(R.id.codeButton)
            //val ct = findViewById<EditText>(R.id.codeText)
            cb.setOnClickListener {
                val dispScelto = scanResults[san]
                addVlog("trying to connect to " + dispScelto.device.address)

                var temp = dispScelto.device.connectGatt(this, false, gattCallback)
                addGlog("connectGatt ha restituito = $temp")


            }



            val sw_el2 = findViewById<Switch>(R.id.switchEL2)
            sw_el2.setOnClickListener()
            {
                elaborazione_local = sw_el2.isChecked
            }

            val dbtt = findViewById<Button>(R.id.discoveryButton)
            dbtt.setOnClickListener {


                var gatt = actual_gatt
                var tatus = actual_status
                var newstate = actual_newState

                Handler(Looper.getMainLooper()).post {
                    var temp = gatt?.discoverServices()
                    addVlog("${gatt.toString()}.discordservice() returned ${temp.toString()}")
                }

                addVlog("handler discover service reached an end")
                val gattServices: List<BluetoothGattService> = gatt!!.getServices()
                addGlog("Services count: " + gattServices.size)
                if (gattServices.size > 0) {
                    addGlog("services: " + gattServices.toString())
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    gatt.printGattTable()
                }

            }

            val my_textlog = findViewById<TextView>(R.id.textlog)
            my_textlog.setOnClickListener{
                addGlog("textlog cliccata")
                copyTextToClipboard(my_textlog.text.toString())
            }

            val inpButton = findViewById<Button>(R.id.inputCommand)
            val inpTx = findViewById<EditText>(R.id.inputTextField)
            inpButton.setOnClickListener {
                if (inpTx.getText().toString() == "verb") {
                    addGlog("verberose mode active")
                    verbLog = true
                }
                else if (inpTx.getText().toString() == "sms") {
                    try {
                        sendSMS("+393497506302", "test sms")
                        addGlog("sms inviato con successo")
                    }catch(e: Exception){
                        addGlog("invio sms fallito: ${e.toString()}")
                    }
                }
                else if (inpTx.getText().toString() == "minit") {
                    minitlog = !(minitlog)
                }
                else if (inpTx.getText().toString() == "noot") {
                    addVlog("enablenotification")
                    var usable_serviceUuid = UUID.fromString("c8c0a708-e361-4b5e-a365-98fa6b0a836f")
                    var usable_charactertiscUuid = UUID.fromString("d5913036-2d8a-41ee-85b9-4e361aa5c8a7")
                    var rwc = actual_gatt?.getService(usable_serviceUuid)
                        ?.getCharacteristic(usable_charactertiscUuid)
                    if (rwc != null) {
                        enableNotifications(rwc)
                    }

                    addVlog("enablenotification test")
                    usable_serviceUuid = UUID.fromString("c8c0a708-e361-4b5e-a365-98fa6b0a836f")
                    usable_charactertiscUuid = UUID.fromString("09bf2c52-d1d9-c0b7-4145-475964544307")
                    rwc = actual_gatt?.getService(usable_serviceUuid)
                        ?.getCharacteristic(usable_charactertiscUuid)
                    if (rwc != null) {
                        enableNotifications(rwc)
                    }
                }
                else if (inpTx.getText().toString() == "notc") {
                    addVlog("enablenotification test")
                    var usable_serviceUuid = UUID.fromString("c8c0a708-e361-4b5e-a365-98fa6b0a836f")
                    var usable_charactertiscUuid =
                        UUID.fromString("d5913036-2d8a-41ee-85b9-4e361aa5c8a7")
                    var rwc = actual_gatt?.getService(usable_serviceUuid)
                        ?.getCharacteristic(usable_charactertiscUuid)
                    if (rwc != null) {
                        enableNotifications(rwc)
                    }
                }
                else if (inpTx.getText().toString() == "notd") {
                    addVlog("enablenotification test")
                    var usable_serviceUuid = UUID.fromString("c8c0a708-e361-4b5e-a365-98fa6b0a836f")
                    var usable_charactertiscUuid = UUID.fromString("09bf2c52-d1d9-c0b7-4145-475964544307")
                    var rwc = actual_gatt?.getService(usable_serviceUuid)
                        ?.getCharacteristic(usable_charactertiscUuid)
                    if (rwc != null) {
                        enableNotifications(rwc)
                    }
                }
                else if (inpTx.getText().toString() == "not2") {
                    addVlog("enablenotification test")
                    val usable_serviceUuid = UUID.fromString("c8c0a708-e361-4b5e-a365-98fa6b0a836f")
                    val usable_charactertiscUuid = UUID.fromString("09bf2c52-d1d9-c0b7-4145-475964544307")
                    val rwc = actual_gatt?.getService(usable_serviceUuid)
                        ?.getCharacteristic(usable_charactertiscUuid)
                    if (rwc != null) {
                        enableNotifications(rwc)
                    }
                }
                else if (inpTx.getText().toString() == "timeble") {
                    var pkt = byteArrayOfInts(0x8B, 0x00)
                    repeat(18) {
                        pkt += 0
                    }

                    writeBleVal("c8c0a708-e361-4b5e-a365-98fa6b0a836f", "d5913036-2d8a-41ee-85b9-4e361aa5c8a7", pkt)

                }
                else if (inpTx.getText().toString() == "check") {
                    addGlog("check:per controllare se tutti i parametri sono ok")
                    var pkt = byteArrayOfInts(0X89, 0X00)
                    repeat(18) {
                        pkt += 0
                    }

                    writeBleVal("c8c0a708-e361-4b5e-a365-98fa6b0a836f", "d5913036-2d8a-41ee-85b9-4e361aa5c8a7", pkt)

                }
                else if (inpTx.getText().toString() == "esseyaxl") {
                    addGlog("esseyAXL: per calibrare l'accellerometro")
                    var pkt = byteArrayOfInts(0x41, 0x01, 0x04)
                    repeat(17) {
                        pkt += 0
                    }

                    writeBleVal("c8c0a708-e361-4b5e-a365-98fa6b0a836f", "d5913036-2d8a-41ee-85b9-4e361aa5c8a7", pkt)

                }

                else if (inpTx.getText().toString() == "json_reset") {
                    jList.clear()
                    jNumber = 0
                }
                else if (inpTx.getText().toString() == "json_tv") {
                    try {
                        for(i in 0..15){
                            addjfri((0..99).random(),(0..99).random(),(0..99).random())
                        }
                    }catch(e: Exception){
                        addGlog("e: ${e.toString()}")
                    }
                }
                else if (inpTx.getText().toString() == "json_get") {
                    try {
                        get(true)
                        addVlog("trying a json_get")
                    }catch(e: Exception){
                        addGlog("e: ${e.toString()}")
                    }
                }
                else if (inpTx.getText().toString() == "essey") {
                    addGlog("essey: per avviare il flusso dati")
                    var pkt = byteArrayOfInts(0x02, 0X03,0xF8,0x05,0x04)
                    repeat(15) {
                        pkt += 0
                    }

                    writeBleVal("c8c0a708-e361-4b5e-a365-98fa6b0a836f", "d5913036-2d8a-41ee-85b9-4e361aa5c8a7", pkt)

                }
                else if (inpTx.getText().toString() == "ra") {
                    addGlog("always read enable")
                    always_read = true
                }
                else {
                    addGlog("command not recognized")
                }
            }

            val readb = findViewById<Button>(R.id.readButton)
            readb.setOnClickListener {
                var gatt = actual_gatt
                addVlog("invoked S:$selected_service C:$selected_ch")
                val temp = readBleVal(selected_service, selected_ch)
                addVlog("La read ha restituito: '${temp.toString()}'")
            }
            val readd = findViewById<Button>(R.id.readButtonData)
            readd.setOnClickListener {
                val temp = readBleVal("c8c0a708-e361-4b5e-a365-98fa6b0a836f", "09bf2c52-d1d9-c0b7-4145-475964544307")
                addVlog("La readData ha restituito: '${temp.toString()}'")
            }
            val readc = findViewById<Button>(R.id.readButtonCommand)
            readc.setOnClickListener {
                val temp = readBleVal("c8c0a708-e361-4b5e-a365-98fa6b0a836f", "d5913036-2d8a-41ee-85b9-4e361aa5c8a7")
                addVlog("La readCommand ha restituito: '${temp.toString()}'")
            }
        }catch(e: Exception){
            addGlog("Errore onCreate: $e")
        }
    }

    fun get(force_response : Boolean) { //forza una risposta
        if(elaborazione_local == false) {
            try {
                val client = OkHttpClient()
                val url = URL("$base_url/_search?q=(f:>0 AND i:>18) OR (r:>0 AND i:>18)&size=10000") //c'è anche la query

                addVlog("url: $url")
                val request = Request.Builder()
                    .url(url)
                    .get()
                    .build()

                var r_string = "default value"
                doAsync {
                    val response = client.newCall(request).execute()
                    r_string.toString()

                    val responseBody = response.body!!.string()
                    r_string = responseBody
                }
                while (r_string == "default value") {

                }

                val mapperAll = ObjectMapper()
                val objData = mapperAll.readTree(r_string)


                addVlog("r_string: $r_string")
                objData.get("hits").forEachIndexed { index, jsonNode ->

                    try {
                        val jsonArray = JSONArray(jsonNode.toString())
                        addVlog("trying on array ${jsonNode.toString()}")
                        for (i in 0 until jsonArray.length()) {
                            val jsonObject: JSONObject = jsonArray.getJSONObject(i)
                            addVlog("·${index.toString()} : ${jsonObject.toString()}")
                            val ni = jsonObject.get("_source")
                            if(force_response == true) {
                                addGlog("\n" + mss(ni.toString()))
                            }
                            else{

                                allarmeElastic(jsonArray.length())
                            }

                        }
                    } catch (e: Exception) {

                        addVlog("${index.toString()} saltato + ${e.toString()}")

                    }
                }
            } catch (e: java.lang.Exception) {
                if(force_response == true) {
                    addGlog("exception:${e.toString()}")
                }
            }
        }
    }

    fun delete(){
        if(elaborazione_local == false) {
            try {
                val client = OkHttpClient()
                val url_del = URL("$base_url")

                val request_del = Request.Builder()
                    .url(url_del)
                    .delete()
                    .build()

                var r_string = ""
                doAsync {
                    val response = client.newCall(request_del).execute()

                    r_string = response.toString()

                }
                while (r_string == "default value") {

                }
                addVlog("del respose: $r_string")


            } catch (e: java.lang.Exception) {
                addGlog("exception:${e.toString()}")
            }
        }
    }

    fun postfri(f: Int, r: Int, i: Int) {
        if(elaborazione_local == false) {
            try {
                val client = OkHttpClient()
                val url = URL("$base_url/_doc")

                //or using jackson
                val mapperAll = ObjectMapper()
                val jacksonObj = mapperAll.createObjectNode()
                jacksonObj.put("ni", "${dates()}")
                jacksonObj.put("f", f)
                jacksonObj.put("r", r)
                jacksonObj.put("i", i)
                val jacksonString = jacksonObj.toString()
                val mediaType = "application/json; charset=utf-8".toMediaType()
                val body = jacksonString.toRequestBody(mediaType)
                val request = Request.Builder()
                    .url(url)
                    .post(body)
                    .build()

                addVlog("post request ${request.toString()}")
                var respose_string = ""
                doAsync {
                    val response = client.newCall(request).execute()
                    respose_string = response.toString()
                    val responseBody = response.body!!.string()

                    //Response

                    //we could use jackson if we got a JSON
                    val objData = mapperAll.readTree(responseBody)
                }
                while (respose_string == "") {

                }

                addVlog("jsonString: ${jacksonString.toString()} + response: $respose_string")

            } catch (e: java.lang.Exception) {
                addGlog("exception:${e.toString()}")
            }
        }
    }

    fun mss(s_input: String): String{
        var s = s_input
        s = s.replace("{", "")
        s = s.replace("}", "")
        s = s.replace("\"", "")
        s = s.replace(",", "   ")
        return s
    }

    var lenE_old = 0
    fun allarmeElastic(lenE : Int){
        if(lenE > lenE_old){
            lenE_old = lenE
            addGlog("ALLARME ELASTICSEARCH n° $lenE")
            addElog("ALLARME ELASTICSEARCH n° $lenE")
            sendSMS("+393497506302", "allarme!")
        }
    }

    fun doAsync(f: () -> Unit) {
        Thread({ f() }).start()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun dates() : String{
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
        val current = LocalDateTime.now().format(formatter)
        return current.toString()
    }

}